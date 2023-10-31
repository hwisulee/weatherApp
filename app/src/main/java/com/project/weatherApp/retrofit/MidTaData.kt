package com.project.weatherApp.retrofit

import com.google.gson.annotations.SerializedName

sealed class MidTaData {
    data class MidTa(@SerializedName("response") val response: Response): MidTaData()

    data class Response(
        @SerializedName("header") val header: Header,
        @SerializedName("body") val body: Body
    ): MidTaData()

    data class Header(
        @SerializedName("resultCode") val resultCode: String,
        @SerializedName("resultMsg") val resultMsg: String
    ): MidTaData()

    data class Body(
        @SerializedName("dataType") val dataType: String,
        @SerializedName("items") val items: Items,
        @SerializedName("pageNo") val pageNo: Int,
        @SerializedName("numOfRows") val numOfRows: Int,
        @SerializedName("totalCount") val totalCount: Int
    ): MidTaData()

    data class Items(@SerializedName("item") val item: List<Item>): MidTaData()

    data class Item(
        @SerializedName("regId") val regId: String,
        @SerializedName("taMin3") val taMin3: Int,
        @SerializedName("taMax3") val taMax3: Int,
        @SerializedName("taMin4") val taMin4: Int,
        @SerializedName("taMax4") val taMax4: Int,
        @SerializedName("taMin5") val taMin5: Int,
        @SerializedName("taMax5") val taMax5: Int,
        @SerializedName("taMin6") val taMin6: Int,
        @SerializedName("taMax6") val taMax6: Int,
        @SerializedName("taMin7") val taMin7: Int,
        @SerializedName("taMax7") val taMax7: Int,
        @SerializedName("taMin8") val taMin8: Int,
        @SerializedName("taMax8") val taMax8: Int,
        @SerializedName("taMin9") val taMin9: Int,
        @SerializedName("taMax9") val taMax9: Int,
        @SerializedName("taMin10") val taMin10: Int,
        @SerializedName("taMax10") val taMax10: Int,
    ): MidTaData()
}
