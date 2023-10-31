package com.project.weatherApp.retrofit

import com.google.gson.annotations.SerializedName

sealed class VilageFcstData {
    data class VilageFcst(@SerializedName("response") val response: Response): VilageFcstData()

    data class Response(
        @SerializedName("header") val header: Header,
        @SerializedName("body") val body: Body
    ): VilageFcstData()

    data class Header(
        @SerializedName("resultCode") val resultCode: String,
        @SerializedName("resultMsg") val resultMsg: String
    ): VilageFcstData()

    data class Body(
        @SerializedName("dataType") val dataType: String,
        @SerializedName("items") val items: Items,
        @SerializedName("pageNo") val pageNo: Int,
        @SerializedName("numOfRows") val numOfRows: Int,
        @SerializedName("totalCount") val totalCount: Int
    ): VilageFcstData()

    data class Items(@SerializedName("item") val item: List<Item>): VilageFcstData()

    data class Item(
        @SerializedName("baseDate") val baseDate: String,
        @SerializedName("baseTime") val baseTime: String,
        @SerializedName("category") val category: String,
        @SerializedName("fcstDate") val fcstDate: String,
        @SerializedName("fcstTime") val fcstTime: String,
        @SerializedName("fcstValue") val fcstValue: String,
        @SerializedName("nx") val nx: Int,
        @SerializedName("ny") val ny: Int
    ): VilageFcstData()
}