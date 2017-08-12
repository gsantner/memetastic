package io.github.gsantner.memetastic.util;

import android.app.Activity;

public class HelpersA extends io.github.gsantner.opoc.util.HelpersA {
    private HelpersA(Activity activity) {
        super(activity);
    }

    public static HelpersA get(Activity activity) {
        return new HelpersA(activity);
    }
}
