package com.mindmodus.bettereveryloop.interfaces;


import com.mindmodus.bettereveryloop.models.Gfycat;

import retrofit2.Call;
import retrofit2.http.GET;

public interface RedditBetterEveryLoopApi {

    @GET("/v1/gfycats/trending?count=20")
    Call<Gfycat> getGifs();

}