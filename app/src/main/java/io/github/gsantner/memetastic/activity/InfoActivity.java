package io.github.gsantner.memetastic.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.util.Helpers;
import io.github.gsantner.opoc.util.HelpersA;
import io.github.gsantner.opoc.util.SimpleMarkdownParser;

public class InfoActivity extends AppCompatActivity {
    //####################
    //##  Ui Binding
    //####################
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.info__activity__text_app_version)
    TextView textAppVersion;

    @BindView(R.id.info__activity__text_team)
    TextView textTeam;

    @BindView(R.id.info__activity__text_contributors)
    TextView textContributors;

    @BindView(R.id.info__activity__text_license)
    TextView textLicense;

    //####################
    //##  Methods
    //####################
    @Override
    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.info__activity);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        textTeam.setText(new SpannableString(Html.fromHtml(
                Helpers.get().loadMarkdownForTextViewFromRaw(R.raw.maintainers, ""))));
        textTeam.setMovementMethod(LinkMovementMethod.getInstance());

        textContributors.setText(new SpannableString(Html.fromHtml(
                Helpers.get().loadMarkdownForTextViewFromRaw(R.raw.contributors, "* ")
        )));
        textContributors.setMovementMethod(LinkMovementMethod.getInstance());

        // License text MUST be shown
        try {
            textLicense.setText(new SpannableString(Html.fromHtml(
                    SimpleMarkdownParser.get().parse(getString(R.string.copyright_license_text_official).replace("\n", "  \n"),
                            SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW, "").getHtml()
            )));
        } catch (IOException e) {
            e.printStackTrace();
        }


        // App version
        try {
            PackageManager manager = getPackageManager();
            PackageInfo info = manager.getPackageInfo(getPackageName(), 0);
            textAppVersion.setText(getString(R.string.app_version_v, info.versionName));
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.info__activity__text_app_version)
    public void onVersionClicked(View v) {
        Helpers.get().openWebpageInExternalBrowser(getString(R.string.app_www_source));
    }

    @OnClick({R.id.info__activity__text_app_version, R.id.info__activity__button_third_party_licenses, R.id.info__activity__button_gplv3_license})
    public void onButtonClicked(View v) {
        Context context = v.getContext();
        switch (v.getId()) {
            case R.id.info__activity__text_app_version: {
                HelpersA.get(this).openWebpageInExternalBrowser(getString(R.string.app_www_source));
                break;
            }
            case R.id.info__activity__button_gplv3_license: {
                HelpersA.get(this).showDialogWithHtmlTextView(R.string.info__licenses, Helpers.get().loadMarkdownForTextViewFromRaw(R.raw.license, ""));
                break;
            }
            case R.id.info__activity__button_third_party_licenses: {
                try {
                    HelpersA.get(this).showDialogWithHtmlTextView(R.string.info__licenses, new SimpleMarkdownParser().parse(
                            getResources().openRawResource(R.raw.licenses_3rd_party),
                            SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW, "").getHtml()
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
