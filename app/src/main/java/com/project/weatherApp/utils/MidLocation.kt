package com.project.weatherApp.utils

import com.project.weatherApp.retrofit.NowLocation

internal class LocationCode {
    var taCode: String = "11B00000"
    var landCode: String = "11B10101"
}

class MidLocation {
    private val midLandFcstLocationData = arrayOf(
        Pair("11B00000", "서울 인천 경기"),
        Pair("11C20000", "대전 세종 충청남도"),
        Pair("11C10000", "충청북도"),
        Pair("11F20000", "광주 전라남도"),
        Pair("11F10000", "전라북도"),
        Pair("11H10000", "대구 경상북도"),
        Pair("11H20000", "부산 울산 경상남도"),
        Pair("11G00000", "제주")
    )

    private val gangwonWest = arrayOf(
        "철원군", "화천군", "양구군", "인제군",
        "춘천시", "흥천군", "횡성군", "원주시",
        "평창군", "영월군", "정선군"
    )

    private val gangwonEast = arrayOf(
        "강릉시", "삼척시", "동해시", "태백시",
        "속초시", "양양군", "고성군"
    )

    private val midTaLocationData1 = arrayOf(
        Pair("11B20601", "수원"), Pair("11B20305", "파주"),
        Pair("11D10301", "춘천"), Pair("11D10401", "원주"),
        Pair("11D20501", "강릉"), Pair("11C20101", "서산"),
        Pair("11C10301", "청주"), Pair("11G00401", "서귀포"),
        Pair("21F20801", "목포"), Pair("11F20401", "여수"),
        Pair("11F10201", "전주"), Pair("21F10501", "군산"),
        Pair("11H20301", "창원"), Pair("11H10501", "안동"),
        Pair("11H10201", "포항")
    )

    private val midTaLocationData2 = arrayOf(
        Pair("11B10101", "서울"), Pair("11B20201", "인천"),
        Pair("11C20401", "대전"), Pair("11C20404", "세종"),
        Pair("11G00201", "제주"), Pair("11F20501", "광주"),
        Pair("11H20201", "부산"), Pair("11H20101", "울산"),
        Pair("11H10701", "대구"),

        Pair("11D10301", "강원"), Pair("11B20601", "경기"),
        Pair("11C10301", "충청북도"), Pair("11C20101", "충청남도"),
        Pair("11F10201", "전라북도"), Pair("21F20801", "전라남도"),
        Pair("11H10501", "경상북도"), Pair("11H20301", "경상남도")
    )

    internal fun locationSetup(locationList: ArrayList<NowLocation>): LocationCode {
        return setting(locationList)
    }

    private fun setting(locationList: ArrayList<NowLocation>): LocationCode {
        val locationCode = LocationCode()
        val (area1, area2) = locationList[0]
        var midTaCode = ""
        var midLandCode = ""

        for (item in midLandFcstLocationData) {
            if (!area1.contains("강원")) {
                val items = item.second.split(" ")
                for (s in items) { if (area1.contains(s)) { midLandCode = item.first; break } }
            } else {
                for (s in gangwonWest) { if (area2 == s) { midLandCode = "11D10000"; break } }
                for (s in gangwonEast) { if (area2 == s) { midLandCode = "11D20000"; break } }
            }

            if (midLandCode != "") break
        }

        for (item in midTaLocationData1) { if (area2.contains(item.second)) { midTaCode = item.first; break } }
        if (midTaCode == "") { for (item in midTaLocationData2) { if (area1.contains(item.second)) { midTaCode = item.first; break } } }

        locationCode.taCode = midTaCode
        locationCode.landCode = midLandCode
        return locationCode
    }
}