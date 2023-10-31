package com.project.weatherApp.retrofit

import com.google.gson.annotations.SerializedName

sealed class ReverseGeocode {
    data class Root(
        @SerializedName("status") val status: Status,
        @SerializedName("results") val results: List<Result>
    ): ReverseGeocode()

    data class Status(
        @SerializedName("code") val code: Long,
        @SerializedName("name") val name: String,
        @SerializedName("message") val message: String
    )

    data class Result(
        @SerializedName("name") val name: String,
        @SerializedName("code") val code: Code,
        @SerializedName("region") val region: Region
    ): ReverseGeocode()

    data class Code(
        @SerializedName("id") val id: String,
        @SerializedName("type") val type: String,
        @SerializedName("mappingId") val mappingId: String
    ): ReverseGeocode()

    data class Region(
        @SerializedName("area0") val area0: Area0,
        @SerializedName("area1") val area1: Area0,
        @SerializedName("area2") val area2: Area0,
        @SerializedName("area3") val area3: Area0,
        @SerializedName("area4") val area4: Area0
    ): ReverseGeocode()

    data class Area0(
        @SerializedName("name") val name: String,
        @SerializedName("coords") val coords: Coords
    ): ReverseGeocode()

    data class Coords(
        @SerializedName("center") val center: Center
    ): ReverseGeocode()

    data class Center(
        @SerializedName("crs") val crs: String,
        @SerializedName("x") val x: Double,
        @SerializedName("y") val y: Double
    ): ReverseGeocode()
}