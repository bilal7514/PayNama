package com.humudtech.paynama;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.humudtech.paynama.Models.District;
import com.humudtech.paynama.Models.Government;
import com.humudtech.paynama.Models.User;
import com.humudtech.paynama.utils.DetectConnection;
import com.humudtech.paynama.utils.Tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class SignUpActivity extends AppCompatActivity {
    com.android.volley.RequestQueue requestQueue;
    Spinner government, account_type, district;
    TextView tv_progress;
    NestedScrollView scroll_view;
    LinearLayout progress_bar;
    EditText cnic, password, confirm_password, p_num, email;
    Button sign_up;
    boolean error = false;
    Pattern EMAIL_ADDRESS_PATTERN = Patterns.EMAIL_ADDRESS;
    String newToken = "00", company = "0", gov, acc_type;
    int p_gov, p_acc_type, p_district;
    SharedPreferences sharedPreferences;
    List<Government> governments, pensionerGovernments;
    List<District> districtList;
    List<String> account_types;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        getSupportActionBar().hide();

        government = findViewById(R.id.gov);
        account_type = findViewById(R.id.acc_type);
        district = findViewById(R.id.district);
        tv_progress = findViewById(R.id.tv_progress);
        scroll_view = findViewById(R.id.scroll_view);
        progress_bar = findViewById(R.id.progress_bar);
        cnic = findViewById(R.id.cnic);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.confirm_password);
        p_num = findViewById(R.id.p_num);
        email = findViewById(R.id.email);
        sign_up = findViewById(R.id.sign_up);

        districtList = new ArrayList<>();
        governments = new ArrayList<>();
        pensionerGovernments = new ArrayList<>();
        account_types = new ArrayList<>();

        districtList.add(new District("0","Select District"));
        governments.add(new Government("Select Government","0"));
        pensionerGovernments.add(new Government("Select Government","0"));

        account_types.add("Select Account Type");
        account_types.add("Employee");
        account_types.add("Pensioner");

        Tools.setSystemBarColor(this, android.R.color.white);
        Tools.setSystemBarLight(this);

        sharedPreferences = getSharedPreferences("UserData", MODE_PRIVATE);

        ArrayAdapter<String> adapterAccountType = new ArrayAdapter<String>(SignUpActivity.this, android.R.layout.simple_spinner_dropdown_item, account_types);
        adapterAccountType.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        account_type.setAdapter(adapterAccountType);
        ArrayAdapter<Government> governmentArrayAdapter = new ArrayAdapter<Government>(SignUpActivity.this, android.R.layout.simple_spinner_dropdown_item, governments);
        governmentArrayAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        government.setAdapter(governmentArrayAdapter);
        ArrayAdapter<District> districtArrayAdapter = new ArrayAdapter<District>(SignUpActivity.this, android.R.layout.simple_spinner_dropdown_item, districtList);
        districtArrayAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        district.setAdapter(districtArrayAdapter);

        district.setSelection(0);
        government.setSelection(0);
        account_type.setSelection(0);

        government.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
             //   ((TextView) parent.getChildAt(0)).setTextColor(getResources().getColor(R.color.colorAccentLight));
                Government government = (Government) parent.getSelectedItem();
                gov = government.getCode();
                p_gov = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
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
        account_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                acc_type = parent.getSelectedItem().toString();
                p_acc_type = position;
                if(acc_type.equals("Pensioner")){
                    ArrayAdapter<Government> governmentArrayAdapter = new ArrayAdapter<>(SignUpActivity.this, android.R.layout.simple_spinner_dropdown_item, pensionerGovernments);
                    governmentArrayAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                    government.setAdapter(governmentArrayAdapter);
                    government.setSelection(0);
                }else{
                    ArrayAdapter<Government> governmentArrayAdapter = new ArrayAdapter<>(SignUpActivity.this, android.R.layout.simple_spinner_dropdown_item, governments);
                    governmentArrayAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                    government.setAdapter(governmentArrayAdapter);
                    government.setSelection(0);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        scroll_view.setOnTouchListener((view, motionEvent) -> {
            DetectConnection.hideKeyboard(view, SignUpActivity.this);
            return false;
        });
        if (!DetectConnection.checkInternetConnection(SignUpActivity.this)) {
            if(!isFinishing()){
                DetectConnection.showNoInternet(SignUpActivity.this);
            }
        }
        else{
            getDistricts();
        }

        scroll_view.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);

        sign_up.setOnClickListener(view -> {
            if (!DetectConnection.checkInternetConnection(SignUpActivity.this)) {
                if(!isFinishing()){
                    DetectConnection.showNoInternet(SignUpActivity.this);
                }
            }else{
                signUp();
            }
        });
    }

    private void signUp() {
        if(!validation()){
            scroll_view.setVisibility(View.GONE);
            progress_bar.setVisibility(View.VISIBLE);
            tv_progress.setVisibility(View.VISIBLE);
            String HttpUrl= DetectConnection.getUrl()+"android/sign-up.php";
            StringRequest stringRequest=new StringRequest(Request.Method.POST, HttpUrl, response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getString("code").equals("200"))
                    {
                        SharedPreferences.Editor e = sharedPreferences.edit();
                        e.putString("userObject",jsonObject.getJSONObject("user").toString());
                        e.putString("isLoggedIn","1");
                        e.apply();
                        Intent i = new Intent(SignUpActivity.this, BaseActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }else
                    {
                        if(!isFinishing()){
                            DetectConnection.showError(SignUpActivity.this,jsonObject.getString("msg"));
                        }

                        scroll_view.setVisibility(View.VISIBLE);
                        progress_bar.setVisibility(View.GONE);
                        tv_progress.setVisibility(View.GONE);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                scroll_view.setVisibility(View.VISIBLE);
                progress_bar.setVisibility(View.GONE);
                if(!isFinishing()){
                    DetectConnection.showError(SignUpActivity.this,error.getMessage());
                }
            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("cnic",cnic.getText().toString());
                    params.put("p_num",p_num.getText().toString());
                    params.put("email",email.getText().toString());
                    params.put("password",password.getText().toString());
                    params.put("acc_type",acc_type);
                    params.put("company",company);
                    params.put("gov",gov);
                    params.put("token",newToken);
                    return params;
                }
            };
            requestQueue = Volley.newRequestQueue(SignUpActivity.this);
            int socketTimeout = 30000;
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            stringRequest.setRetryPolicy(policy);
            requestQueue.add(stringRequest);
        }
    }

    private void getDistricts() {

        String url = DetectConnection.getUrl()+"android/get-companies.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {
                JSONObject jsonObject = new JSONObject(response);
                if(jsonObject.getString("code").equals("200"))
                {
                    JSONArray array = jsonObject.getJSONArray("districts");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject object = array.getJSONObject(i);
                        District district = new District(object.getString("RecID"),object.getString("fname"));
                        districtList.add(district);
                    }
                    ArrayAdapter<District> adapter = new ArrayAdapter<>(SignUpActivity.this, android.R.layout.simple_spinner_dropdown_item, districtList);
                    adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                    district.setAdapter(adapter);
                    JSONArray array1 = jsonObject.getJSONArray("governments");
                    for (int i = 0; i < array1.length(); i++) {
                        JSONObject object = array1.getJSONObject(i);
                        Government government = new Government(object.getString("complete_name"),object.getString("code"));
                        governments.add(government);
                    }
                    JSONArray array2 = jsonObject.getJSONArray("pensioner_governments");
                    for (int i = 0; i < array2.length(); i++) {
                        JSONObject object = array2.getJSONObject(i);
                        Government government = new Government(object.getString("complete_name"),object.getString("code"));
                        pensionerGovernments.add(government);
                    }
                    ArrayAdapter<Government> governmentArrayAdapter = new ArrayAdapter<>(SignUpActivity.this, android.R.layout.simple_spinner_dropdown_item, governments);
                    governmentArrayAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                    government.setAdapter(governmentArrayAdapter);
                }else
                {
                    if(!isFinishing()){
                        DetectConnection.showError(SignUpActivity.this,jsonObject.getString("msg"));
                    }
                }
                scroll_view.setVisibility(View.VISIBLE);
                progress_bar.setVisibility(View.GONE);
            } catch (Exception e) {
            }
        }, error -> {
            scroll_view.setVisibility(View.VISIBLE);
            progress_bar.setVisibility(View.GONE);
            if(!isFinishing()){
                DetectConnection.showError(SignUpActivity.this,"Something went wrong. Try again!");
            }
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                return params;
            }
        };
        requestQueue = Volley.newRequestQueue(SignUpActivity.this);
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue.add(request);
    }
    private boolean checkEmail(String email) {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }
    public boolean validation(){
        error = false;
        if(p_acc_type==0){
            error = true;
            DetectConnection.showError(SignUpActivity.this,"Select an account type first");
        }
        if(p_district==0){
            error = true;
            DetectConnection.showError(SignUpActivity.this,"Select your current station first");
        }
        if(p_gov==0){
            error = true;
            DetectConnection.showError(SignUpActivity.this,"Select your government / authority first");
        }
        if(TextUtils.isEmpty(email.getText().toString())) {
            email.setError("Email is required");
            email.requestFocus();
            error = true;
        }
        if(!checkEmail(email.getText().toString())){
            email.setError("Invalid Email address");
            email.requestFocus();
            error = true;
        }
        if(TextUtils.isEmpty(cnic.getText().toString())) {
            cnic.setError("Please enter your CNIC");
            cnic.requestFocus();
            error = true;
        }
        if(cnic.getText().toString().length()!=13) {
            cnic.setError("Enter your 13 digit CNIC without dashes");
            cnic.requestFocus();
            error = true;
        }
        if(TextUtils.isEmpty(p_num.getText().toString())) {
            p_num.setError("Please enter your Personal Number");
            p_num.requestFocus();
            error = true;
        }
        if(p_num.getText().toString().length()!=8) {
            p_num.setError("Enter your 8 digit personal number");
            p_num.requestFocus();
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

}
