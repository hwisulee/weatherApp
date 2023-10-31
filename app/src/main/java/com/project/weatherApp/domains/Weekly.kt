package com.project.weatherApp.domains

data class Weekly (
    var day: String,
    var picPath: String,
    var status: String,
    var highTemper: Int,
    var lowTemper: Int
)