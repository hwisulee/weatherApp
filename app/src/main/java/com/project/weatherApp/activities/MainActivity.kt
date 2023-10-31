package com.project.weatherApp.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.project.weatherApp.R
import com.project.weatherApp.adapters.HourlyAdapter
import com.project.weatherApp.databinding.ActivityMainBinding
import com.project.weatherApp.domains.Hourly
import com.project.weatherApp.retrofit.ConnectViewModel
import com.project.weatherApp.retrofit.HL
import com.project.weatherApp.retrofit.NowLocation
import com.project.weatherApp.retrofit.Status
import com.project.weatherApp.retrofit.classifying
import com.project.weatherApp.utils.CoordinateChanger
import com.project.weatherApp.utils.LocationCode
import com.project.weatherApp.utils.TimeMaker

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val retrofitViewModel: ConnectViewModel by viewModels()
    private lateinit var getResult: ActivityResultLauncher<Intent>
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private val handler: Handler = Handler(Looper.getMainLooper())
    private var lastUpdatedHour = 0
    private var lastUpdatedMin = 0
    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private val taList = ArrayList<HL>()
    private val weatherList = ArrayList<Status>()
    private val locationList = ArrayList<NowLocation>()

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val responsePermissions = permissions.entries.filter { it.key in locationPermissions }
        if (responsePermissions.filter { it.value }.size == locationPermissions.size) setLocationListener()
        else Toast.makeText(this@MainActivity, "No", Toast.LENGTH_SHORT).show()
    }
    companion object {
        val locationPermissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        getLocation()

        if (savedInstanceState != null) {
            lastUpdatedHour = savedInstanceState.getInt("lastUpdatedHour")
            lastUpdatedMin = savedInstanceState.getInt("lastUpdatedMin")
        }
        setVariable()
    }

    private fun initView() {
        setupData()
        setupView()
    }

    private fun setupData() {
        CoordinateChanger().getCoordinate(latitude, longitude).also {
            if (updateDelayMaker() || lastUpdatedMin == 0) retrofitViewModel.connector("main", it, LocationCode())  else refreshing("normal")
        }
    }

    @SuppressLint("SetTextI18n", "DiscouragedApi")
    private fun setupView() {
        changeTheme()

        try {
            with(binding) {
                // Now Date - 현재 날짜
                TimeMaker().getSetting("NOW")[0].also {
                    val (date, time, e) = it.split(" ")
                    val year = date.slice(0 until 4)
                    val (month, day) = date.slice(4..date.lastIndex).chunked(2)
                    var (hour, minute) = time.chunked(2)
                    val ampm = if (hour.toInt() < 12) "오전" else "오후"
                    hour = if (hour.toInt() > 12) "${hour.toInt() - 12}" else hour
                    dateNow.text = "${month}월 ${day}일 ${e}요일 | $ampm ${hour}:${minute}"
                    lastUpdate.text = if (lastUpdatedHour == 0) "마지막 업데이트 : ${year}-${month}-${day}, $ampm ${hour}:${minute}" else "마지막 업데이트 : ${year}-${month}-${day}, $ampm ${lastUpdatedHour}:${lastUpdatedMin}"
                    if (updateDelayMaker() || lastUpdatedMin == 0) {
                        lastUpdatedHour = hour.toInt()
                        lastUpdatedMin = minute.toInt()
                    }
                }

                // Error Dialog
                retrofitViewModel.errorCode.observe(this@MainActivity) {
                    if (it.isNotEmpty()) {
                        val builder = AlertDialog.Builder(applicationContext)
                        it.forEach {
                            builder.setTitle("SYSTEM ERROR").setMessage("CODE: ${it.first}\nSYSTEM: ${it.second}\nMESSAGE: ${classifying().classifying(it.first)}").setPositiveButton("확인", null).create().show()
                        }
                    }
                }

                // UltraNcst Result - 현재 기온, 습도, 풍속, 강수량
                retrofitViewModel.resultUltraNcst.observe(this@MainActivity) {
                    val ultraNcstItem = it.body.items.item
                    ultraNcstItem.groupBy { it.category }.forEach {
                        when (it.key) {
                            "T1H" -> { temperNow.text = "${it.value[0].obsrValue}º" }
                            "RN1" -> { rain.text = "${it.value[0].obsrValue}%" }
                            "WSD" -> { windspd.text = "${it.value[0].obsrValue}km/h" }
                            "REH" -> { humidity.text = "${it.value[0].obsrValue}%"}
                        }
                    }
                }

                // UltraFcst Result - 현재 하늘상태
                retrofitViewModel.resultUltraFcst.observe(this@MainActivity) {
                    val ultraFcstItem = it.body.items.item
                    val nowSky = ultraFcstItem.groupBy { it.category }["SKY"]!![0].fcstValue
                    val nowPty = ultraFcstItem.groupBy { it.category }["PTY"]!![0].fcstValue

                    val nowTime = TimeMaker().getSetting("NOW")[0].split(" ")[1].chunked(2)[0].toInt()
                    var picPath = ""
                    if (nowPty == "0") {
                        when (nowSky) {
                            "1" -> {
                                weatherNow.text = "맑음"
                                picPath = if (nowTime in 7 until 20) "sun" else "moon"
                            }
                            "3" -> {
                                weatherNow.text = "구름 많음"
                                picPath = if (nowTime in 7 until 20) "cloudy_sunny" else "cloudy_moon"
                            }
                            "4" -> {
                                weatherNow.text = "흐림"
                                picPath = "cloudy"
                            }
                        }
                    } else {
                        when (nowPty) {
                            "1", "5", "6" -> {
                                weatherNow.text = "비"
                                picPath = "rainy"
                            }
                            "2" -> {
                                weatherNow.text = "눈"
                                picPath = "rainy_snowy"
                            }
                            "3", "7" -> {
                                weatherNow.text = "비 · 눈"
                                picPath = "snowy"
                            }
                        }
                    }

                    val drawableResourceId = weatherNowImg.resources.getIdentifier(picPath, "drawable", root.context.packageName)
                    Glide.with(binding.root.context).load(drawableResourceId).into(weatherNowImg)
                }

                // 2AM Vilage Result - 금일 최고-최저 온도
                retrofitViewModel.result2AMVilage.observe(this@MainActivity) {
                    val hLItem = it.body.items.item
                    val todayTMX = hLItem.groupBy{ it.category }["TMX"]!![0].fcstValue
                    val todayTMN = hLItem.groupBy{ it.category }["TMN"]!![0].fcstValue
                    temperHL.text = "최고: ${todayTMX.split(".")[0]}º / 최저: ${todayTMN.split(".")[0]}º"


                    for (i in 1 .. 2) {
                        val taMax = hLItem.groupBy { it.category }["TMX"]!![i].fcstValue
                        val taMin = hLItem.groupBy { it.category }["TMN"]!![i].fcstValue
                        taList.add(HL(taMax, taMin))
                    }

                    var sky = ""
                    var pty = ""
                    var wsd = ""
                    var reh = ""
                    var pcp = ""
                    hLItem.groupBy { it.fcstTime }["1500"]!!.forEachIndexed { index, item ->
                        if (item.category == "SKY") sky = item.fcstValue
                        if (item.category == "PTY") pty = item.fcstValue
                        if (item.category == "WSD") wsd = item.fcstValue
                        if (item.category == "REH") reh = item.fcstValue
                        if (item.category == "PCP") pcp = item.fcstValue
                        if ((index + 1) % 13 == 0) weatherList.add(Status(sky, pty, wsd, reh, pcp))
                    }
                }

                // Vilage Result - 다음 N시간 날씨 리스트
                val items = ArrayList<Hourly>()
                val adapter = HourlyAdapter(items)

                retrofitViewModel.resultVilage.observe(this@MainActivity) {
                    val vilageItem = it.body.items.item
                    if (items.isEmpty()) {
                        val vilageTemperGroup = vilageItem.filter { it.category == "TMP" }
                        val vilageSkyGroup = vilageItem.filter { it.category == "SKY" }
                        val vilagePtyGroup = vilageItem.filter { it.category == "PTY" }

                        for (i in 0 until 18) {
                            val timeTemp = vilageTemperGroup[i].fcstTime.chunked(2)
                            val newTime = if (timeTemp[0] == "00") "0시" else "${vilageTemperGroup[i].fcstTime.chunked(2)[0]}시"
                            var sky = ""

                            if (vilagePtyGroup[i].fcstValue == "0") {
                                when (vilageSkyGroup[i].fcstValue) {
                                    "1" -> sky = if (timeTemp[0].toInt() in 7 until 20) "sun" else "moon"
                                    "3" -> sky = if (timeTemp[0].toInt() in 7 until 20) "cloudy_sunny" else "cloudy_moon"
                                    "4" -> sky = "cloudy"
                                }
                            } else {
                                when (vilagePtyGroup[i].fcstValue) {
                                    "1", "4" -> sky = "rainy"
                                    "2" -> sky = "rainy_snowy"
                                    "3" -> sky = "snowy"
                                }
                            }

                            items.add(Hourly(newTime, sky, vilageTemperGroup[i].fcstValue.toInt()))
                        }
                    }
                }

                adapter.submitList(items)
                recyclerView.apply {
                    this.adapter = adapter
                    this.layoutManager = LinearLayoutManager(binding.activityMain?.applicationContext, LinearLayoutManager.HORIZONTAL, false)
                    setHasFixedSize(true)
                }

                // Satlit Result - 위성영상 이미지
                retrofitViewModel.resultSatlit.observe(this@MainActivity) {
                    val satlitItem = it.body.items.item
                    val errImg = weatherNowImg.resources.getIdentifier("error_img", "drawable", root.context.packageName)
                    val url = satlitItem[0].imgSrc.replace("[", "").replace("]", "").split(", ")
                    Glide.with(binding.root.context).load(url[url.lastIndex]).error(errImg).transform(
                        CenterCrop(), RoundedCorners(20)
                    ).listener(object: RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.d("aa", "===${e}")
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable,
                            model: Any,
                            target: Target<Drawable>?,
                            dataSource: DataSource,
                            isFirstResource: Boolean
                        ): Boolean {
                            return false
                        }

                    }).into(raidImgView)
                }

                retrofitViewModel.resultReverseGeocode.observe(this@MainActivity) {
                    locationList += NowLocation(it.results[0].region.area1.name, it.results[0].region.area2.name)
                }

                handler.postDelayed({
                    refreshing("normal")
                }, 3000)
            }
        } catch (_: Exception) {
            handler.postDelayed({
                setupView()
            }, 3000)
        }
    }

    private fun setVariable() {
        with(binding) {
            nextBtn.setOnClickListener {
                val data = Intent(this@MainActivity, WeekActivity::class.java).apply {
                    putExtra("taList", taList)
                    putExtra("weatherList", weatherList)
                    putExtra("locationList", locationList)
                }
                getResult.launch(data)
            }
            swipeRefresh.setOnRefreshListener { refreshing("refresh") }
            scrollView.viewTreeObserver.addOnScrollChangedListener { mainBackGround.isEnabled = (scrollView.scrollY == 0) }
            mainWeatherBox.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.weather.go.kr/w/index.do"))) }
            raidImgView.setOnClickListener { startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.weather.go.kr/w/image/sat/gk2a.do"))) }
            getResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
        }
    }
    private fun changeTheme() {
        with(binding) {
            when(TimeMaker().getSetting("")[0]) {
                "morning", "evening" -> {
                    mainBackGround.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_evening)
                    weatherBox.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_evening_box1)
                    raidMapView.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_evening_box1)
                }
                "afternoon" -> {
                    mainBackGround.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_afternoon)
                    weatherBox.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_afternoon_box1)
                    raidMapView.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_afternoon_box1)
                }
                else -> {
                    mainBackGround.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_night)
                    weatherBox.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_night_box1)
                    raidMapView.background = ContextCompat.getDrawable(applicationContext, R.drawable.background_night_box1)
                }
            }
        }
    }

    private fun updateDelayMaker(): Boolean {
        val nowDateMin = TimeMaker().getSetting("NOW")[0].split(" ")[1].chunked(2)[1].toInt()
        val sum = if (nowDateMin - lastUpdatedMin < 0) nowDateMin - lastUpdatedMin + 60 else nowDateMin - lastUpdatedMin
        return sum >= 3
    }

    private fun refreshing(code: String) {
        with(binding) {
            when(code) {
                "normal" -> {
                    // Ended Refresh & Loading, Update UI
                    swipeRefresh.isRefreshing = false
                    shimmerLoading.stopShimmer()
                    shimmerLoading.visibility = View.GONE
                    swipeRefresh.visibility = View.VISIBLE
                }

                "refresh" -> {
                    // Started Refresh & Loading, Update UI
                    shimmerLoading.startShimmer()
                    shimmerLoading.visibility = View.VISIBLE
                    swipeRefresh.visibility = View.GONE
                    setupData()
                }
            }
        }
    }

    private fun getLocation() {
        if (::locationManager.isInitialized.not()) locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (isGpsEnable) permissionLauncher.launch(locationPermissions)
    }

    @SuppressLint("MissingPermission")
    private fun setLocationListener() {
        val minTime: Long = 1500
        val minDistance = 100f

        if (::locationListener.isInitialized.not()) locationListener = LocationListener()

        with(locationManager) {
            requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, locationListener)
            requestLocationUpdates(LocationManager.NETWORK_PROVIDER, minTime, minDistance, locationListener)
        }
    }

    inner class LocationListener : android.location.LocationListener {
        override fun onLocationChanged(location: Location) {
            latitude = location.latitude
            longitude = location.longitude
            initView()
            removeLocationListener()
        }

        private fun removeLocationListener() {
            if (::locationManager.isInitialized && ::locationListener.isInitialized) locationManager.removeUpdates(locationListener)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("lastUpdatedHour", lastUpdatedHour)
        outState.putInt("lastUpdatedMin", lastUpdatedMin)
    }
}