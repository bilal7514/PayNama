package com.humudtech.paynama;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.iid.FirebaseInstanceId;
import com.humudtech.paynama.Models.District;
import com.humudtech.paynama.Models.Government;
import com.humudtech.paynama.utils.DetectConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    com.android.volley.RequestQueue requestQueue;
    Spinner government, account_type, district;
    TextView tv_progress;
    NestedScrollView scroll_view;
    LinearLayout progress_bar;
    EditText cnic, password, confirm_password, p_num;
    Button sign_up;
    boolean error = false;
    String newToken = "", company = "0", gov, acc_type;
    int p_gov, p_acc_type, p_district;
    SharedPreferences sharedPreferences;
    List<Government> governments;
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
        sign_up = findViewById(R.id.sign_up);

        districtList = new ArrayList<>();
        governments = new ArrayList<>();
        account_types = new ArrayList<>();

        districtList.add(new District("0","Select District"));

        account_types.add("Select Account Type");
        account_types.add("Employee");
        account_types.add("Pensioner");

        governments.add(new Government("Select Government","0"));
        governments.add(new Government("Federal","F"));
        governments.add(new Government("Punjab","P"));
        governments.add(new Government("District Education Authority","DEAP"));
        governments.add(new Government("District Health Authority","DHAP"));

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
            DetectConnection.showNoInternet(SignUpActivity.this);
        }
        else{
            getDistricts();
        }

        scroll_view.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);

        sign_up.setOnClickListener(view -> {
            if (!DetectConnection.checkInternetConnection(SignUpActivity.this)) {
                DetectConnection.showNoInternet(SignUpActivity.this);
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
            FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener( SignUpActivity.this, instanceIdResult -> {
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
                        Intent i = new Intent(SignUpActivity.this, BaseActivity.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                        finish();
                    }else
                    {
                        DetectConnection.showError(SignUpActivity.this,jsonObject.getString("msg"));
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
                DetectConnection.showError(SignUpActivity.this,error.getMessage());

            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("cnic",cnic.getText().toString());
                    params.put("p_num",p_num.getText().toString());
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
                }else
                {
                    DetectConnection.showError(SignUpActivity.this,jsonObject.getString("msg"));
                }
                scroll_view.setVisibility(View.VISIBLE);
                progress_bar.setVisibility(View.GONE);
            } catch (Exception e) {
            }
        }, error -> {
            scroll_view.setVisibility(View.VISIBLE);
            progress_bar.setVisibility(View.GONE);
            DetectConnection.showError(SignUpActivity.this,"Something went wrong. Try again!");
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
        if(TextUtils.isEmpty(cnic.getText().toString())) {
            cnic.setError("Please enter your CNIC");
            cnic.requestFocus();
            error = true;
        }
        if(TextUtils.isEmpty(p_num.getText().toString())) {
            p_num.setError("Please enter your Personal Number");
            p_num.requestFocus();
            error = true;
        }

        if(TextUtils.isEmpty(password.getText().toString())) {
            password.setError("Password is required");
            password.requestFocus();
            error = true;
        }
        if(password.getText().toString().length()<6) {
            password.setError("Password length must be at-least 6 characters");
            password.requestFocus();
            error = true;
        }
        if(TextUtils.isEmpty(confirm_password.getText().toString())) {
            confirm_password.setError("Please re-type password");
            confirm_password.requestFocus();
            error = true;
        }else if(!confirm_password.getText().toString().equals(password.getText().toString())) {
            confirm_password.setError("Passwords does not match");
            confirm_password.requestFocus();
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
