/*#######################################################
 *
 *   Maintained by Gregor Santner, 2017-
 *   https://gsantner.net/
 *
 *   License: Apache 2.0
 *  https://github.com/gsantner/opoc/#licensing
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
#########################################################*/
package net.gsantner.opoc.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue", "SpellCheckingInspection", "deprecation"})
public class ZipUtils {
    private static final int BUFFER_SIZE = 4096;

    public static boolean unzip(final File zipFile, final File destRootFolder, boolean flatten) {
        return unzip(zipFile, destRootFolder, flatten, null);
    }

    public static boolean unzip(final File zipFile, final File destRootFolder,
                                final boolean flatten, final Callback.a1<Float> progressCallback) {
        try {
            final float knownLength = progressCallback == null ? -1f : getZipLength(zipFile);
            return unzip(new FileInputStream(zipFile), destRootFolder, flatten, progressCallback, knownLength);
        } catch (IOException ignored) {
            return false;
        }
    }

    public static boolean unzip(final InputStream input, final File destRootFolder,
                                final boolean flatten, final Callback.a1<Float> progressCallback,
                                final float knownLength) throws IOException {
        String filename;
        final ZipInputStream in = new ZipInputStream(new BufferedInputStream(input));

        int count;
        int written = 0;
        final byte[] buffer = new byte[BUFFER_SIZE];
        float invLength = 1f / knownLength;

        ZipEntry ze;
        while ((ze = in.getNextEntry()) != null) {
            filename = ze.getName();
            if (ze.isDirectory()) {
                if (!flatten && !new File(destRootFolder, filename).mkdirs())
                    return false;
            } else {
                if (flatten) {
                    final int idx = filename.lastIndexOf("/");
                    if (idx != -1)
                        filename = filename.substring(idx + 1);
                }

                final FileOutputStream out = new FileOutputStream(new File(destRootFolder, filename));
                while ((count = in.read(buffer)) != -1) {
                    out.write(buffer, 0, count);
                    if (invLength != -1f) {
                        written += count;
                        progressCallback.callback(written * invLength);
                    }
                }

                out.close();
                in.closeEntry();
            }
        }
        in.close();
        return true;
    }

    // TODO Maybe there's a way to avoid reading the zip twice,
    // but ZipEntry.getSize() may return -1 so we can't just cache the ZipEntries
    private static long getZipLength(final File zipFile) {
        int count;
        long totalSize = 0;
        byte[] buffer = new byte[BUFFER_SIZE];
        try {
            final ZipInputStream in = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));

            ZipEntry ze;
            while ((ze = in.getNextEntry()) != null) {
                if (!ze.isDirectory()) {
                    if (ze.getSize() == -1) {
                        while ((count = in.read(buffer)) != -1)
                            totalSize += count;
                    } else {
                        totalSize += ze.getSize();
                    }
                }
            }

            in.close();
            return totalSize;
        } catch (IOException ignored) {
            return -1;
        }
    }

    public static void zipFolder(final File srcFolder, final OutputStream out) throws IOException {
        ZipOutputStream outZip = null;
        try {
            outZip = new ZipOutputStream(out);
            addFolderToZip("", srcFolder, outZip);
        } finally {
            if (outZip != null) {
                try {
                    outZip.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static void addFileToZip(final String pathInsideZip, final File fileToZip,
                                     final ZipOutputStream outZip) throws IOException {
        if (fileToZip.isDirectory()) {
            addFolderToZip(pathInsideZip, fileToZip, outZip);
        } else {
            FileInputStream in = null;
            try {
                in = new FileInputStream(fileToZip);
                outZip.putNextEntry(new ZipEntry(pathInsideZip + "/" + fileToZip.getName()));

                int count;
                byte[] buffer = new byte[BUFFER_SIZE];
                while ((count = in.read(buffer)) > 0)
                    outZip.write(buffer, 0, count);

            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException ignored) {
                    }
                }
            }
        }
    }


    private static void addFolderToZip(String pathInsideZip, final File folderToZip,
                                       final ZipOutputStream outZip) throws IOException {
        pathInsideZip = pathInsideZip.isEmpty() ?
                folderToZip.getName() :
                pathInsideZip + "/" + folderToZip.getName();

        File[] files = folderToZip.listFiles();
        if (files != null) {
            for (File file : files)
                addFileToZip(pathInsideZip, file, outZip);
        }
    }
}
