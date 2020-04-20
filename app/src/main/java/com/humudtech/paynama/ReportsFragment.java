package com.humudtech.paynama;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;


import androidx.annotation.NonNull;

import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.humudtech.paynama.Models.District;
import com.humudtech.paynama.Models.DistrictModel;
import com.humudtech.paynama.Models.Month;
import com.humudtech.paynama.Models.Report;
import com.humudtech.paynama.Models.User;
import com.humudtech.paynama.Models.Year;
import com.humudtech.paynama.utils.DetectConnection;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.content.Context.MODE_PRIVATE;


public class ReportsFragment extends Fragment {

    com.android.volley.RequestQueue requestQueue;
    Spinner report, month, year, district;
    NestedScrollView scroll_view;
    LinearLayout progress_bar, monthLayout;
    SharedPreferences sharedPreferences;
    Button generate;
    int p_district, p_report, p_month, p_year;
    List<Report> reports;
    User applicationUser;
    List<District> districtList;
    List<String> reportList, yearList, monthList;
    String errorMessage, district_param, month_param, year_param, report_param, searchType, api, param, showAds;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_reports, container, false);
        report = root.findViewById(R.id.report);
        month = root.findViewById(R.id.month);
        year = root.findViewById(R.id.year);
        district = root.findViewById(R.id.district);
        scroll_view = root.findViewById(R.id.scroll_view);
        progress_bar = root.findViewById(R.id.progress_bar);
        monthLayout = root.findViewById(R.id.month_layout);
        generate = root.findViewById(R.id.generate);

        ((BaseActivity) getActivity()).showBanner();

        reports = new ArrayList<>();
        sharedPreferences= getActivity().getSharedPreferences("UserData", MODE_PRIVATE);
        applicationUser = new User();
        applicationUser = DetectConnection.getUserObject(sharedPreferences.getString("userObject",""));

        if (!DetectConnection.checkInternetConnection(getActivity())) {
            if(!getActivity().isFinishing()){
                DetectConnection.showNoInternet(getActivity());
            }
        }else{
            getData();
        }
        generate.setOnClickListener(v -> {
            if (!DetectConnection.checkInternetConnection(getActivity())) {
                if(!getActivity().isFinishing()){
                    DetectConnection.showNoInternet(getActivity());
                }

            }else{
                generateFile();
            }

        });
        return root;
    }

    private void getData() {
        scroll_view.setVisibility(View.GONE);
        progress_bar.setVisibility(View.VISIBLE);
        String url = DetectConnection.getUrl()+"android/get-reports.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            Gson gson = new Gson();
            try {
                JSONObject jsonObject = new JSONObject(response);
                if(jsonObject.getString("code").equals("200"))
                {
                    showAds = jsonObject.getString("show_ads");
                    Type listType = new TypeToken<List<Report>>() {}.getType();
                    reports = gson.fromJson(jsonObject.getJSONArray("reports").toString(), listType);
                    initSpinners();
                }else
                {
                    if(!getActivity().isFinishing()){
                        DetectConnection.showError(getActivity(),jsonObject.getString("msg"));
                    }

                }
                if(showAds.equals("1")){
                    ((BaseActivity) getActivity()).showBanner();
                }else {
                    ((BaseActivity) getActivity()).hideBanner();
                }
                scroll_view.setVisibility(View.VISIBLE);
                progress_bar.setVisibility(View.GONE);
            } catch (Exception e) {
            }
        }, error -> {
            scroll_view.setVisibility(View.VISIBLE);
            progress_bar.setVisibility(View.GONE);
                DetectConnection.showError(getActivity(),error.getMessage());
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("type",applicationUser.getAccType());
                return params;
            }
        };

        requestQueue = Volley.newRequestQueue(getActivity());
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue.add(request);
    }

    private void initReports(){
        reportList = new ArrayList<>();
        reportList.add("Select Report Type");
    }
    private void initDistricts(){
        districtList = new ArrayList<>();
        districtList.add(new District("0","Select District"));
    }
    private void initYears(){
        yearList = new ArrayList<>();
        yearList.add("Select Year");
    }
    private void initMonths(){
        monthList = new ArrayList<>();
        monthList.add("Select Month");
    }
    private void initSpinners() {
        initReports();
        initDistricts();
        initYears();
        initMonths();
        for (Report report: reports) {
            reportList.add(report.getTitle());
        }

        ArrayAdapter<String> adapterReportType = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, reportList);
        adapterReportType.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        report.setAdapter(adapterReportType);

        ArrayAdapter<District> adapterDistrict = new ArrayAdapter<District>(getActivity(), android.R.layout.simple_spinner_dropdown_item,districtList);
        adapterDistrict.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        district.setAdapter(adapterDistrict);

        ArrayAdapter<String> adapterYear = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, yearList);
        adapterYear.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        year.setAdapter(adapterYear);

        ArrayAdapter<String> adapterMonth = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, monthList);
        adapterMonth.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        month.setAdapter(adapterMonth);

        report.setSelection(0);
        district.setSelection(0);
        year.setSelection(0);
        month.setSelection(0);

        report.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                report_param = parent.getSelectedItem().toString();
                p_report = position;
                initDistricts();
                if(position>0){
                    searchType = reports.get(position-1).getSearchType();
                    api = reports.get(position-1).getApi();
                    if(searchType.equals("CNIC")){
                        param = applicationUser.getCnic();
                    }else if(searchType.equals("DDO Code")){
                        param = applicationUser.getDdo();
                    }else if(searchType.equals("Personal Number")){
                        param = applicationUser.getPNum();
                    }
                    for (DistrictModel dist: reports.get(position-1).getDistricts()) {
                        districtList.add(new District(dist.getId(),dist.getTitle()));
                    }
                }
                ArrayAdapter<District> adapterDistrict = new ArrayAdapter<District>(getActivity(), android.R.layout.simple_spinner_dropdown_item,districtList);
                adapterDistrict.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                district.setAdapter(adapterDistrict);
                adapterDistrict.notifyDataSetChanged();
                district.setSelection(0);
                if(report_param.equals("Income Tax Statement")){
                    monthLayout.setVisibility(View.GONE);
                    p_month = 100;
                }else{
                    monthLayout.setVisibility(View.VISIBLE);
                    p_month = 0;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        district.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                District district = (District) parent.getSelectedItem();
                district_param = district.getId();
                p_district = position;
                initYears();
                if(position>0){
                    for (Year yr: reports.get(p_report-1).getDistricts().get(position-1).getYears()) {
                        yearList.add(yr.getTitle());
                    }
                }

                ArrayAdapter<String> adapterYear = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, yearList);
                adapterYear.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                year.setAdapter(adapterYear);
                adapterYear.notifyDataSetChanged();
                year.setSelection(0);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                year_param = parent.getSelectedItem().toString();
                p_year = position;
                initMonths();
                if(position>0){
                    for (Month mon: reports.get(p_report-1).getDistricts().get(p_district-1).getYears().get(position-1).getMonths()) {
                        monthList.add(mon.getTitle());
                    }
                }
                ArrayAdapter<String> adapterMonth = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, monthList);
                adapterMonth.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                month.setAdapter(adapterMonth);
                adapterMonth.notifyDataSetChanged();
                month.setSelection(0);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                month_param = parent.getSelectedItem().toString();
                p_month = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void generateFile() {
        if(findErrors()){
            if(!getActivity().isFinishing()){
                DetectConnection.showError(getActivity(), errorMessage);
            }

        }else{
            if(showAds.equals("1")){
                ((BaseActivity) getActivity()).loadInterstitialAd();
            }
            scroll_view.setVisibility(View.GONE);
            progress_bar.setVisibility(View.VISIBLE);
            String url = DetectConnection.getUrl()+api;

            StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(jsonObject.getString("code").equals("200")){
                        if(!getActivity().isFinishing()){
                            DetectConnection.showSuccess(getActivity(),jsonObject.getString("file"));
                        }

                    }else{
                        if(!getActivity().isFinishing()){
                            DetectConnection.showError(getActivity(),jsonObject.getString("msg"));
                        }

                    }
                } catch (Exception e) {
                }
                scroll_view.setVisibility(View.VISIBLE);
                progress_bar.setVisibility(View.GONE);
            }, error -> {
                scroll_view.setVisibility(View.VISIBLE);
                progress_bar.setVisibility(View.GONE);
                if(!getActivity().isFinishing()){
                    DetectConnection.showError(getActivity(),"Something went wrong");
                }

            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("company",district_param);
                    params.put("type",report_param);
                    params.put("gov",applicationUser.getGov());
                    params.put("year",year_param);
                    params.put("month",month_param);
                    params.put("search_type",searchType);
                    params.put("param",param);
                    params.put("app_code","17");
                    return params;
                }
            };

            requestQueue = Volley.newRequestQueue(getActivity());
            int socketTimeout = 300000;
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            request.setRetryPolicy(policy);
            requestQueue.add(request);
        }
    }
    private boolean findErrors(){
        boolean error = false;
        if(p_report==0){
            error = true;
            errorMessage = "Please Select a Report Type";
        }else if(p_district==0){
            error = true;
            errorMessage = "Please Select a District";
        }else if(p_year==0){
            error = true;
            errorMessage = "Please Select a Year";
        }else if(p_month==0){
            error = true;
            errorMessage = "Please Select a Month";
        }
        return error;
    }
}
