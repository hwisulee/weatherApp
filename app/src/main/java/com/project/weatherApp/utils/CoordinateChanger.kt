package com.project.weatherApp.utils

import android.util.Log
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

internal class Coordinate {
    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var x: Double = 0.0
    var y: Double = 0.0
}

class CoordinateChanger {
    private val toGrid = 0
    private val toGps = 1

    internal fun getCoordinate(latitude: Double, longitude: Double): Coordinate {
        return convertToGridGps(toGrid, latitude, longitude)
    }

    private fun convertToGridGps(mode: Int, latitude: Double, longitude: Double): Coordinate {
        /*
        re: 지구 반경(km)
        grid: 격자 간격(km)
        slat1: 투영 위도1(degree)
        slat2: 투영 위도2(degree)
        olon: 기준점 경도(degree)
        olat: 기준점 위도(degree)
        xo: 기준점 X 좌표(grid)
        yo: 기준점 Y 좌표(grid)
         */
        val RE = 6371.00877
        val GRID = 5.0
        val SLAT1 = 30.0
        val SLAT2 = 60.0
        val OLON = 126.0
        val OLAT = 38.0
        val xo = 43
        val yo = 136

        // LCC DFS 좌표변환 (code: "toGrid"(위경도 -> 좌표), "toGps"(좌표 -> 위경도))
        val degrad = Math.PI / 180.0
        val raddeg = 180.0 / Math.PI

        val re = RE / GRID
        val slat1 = SLAT1 * degrad
        val slat2 = SLAT2 * degrad
        val olon = OLON * degrad
        val olat = OLAT * degrad


        var sn = tan(Math.PI * 0.25 + slat2 * 0.5) / tan(Math.PI * 0.25 + slat1 * 0.5)
        sn = ln(cos(slat1) / cos(slat2)) / ln(sn)
        var sf = tan(Math.PI * 0.25 + slat1 * 0.5)
        sf = sf.pow(sn) * cos(slat1) / sn
        var ro = tan(Math.PI * 0.25 + olat * 0.5)
        ro = re * sf / ro.pow(sn)

        val rs = Coordinate()
        rs.latitude = latitude
        rs.longitude = longitude

        when (mode) {
            toGrid -> {
                var ra = tan(Math.PI * 0.25 + latitude * degrad * 0.5)
                ra = re * sf / ra.pow(sn)

                var theta = longitude * degrad - olon
                if (theta > Math.PI) theta -= 2.0 * Math.PI
                if (theta < -Math.PI) theta += 2.0 * Math.PI
                theta *= sn

                rs.x = floor(ra * sin(theta) + xo + 0.5)
                rs.y = floor(ro - ra * cos(theta) + yo + 0.5)
            }

            toGps -> {
                val xn = latitude - xo
                val yn = ro - longitude + yo

                var ra = sqrt(xn * xn + yn * yn)
                if (sn < 0.0) ra = -ra

                var alat = (re * sf / ra).pow((1.0 / sn))
                alat = 2.0 * atan(alat) - Math.PI * 0.5

                var theta: Double
                if (abs(xn) <= 0.0) theta = 0.0
                else {
                    if (abs(yn) <= 0.0) {
                        theta = Math.PI * 0.5
                        if (xn < 0.0) theta = -theta
                    }
                    else theta = atan2(xn, yn)
                }

                val alon = theta / sn + olon

                rs.latitude = alat * raddeg
                rs.longitude = alon * raddeg
            }
        }

        return rs
    }
}