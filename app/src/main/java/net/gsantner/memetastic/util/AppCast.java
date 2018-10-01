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
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

public class AppCast {
    //########################
    //## Send broadcast
    //########################
    private static void sendBroadcast(Context c, Intent i) {
        if (c != null) {
            LocalBroadcastManager.getInstance(c).sendBroadcast(i);
        }
    }

    //########################
    //## Filter
    //########################
    public static IntentFilter getLocalBroadcastFilter() {
        IntentFilter intentFilter = new IntentFilter();
        String[] BROADCAST_ACTIONS = {
                ASSET_DOWNLOAD_REQUEST.ACTION,
                DOWNLOAD_STATUS.ACTION,
                ASSETS_LOADED.ACTION
        };
        for (String action : BROADCAST_ACTIONS) {
            intentFilter.addAction(action);
        }
        return intentFilter;
    }

    //########################
    //## Actions
    //########################
    public static class ASSET_DOWNLOAD_REQUEST {
        public static final String ACTION = "ASSET_DOWNLOAD_REQUEST";
        public static final String EXTRA_RESULT = "EXTRA_RESULT";

        public static void send(Context c, int result) {
            Intent intent = new Intent(ACTION);
            intent.putExtra(EXTRA_RESULT, result);
            sendBroadcast(c, intent);
        }
    }

    public static class ASSETS_LOADED {
        public static final String ACTION = "ASSETS_LOADED";

        public static void send(Context c) {
            Intent intent = new Intent(ACTION);
            sendBroadcast(c, intent);
        }
    }

    public static class DOWNLOAD_STATUS {
        public static final String ACTION = "DOWNLOAD_STATUS";
        public static final String EXTRA_STATUS = "EXTRA_STATUS";
        public static final String EXTRA_PERCENT = "EXTRA_PERCENT";

        public static void send(Context c, int status, int percent) {
            Intent intent = new Intent(ACTION);
            intent.putExtra(EXTRA_STATUS, status);
            intent.putExtra(EXTRA_PERCENT, percent);
            sendBroadcast(c, intent);
        }
    }
}
