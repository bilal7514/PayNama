package com.humudtech.paynama;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.humudtech.paynama.Adapters.BooksAdapter;
import com.humudtech.paynama.Models.Book;
import com.humudtech.paynama.utils.DetectConnection;
import com.humudtech.paynama.utils.Tools;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SplashActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    com.android.volley.RequestQueue requestQueue;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);
        Tools.setSystemBarColor(this, android.R.color.white);
        Tools.setSystemBarLight(this);
        startApplication();
    }
    @Override
    public void onStop () {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
    private void startApplication() {
        if (!DetectConnection.checkInternetConnection(SplashActivity.this)) {
            if(!isFinishing()){
                DetectConnection.showNoInternet(SplashActivity.this);
            }
        }
        else{
            String url = DetectConnection.getUrl()+"android/get-key.php";
            StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
                SharedPreferences.Editor e = sharedPreferences.edit();
                e.putString("banner_ad",response);
                e.apply();
                if(sharedPreferences.getString("isLoggedIn","").equals("1")){
                    Intent intent=new Intent(getApplicationContext(),BaseActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
                else{
                    Intent intent=new Intent(getApplicationContext(),LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }, error -> {
            }){
                @Override
                protected Map<String, String> getParams() {
                    // Creating Map String Params.
                    Map<String, String> params = new HashMap<String, String>();
                    return params;
                }
            };

            requestQueue = Volley.newRequestQueue(SplashActivity.this);
            int socketTimeout = 30000;
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            request.setRetryPolicy(policy);
            requestQueue.add(request);
        }
    }
}
