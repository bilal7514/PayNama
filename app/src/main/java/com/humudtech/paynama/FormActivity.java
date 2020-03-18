package com.humudtech.paynama;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.humudtech.paynama.Models.District;
import com.humudtech.paynama.Models.Government;
import com.humudtech.paynama.utils.DetectConnection;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FormActivity extends AppCompatActivity {

    List<Government> governments;
    Spinner type, gov, month, year, search_type;
    EditText param;
    String govt, file_type, _month, _year, _search_type, errorMessage;
    int p_gov, p_type, p_month, p_year, p_search_type;
    ProgressBar progressBar;
    ConstraintLayout formLayout;
    List<String> years, months, search_types;
    com.android.volley.RequestQueue requestQueue;
    Button generate;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        formLayout = findViewById(R.id.form_layout);
        progressBar = findViewById(R.id.progress_bar);
        formLayout.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        param = findViewById(R.id.param);
        type = findViewById(R.id.type);
        gov = findViewById(R.id.gov);
        month = findViewById(R.id.month);
        year = findViewById(R.id.year);
        search_type = findViewById(R.id.search_type);
        generate = findViewById(R.id.generate);

        years = new ArrayList<>();
        months = new ArrayList<>();
        search_types = new ArrayList<>();
        search_types.add("Select Search Filter");
        search_types.add("CNIC");
        search_types.add("DDO Code");
        search_types.add("Personal Number");
        years.add("Select a Year");
        months.add("Select a Month");
        governments = new ArrayList<>();
        governments.add(new Government("Select a Government","0"));
        governments.add(new Government("Federal","F"));
        governments.add(new Government("Punjab","P"));
        governments.add(new Government("District Education Authority","DEAP"));
        governments.add(new Government("District Health Authority","DHAP"));

        ArrayAdapter<Government> adapter = new ArrayAdapter<Government>(FormActivity.this, android.R.layout.simple_spinner_dropdown_item, governments);
        adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        gov.setAdapter(adapter);
        gov.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                Government government = (Government) parent.getSelectedItem();
                govt = government.getCode();
                p_gov = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> adapterSearchType = new ArrayAdapter<String>(FormActivity.this, android.R.layout.simple_spinner_dropdown_item, search_types);
        adapterSearchType.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        search_type.setAdapter(adapterSearchType);

        search_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                _search_type = parent.getSelectedItem().toString();
                p_search_type = position;
                if(position==1){
                    param.setHint("Type your CNIC");
                }else if(position==2){
                    param.setHint("Type your DDO Code");
                }else if(position==3){
                    param.setHint("Type your Personal No.");
                }else{
                    param.setHint("Type CNIC / Personal No. / DDO code");
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        year.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);

                _year = parent.getSelectedItem().toString();
                p_year = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        month.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);

                _month = parent.getSelectedItem().toString();
                p_month = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        List<String> types = new ArrayList<String>();
        types.add("Select a File Type");
        types.add("Pay Slip");
        types.add("Expenditure");

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(FormActivity.this, android.R.layout.simple_spinner_dropdown_item, types);
        adapter1.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
        type.setAdapter(adapter1);

        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                ((TextView) parent.getChildAt(0)).setTextColor(Color.WHITE);
                file_type = parent.getSelectedItem().toString();
                p_type = position;
                if(position==2){
                    search_type.setSelection(2);
                    search_type.setEnabled(false);
                }else{
                    search_type.setSelection(0);
                    search_type.setEnabled(true);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        if (!DetectConnection.checkInternetConnection(FormActivity.this)) {
            Intent intent=new Intent(FormActivity.this,NoInternetActivity.class);
            startActivity(intent);
        }else{
            getData();
        }
        generate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateFile();
            }
        });
    }

    private void generateFile() {
        if(findErrors()){
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }else{
            formLayout.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            String url = "";
            if(file_type.equals("Pay Slip")){
                url = DetectConnection.getUrl()+"android/generate-payslip.php";
            }else if(file_type.equals("Expenditure")){
                url = DetectConnection.getUrl()+"android/generate-expenditure.php";
            }
            StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    if(!jsonObject.getString("code").equals("200")){
                        showErrorDialog(jsonObject.getString("msg"));
                    }else{
                        showSuccessDialog(jsonObject.getString("msg"),jsonObject.getString("file"));
                    }
                    formLayout.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                } catch (Exception e) {
                }
            }, error -> {
            }){
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("company",getIntent().getExtras().getString("company"));
                    params.put("type",file_type);
                    params.put("gov",govt);
                    params.put("year",_year);
                    params.put("month",_month);
                    params.put("app_code","6");
                    params.put("search_type",_search_type);
                    params.put("param",param.getText().toString());
                    return params;
                }
            };

            requestQueue = Volley.newRequestQueue(FormActivity.this);
            int socketTimeout = 30000;
            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            request.setRetryPolicy(policy);
            requestQueue.add(request);
        }
    }
    private boolean findErrors(){
        boolean error = false;
        if(p_gov==0){
            error = true;
            errorMessage = "Please Select a Government";
        }else if(p_month==0){
            error = true;
            errorMessage = "Please Select a Month";
        }else if(p_year==0){
            error = true;
            errorMessage = "Please Select a Year";
        }else if(p_type==0){
            error = true;
            errorMessage = "Please Select a File Type";
        }else if(param.getText().toString().isEmpty()){
            error = true;
            errorMessage = "Please Type Your CNIC, Personal No. or DDO Code";
        }
        else if(param.getText().toString().isEmpty()){
            error = true;
            errorMessage = "Please Select a Search Filter";
        }
        return error;
    }
    private void getData() {

        String url = DetectConnection.getUrl()+"android/get-data.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            try {

                JSONObject jsonObject = new JSONObject(response);
                if(jsonObject.getString("code").equals("200")){
                    JSONArray _years = jsonObject.getJSONArray("years");
                    for (int i = 0; i < _years.length(); i++) {
                        years.add(_years.getString(i));
                    }
                    JSONArray _months = jsonObject.getJSONArray("months");
                    for (int i = 0; i < _months.length(); i++) {
                        months.add(_months.getString(i));
                    }
                    //fill data in spinner
                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(FormActivity.this, android.R.layout.simple_spinner_dropdown_item, years);
                    adapter2.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                    year.setAdapter(adapter2);

                    ArrayAdapter<String> adapter3 = new ArrayAdapter<String>(FormActivity.this, android.R.layout.simple_spinner_dropdown_item, months);
                    adapter3.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);
                    month.setAdapter(adapter3);
                    gov.setSelection(0);
                    year.setSelection(0);
                    month.setSelection(0);
                    type.setSelection(0);
                }else{
                    Toast.makeText(this, "Oops! Something went wrong. Restart application", Toast.LENGTH_LONG).show();
                }
                formLayout.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            } catch (Exception e) {
            }
        }, error -> {
        }){
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("company",getIntent().getExtras().getString("company"));
                return params;
            }
        };

        requestQueue = Volley.newRequestQueue(FormActivity.this);
        int socketTimeout = 30000;
        RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        request.setRetryPolicy(policy);
        requestQueue.add(request);

    }
    private void showSuccessDialog(String msg, String file) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_info);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        ((TextView) dialog.findViewById(R.id.content)).setText(msg);
        dialog.findViewById(R.id.bt_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = DetectConnection.getUrl()+file;
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                request.setDescription("Downloading File");
                String fileName = url.substring(url.lastIndexOf('/') + 1);
                request.setTitle(fileName);
                request.setVisibleInDownloadsUi(true);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                // in order for this if to run, you must use the android 3.2 to compile your app
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    request.allowScanningByMediaScanner();
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                }
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                // get download service and enqueue file
                DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                manager.enqueue(request);
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }
    private void showErrorDialog(String msg) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE); // before
        dialog.setContentView(R.layout.dialog_warning);
        dialog.setCancelable(false);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        ((TextView) dialog.findViewById(R.id.content)).setText(msg);
        dialog.findViewById(R.id.bt_close).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
        dialog.getWindow().setAttributes(lp);
    }

}
