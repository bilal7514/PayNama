package com.humudtech.paynama.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Month implements Serializable {
    @SerializedName("title")
    @Expose
    String Title;

    public String getTitle() {
        return Title;
    }
    public void setTitle(String title) {
        Title = title;
    }
}
