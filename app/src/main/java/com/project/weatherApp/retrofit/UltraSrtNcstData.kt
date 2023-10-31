package com.project.weatherApp.retrofit

import com.google.gson.annotations.SerializedName

sealed class UltraSrtNcstData {
    data class UltraSrtNcst(@SerializedName("response") val response: Response): UltraSrtNcstData()

    data class Response(
        @SerializedName("header") val header: Header,
        @SerializedName("body") val body: Body
    ): UltraSrtNcstData()

    data class Header(
        @SerializedName("resultCode") val resultCode: String,
        @SerializedName("resultMsg") val resultMsg: String
    ): UltraSrtNcstData()

    data class Body(
        @SerializedName("dataType") val dataType: String,
        @SerializedName("items") val items: Items,
        @SerializedName("pageNo") val pageNo: Int,
        @SerializedName("numOfRows") val numOfRows: Int,
        @SerializedName("totalCount") val totalCount: Int
    ): UltraSrtNcstData()

    data class Items(@SerializedName("item") val item: List<Item>): UltraSrtNcstData()

    data class Item(
        @SerializedName("baseDate") val baseDate: String,
        @SerializedName("baseTime") val baseTime: String,
        @SerializedName("category") val category: String,
        @SerializedName("fcstDate") val fcstDate: String,
        @SerializedName("nx") val nx: Int,
        @SerializedName("ny") val ny: Int,
        @SerializedName("obsrValue") val obsrValue: String
    ): UltraSrtNcstData()
}