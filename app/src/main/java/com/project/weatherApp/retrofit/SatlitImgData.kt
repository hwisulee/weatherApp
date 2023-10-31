package com.project.weatherApp.retrofit

import com.google.gson.annotations.SerializedName

sealed class SatlitImgData {
    data class SatlitImgInfoService(@SerializedName("response") val response: Response): SatlitImgData()

    data class Response(
        @SerializedName("header") val header: Header,
        @SerializedName("body") val body: Body
    ): SatlitImgData()

    data class Header(
        @SerializedName("resultCode") val resultCode: String,
        @SerializedName("resultMsg") val resultMsg: String
    ): SatlitImgData()

    data class Body(
        @SerializedName("dataType") val dataType: String,
        @SerializedName("items") val items: Items,
        @SerializedName("pageNo") val pageNo: Int,
        @SerializedName("numOfRows") val numOfRows: Int,
        @SerializedName("totalCount") val totalCount: Int
    ): SatlitImgData()

    data class Items(@SerializedName("item") val item: List<Item>): SatlitImgData()

    data class Item(
        @SerializedName("satImgC-file") val imgSrc: String,
    ): SatlitImgData()
}