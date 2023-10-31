package com.project.weatherApp.utils

import android.os.Build
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TimeMaker {
    /*
    timeList[0] = UltraSrtNcst, Fcst TIme, timeList[1] = VilageFcst Time, timeList[2] = MidFcst Time, timeList[3] = SatlitImg Time
    moreInformation => checked TimeMaker().timeChanger()
     */
    internal fun getSetting(code: String): MutableList<String> {
        return when (code) {
            "API" -> timeChanger(getNowDate("normal"))
            "NOW" -> mutableListOf(getNowDate("normal"))
            else -> mutableListOf(timeChecker(getNowDate("normal")))
        }
    }

    private fun getNowDate(code: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val dateFormat = DateTimeFormatter.ofPattern("yyyyMMdd HHmm E")
            val nowDate = if (code == "normal") LocalDateTime.now() else LocalDateTime.now().minusDays(1)
            nowDate.format(dateFormat)
        } else {
            val dateFormat = DateTimeFormat.forPattern("yyyyMMdd HHmm E")
            val nowDate = if (code == "normal") org.joda.time.LocalDateTime.now() else org.joda.time.LocalDateTime.now().minusDays(1)
            DateTime.parse(nowDate.toString(), dateFormat).toString()
        }
    }

    private fun timeChecker(time: String): String {
        return when(time.split(" ")[1].chunked(2)[0].toInt()) {
            in 0 until 6, 22, 23 -> "night"
            in 6 until  12 -> "morning"
            in 12 until 17 -> "afternoon"
            in 17 until 22 -> "evening"
            else -> ""
        }
    }

    private fun timeChanger(data: String): MutableList<String> {
        /*
            < 초단기실황 >
            이 API는 매시간 30분에 생성되고 10분마다 최신 정보로 업데이트 된다.
            예를 들어 00시 실황을 보기 위해선 00:40 이후에 조회를 해야 볼 수 있다.
            따라서 40분 이전이라면 이전시간 것을 불러온다. (00시 이전 시간을 유의할 것)

            < 단기예보 >
            이 API는 3시간 단위로 정보를 갱신하기 때문에 정해진 시간이 아니라면 이전에 갱신된 데이터를 불러오기 위함
            0200, 0500, 0800, 1100, 1400, 1700, 2000, 2300

            < 중기예보 >
            매일 06시 18시 기준으로 데이터가 업데이트 되기에 06시 기준으로 요청변수 time의 일자가 바뀌어야함.

            < 위성영상 >
            매일 09시 기준으로 요청변수 time의 일자가 바뀌어야함.
         */

        val timeList = MutableList(4) { "" }
        val (date, time, _) = data.split(" ")
        val yesterday = getNowDate("").split(" ")[0]
        val (hour, minute) = time.chunked(2).map { it.toInt() }

        // UltraSrtNcst Time
        val uTime = if (minute <= 40) {
            if (hour == 0) "$yesterday $time"
            else {
                val newTime = when(time.chunked(2)[0].toInt()) {
                    in 1 .. 10 -> "0${hour - 1}00"
                    in 11 .. 23 -> "${hour - 1}00"
                    else -> ""
                }
                "$date $newTime"
            }
        } else {
            "$date $time"
        }
        timeList[0] = uTime

        // VilageFcst Time
        val vTime = when (hour) {
            in 2..4 -> "0200"
            in 5..7 -> "0500"
            in 8..10 -> "0800"
            in 11..13 -> "1100"
            in 14..16 -> "1400"
            in 17..19 -> "1700"
            in 20..22 -> "2000"
            else -> "2300"
        }

        timeList[1] = if (vTime == "2300") "$yesterday $vTime" else "$date $vTime"

        // MidFcst Time
        val mTime = if (hour in 6 .. 17) "0600" else "1800"
        timeList[2] = if (hour in 0 .. 5) "$yesterday $mTime" else "$date $mTime"

        // SatlitImg Time
        timeList[3] = if (hour in 0 .. 8) yesterday else date

        return timeList
    }
}