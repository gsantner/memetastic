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
package net.gsantner.memetastic.util;

import android.content.Context;
import android.os.Build;
import android.support.v7.widget.PopupMenu;
import android.view.View;

import net.gsantner.memetastic.App;
import net.gsantner.memetastic.service.AssetUpdater;
import net.gsantner.memetastic.service.MigrationThread;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;

public class ContextUtils extends net.gsantner.opoc.util.ContextUtils {
    public ContextUtils(Context context) {
        super(context);
    }


    public static ContextUtils get() {
        return new ContextUtils(App.get());
    }

    public static void checkForAssetUpdates(Context context) {
        new MigrationThread(context).start();
        Date sevenDaysAgo = new Date(System.currentTimeMillis() - 7 * 1000 * 60 * 60 * 24);
        if (AppSettings.get().getLastAssetArchiveCheckDate().before(sevenDaysAgo)) {
            new AssetUpdater.UpdateThread(context, false).start();
        }
    }

    public static void popupMenuEnableIcons(PopupMenu popupMenu) {
        try {
            Field[] fields = popupMenu.getClass().getDeclaredFields();
            for (Field field : fields) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popupMenu);
                    Class<?> classPopupHelper = Class.forName(menuPopupHelper.getClass().getName());
                    Method setForceIcons = classPopupHelper.getMethod("setForceShowIcon", boolean.class);
                    setForceIcons.invoke(menuPopupHelper, true);
                    break;
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public int getImmersiveUiVisibility() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            int statusBarFlag = View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                statusBarFlag = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            return statusBarFlag
                    //| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        }
        return 0;
    }

    public void enableImmersiveMode(final View decorViewOfActivity) {
        decorViewOfActivity.setSystemUiVisibility(getImmersiveUiVisibility());
        decorViewOfActivity.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                    decorViewOfActivity.setSystemUiVisibility(getImmersiveUiVisibility());
                }
            }
        });
    }

    /**
     * Calculates the scaling factor to convert conf size to size in pixels
     *
     * @param w width of the bitmap where a text should be written on
     * @param h height of the bitmap where a text should be written on
     * @return the size of the conf in pixels
     */
    public float getScalingFactorInPixelsForWritingOnPicture(int w, int h) {
        final float fontScaler = (float) 133;
        final int raster = 50;
        int size = Math.min(w, h);
        int rest = size % raster;

        // Round
        int addl = rest >= raster / 2 ? raster - rest : -rest;

        return (size + addl) / (fontScaler);
    }
}
