package com.humudtech.paynama;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.iid.FirebaseInstanceId;
import com.humudtech.paynama.utils.DetectConnection;
import com.humudtech.paynama.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    com.android.volley.RequestQueue requestQueue;
    SharedPreferences sharedPreferences;
    private ProgressBar progress_bar;
    private FloatingActionButton fab;
    boolean error = false;
    String newToken = "";
    LinearLayout layout;
    TextInputEditText username, password;
    int PERMISSION_ALL = 1;
    String[] PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().hide();
        layout = findViewById(R.id.form_layout);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);

        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        layout.setOnTouchListener((view, motionEvent) -> {
            DetectConnection.hideKeyboard(view, LoginActivity.this);
            return false;
        });

        if(!hasPermissions(LoginActivity.this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }else{

        }

        findViewById(R.id.sign_up_for_account).setOnClickListener(view -> {
            if(!hasPermissions(LoginActivity.this, PERMISSIONS)){
                ActivityCompat.requestPermissions(LoginActivity.this, PERMISSIONS, PERMISSION_ALL);
            }else{
                if (!DetectConnection.checkInternetConnection(LoginActivity.this)) {
                    DetectConnection.showNoInternet(LoginActivity.this);
                }else{
                    Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                    startActivity(intent);
                }

            }
        });
        fab.setOnClickListener(v -> {
            if(!hasPermissions(LoginActivity.this, PERMISSIONS)){
                ActivityCompat.requestPermissions(LoginActivity.this, PERMISSIONS, PERMISSION_ALL);
            }else{
                if (!DetectConnection.checkInternetConnection(LoginActivity.this)) {
                    DetectConnection.showNoInternet(LoginActivity.this);
                }else{
                    login();
                }
            }
        });
        if(sharedPreferences.getString("isLoggedIn","").equals("1")){
            Intent i = new Intent(LoginActivity.this, BaseActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
    }
    private void login() {
        if(!validate()){
            String HttpUrl= DetectConnection.getUrl()+"android/login.php";
            progress_bar.setVisibility(View.VISIBLE);
            fab.setAlpha(0f);
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( LoginActivity.this, instanceIdResult -> {
                newToken = instanceIdResult.getToken();
            });
            StringRequest stringRequest=new StringRequest(Request.Method.POST, HttpUrl, response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getString("code").equals("200"))
                    {
                        SharedPreferences.Editor e = sharedPreferences.edit();
                        e.putString("userObject",jsonObject.getJSONObject("user").toString());
                        e.putString("isLoggedIn","1");
                        e.apply();
                        Intent i = new Intent(LoginActivity.this, BaseActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }else
                    {
                        DetectConnection.showError(LoginActivity.this,jsonObject.getString("msg"));
                    }
                    progress_bar.setVisibility(View.GONE);
                    fab.setAlpha(1f);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                progress_bar.setVisibility(View.GONE);
                fab.setAlpha(1f);
                DetectConnection.showError(LoginActivity.this,"Something went wrong. Try again!");
            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("username",username.getText().toString());
                    params.put("password",password.getText().toString());
                    params.put("token",newToken);
                    return params;
                }
            };
            requestQueue = Volley.newRequestQueue(LoginActivity.this);
            int socketTimeout = 30000;
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            stringRequest.setRetryPolicy(policy);
            requestQueue.add(stringRequest);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }
    private boolean validate() {
        error = false;
        if(TextUtils.isEmpty(username.getText().toString())) {
            username.setError("Username is required");
            username.requestFocus();
            error = true;
        }
        else if(TextUtils.isEmpty(password.getText().toString())) {
            password.setError("Enter Your Password");
            password.requestFocus();
            error = true;
        }
        return error;
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }
    @Override
    public void onStop () {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
}
