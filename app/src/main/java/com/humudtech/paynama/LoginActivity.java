package com.humudtech.paynama;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.security.ProviderInstaller;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.humudtech.paynama.utils.DetectConnection;
import com.humudtech.paynama.utils.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {
    com.android.volley.RequestQueue requestQueue;
    Dialog dialog;
    EditText cnic;
    SharedPreferences sharedPreferences;
    TextView openGmail;
    private ProgressBar progress_bar;
    private FloatingActionButton fab;
    boolean error = false;
    String newToken = "00";
    LinearLayout layout, progress;
    TextView forgot, guest;
    TextInputEditText username, password;
    CheckBox checkBox;
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
        progress = findViewById(R.id.progress_bar1);
        progress_bar = (ProgressBar) findViewById(R.id.progress_bar);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        openGmail = findViewById(R.id.tv_email);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        checkBox = findViewById(R.id.checkBox);
        guest = findViewById(R.id.guest);

        Tools.setSystemBarColor(this, android.R.color.white);
        Tools.setSystemBarLight(this);

        forgot = findViewById(R.id.forgot);

        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        layout.setOnTouchListener((view, motionEvent) -> {
            DetectConnection.hideKeyboard(view, LoginActivity.this);
            return false;
        });
        openGmail.setOnClickListener(v -> {
            openGmail();
        });

        if(!hasPermissions(LoginActivity.this, PERMISSIONS)){
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            }else{
                password.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            }
        });
        findViewById(R.id.sign_up_for_account).setOnClickListener(view -> {
            if(!hasPermissions(LoginActivity.this, PERMISSIONS)){
                ActivityCompat.requestPermissions(LoginActivity.this, PERMISSIONS, PERMISSION_ALL);
            }else{
                if (!DetectConnection.checkInternetConnection(LoginActivity.this)) {
                    if(!isFinishing()){
                        DetectConnection.showNoInternet(LoginActivity.this);
                    }

                }else{
                    Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                    startActivity(intent);
                }

            }
        });
        guest.setOnClickListener(view -> {
            if(!hasPermissions(LoginActivity.this, PERMISSIONS)){
                ActivityCompat.requestPermissions(LoginActivity.this, PERMISSIONS, PERMISSION_ALL);
            }else{
                if (!DetectConnection.checkInternetConnection(LoginActivity.this)) {
                    if(!isFinishing()){
                        DetectConnection.showNoInternet(LoginActivity.this);
                    }

                }else{
                    Intent intent = new Intent(LoginActivity.this,BaseActivity.class);
                    startActivity(intent);
                }

            }
        });
        fab.setOnClickListener(v -> {
            if(!hasPermissions(LoginActivity.this, PERMISSIONS)){
                ActivityCompat.requestPermissions(LoginActivity.this, PERMISSIONS, PERMISSION_ALL);
            }else{
                if (!DetectConnection.checkInternetConnection(LoginActivity.this)) {
                    if(!isFinishing()){
                        DetectConnection.showNoInternet(LoginActivity.this);
                    }
                }else{
                    login();
                }
            }
        });
        forgot.setOnClickListener(v -> {
            if (!DetectConnection.checkInternetConnection(LoginActivity.this)) {
                if(!isFinishing()){
                    DetectConnection.showNoInternet(LoginActivity.this);
                }
            }else{
                showRecoverDialog();
            }
        });
        if(sharedPreferences.getString("isLoggedIn","").equals("1")){
            Intent i = new Intent(LoginActivity.this, BaseActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
            finish();
        }
        updateAndroidSecurityProvider(LoginActivity.this);
    }

    private void showRecoverDialog() {
        dialog = new Dialog(LoginActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_forgot);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final Button find = (Button) dialog.findViewById(R.id.recover);
        cnic = (EditText) dialog.findViewById(R.id.et_cnic);

        find.setOnClickListener(v -> {
            if (!DetectConnection.checkInternetConnection(LoginActivity.this)) {
                DetectConnection.showNoInternet(LoginActivity.this);
            }else{
                FindAccount();
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void FindAccount() {
        if(cnic.getText().toString().length()!=13){
            password.setError("Please type your 13 digit CNIC without dashes");
        }else{
            dialog.dismiss();
            layout.setVisibility(View.GONE);
            progress.setVisibility(View.VISIBLE);
            String HttpUrl= DetectConnection.getUrl()+"android/mail.php";
            StringRequest stringRequest=new StringRequest(Request.Method.POST, HttpUrl, response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getString("code").equals("200"))
                    {
                        DetectConnection.showSuccessGeneral(LoginActivity.this,jsonObject.getString("msg"));
                    }else
                    {
                        DetectConnection.showError(LoginActivity.this,jsonObject.getString("msg"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                layout.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
            }, error -> {
                layout.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                DetectConnection.showError(LoginActivity.this,"Something went wrong. Try again!");
            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("cnic",cnic.getText().toString());
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
    private void login() {
        if(!validate()){
            String HttpUrl= DetectConnection.getUrl()+"android/login.php";
            progress_bar.setVisibility(View.VISIBLE);
            fab.setAlpha(0f);
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
                        if(!isFinishing()){
                            DetectConnection.showError(LoginActivity.this,jsonObject.getString("msg"));
                        }

                    }
                    progress_bar.setVisibility(View.GONE);
                    fab.setAlpha(1f);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                progress_bar.setVisibility(View.GONE);
                fab.setAlpha(1f);
                if(!isFinishing()){
                    DetectConnection.showError(LoginActivity.this,error.getMessage());
                }

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
    private void updateAndroidSecurityProvider(Activity callingActivity) {
        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            // Thrown when Google Play Services is not installed, up-to-date, or enabled
            // Show dialog to allow users to install, update, or otherwise enable Google Play services.
            GooglePlayServicesUtil.getErrorDialog(e.getConnectionStatusCode(), callingActivity, 0);
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e("SecurityException", "Google Play Services not available.");
        }
    }
    private void openGmail() {
        // perform click on Email ID
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/html");
        final PackageManager pm = getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(emailIntent, 0);
        String className = null;
        for (final ResolveInfo info : matches) {
            if (info.activityInfo.packageName.equals("com.google.android.gm")) {
                className = info.activityInfo.name;
                if (className != null && !className.isEmpty()) {
                    break;
                }
            }
        }
        emailIntent.setData(Uri.parse("mailto:helpdesk@paynama.net"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback of " + getResources().getString(R.string.app_name));
        emailIntent.setClassName("com.google.android.gm", className);
        try {
            startActivity(emailIntent);
        } catch (ActivityNotFoundException ex) {
            // handle error
        }

    }
}
