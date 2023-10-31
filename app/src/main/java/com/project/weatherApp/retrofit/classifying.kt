package com.project.weatherApp.retrofit

class classifying {

    internal fun classifying(num: String): String {
        return when(num) {
            "00" -> "NORMAL_SERVICE"
            "01" -> "APLLICATION_ERROR"
            "02" -> "DB_ERROR"
            "03" -> "NODATA_ERROR"
            "04" -> "HTTP_ERROR"
            "05" -> "SERVICETIME_OUT"
            "10" -> "INVALD_REQUEST_PARAMETER_ERROR"
            "11" -> "NO_MANDATORY_REQUEST_PARAMETERS_ERROR"
            "12" -> "NO_OPENAPI_SERVICE_ERROR"
            "20" -> "SERVICE_ACCESS_DENIED_ERROR"
            "21" -> "TEMPORARILY_DISABLE_THE_SERVICEKEY_ERROR"
            "22" -> "LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR"
            "30" -> "SERVICE_KEY_IS_NOT_REGISTERED_ERROR"
            "31" -> "DEADLINE_HAS_EXPIRED_ERROR"
            "32" -> "UNREGISTERED_IP_ERROR"
            "33" -> "UNSIGNED_CALL_ERROR"
            "99" -> "UNKNOWN_ERROR"
            else -> ""
        }
    }
}