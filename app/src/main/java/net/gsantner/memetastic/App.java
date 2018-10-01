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
package net.gsantner.memetastic;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.util.Log;

import net.gsantner.memetastic.util.AppSettings;
import net.gsantner.opoc.util.ShareUtil;

import io.github.gsantner.memetastic.BuildConfig;
import io.github.gsantner.memetastic.R;

/**
 * The apps application object
 */
public class App extends Application {
    private volatile static App app;
    public AppSettings settings;

    public static App get() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        settings = AppSettings.get();

        if (settings.isAppFirstStart(false)) {
            // Set default values (calculated in getters)
            settings.setGridColumnCountPortrait(settings.getGridColumnCountPortrait());
            settings.setGridColumnCountLandscape(settings.getGridColumnCountLandscape());
        }
    }

    public void shareBitmapToOtherApp(Bitmap bitmap, Activity activity) {
        ShareUtil su = new ShareUtil(activity).setFileProviderAuthority(getString(R.string.app_fileprovider));
        su.setChooserTitle(getString(R.string.share_meme_via__appspecific));
        su.shareImage(bitmap, Bitmap.CompressFormat.JPEG, 65, "MT-meme");
/*
        File file = new File(getCacheDir(), getString(R.string.cached_picture_filename));
        if (ContextUtils.get().writeImageToFileJpeg(file, bitmap)) {
            Uri imageUri = FileProvider.getUriForFile(this, getString(R.string.app_fileprovider), file);
            if (imageUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(imageUri, getContentResolver().getType(imageUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, imageUri);
                activity.startActivity(Intent.createChooser(shareIntent, getString(R.string.main__share_meme_prompt)));
            }
        }*/
    }

    public static void log(String text) {
        if (BuildConfig.DEBUG) {
            Log.d("MemeTastic", text);
        }
    }
}
