package com.humudtech.paynama;

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
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.messaging.FirebaseMessaging;
import com.humudtech.paynama.Models.District;
import com.humudtech.paynama.Models.User;
import com.humudtech.paynama.utils.DetectConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {
    Dialog dialog;
    com.android.volley.RequestQueue requestQueue;
    EditText email;
    private LinearLayout progress_bar;
    EditText old, _new, confirm;
    TextView id, p_num;
    NestedScrollView scrollView;
    Pattern EMAIL_ADDRESS_PATTERN = Patterns.EMAIL_ADDRESS;
    SharedPreferences sharedPreferences;
    LinearLayout change_password, logout, edit_profile, rate_us, edit_email, share, write;
    User applicationUser;
    boolean error = false;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        edit_profile = root.findViewById(R.id.edit_profile);
        edit_email = root.findViewById(R.id.edit_email);
        share = root.findViewById(R.id.share);
        write = root.findViewById(R.id.email_us);
        change_password = root.findViewById(R.id.change_password);
        logout = root.findViewById(R.id.logout);
        rate_us = root.findViewById(R.id.rate_us);
        scrollView = root.findViewById(R.id.scroll_view);
        progress_bar = root.findViewById(R.id.progress_bar);
        p_num = root.findViewById(R.id.p_num);
        id = root.findViewById(R.id.id);
        ((BaseActivity) getActivity()).showBanner();

        edit_email.setVisibility(View.GONE);
        edit_profile.setVisibility(View.GONE);

        sharedPreferences= getActivity().getSharedPreferences("UserData", MODE_PRIVATE);
        applicationUser = new User();
        applicationUser = DetectConnection.getUserObject(sharedPreferences.getString("userObject",""));

        if(applicationUser!=null){
            scrollView.setVisibility(View.VISIBLE);
            if (applicationUser.getAccType().equals("DDO")) { //crash here
                id.setText("DDO Code. "+applicationUser.getDdo());
                edit_email.setVisibility(View.VISIBLE);
            }else {
                id.setText("CNIC. "+applicationUser.getCnic());
                edit_profile.setVisibility(View.VISIBLE);
            }
            p_num.setText("District. "+applicationUser.getDistrict());
            edit_profile.setOnClickListener(v -> {
                if (!DetectConnection.checkInternetConnection(getActivity())) {
                    if(!getActivity().isFinishing()){
                        DetectConnection.showNoInternet(getActivity());
                    }
                }else{
                    Navigation.findNavController(v).navigate(R.id.editProfileFragment);
                }
            });
            change_password.setOnClickListener(v -> ChangePassword());
            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((BaseActivity) getActivity()).loadInterstitialAd();
                    Logout();
                }
            });
            rate_us.setOnClickListener(v -> RateUs());
            write.setOnClickListener(v -> openGmail());
            share.setOnClickListener(v -> shareApp());
            edit_email.setOnClickListener(v -> changeEmail());
        }else{
            scrollView.setVisibility(View.GONE);
            final Dialog dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_warning);
            dialog.setCancelable(true);

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(dialog.getWindow().getAttributes());
            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

            ((TextView) dialog.findViewById(R.id.content)).setText("Please Sign in to continue!");
            dialog.findViewById(R.id.bt_close).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(),LoginActivity.class);
                    startActivity(intent);
                }
            });
            dialog.show();
            dialog.getWindow().setAttributes(lp);
        }

        return root;
    }

    private void changeEmail() {
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_email);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final Button save = (Button) dialog.findViewById(R.id.bt_save);
        email = (EditText) dialog.findViewById(R.id.et_email);
        email.setText(applicationUser.getEmail());

        save.setOnClickListener(v -> {
            if (!DetectConnection.checkInternetConnection(getActivity())) {
                DetectConnection.showNoInternet(getActivity());
            }else{
                ChangeServerEmail();
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
    private boolean checkEmail(String email) {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    public boolean emailValidation(){
        error = false;
        if(TextUtils.isEmpty(email.getText().toString())) {
            email.setError("Please enter your email");
            email.requestFocus();
            error = true;
        }
        if(!checkEmail(email.getText().toString())){
            email.setError("Email address is not valid");
            email.requestFocus();
            error = true;
        }

        return error;
    }

    private void ChangeServerEmail() {
        if(!emailValidation()){
            dialog.dismiss();
            scrollView.setVisibility(View.GONE);
            progress_bar.setVisibility(View.VISIBLE);
            String HttpUrl= DetectConnection.getUrl()+"android/change-email.php";

            StringRequest stringRequest=new StringRequest(Request.Method.POST, HttpUrl, response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getString("code").equals("200"))
                    {
                        SharedPreferences.Editor e = sharedPreferences.edit();
                        e.putString("userObject",jsonObject.getJSONObject("user").toString());
                        e.apply();
                        applicationUser = DetectConnection.getUserObject(sharedPreferences.getString("userObject",""));
                        DetectConnection.showSuccessGeneral(getActivity(),jsonObject.getString("msg"));
                    }else
                    {
                        DetectConnection.showError(getActivity(),jsonObject.getString("msg"));
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                scrollView.setVisibility(View.VISIBLE);
                progress_bar.setVisibility(View.GONE);
            }, error -> {
                scrollView.setVisibility(View.VISIBLE);
                progress_bar.setVisibility(View.GONE);
                DetectConnection.showError(getActivity(),"Something went wrong. Try again!");
            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("email",email.getText().toString());
                    params.put("id",applicationUser.getId());
                    params.put("token",applicationUser.getToken());
                    return params;
                }
            };
            requestQueue = Volley.newRequestQueue(getActivity());
            int socketTimeout = 30000;
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            stringRequest.setRetryPolicy(policy);
            requestQueue.add(stringRequest);
        }

    }

    private void Logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Logout").setMessage("Are you sure?").setCancelable(false);
        builder.setNegativeButton("Yes", (dialog, which) -> {
            if (!DetectConnection.checkInternetConnection(getActivity())) {
                if(!getActivity().isFinishing()){
                    DetectConnection.showNoInternet(getActivity());
                }
            }else{
                UnSubscribeFromTopics();
                sharedPreferences.edit().clear().commit();
                Intent intent = new Intent(getActivity(),LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
            dialog.dismiss();
        });
        builder.setPositiveButton("No", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
    private void RateUs() {
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);

        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            getActivity().startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            getActivity().startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
        }
    }
    private void ChangePassword(){
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_password);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final Button save = (Button) dialog.findViewById(R.id.bt_save);
        old = (EditText) dialog.findViewById(R.id.et_old);
        _new = (EditText) dialog.findViewById(R.id.et_new);
        confirm = (EditText) dialog.findViewById(R.id.et_confirm);
        
        save.setOnClickListener(v -> {
            if (!DetectConnection.checkInternetConnection(getActivity())) {
                if(!getActivity().isFinishing()){
                    DetectConnection.showNoInternet(getActivity());
                }
            }else{
                ChangeServerPassword();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void ChangeServerPassword() {
        if(!validation()){
            dialog.dismiss();
            scrollView.setVisibility(View.GONE);
            progress_bar.setVisibility(View.VISIBLE);
            String HttpUrl= DetectConnection.getUrl()+"android/change-password.php";


            StringRequest stringRequest=new StringRequest(Request.Method.POST, HttpUrl, response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getString("code").equals("200"))
                    {
                        SharedPreferences.Editor e = sharedPreferences.edit();
                        e.putString("userObject",jsonObject.getJSONObject("user").toString());
                        e.apply();
                        applicationUser = DetectConnection.getUserObject(sharedPreferences.getString("userObject",""));
                        if(!getActivity().isFinishing()){
                            DetectConnection.showSuccessGeneral(getActivity(),jsonObject.getString("msg"));
                        }
                    }else
                    {
                        if(!getActivity().isFinishing()){
                            DetectConnection.showError(getActivity(),jsonObject.getString("msg"));
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
                scrollView.setVisibility(View.VISIBLE);
                progress_bar.setVisibility(View.GONE);
            }, error -> {
                scrollView.setVisibility(View.VISIBLE);
                progress_bar.setVisibility(View.GONE);
                if(!getActivity().isFinishing()){
                    DetectConnection.showError(getActivity(),"Something went wrong. Try again!");
                }

            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("password",_new.getText().toString());
                    params.put("id",applicationUser.getId());
                    params.put("token",applicationUser.getToken());
                    return params;
                }
            };
            requestQueue = Volley.newRequestQueue(getActivity());
            int socketTimeout = 30000;
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            stringRequest.setRetryPolicy(policy);
            requestQueue.add(stringRequest);
        }
    }

    public boolean validation(){
        error = false;

        if(TextUtils.isEmpty(old.getText().toString())) {
            old.setError("Please enter your old password");
            old.requestFocus();
            error = true;
        }
        if(!old.getText().toString().equals(applicationUser.getPassword())){
            old.setError("Old password is incorrect");
            old.requestFocus();
            error = true;
        }

        if(TextUtils.isEmpty(_new.getText().toString())) {
            _new.setError("Please enter your new password");
            _new.requestFocus();
            error = true;
        }

        if(_new.getText().toString().length()<6) {
            _new.setError("Password length must be at-least 6 characters");
            _new.requestFocus();
            error = true;
        }
        if(TextUtils.isEmpty(confirm.getText().toString())) {
            confirm.setError("Please re-type password");
            confirm.requestFocus();
            error = true;
        }else if(!confirm.getText().toString().equals(_new.getText().toString())) {
            confirm.setError("Passwords does not match");
            confirm.requestFocus();
            error = true;
        }
        return error;
    }
    @Override
    public void onStop () {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
    public Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;

    }
    private void openGmail() {
        // perform click on Email ID
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("text/html");
        final PackageManager pm = getActivity().getPackageManager();
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

    public void shareApp() {
        // share app with your friends
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/*");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name));
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Install" + getResources().getString(R.string.app_name) + " here: https://play.google.com/store/apps/details?id=" + getActivity().getApplicationContext().getPackageName());
        startActivity(Intent.createChooser(shareIntent, "Share Using"));
    }
    public List<String> getTopics(){
        List<String> topics = new ArrayList<>();
        topics.add(applicationUser.getAccType().replaceAll(" ","_").toLowerCase());
        topics.add(applicationUser.getDistrict().replaceAll(" ","_").toLowerCase());
        topics.add(applicationUser.getGov().replaceAll(" ","_").toLowerCase());
        topics.add(applicationUser.getAccType().replaceAll(" ","_").toLowerCase() + "_" +  applicationUser.getDistrict().replaceAll(" ","_").toLowerCase());
        topics.add(applicationUser.getAccType().replaceAll(" ","_").toLowerCase() + "_" +  applicationUser.getGov().replaceAll(" ","_").toLowerCase());
        topics.add(applicationUser.getDistrict().replaceAll(" ","_").toLowerCase() + "_" +  applicationUser.getGov().replaceAll(" ","_").toLowerCase());
        topics.add(applicationUser.getAccType().replaceAll(" ","_").toLowerCase() + "_" +  applicationUser.getDistrict().replaceAll(" ","_").toLowerCase() + "_" +  applicationUser.getGov().replaceAll(" ","_").toLowerCase());
        return topics;
    }
    private void UnSubscribeFromTopics() {
        List<String> topics = getTopics();
        for (String topic : topics) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(task -> {
            });
        }
    }
}
