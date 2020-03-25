package com.humudtech.paynama.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Report implements  Serializable{
    @SerializedName("title")
    @Expose
    String Title;
    @SerializedName("api")
    @Expose
    String api;
    @SerializedName("search_type")
    @Expose
    String searchType;
    @SerializedName("districts")
    @Expose
    List<DistrictModel> districts;

    public String getTitle() {
        return Title;
    }
    public void setTitle(String title) {
        Title = title;
    }
    public String getApi() {
        return api;
    }
    public void setApi(String api) {
        this.api = api;
    }
    public String getSearchType() {
        return searchType;
    }
    public void setSearchType(String searchType) {
        this.searchType = searchType;
    }
    public List<DistrictModel> getDistricts() {
        return districts;
    }
    public void setDistricts(List<DistrictModel> districts) {
        this.districts = districts;
    }
}