package com.humudtech.paynama;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.humudtech.paynama.utils.DetectConnection;

public class NoInternetActivity extends AppCompatActivity {
    private ProgressBar progress_bar;
    private LinearLayout lyt_no_connection;
    private AppCompatButton bt_retry;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);
        getSupportActionBar().hide();
        initComponent();
    }
    private void initComponent() {
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        lyt_no_connection = (LinearLayout) findViewById(R.id.lyt_no_connection);
        bt_retry = (AppCompatButton) findViewById(R.id.bt_retry);

        progress_bar.setVisibility(View.GONE);
        lyt_no_connection.setVisibility(View.VISIBLE);

        bt_retry.setOnClickListener(v -> AgainCheck());
    }
    private void AgainCheck() {

        progress_bar.setVisibility(View.VISIBLE);
        lyt_no_connection.setVisibility(View.GONE);

        new Handler().postDelayed(() -> {
            if (DetectConnection.checkInternetConnection(getApplicationContext())) {
                finish();
            } else {
                progress_bar.setVisibility(View.GONE);
                lyt_no_connection.setVisibility(View.VISIBLE);
            }
        }, 3000);
    }
}
