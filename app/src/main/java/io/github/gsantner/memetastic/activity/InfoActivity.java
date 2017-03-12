package io.github.gsantner.memetastic.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.github.gsantner.memetastic.R;
import io.github.gsantner.memetastic.util.Helpers;

public class InfoActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.info__activity__text_app_version)
    TextView textAppVersion;

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

        textAppVersion.setText("App Version   v" + Helpers.getAppVersionName(this));
    }

    @OnClick(R.id.info__activity__text_app_version)
    public void onVersionClicked(View v) {
        Helpers.openWebpage(this, getString(R.string.app_www_source));
    }

    @OnClick(R.id.info__activity__button_licenses)
    public void showLicensesDialog(View v) {
        WebView wv = new WebView(this);
        wv.loadUrl("file:///android_res/raw/licenses.html");
        AlertDialog.Builder dialog = new AlertDialog.Builder(this)
                .setPositiveButton(android.R.string.ok, null)
                .setTitle(R.string.info__license)
                .setView(wv);
        dialog.show();
    }


}
