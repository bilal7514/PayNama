package com.humudtech.paynama;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.humudtech.paynama.Models.District;
import com.humudtech.paynama.Models.Government;
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


public class EditProfileFragment extends Fragment {
    
    com.android.volley.RequestQueue requestQueue;
    Spinner government, account_type, district;
    TextView tv_progress;
    NestedScrollView scroll_view;
    LinearLayout progress_bar;
    EditText cnic, p_num, email;
    Button update;
    boolean error = false;
    Pattern EMAIL_ADDRESS_PATTERN = Patterns.EMAIL_ADDRESS;
    String company = "0", gov, acc_type;
    int p_gov, p_acc_type, p_district, selected_gov = 0;
    SharedPreferences sharedPreferences;
    List<Government> governments, pensionerGovernments;
    List<District> districtList;
    List<String> account_types;
    User applicationUser;
    String selectedAccType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_edit_profile, container, false);
        government = root.findViewById(R.id.gov);
        account_type = root.findViewById(R.id.acc_type);
        district = root.findViewById(R.id.district);
        scroll_view = root.findViewById(R.id.scroll_view);
        progress_bar = root.findViewById(R.id.progress_bar);
        cnic = root.findViewById(R.id.cnic);
        p_num = root.findViewById(R.id.p_num);
        email = root.findViewById(R.id.email);
        update = root.findViewById(R.id.update);

        districtList = new ArrayList<>();
        governments = new ArrayList<>();
        pensionerGovernments = new ArrayList<>();
        account_types = new ArrayList<>();

        districtList.add(new District("0","Select District"));

        account_types.add("Select Account Type");
        account_types.add("Employee");
        account_types.add("Pensioner");

        governments.add(new Government("Select Government","0"));
        pensionerGovernments.add(new Government("Select Government","0"));

        sharedPreferences= getActivity().getSharedPreferences("UserData", MODE_PRIVATE);
        applicationUser = new User();
        applicationUser = DetectConnection.getUserObject(sharedPreferences.getString("userObject",""));

        ArrayAdapter<String> adapterAccountType = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, account_types);
        adapterAccountType.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        account_type.setAdapter(adapterAccountType);

        selectedAccType = applicationUser.getAccType();

        account_type.setSelection(account_types.indexOf(selectedAccType));
        email.setText(applicationUser.getEmail());
        cnic.setText(applicationUser.getCnic());
        p_num.setText(applicationUser.getPNum());

        government.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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
                    ArrayAdapter<Government> governmentArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, pensionerGovernments);
                    governmentArrayAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                    government.setAdapter(governmentArrayAdapter);
                }else{
                    ArrayAdapter<Government> governmentArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, governments);
                    governmentArrayAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                    government.setAdapter(governmentArrayAdapter);
                }
                government.setSelection(selected_gov);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        scroll_view.setOnTouchListener((view, motionEvent) -> {
            DetectConnection.hideKeyboard(view, getActivity());
            return false;
        });
        if (!DetectConnection.checkInternetConnection(getActivity())) {
            if(!getActivity().isFinishing()){
                DetectConnection.showNoInternet(getActivity());
            }
        }
        else{
            getDistricts();
        }
        scroll_view.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);

        update.setOnClickListener(view -> {
            if (!DetectConnection.checkInternetConnection(getActivity())) {
                if(!getActivity().isFinishing()){
                    DetectConnection.showNoInternet(getActivity());
                }
            }else{
                updateProfile();
            }
        });
        return root;
    }
    private void updateProfile() {
        if(!validation()){
            ((BaseActivity) getActivity()).loadInterstitialAd();
            scroll_view.setVisibility(View.GONE);
            progress_bar.setVisibility(View.VISIBLE);
            String HttpUrl= DetectConnection.getUrl()+"android/edit-profile.php";
            StringRequest stringRequest=new StringRequest(Request.Method.POST, HttpUrl, response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getString("code").equals("200"))
                    {
                        SharedPreferences.Editor e = sharedPreferences.edit();
                        e.putString("userObject",jsonObject.getJSONObject("user").toString());
                        e.apply();
                        applicationUser = DetectConnection.getUserObject(sharedPreferences.getString("userObject",""));
                        if(!getActivity().isFinishing()){ // crash here
                            DetectConnection.showSuccessGeneral(getActivity(),jsonObject.getString("msg"));
                        }
                    }else
                    {
                        if(!getActivity().isFinishing()){
                            DetectConnection.showError(getActivity(),jsonObject.getString("msg"));
                        }
                    }
                    scroll_view.setVisibility(View.VISIBLE);
                    progress_bar.setVisibility(View.GONE);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }, error -> {
                scroll_view.setVisibility(View.VISIBLE);
                progress_bar.setVisibility(View.GONE);
                if(!getActivity().isFinishing()){
                    DetectConnection.showError(getActivity(),error.getMessage());
                }
            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("cnic",cnic.getText().toString());
                    params.put("p_num",p_num.getText().toString());
                    params.put("email",email.getText().toString());
                    params.put("password",applicationUser.getPassword());
                    params.put("acc_type",acc_type);
                    params.put("company",company);
                    params.put("gov",gov);
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
    private void getDistricts() {
        final int[] j = {0};
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
                        if(applicationUser.getCompany().equals(district.getId())){
                            j[0] = districtList.indexOf(district);
                        }
                    }
                    ArrayAdapter<Government> governmentArrayAdapter;
                    ArrayAdapter<District> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, districtList);
                    adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                    district.setAdapter(adapter);
                    district.setSelection(j[0]);

                    JSONArray array1 = jsonObject.getJSONArray("pensioner_governments");
                    for (int i = 0; i < array1.length(); i++) {
                        JSONObject object = array1.getJSONObject(i);
                        Government government = new Government(object.getString("complete_name"),object.getString("code"));
                        pensionerGovernments.add(government);
                        if(applicationUser.getAccType().equals("Pensioner")){
                            if(applicationUser.getGov().equals(government.getCode())){
                                selected_gov = governments.indexOf(government);
                            }
                        }
                    }
                    JSONArray array2 = jsonObject.getJSONArray("governments");
                    for (int i = 0; i < array2.length(); i++) {
                        JSONObject object = array2.getJSONObject(i);
                        Government government = new Government(object.getString("complete_name"),object.getString("code"));
                        governments.add(government);
                        if(applicationUser.getAccType().equals("Employee")){
                            if(applicationUser.getGov().equals(government.getCode())){
                                selected_gov = governments.indexOf(government);
                            }
                        }
                    }
                    if(applicationUser.getAccType().equals("Employee")){
                        governmentArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, governments);
                    }else {
                        governmentArrayAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, pensionerGovernments);
                    }
                    governmentArrayAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                    government.setAdapter(governmentArrayAdapter);
                }else
                {
                    if(!getActivity().isFinishing()){
                        DetectConnection.showError(getActivity(),jsonObject.getString("msg"));
                    }
                }
                government.setSelection(selected_gov);
                scroll_view.setVisibility(View.VISIBLE);
                progress_bar.setVisibility(View.GONE);
            } catch (Exception e) {
            }
        }, error -> {
            scroll_view.setVisibility(View.VISIBLE);
            progress_bar.setVisibility(View.GONE);
            if(!getActivity().isFinishing()){ // crash here
                DetectConnection.showError(getActivity(),"Something went wrong. Try again!");
            }
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
    private boolean checkEmail(String email) {
        return EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }
    public boolean validation(){
        error = false;
        if(p_acc_type==0){
            error = true;
            DetectConnection.showError(getActivity(),"Select an account type first");
        }
        if(p_district==0){
            error = true;
            DetectConnection.showError(getActivity(),"Select your current station first");
        }
        if(p_gov==0){
            error = true;
            DetectConnection.showError(getActivity(),"Select your government / authority first");
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
