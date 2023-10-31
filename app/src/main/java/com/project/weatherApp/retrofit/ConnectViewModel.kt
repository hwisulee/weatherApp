package com.project.weatherApp.retrofit

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.project.weatherApp.BuildConfig
import com.project.weatherApp.utils.Coordinate
import com.project.weatherApp.utils.LocationCode
import com.project.weatherApp.utils.MidLocation
import com.project.weatherApp.utils.TimeMaker
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.net.SocketTimeoutException

class ConnectViewModel: ViewModel() {
    companion object {
        private const val BASE_URL1 = "http://apis.data.go.kr/"
        private const val BASE_URL2 = "https://naveropenapi.apigw.ntruss.com/"
    }

    private var client1 = Client.getService(BASE_URL1)
    private var client2 = Client.getService(BASE_URL2)
    private var job: Job? = null
    private val loadError = MutableLiveData<String?>()
    private val loading = MutableLiveData<Boolean>()
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable -> onError("Exception: ${throwable.localizedMessage}") }
    private val authKey = BuildConfig.authKey
    private val clientID = BuildConfig.naverClientID
    private val clientKey = BuildConfig.naverClientKey

    var resultUltraNcst = MutableLiveData<UltraSrtNcstData.Response>()
    var resultUltraFcst = MutableLiveData<UltraSrtFcstData.Response>()
    var result2AMVilage = MutableLiveData<VilageFcstData.Response>()
    var resultVilage = MutableLiveData<VilageFcstData.Response>()
    var resultMidTa = MutableLiveData<MidTaData.Response>()
    var resultMidLandFcst = MutableLiveData<MidLandFcstData.Response>()
    var resultSatlit = MutableLiveData<SatlitImgData.Response>()
    var resultReverseGeocode = MutableLiveData<ReverseGeocode.Root>()
    var errorCode = MutableLiveData<MutableList<Pair<String, String>>>()

    internal fun connector(code: String, data: Coordinate, locationCode: LocationCode) { tryConnectAPI(code, data, locationCode) }

    private fun tryConnectAPI(code: String, data: Coordinate, locationCode: LocationCode) {
        val timeList = TimeMaker().getSetting("API")
        val nx = data.x.toInt()
        val ny = data.y.toInt()
        val coordinate = "${data.longitude},${data.latitude}"
        val taCode = locationCode.taCode
        val landCode = locationCode.landCode

        loading.postValue(true)
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            withContext(Dispatchers.Main) {
                try {
                    when (code) {
                        "main" -> {
                            val response1 = async {
                                val (date, time, _) = timeList[0].split(" ")
                                resultUltraNcst.postValue(client1.getUltraSrtNcst(authKey, 1, 10, "JSON", date, time, nx, ny).response)
                            }
                            val response2 = async {
                                val (date, time, _) = timeList[0].split(" ")
                                resultUltraFcst.postValue(client1.getUltraSrtFcst(authKey, 1, 100, "JSON", date, time, nx, ny).response)
                            }
                            val response3 = async {
                                val (date, _, _) = timeList[1].split(" ")
                                result2AMVilage.postValue(client1.getVilageFcst(authKey, 1, 750, "JSON", date, "0200", nx, ny).response)
                            }
                            val response4 = async {
                                val (date, time, _) = timeList[1].split(" ")
                                resultVilage.postValue(client1.getVilageFcst(authKey, 1, 250, "JSON", date, time, nx, ny).response)
                            }
                            val response5 = async { resultSatlit.postValue(client1.getSatlitImg(authKey, 1, 10, "JSON", "G2", "rgbt", "ko", timeList[3]).response) }

                            val response6 = async { resultReverseGeocode.postValue(client2.getReverseGeocode(clientID, clientKey, coordinate, "legalcode", "json")) }

                            awaitAll(response1, response2, response3, response4, response5, response6).let {
                                loadError.postValue("")
                                loading.postValue(false)

                                val errChecker: MutableList<Pair<String, String>> = emptyList<Pair<String, String>>().toMutableList()
                                when {
                                    resultUltraNcst.value!!.header.resultCode != "00" -> errChecker += Pair(resultUltraNcst.value!!.header.resultCode, "UltraSrtNcst")
                                    resultUltraFcst.value!!.header.resultCode != "00" -> errChecker += Pair(resultUltraFcst.value!!.header.resultCode, "UltraSrtFcst")
                                    result2AMVilage.value!!.header.resultCode != "00" -> errChecker += Pair(result2AMVilage.value!!.header.resultCode, "VilageFcst_YesterDay")
                                    resultVilage.value!!.header.resultCode != "00" -> errChecker += Pair(resultVilage.value!!.header.resultCode, "VilageFcst_Today")
                                    resultSatlit.value!!.header.resultCode != "00" -> errChecker += Pair(resultSatlit.value!!.header.resultCode, "SatlitImgInfoService")
                                }

                                if (errChecker.isNotEmpty()) errorCode(errChecker)
                            }
                        }

                        "sub" -> {
                            val response1 = async { resultMidTa.postValue(client1.getMidTa(authKey, 1, 10, "JSON", taCode, timeList[2]).response) }
                            val response2 = async { resultMidLandFcst.postValue(client1.getMidLandFcst(authKey, 1, 10, "JSON", landCode, timeList[2]).response) }

                            awaitAll(response1, response2).let {
                                loadError.postValue("")
                                loading.postValue(false)

                                val errChecker: MutableList<Pair<String, String>> = emptyList<Pair<String, String>>().toMutableList()
                                when {
                                    resultMidTa.value!!.header.resultCode != "00" -> errChecker += Pair(resultMidTa.value!!.header.resultCode, "MidTa")
                                    resultMidLandFcst.value!!.header.resultCode != "00" -> errChecker += Pair(resultMidLandFcst.value!!.header.resultCode, "MidLandFcst")
                                }
                                if (errChecker.isNotEmpty()) errorCode(errChecker)
                            }
                        }
                    }

                } catch (e: SocketTimeoutException) { e.printStackTrace()
                    Log.d("test", "try again")
                    tryConnectAPI(code, data, locationCode)
                } catch (e: HttpException) { e.printStackTrace()
                } catch (e: Throwable) { e.printStackTrace()
                }
            }
        }
    }

    private fun errorCode(code: MutableList<Pair<String, String>>) {
        /*
        00 NORMAL_SERVICE 정상
        01 APLLICATION_ERROR 에플리케이션 에러
        02 DB_ERROR 데이터베이스 에러
        03 NODATA_ERROR 데이터 없음 에러
        04 HTTP_ERROR HTTP 에러
        05 SERVICETIME_OUT 서비스 연결 실패 에러
        10 INVALD_REQUEST_PARAMETER_ERROR 잘못된 요청 파라메터 에러
        11 NO_MANDATORY_REQUEST_PARAMETERS_ERROR 필수요청 파라메터가 없음
        12 NO_OPENAPI_SERVICE_ERROR 해당 오픈 API 서비스가 없거나 폐기됨
        20 SERVICE_ACCESS_DENIED_ERROR 서비스 접근 거부
        21 TEMPORARILY_DISABLE_THE_SERVICEKEY_ERROR 일시적으로 사용할 수 없는 서비스 키
        22 LIMITED_NUMBER_OF_SERVICE_REQUESTS_EXCEEDS_ERROR 서비스 오청제한횟수 초과에러
        30 SERVICE_KEY_IS_NOT_REGISTERED_ERROR 등록되지 않은 서비스키
        31 DEADLINE_HAS_EXPIRED_ERROR 기한만료된 서비스키
        32 UNREGISTERED_IP_ERROR 등록되지 않은 IP
        33 UNSIGNED_CALL_ERROR 서명되지 않은 호출
        99 UNKNOWN_ERROR 기타 에러
         */
        errorCode.postValue(code)
    }

    private fun onError(message: String) {
        loadError.postValue(message)
        loading.postValue(false)
    }

    override fun onCleared() {
        job?.cancel()
    }
}
