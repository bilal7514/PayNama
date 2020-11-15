package com.humudtech.paynama;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;
import com.humudtech.paynama.Models.User;
import com.humudtech.paynama.utils.DetectConnection;
import com.humudtech.paynama.utils.Tools;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.view.MenuItemCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import java.util.ArrayList;
import java.util.List;

import static androidx.navigation.Navigation.findNavController;

public class BaseActivity extends AppCompatActivity implements SearchView.OnQueryTextListener{
    private InterstitialAd mInterstitialAd;
    AppBarConfiguration appBarConfiguration;
    AdRequest BannerAdRequest, InterstitialAdRequest;
    SharedPreferences sharedPreferences;
    User applicationUser;
    SearchView searchView;
    //AdView mAdView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.abs_layout);

        ActionBar menu = getSupportActionBar();
        menu.setDisplayShowHomeEnabled(true);
        menu.setLogo(R.drawable.ic_apps);
        menu.setDisplayUseLogoEnabled(true);

        sharedPreferences= getSharedPreferences("UserData", MODE_PRIVATE);
        applicationUser = new User();
        applicationUser = DetectConnection.getUserObject(sharedPreferences.getString("userObject",""));
        MobileAds.initialize(this, initializationStatus -> {
        });

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(sharedPreferences.getString("banner_ad","ca-app-pub-5312186303011441/6606399397"));
        InterstitialAdRequest = new AdRequest.Builder().build();
        mInterstitialAd.loadAd(InterstitialAdRequest);
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
            }

            @Override
            public void onAdLeftApplication() {
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
                // Code to be executed when the interstitial ad is closed.
            }
        });
        //Send Request
//        mAdView = findViewById(R.id.bannerAdView);
//        BannerAdRequest = new AdRequest.Builder().build();
//        mAdView.loadAd(BannerAdRequest);
//        mAdView.setAdListener(new AdListener() {
//            @Override
//            public void onAdLoaded() {
//                // Code to be executed when an ad finishes loading.
//            }
//
//            @Override
//            public void onAdFailedToLoad(int errorCode) {
//                // Code to be executed when an ad request fails.
//            }
//
//            @Override
//            public void onAdOpened() {
//                // Code to be executed when an ad opens an overlay that
//                // covers the screen.
//            }
//
//            @Override
//            public void onAdClicked() {
//                // Code to be executed when the user clicks on an ad.
//            }
//
//            @Override
//            public void onAdLeftApplication() {
//                // Code to be executed when the user has left the app.
//            }
//
//            @Override
//            public void onAdClosed() {
//                // Code to be executed when the user is about to return
//                // to the app after tapping on an ad.
//            }
//        });

        BottomNavigationView navView = findViewById(R.id.nav_view);
        appBarConfiguration = new AppBarConfiguration.Builder(
                 R.id.navigation_notifications, R.id.navigation_reports, R.id.navigation_settings, R.id.navigation_forms, R.id.navigation_books)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        if(applicationUser!=null){
            subscribeToTopics();
        }
    }

    private void subscribeToTopics() {
        List<String> topics = getTopics();
        for (String topic : topics) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(task -> {
            });
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.base, menu);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));
        searchView.setQueryHint("Search Notifications...");
        searchView.setOnQueryTextListener(this);
        return true;
    }
    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.clearFocus();
        searchView.onActionViewCollapsed();
        Bundle bundle = new Bundle();
        bundle.putString("query",query);
        findNavController(BaseActivity.this,R.id.nav_host_fragment).navigate(R.id.searchResultsFragment,bundle);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        return false;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
    public void loadInterstitialAd(){
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }
    public void hideBanner(){
        //mAdView.setVisibility(View.GONE);
    }
    public void showBanner(){
        //mAdView.setVisibility(View.VISIBLE);
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
}