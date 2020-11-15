package com.humudtech.paynama;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.humudtech.paynama.Adapters.BooksAdapter;
import com.humudtech.paynama.Adapters.FormsAdapter;
import com.humudtech.paynama.Models.Book;
import com.humudtech.paynama.Models.Form;
import com.humudtech.paynama.utils.DetectConnection;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BooksFragment extends Fragment {
    List<Book> books;
    RecyclerView recyclerView;
    private ProgressBar progress_bar;
    LinearLayout layout;
    BooksAdapter adapter;
    com.android.volley.RequestQueue requestQueue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root =  inflater.inflate(R.layout.fragment_books, container, false);
        ((BaseActivity) getActivity()).hideBanner();

        progress_bar = (ProgressBar) root.findViewById(R.id.progress_bar);
        layout = (LinearLayout) root.findViewById(R.id.layout);
        books = new ArrayList<>();
        recyclerView= (RecyclerView) root.findViewById(R.id.forms_recycler);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        recyclerView.setHasFixedSize(true);
        progress_bar.setVisibility(View.VISIBLE);
        layout.setVisibility(View.GONE);

        if (!DetectConnection.checkInternetConnection(getActivity())) {
            DetectConnection.showNoInternet(getActivity());
        }else{
            loadBooks();
        }

        return  root;
    }
    @Override
    public void onStop () {
        super.onStop();
        if (requestQueue != null) {
            requestQueue.cancelAll(this);
        }
    }
    private void loadBooks() {

        String url = DetectConnection.getUrl()+"android/get-books.php";
        StringRequest request = new StringRequest(Request.Method.POST, url, response -> {
            Gson gson = new Gson();
            try {
                JSONObject jsonObject = new JSONObject(response);
                if(jsonObject.getString("code").equals("404")){
                    DetectConnection.showError(getActivity(),jsonObject.getString("msg"));
                }else if(jsonObject.getString("code").equals("200")){
                    Type listType = new TypeToken<List<Book>>() {}.getType();
                    books = gson.fromJson(jsonObject.getJSONArray("books").toString(), listType);
                    adapter = new BooksAdapter(getActivity(), books);
                    recyclerView.setAdapter(adapter);
                    adapter.notifyDataSetChanged();
                    // initScrollListener();
                }
                else{
                    DetectConnection.showError(getActivity(),jsonObject.getString("msg"));
                }
                layout.setVisibility(View.VISIBLE);
                progress_bar.setVisibility(View.GONE);
            } catch (Exception e) {
                DetectConnection.showError(getActivity(),"Something went wrong!");
            }
        }, error -> progress_bar.setVisibility(View.GONE)){
            @Override
            protected Map<String, String> getParams() {
                // Creating Map String Params.
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
}