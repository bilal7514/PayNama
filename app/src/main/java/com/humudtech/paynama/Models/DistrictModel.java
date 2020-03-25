package com.humudtech.paynama.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class DistrictModel implements  Serializable{
    @SerializedName("id")
    @Expose
    String Id;
    @SerializedName("title")
    @Expose
    String Title;
    @SerializedName("years")
    @Expose
    List<Year> years;

    public String getTitle() {
        return Title;
    }
    public void setTitle(String title) {
        Title = title;
    }
    public List<Year> getYears() {
        return years;
    }
    public void setYears(List<Year> years) {
        this.years = years;
    }
    public String getId() {
        return Id;
    }
    public void setId(String id) {
        Id = id;
    }
}