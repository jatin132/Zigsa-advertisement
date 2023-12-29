package com.zigsaadvertisement

import com.zigsawaitlist.network.TVCodeRequest
import com.zigsawaitlist.network.TVCodeResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface APIService {
    @POST("public/tv-code")
    fun getTvCode(@Body request: TVCodeRequest): Call<TVCodeResponse>

    @GET("tv/slides-lists/{webview_uuid}")
    fun getData(@Header("Authorization") token: String, @Path("webview_uuid") uuid: String): Call<List<AdvertisementModel>>
}