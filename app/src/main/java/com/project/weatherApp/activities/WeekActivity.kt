package com.project.weatherApp.activities

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.project.weatherApp.R
import com.project.weatherApp.adapters.WeeklyAdapter
import com.project.weatherApp.databinding.ActivityWeekBinding
import com.project.weatherApp.domains.Weekly
import com.project.weatherApp.retrofit.ConnectViewModel
import com.project.weatherApp.retrofit.HL
import com.project.weatherApp.retrofit.NowLocation
import com.project.weatherApp.retrofit.Status
import com.project.weatherApp.retrofit.classifying
import com.project.weatherApp.utils.Coordinate
import com.project.weatherApp.utils.MidLocation
import com.project.weatherApp.utils.TimeMaker

class WeekActivity: AppCompatActivity() {
    private val binding: ActivityWeekBinding by lazy { ActivityWeekBinding.inflate(layoutInflater) }
    private val retrofitViewModel: ConnectViewModel by viewModels()
    private val handler: Handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setupData()
        setupView()
        setVariable()
    }

    private fun setupData() {
        val locationList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getParcelableArrayListExtra("locationList", NowLocation::class.java)
        } else {
            intent.getParcelableArrayListExtra("locationList")
        }
        MidLocation().locationSetup(locationList!!).also {
            retrofitViewModel.connector("sub", Coordinate(), it)
        }
    }

    private fun setupView() {
        changeTheme()

        try {
            with(binding) {
                // Time
                val eList = mutableListOf<String>()
                TimeMaker().getSetting("NOW")[0].also {
                    val (date, time, e) = it.split(" ")
                    val eRule = listOf("월", "화", "수", "목", "금", "토", "일")
                    var nowIdx = eRule.indexOf(e)
                    for (i in 1 .. 10) {
                        nowIdx += 1
                        if (nowIdx >= 7) nowIdx -= 7

                        eList += "${eRule[nowIdx]}요일"
                    }
                }

                val taList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra("taList", HL::class.java)
                } else {
                    intent.getParcelableArrayListExtra("taList")
                }
                val weatherList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra("weatherList", Status::class.java)
                } else {
                    intent.getParcelableArrayListExtra("weatherList")
                }

                // Error Dialog
                retrofitViewModel.errorCode.observe(this@WeekActivity) {
                    if (it.isNotEmpty()) {
                        val builder = AlertDialog.Builder(applicationContext)
                        it.forEach {
                            builder.setTitle("SYSTEM ERROR").setMessage("CODE: ${it.first}\nSYSTEM: ${it.second}\nMESSAGE: ${classifying().classifying(it.first)}").setPositiveButton("확인", null).create().show()
                        }
                    }
                }

                // Weather Box
                val drawableResourceId = weatherNowImg.resources.getIdentifier(skyClassifyingCode(weatherList!![0].SKY, weatherList[0].PTY), "drawable", root.context.packageName)
                Glide.with(binding.root.context).load(drawableResourceId).into(weatherNowImg)

                temperNow.text = "${taList!![0].H.split(".")[0]}º"
                rain.text = if (weatherList[0].PCP == "강수없음") "${weatherList[0].PCP}" else "${weatherList[0].PCP}%"
                windspd.text = "${weatherList[0].WSD}km/h"
                humidity.text = "${weatherList[0].REH}%"
                skyNow.text = skyClassifying(skyClassifyingCode(weatherList!![0].SKY, weatherList[0].PTY), "reverse")

                // MidTa Result - 중기 최고 최저 기온
                val temperList = ArrayList<Pair<Int, Int>>()
                retrofitViewModel.resultMidTa.observe(this@WeekActivity) {
                    val midTaItem = it.body.items.item
                    temperList.add(Pair(taList[1].H.split(".")[0].toInt(), taList[1].L.split(".")[0].toInt()))
                    temperList.add(Pair(midTaItem[0].taMax3, midTaItem[0].taMin3))
                    temperList.add(Pair(midTaItem[0].taMax4, midTaItem[0].taMin4))
                    temperList.add(Pair(midTaItem[0].taMax5, midTaItem[0].taMin5))
                    temperList.add(Pair(midTaItem[0].taMax6, midTaItem[0].taMin6))
                    temperList.add(Pair(midTaItem[0].taMax7, midTaItem[0].taMin7))
                    temperList.add(Pair(midTaItem[0].taMax8, midTaItem[0].taMin8))
                    temperList.add(Pair(midTaItem[0].taMax9, midTaItem[0].taMin9))
                    temperList.add(Pair(midTaItem[0].taMax10, midTaItem[0].taMin10))
                }


                // MidLandFcst Result - 중기 하늘 상태
                val skyList = ArrayList<String>()
                retrofitViewModel.resultMidLandFcst.observe(this@WeekActivity) {
                    val midLandFcstItem = it.body.items.item
                    skyList.add(skyClassifyingCode(weatherList[1].SKY, weatherList[1].PTY))
                    skyList.add(midLandFcstItem[0].wf3Am)
                    skyList.add(midLandFcstItem[0].wf4Am)
                    skyList.add(midLandFcstItem[0].wf5Am)
                    skyList.add(midLandFcstItem[0].wf6Am)
                    skyList.add(midLandFcstItem[0].wf7Am)
                    skyList.add(midLandFcstItem[0].wf8)
                    skyList.add(midLandFcstItem[0].wf9)
                    skyList.add(midLandFcstItem[0].wf10)
                }

                // List Setting 내일 꺼는 상단 메인에, 2일후부터가 리스트에
                val items = ArrayList<Weekly>()
                repeat(8) {
                    if (it == 0) items.add(Weekly(eList[it + 1], skyList[it], skyClassifying(skyClassifyingCode(weatherList[1].SKY, weatherList[1].PTY), "reverse"), temperList[it].first, temperList[it].second))
                    else items.add(Weekly(eList[it + 1], skyClassifying(skyList[it], ""), skyList[it], temperList[it].first, temperList[it].second))
                }

                // View Setting
                val adapter = WeeklyAdapter(items)
                recyclerView.apply {
                    this.adapter = adapter
                    this.layoutManager = LinearLayoutManager(binding.activityWeek?.applicationContext, LinearLayoutManager.VERTICAL, false)
                    setHasFixedSize(true)
                }

                adapter.submitList(items)
            }
        } catch (_: Exception) {
            handler.postDelayed({
                setupView()
            }, 3000)
        }
    }

    private fun setVariable() {
        with(binding) {
            backBtn.setOnClickListener { finish() }
        }
    }

    private fun skyClassifyingCode(sky: String, pty: String): String {
        var newSky = ""
        if (pty == "0") {
            when (sky) {
                "1" -> newSky = "sun"
                "3" -> newSky = "cloudy_sunny"
                "4" -> newSky = "cloudy"
            }
        } else {
            when (sky) {
                "1", "4" -> newSky = "rainy"
                "2" -> newSky = "rainy_snowy"
                "3" -> newSky = "snowy"
            }
        }

        return newSky
    }

    private fun skyClassifying(sky: String, code: String): String {
        var nowSky = ""
        if (code == "") {
            nowSky = when (sky) {
                "맑음" -> "sun"
                "구름많음" -> "cloudy_sunny"
                "흐림" -> "cloudy"
                "비", "구름많고, 비", "구름많고 소나기", "흐리고 비", "흐리고 소나기" -> "rainy"
                "눈", "구름많고 눈", "흐리고 눈" -> "snowy"
                "구름많고 비/눈", "흐리고 비/눈" -> "rainy_snowy"
                else -> "error_img"
            }
        } else {
            nowSky = when (sky) {
                "sun" -> "맑음"
                "cloudy_sunny" -> "구름많음"
                "cloudy" -> "흐림"
                "rainy" -> "흐리고 비"
                "snowy" -> "흐리고 눈"
                "rainy_snowy" -> "흐리고 비/눈"
                else -> "error_img"
            }
        }

        return nowSky
    }

    private fun changeTheme() {
        with(binding) {
            when(TimeMaker().getSetting("")[0]) {
                "morning", "evening" -> {
                    weekendBackGround.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_evening)
                    weekendMainBackGround.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_evening_box2)
                }
                "afternoon" -> {
                    weekendBackGround.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_afternoon)
                    weekendMainBackGround.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_afternoon_box2)
                }
                else -> {
                    weekendBackGround.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_night)
                    weekendMainBackGround.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_night_box2)
                }
            }
        }
    }
}