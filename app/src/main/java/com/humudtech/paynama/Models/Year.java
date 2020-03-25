package com.humudtech.paynama.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Year implements  Serializable{
    @SerializedName("title")
    @Expose
    String Title;
    @SerializedName("months")
    @Expose
    List<Month> months;
    public String getTitle() {
        return Title;
    }
    public void setTitle(String title) {
        Title = title;
    }
    public List<Month> getMonths() {
        return months;
    }

    public void setMonths(List<Month> months) {
        this.months = months;
    }
}