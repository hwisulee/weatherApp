package com.project.weatherApp.retrofit

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Status(
    val SKY: String,
    val PTY: String,
    val WSD: String,
    val REH: String,
    val PCP: String
): Parcelable


@Parcelize
data class HL(
    val H: String,
    val L: String
): Parcelable

@Parcelize
data class NowLocation(
    val area1: String,
    val area2: String
): Parcelable