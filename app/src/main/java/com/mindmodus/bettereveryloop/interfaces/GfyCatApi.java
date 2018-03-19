package com.mindmodus.bettereveryloop.interfaces;


import com.mindmodus.bettereveryloop.models.Gfycat;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.GET;

public interface GfyCatApi {

    @GET("/v1/gfycats/trending?count=20")
    Single<Gfycat> getGifs();

}
