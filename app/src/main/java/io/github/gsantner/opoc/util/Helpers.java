/*
 * ---------------------------------------------------------------------------- *
 * Gregor Santner <gsantner.github.io> wrote this file. You can do whatever
 * you want with this stuff. If we meet some day, and you think this stuff is
 * worth it, you can buy me a coke in return. Provided as is without any kind
 * of warranty. No attribution required.                  - Gregor Santner
 *
 * License: Creative Commons Zero (CC0 1.0)
 *  http://creativecommons.org/publicdomain/zero/1.0/
 * ----------------------------------------------------------------------------
 */
package io.github.gsantner.opoc.util;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.RawRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.webkit.WebView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import io.github.gsantner.memetastic.App;
import io.github.gsantner.memetastic.BuildConfig;
import io.github.gsantner.memetastic.R;

@SuppressWarnings({"WeakerAccess", "unused", "SameParameterValue"})
public class Helpers {
    protected Context context;

    protected Helpers(Context context) {
        this.context = context;
    }

    public static Helpers get() {
        return new Helpers(App.get());
    }

    public String str(@StringRes int strResId) {
        return context.getString(strResId);
    }

    public Drawable drawable(@DrawableRes int resId) {
        return ContextCompat.getDrawable(context, resId);
    }

    public int color(@ColorRes int resId) {
        return ContextCompat.getColor(context, resId);
    }

    public Context context() {
        return context;
    }

    public String colorToHexString(int intColor) {
        return String.format("#%06X", 0xFFFFFF & intColor);
    }

    public String getAppVersionName() {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "unknown";
        }
    }


    public void openWebpageInExternalBrowser(String url) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public void showDonateBitcoinRequest() {
        if (!BuildConfig.IS_GPLAY_BUILD) {
            String btcUri = String.format("bitcoin:%s?amount=%s&label=%s&message=%s",
                    str(R.string.donate__bitcoin_id), str(R.string.donate__bitcoin_amount),
                    str(R.string.donate__bitcoin_message), str(R.string.donate__bitcoin_message));
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(btcUri));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                openWebpageInExternalBrowser(str(R.string.donate__bitcoin_url));
            }
        }
    }

    public String readTextfileFromRawRes(@RawRes int rawResId, String linePrefix, String linePostfix) {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        String line;

        linePrefix = linePrefix == null ? "" : linePrefix;
        linePostfix = linePostfix == null ? "" : linePostfix;

        try {
            br = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(rawResId)));
            while ((line = br.readLine()) != null) {
                sb.append(linePrefix);
                sb.append(line);
                sb.append(linePostfix);
                sb.append("\n");
            }
        } catch (Exception ignored) {
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ignored) {
                }
            }
        }
        return sb.toString();
    }

    public void showDialogWithRawFileInWebView(String fileInRaw, @StringRes int resTitleId) {
        WebView wv = new WebView(context);
        wv.loadUrl("file:///android_res/raw/" + fileInRaw);
        AlertDialog.Builder dialog = new AlertDialog.Builder(context)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(resTitleId)
                .setView(wv);
        dialog.show();
    }

    @SuppressLint("RestrictedApi")
    public void setTintColorOfButton(AppCompatButton button, @ColorRes int resColor) {
        button.setSupportBackgroundTintList(ColorStateList.valueOf(
                color(resColor)
        ));
    }

    public boolean isConnectedToInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetInfo != null && activeNetInfo.isConnectedOrConnecting();
    }

    public void restartApp(Class classToStartupWith) {
        Intent restartIntent = new Intent(context, classToStartupWith);
        PendingIntent restartIntentP = PendingIntent.getActivity(context, 555,
                restartIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, restartIntentP);
        System.exit(0);
    }

    public String loadMarkdownForTextViewFromRaw(@RawRes int rawMdFile, String prepend) {
        try {
            return new SimpleMarkdownParser()
                    .parse(context.getResources().openRawResource(rawMdFile),
                            SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW, prepend)
                    .replaceColor("#000001", color(R.color.accent))
                    .removeMultiNewlines().replaceBulletCharacter("*").getHtml();
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }

    public double getEstimatedScreenSizeInches() {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();

        double density = dm.density * 160;
        double x = Math.pow(dm.widthPixels / density, 2);
        double y = Math.pow(dm.heightPixels / density, 2);
        double screenInches = Math.sqrt(x + y) * 1.16;  // 1.16 = est. Nav/Statusbar
        screenInches = screenInches < 4.0 ? 4.0 : screenInches;
        screenInches = screenInches > 12.0 ? 12.0 : screenInches;
        return screenInches;
    }

    public boolean isInPortraitMode() {
        return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
    }

    public Locale getLocaleByAndroidCode(String code) {
        if (!TextUtils.isEmpty(code)) {
            return code.contains("-r")
                    ? new Locale(code.substring(0, 2), code.substring(4, 6)) // de-rAt
                    : new Locale(code); // de
        }
        return Locale.getDefault();
    }

    //  "en"/"de"/"de-rAt"; Empty string = default locale
    public void setAppLanguage(String androidLocaleString) {
        Locale locale = getLocaleByAndroidCode(androidLocaleString);
        Configuration config = context.getResources().getConfiguration();
        config.locale = locale != null ? locale : Locale.getDefault();
        context.getResources().updateConfiguration(config, null);
    }
}
