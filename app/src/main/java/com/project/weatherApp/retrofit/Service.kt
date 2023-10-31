package com.project.weatherApp.retrofit

import com.project.weatherApp.BuildConfig
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Query

interface Service {
    @GET("1360000/VilageFcstInfoService_2.0/getUltraSrtNcst")
    suspend fun getUltraSrtNcst(
        @Query("serviceKey") authKey: String,
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("dataType") dataType: String,
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): UltraSrtNcstData.UltraSrtNcst

    @GET("1360000/VilageFcstInfoService_2.0/getUltraSrtFcst")
    suspend fun getUltraSrtFcst(
        @Query("serviceKey") authKey: String,
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("dataType") dataType: String,
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): UltraSrtFcstData.UltraSrtFcst

    @GET("1360000/VilageFcstInfoService_2.0/getVilageFcst")
    suspend fun getVilageFcst(
        @Query("serviceKey") authKey: String,
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("dataType") dataType: String,
        @Query("base_date") baseDate: String,
        @Query("base_time") baseTime: String,
        @Query("nx") nx: Int,
        @Query("ny") ny: Int
    ): VilageFcstData.VilageFcst

    @GET("1360000/MidFcstInfoService/getMidTa")
    suspend fun getMidTa(
        @Query("serviceKey") authKey: String,
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("dataType") dataType: String,
        @Query("regId") regId: String,
        @Query("tmFc") tmFc: String
    ): MidTaData.MidTa

    @GET("1360000/MidFcstInfoService/getMidLandFcst")
    suspend fun getMidLandFcst(
        @Query("serviceKey") authKey: String,
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("dataType") dataType: String,
        @Query("regId") regId: String,
        @Query("tmFc") tmFc: String
    ): MidLandFcstData.MidLandFcst

    @GET("1360000/SatlitImgInfoService/getInsightSatlit")
    suspend fun getSatlitImg(
        @Query("serviceKey") authKey: String,
        @Query("pageNo") pageNo: Int,
        @Query("numOfRows") numOfRows: Int,
        @Query("dataType") dataType: String,
        @Query("sat") sat: String,
        @Query("data") data: String,
        @Query("area") area: String,
        @Query("time") time: String
    ): SatlitImgData.SatlitImgInfoService

    @GET("map-reversegeocode/v2/gc?")
    suspend fun getReverseGeocode(
        @Query("X-NCP-APIGW-API-KEY-ID") clientID: String,
        @Query("X-NCP-APIGW-API-KEY") clientKey: String,
        @Query("coords") coords: String,
        @Query("orders") orders: String,
        @Query("output") output: String
    ): ReverseGeocode.Root
}