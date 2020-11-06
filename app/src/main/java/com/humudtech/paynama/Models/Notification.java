package com.humudtech.paynama.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class Notification  implements  Serializable{

    @SerializedName("title")
    @Expose
    String Title;
    @SerializedName("thumbnail")
    @Expose
    String Thumbnail;
    @SerializedName("issue_date")
    @Expose
    String IssueDate;
    @SerializedName("reference")
    @Expose
    String Reference;
    @SerializedName("img")
    @Expose
    ArrayList<String> Images;

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getThumbnail() {
        return Thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        Thumbnail = thumbnail;
    }

    public String getIssueDate() {
        return IssueDate;
    }

    public void setIssueDate(String issueDate) {
        IssueDate = issueDate;
    }

    public String getReference() {
        return Reference;
    }

    public void setReference(String reference) {
        Reference = reference;
    }

    public ArrayList<String> getImages() {
        return Images;
    }

    public void setImages(ArrayList<String> images) {
        Images = images;
    }
}