package io.github.gsantner.memetastic.util;

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
