package com.humudtech.paynama.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

public class Form implements Serializable {
    @SerializedName("title")
    @Expose
    String Title;
    @SerializedName("thumbnail")
    @Expose
    String Thumbnail;
    @SerializedName("img")
    @Expose
    ArrayList<String> Images;
    @SerializedName("file")
    @Expose
    String File;
    @SerializedName("category")
    @Expose
    String Category;
    @SerializedName("extension")
    @Expose
    String Extension;

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getFile() {
        return File;
    }

    public void setFile(String file) {
        File = file;
    }


    public String getThumbnail() {
        return Thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        Thumbnail = thumbnail;
    }

    public ArrayList<String> getImages() {
        return Images;
    }

    public void setImages(ArrayList<String> images) {
        Images = images;
    }

    public String getExtension() {
        return Extension;
    }

    public void setExtension(String extension) {
        Extension = extension;
    }
}
