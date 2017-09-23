package io.github.gsantner.memetastic.util;

import android.app.Activity;

public class ActivityUtils extends net.gsantner.opoc.util.ActivityUtils {
    private ActivityUtils(Activity activity) {
        super(activity);
    }

    public static ActivityUtils get(Activity activity) {
        return new ActivityUtils(activity);
    }
}
