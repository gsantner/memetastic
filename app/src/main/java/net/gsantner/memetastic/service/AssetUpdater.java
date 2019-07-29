/*#######################################################
 *
 *   Maintained by Gregor Santner, 2016-
 *   https://gsantner.net/
 *
 *   License of this file: GNU GPLv3 (Commercial upon request)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
#########################################################*/
package net.gsantner.memetastic.service;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;

import net.gsantner.memetastic.activity.MainActivity;
import net.gsantner.memetastic.data.MemeConfig;
import net.gsantner.memetastic.data.MemeData;
import net.gsantner.memetastic.util.AppCast;
import net.gsantner.memetastic.util.AppSettings;
import net.gsantner.memetastic.util.PermissionChecker;
import net.gsantner.opoc.util.FileUtils;
import net.gsantner.opoc.util.NetworkUtils;
import net.gsantner.opoc.util.ZipUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import io.github.gsantner.memetastic.R;

@SuppressLint("SimpleDateFormat")
public class AssetUpdater {
    public static final SimpleDateFormat FORMAT_MINUTE = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    public static final SimpleDateFormat FORMAT_MINUTE_FILE = new SimpleDateFormat("yyyy-MM-dd'T'HH-mm");

    private static final String URL_ARCHIVE_ZIP = "https://github.com/gsantner/memetastic-assets/archive/master.zip";
    private static final String URL_API = "https://api.github.com/repos/gsantner/memetastic-assets";
    private final static String MEMETASTIC_CONFIG_FILE = "+0A_memetastic.conf.json";
    private final static String[] MEMETASTIC_IMAGES_EXTS = {"png", "jpg", "jpeg", "webp"};
    private final static String[] MEMETASTIC_FONT_EXTS = {"otf", "ttf"};

    public static File getDownloadedAssetsDir(AppSettings appSettings) {
        return new File(new File(appSettings.getSaveDirectory(), ".downloads"), "memetastic-assets");
    }

    public static File getCustomAssetsDir(AppSettings appSettings) {
        return new File(appSettings.getSaveDirectory(), "templates");
    }

    public static File getBundledAssetsDir(AppSettings appSettings) {
        return new File(appSettings.getContext().getCacheDir(), "bundled");
    }

    public static File getMemesDir(AppSettings appSettings) {
        return new File(appSettings.getSaveDirectory(), "memes");
    }

    public static class UpdateThread extends Thread {
        public static final int ASSET_DOWNLOAD_REQUEST__FAILED = -1;
        public static final int ASSET_DOWNLOAD_REQUEST__CHECKING = 1;
        public static final int ASSET_DOWNLOAD_REQUEST__DO_DOWNLOAD_ASK = 2;
        public static final int DOWNLOAD_STATUS__DOWNLOADING = 1;
        public static final int DOWNLOAD_STATUS__UNZIPPING = 2;
        public static final int DOWNLOAD_STATUS__FINISHED = 3;
        public static final int DOWNLOAD_STATUS__FAILED = -1;


        private static boolean _isAlreadyDownloading = false;

        private boolean _doDownload;
        private Context _context;
        private AppSettings _appSettings;
        private int _lastPercent = -1;

        public UpdateThread(Context context, boolean doDownload) {
            _doDownload = doDownload;
            _context = context;
            _appSettings = AppSettings.get();
        }

        @Override
        public void run() {
            if (PermissionChecker.hasExtStoragePerm(_context)) {
                if (MainActivity.LOCAL_ONLY_MODE || MainActivity.DISABLE_ONLINE_ASSETS) {
                    return;
                }
                AppCast.ASSET_DOWNLOAD_REQUEST.send(_context, ASSET_DOWNLOAD_REQUEST__CHECKING);
                String apiJsonS = NetworkUtils.performCall(URL_API, NetworkUtils.GET);
                try {
                    JSONObject apiJson = new JSONObject(apiJsonS);
                    String lastUpdate = apiJson.getString("pushed_at");
                    int datesubstrindex = lastUpdate.indexOf(":", lastUpdate.indexOf(":") + 1);
                    Date date = FORMAT_MINUTE.parse(lastUpdate.substring(0, datesubstrindex));
                    if (date.after(_appSettings.getLastAssetArchiveDate())) {
                        _appSettings.setLastArchiveCheckDate(new Date(System.currentTimeMillis()));
                        if (!_doDownload) {
                            AppCast.ASSET_DOWNLOAD_REQUEST.send(_context, ASSET_DOWNLOAD_REQUEST__DO_DOWNLOAD_ASK);
                        } else {
                            doDownload(date);
                            new LoadAssetsThread(_context).start();
                        }
                    }
                    return;
                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
            AppCast.ASSET_DOWNLOAD_REQUEST.send(_context, ASSET_DOWNLOAD_REQUEST__FAILED);
        }


        @SuppressWarnings("ResultOfMethodCallIgnored")
        private synchronized void doDownload(Date date) throws ParseException {
            if (_isAlreadyDownloading || date.before(_appSettings.getLastAssetArchiveDate())) {
                return;
            }
            _isAlreadyDownloading = true;
            File templatesDir = getDownloadedAssetsDir(_appSettings);
            File file = new File(_appSettings.getSaveDirectory(), ".downloads");
            MemeData.getFonts().clear();
            MemeData.getImages().clear();
            MemeData.clearImagesWithTags();
            FileUtils.deleteRecursive(file);
            boolean ok = false;
            if ((file.exists() || file.mkdirs()) && (templatesDir.exists() || templatesDir.mkdirs())) {
                // Download
                _lastPercent = -1;
                AppCast.DOWNLOAD_STATUS.send(_context, DOWNLOAD_STATUS__DOWNLOADING, 0);
                file = new File(file, FORMAT_MINUTE_FILE.format(date) + ".memetastic.zip");
                ok = NetworkUtils.downloadFile(URL_ARCHIVE_ZIP, file, (aFloat) -> {
                    int perc = (int) (aFloat * 100);
                    if (_lastPercent != perc) {
                        _lastPercent = (perc);
                        AppCast.DOWNLOAD_STATUS.send(_context, DOWNLOAD_STATUS__DOWNLOADING, _lastPercent * 3 / 4);
                    }
                });

                // Unpack
                _lastPercent = -1;
                AppCast.DOWNLOAD_STATUS.send(_context, DOWNLOAD_STATUS__UNZIPPING, 75);
                if (ok) {
                    ok = ZipUtils.unzip(file, templatesDir, true, (aFloat) -> {
                        int perc = (int) (aFloat * 100);
                        if (_lastPercent != perc) {
                            _lastPercent = perc;
                            AppCast.DOWNLOAD_STATUS.send(_context, DOWNLOAD_STATUS__UNZIPPING, 75 + _lastPercent / 4);
                        }

                    });
                }
                AppCast.DOWNLOAD_STATUS.send(_context, ok ? DOWNLOAD_STATUS__FINISHED : DOWNLOAD_STATUS__FAILED, 100);
                _appSettings.setLastArchiveDate(date);
                _isAlreadyDownloading = false;
            }
        }
    }

    public static class LoadAssetsThread extends Thread {
        private static boolean _isAlreadyLoading = false;

        private Context _context;
        private AppSettings _appSettings;
        private String[] _tagKeys;

        public LoadAssetsThread(Context context) {
            _context = context.getApplicationContext();
            _appSettings = AppSettings.get();
            _tagKeys = context.getResources().getStringArray(R.array.meme_tags__keys);
        }

        @Override
        public void run() {
            if (_isAlreadyLoading) {
                return;
            }
            _isAlreadyLoading = true;
            List<MemeData.Font> fonts = MemeData.getFonts();
            List<MemeData.Image> images = MemeData.getImages();
            fonts.clear();
            images.clear();
            MemeData.setWasInit(false);

            boolean permGranted = PermissionChecker.hasExtStoragePerm(_context);

            if (permGranted) {
                loadConfigFromFolder(getMemesDir(_appSettings), new ArrayList<MemeData.Font>(), MemeData.getCreatedMemes());
                loadConfigFromFolder(getDownloadedAssetsDir(_appSettings), fonts, images);
                loadConfigFromFolder(getCustomAssetsDir(_appSettings), fonts, images);
            }
            if (!permGranted || fonts.isEmpty() || images.isEmpty()) {
                loadBundledAssets(fonts, images);
                loadConfigFromFolder(getBundledAssetsDir(_appSettings), fonts, images);
            }
            MemeData.clearImagesWithTags();
            guessLastUsedFont(fonts);

            MemeData.setWasInit(true);
            _isAlreadyLoading = false;
            AppCast.ASSETS_LOADED.send(_context);
        }

        private void guessLastUsedFont(final List<MemeData.Font> fonts) {
            String lastFont = _appSettings.getLastUsedFont();
            if (lastFont.startsWith(_context.getFilesDir().getAbsolutePath())) {
                lastFont = "";
            }

            if (lastFont.isEmpty() || !(new File(lastFont).exists())) {
                _appSettings.setLastUsedFont(fonts.get(0).fullPath.getAbsolutePath());
            }
        }

        private void loadConfigFromFolder(File folder, List<MemeData.Font> dataFonts, List<MemeData.Image> dataImages) {
            if (!folder.exists() && !folder.mkdirs()) {
                return;
            }
            if (folder.list().length == 0) {
                return;
            }

            MemeConfig.Config conf = null;
            File configFile = new File(folder, MEMETASTIC_CONFIG_FILE);
            FileUtils.touch(new File(folder, ".nomedia"));
            if (configFile.exists()) {
                try {
                    String contents = FileUtils.readTextFile(configFile);
                    JSONObject json = new JSONObject(contents);
                    conf = new MemeConfig.Config().fromJson(json);
                } catch (Exception ignored) {
                }
            }

            // Create new if empty
            if (conf == null) {
                conf = new MemeConfig.Config();
                conf.setFonts(new ArrayList<MemeConfig.Font>());
                conf.setImages(new ArrayList<MemeConfig.Image>());
            }

            boolean assetsChanged = checkForNewAssets(folder, conf);

            for (MemeConfig.Font confFont : conf.getFonts()) {
                MemeData.Font dataFont = new MemeData.Font();
                dataFont.conf = confFont;
                dataFont.fullPath = new File(folder, confFont.getFilename());
                dataFont.typeFace = Typeface.createFromFile(dataFont.fullPath);
                if (dataFont.fullPath.exists()) {
                    if (!dataFonts.contains(dataFont)) {
                        dataFonts.add(dataFont);
                    }
                } else {
                    assetsChanged = true;
                }
            }

            for (MemeConfig.Image confImage : conf.getImages()) {
                MemeData.Image dataImage = new MemeData.Image();
                dataImage.conf = confImage;
                dataImage.fullPath = new File(folder, confImage.getFilename());
                dataImage.isTemplate = true;
                if (dataImage.fullPath.exists()) {
                    if (!dataImages.contains(dataImage)) {
                        dataImages.add(dataImage);
                    }
                } else {
                    assetsChanged = true;
                }
            }

            if (assetsChanged) {
                try {
                    FileUtils.writeFile(configFile, conf.toJson().toString());
                } catch (Exception ignored) {
                }
            }
        }

        private boolean checkForNewAssets(File folder, MemeConfig.Config conf) {
            boolean assetsChanged = false;
            final ArrayList<String> extensions = new ArrayList<>();
            extensions.addAll(Arrays.asList(MEMETASTIC_IMAGES_EXTS));
            extensions.addAll(Arrays.asList(MEMETASTIC_FONT_EXTS));

            // Get all files that are maybe compatible
            ArrayList<String> files = new ArrayList<>(Arrays.asList(
                    folder.list(new FilenameFilter() {
                        @Override
                        public boolean accept(File file, String s) {
                            String flc = s.toLowerCase();
                            for (String extension : extensions) {
                                if (flc.endsWith("." + extension.toLowerCase())) {
                                    return true;
                                }
                            }
                            return false;
                        }
                    })
            ));

            // Check if all fonts and images are indexed
            for (MemeConfig.Font data : conf.getFonts()) {
                if (files.contains(data.getFilename())) {
                    files.remove(data.getFilename());
                }
            }
            for (MemeConfig.Image data : conf.getImages()) {
                if (files.contains(data.getFilename())) {
                    files.remove(data.getFilename());
                }
            }

            // Index everything not indexed yet
            for (String filename : files) {
                String flc = filename.toLowerCase();
                for (String ext : MEMETASTIC_IMAGES_EXTS) {
                    if (flc.endsWith("." + ext)) {
                        MemeConfig.Image image = generateImageEntry(folder, filename, _tagKeys);
                        if (image != null) {
                            conf.getImages().add(image);
                            assetsChanged = true;
                        }
                    }
                }
                for (String ext : MEMETASTIC_FONT_EXTS) {
                    if (flc.endsWith("." + ext)) {
                        MemeConfig.Font font = generateFontEntry(folder, filename);
                        if (font != null) {
                            conf.getFonts().add(font);
                            assetsChanged = true;
                        }
                    }
                }
            }
            return assetsChanged;
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        private void loadBundledAssets(List<MemeData.Font> fonts, List<MemeData.Image> images) {
            AssetManager assetManager = _context.getAssets();
            File config = new File(getBundledAssetsDir(_appSettings), MEMETASTIC_CONFIG_FILE);
            config.delete();
            try {
                File cacheDir = getBundledAssetsDir(_appSettings);
                if (cacheDir.exists() || cacheDir.mkdirs()) {
                    for (String assetFilename : assetManager.list("bundled")) {
                        InputStream is = assetManager.open("bundled/" + assetFilename);
                        byte[] data = FileUtils.readCloseBinaryStream(is);
                        FileUtils.writeFile(new File(cacheDir, assetFilename), data);
                    }
                }
            } catch (IOException ignored) {
            }
        }

    }

    public static MemeConfig.Font generateFontEntry(File folder, String filename) {
        MemeConfig.Font confFont = new MemeConfig.Font();
        confFont.setFilename(filename);
        confFont.setTitle(filename.substring(0, filename.lastIndexOf(".")).replace("_", " "));

        return confFont;
    }

    public static MemeConfig.Image generateImageEntry(File folder, String filename, String[] tagKeys) {
        ArrayList<String> tags = new ArrayList<>();

        // animals__advice_mallard.jpg --> tag animals recognized
        String[] nameSplits = filename.split("__");
        for (String tagKey : tagKeys) {
            for (String nameSplit : nameSplits) {
                if (nameSplit.equals(tagKey)) {
                    tags.add(tagKey);
                }
            }
        }

        if (tags.isEmpty()) {
            tags.add(MemeConfig.Image.IMAGE_TAG_OTHER);
        }

        MemeConfig.Image confImage = new MemeConfig.Image();
        confImage.setCaptions(new ArrayList<MemeConfig.Caption>());
        confImage.setFilename(filename);
        confImage.setTags(tags);
        confImage.setTitle(filename.substring(0, filename.lastIndexOf(".")).replace("_", " "));

        return confImage;
    }
}
