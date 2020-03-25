package com.humudtech.paynama;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.humudtech.paynama.utils.Tools;

public class SplashActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        Handler handler = new Handler();
        handler.postDelayed(() -> startApplication(), 3000);
    }

    private void startApplication() {
        if(sharedPreferences.getString("isLoggedIn","").equals("1")){
            Intent intent=new Intent(getApplicationContext(),BaseActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }else{
            Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();
        }
    }
}
