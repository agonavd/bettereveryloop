package com.mindmodus.bettereveryloop.models;

import com.google.gson.annotations.SerializedName;

public class GfycatGifData {
    @SerializedName("gfyName")
    private String title;

    @SerializedName("max2mbGif")
    private String url;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
