package com.humudtech.paynama.Models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Form implements Serializable {
    @SerializedName("title")
    @Expose
    String Title;
    @SerializedName("icon")
    @Expose
    String Icon;
    @SerializedName("file")
    @Expose
    String File;
    @SerializedName("extension")
    @Expose
    String Extension;

    public String getExtension() {
        return Extension;
    }

    public void setExtension(String extension) {
        Extension = extension;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getIcon() {
        return Icon;
    }

    public void setIcon(String icon) {
        Icon = icon;
    }

    public String getFile() {
        return File;
    }

    public void setFile(String file) {
        File = file;
    }
}
