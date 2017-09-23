package io.github.gsantner.memetastic.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import net.gsantner.opoc.util.SimpleMarkdownParser;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.util.ActivityUtils;
import io.github.gsantner.memetastic.util.ContextUtils;

@SuppressWarnings("unused")
public class AboutActivity extends AppCompatActivity {
    //####################
    //##  Ui Binding
    //####################
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.about__activity__text_app_version)
    TextView textAppVersion;

    @BindView(R.id.about__activity__text_team)
    TextView textTeam;

    @BindView(R.id.about__activity__text_contributors)
    TextView textContributors;

    @BindView(R.id.about__activity__text_license)
    TextView textLicense;

    //####################
    //##  Methods
    //####################
    @Override
    @SuppressLint("SetTextI18n")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about__activity);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        textTeam.setMovementMethod(LinkMovementMethod.getInstance());
        textLicense.setMovementMethod(LinkMovementMethod.getInstance());
        textContributors.setMovementMethod(LinkMovementMethod.getInstance());

        ContextUtils cu = ContextUtils.get();
        cu.setHtmlToTextView(textTeam,
                ContextUtils.get().loadMarkdownForTextViewFromRaw(R.raw.maintainers, "")
        );

        cu.setHtmlToTextView(textContributors,
                cu.loadMarkdownForTextViewFromRaw(R.raw.contributors, "")
        );

        // License text MUST be shown
        try {
            cu.setHtmlToTextView(textLicense,
                    SimpleMarkdownParser.get().parse(getString(R.string.copyright_license_text_official).replace("\n", "  \n"),
                            "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW).getHtml()
            );
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

    @OnClick({R.id.about__activity__text_app_version, R.id.about__activity__button_third_party_licenses, R.id.about__activity__button_app_license})
    public void onButtonClicked(View v) {
        Context context = v.getContext();
        switch (v.getId()) {
            case R.id.about__activity__text_app_version: {
                try {
                    ActivityUtils.get(this).showDialogWithHtmlTextView(R.string.changelog, new SimpleMarkdownParser().parse(
                            getResources().openRawResource(R.raw.changelog),
                            "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW, SimpleMarkdownParser.FILTER_CHANGELOG
                            ).getHtml()
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            case R.id.about__activity__button_app_license: {
                ActivityUtils.get(this).showDialogWithHtmlTextView(R.string.licenses, ContextUtils.get().readTextfileFromRawRes(R.raw.license, "", ""), false, null);
                break;
            }
            case R.id.about__activity__button_third_party_licenses: {
                try {
                    ActivityUtils.get(this).showDialogWithHtmlTextView(R.string.licenses, new SimpleMarkdownParser().parse(
                            getResources().openRawResource(R.raw.licenses_3rd_party),
                            "", SimpleMarkdownParser.FILTER_ANDROID_TEXTVIEW).getHtml()
                    );
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
