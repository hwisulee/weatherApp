package com.project.weatherApp.retrofit

import com.google.gson.annotations.SerializedName

sealed class MidLandFcstData {
    data class MidLandFcst(@SerializedName("response") val response: Response): MidLandFcstData()

    data class Response(
        @SerializedName("header") val header: Header,
        @SerializedName("body") val body: Body
    ): MidLandFcstData()

    data class Header(
        @SerializedName("resultCode") val resultCode: String,
        @SerializedName("resultMsg") val resultMsg: String
    ): MidLandFcstData()

    data class Body(
        @SerializedName("dataType") val dataType: String,
        @SerializedName("items") val items: Items,
        @SerializedName("pageNo") val pageNo: Int,
        @SerializedName("numOfRows") val numOfRows: Int,
        @SerializedName("totalCount") val totalCount: Int
    ): MidLandFcstData()

    data class Items(@SerializedName("item") val item: List<Item>): MidLandFcstData()

    data class Item(
        @SerializedName("regId") val regId: String,
        @SerializedName("wf3Am") val wf3Am: String,
        @SerializedName("wf4Am") val wf4Am: String,
        @SerializedName("wf5Am") val wf5Am: String,
        @SerializedName("wf6Am") val wf6Am: String,
        @SerializedName("wf7Am") val wf7Am: String,
        @SerializedName("wf8") val wf8: String,
        @SerializedName("wf9") val wf9: String,
        @SerializedName("wf10") val wf10: String,
    ): MidLandFcstData()
}