package com.humudtech.paynama;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {
    Dialog dialog;
    Spinner district;
    com.android.volley.RequestQueue requestQueue;
    private LinearLayout progress_bar;
    EditText old, _new, confirm;
    TextView id, p_num;
    NestedScrollView scrollView;
    SharedPreferences sharedPreferences;
    LinearLayout change_password, logout, change_station, rate_us;
    User applicationUser;
    boolean error = false;
    List<District> districtList;
    int p_district;
    String company = "0";
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        change_station = root.findViewById(R.id.change_station);
        change_password = root.findViewById(R.id.change_password);
        logout = root.findViewById(R.id.logout);
        rate_us = root.findViewById(R.id.rate_us);
        scrollView = root.findViewById(R.id.scroll_view);
        progress_bar = root.findViewById(R.id.progress_bar);
        p_num = root.findViewById(R.id.p_num);
        id = root.findViewById(R.id.id);


        sharedPreferences= getActivity().getSharedPreferences("UserData", MODE_PRIVATE);
        applicationUser = new User();
        applicationUser = DetectConnection.getUserObject(sharedPreferences.getString("userObject",""));

        if (applicationUser.getAccType().equals("DDO")) {
            id.setText("DDO Code. "+applicationUser.getDdo());
        }else {
            id.setText("CNIC. "+applicationUser.getCnic());
        }
        p_num.setText("District. "+applicationUser.getDistrict());
        change_station.setOnClickListener(v -> {
            if (!DetectConnection.checkInternetConnection(getActivity())) {
                DetectConnection.showNoInternet(getActivity());
            }else{
                getDistricts();
            }
        });
        change_password.setOnClickListener(v -> ChangePassword());
        logout.setOnClickListener(v -> Logout());
        rate_us.setOnClickListener(v -> RateUs());
        return root;
    }
    private void getDistricts() {
        scrollView.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);
        String url = DetectConnection.getUrl()+"android/get-companies.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if(jsonObject.getString("code").equals("200"))
                {
                    districtList = new ArrayList<>();
                    districtList.add(new District("0","Select District"));
                    JSONArray array = jsonObject.getJSONArray("districts");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        District district = new District(object.getString("RecID"),object.getString("fname"));
                        districtList.add(district);
                    }
                    changeStation();
                }else
                {
                    DetectConnection.showError(getActivity(),jsonObject.getString("msg"));
                }
            } catch (Exception e) {
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
                return params;
            }
        };

        requestQueue = Volley.newRequestQueue(getActivity());
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue.add(request);
    }
    private void changeStation() {
        dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_district);
        dialog.setCancelable(true);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        final Button save = (Button) dialog.findViewById(R.id.bt_save);
        district = (Spinner) dialog.findViewById(R.id.sp_district);

        ArrayAdapter<District> districtArrayAdapter = new ArrayAdapter<District>(getActivity(), android.R.layout.simple_spinner_dropdown_item, districtList);
        districtArrayAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        district.setAdapter(districtArrayAdapter);
        districtArrayAdapter.notifyDataSetChanged();
        district.setSelection(0);
        district.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                District district = (District) parent.getSelectedItem();
                company = district.getId();
                p_district = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        save.setOnClickListener(v -> {
            if (!DetectConnection.checkInternetConnection(getActivity())) {
                DetectConnection.showNoInternet(getActivity());
            }else{
                ChangeServerDistrict();
            }
        });
        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

    private void ChangeServerDistrict() {
        if(p_district!=0){
            dialog.dismiss();
            scrollView.setVisibility(View.GONE);
            progress_bar.setVisibility(View.VISIBLE);
            String HttpUrl= DetectConnection.getUrl()+"android/change-station.php";

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
                        p_num.setText("District. "+applicationUser.getDistrict());
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
                    params.put("company",company);
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
        else {
            DetectConnection.showError(getActivity(),"Select a district first");
        }
    }

    private void Logout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Logout").setMessage("Are you sure?").setCancelable(false);
        builder.setNegativeButton("Yes", (dialog, which) -> {
            if (!DetectConnection.checkInternetConnection(getActivity())) {
                DetectConnection.showNoInternet(getActivity());
            }else{
                logoutFromServer();
            }
            dialog.dismiss();
        });
        builder.setPositiveButton("No", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }
    public void logoutFromServer(){
        progress_bar.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.GONE);
        String url = DetectConnection.getUrl()+"android/logout.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            sharedPreferences.edit().clear().commit();
            Intent intent = new Intent(context,LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            getActivity().finish();
        }, error -> {
            progress_bar.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
            DetectConnection.showError(getActivity(),"Something went wrong. Try again!");
        }){
            @Override
            protected Map<String, String> getParams() {
                // Creating Map String Params.
                Map<String, String> params = new HashMap<String, String>();
                params.put("user",applicationUser.getId());
                params.put("token",applicationUser.getToken());
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(getActivity());
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue.add(request);
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
                DetectConnection.showNoInternet(getActivity());
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
}
