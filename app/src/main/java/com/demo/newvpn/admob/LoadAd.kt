package com.demo.newvpn.admob

import com.demo.newvpn.app.maLog
import com.demo.newvpn.app.myApp
import com.demo.newvpn.bean.AdResBean
import com.demo.newvpn.bean.AdResultBean
import com.demo.newvpn.conf.OnlineConfig
import com.demo.newvpn.util.LimitManger
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAdOptions
import org.json.JSONObject

object LoadAd {
    const val OPEN="machine_ipen"
    const val HOME_BOTTOM="machine_n_home"
    const val RESULT_BOTTOM="machine_n_result"
    const val CONNECT="machine_i"
    const val BACK="machine_i2"

    var openAdShowing=false

    private val loading= arrayListOf<String>()
    private val adResultMap= hashMapOf<String,AdResultBean>()

    fun load(machinetype:String,retryNum:Int=0){
        if(LimitManger.hasLimit()){
            maLog("limit")
            return
        }
        if(loading.contains(machinetype)){
            maLog("$machinetype  loading")
            return
        }

        if(adResultMap.containsKey(machinetype)){
            val resultAdBean = adResultMap[machinetype]
            if(null!=resultAdBean?.ad){
                if(resultAdBean.expired()){
                    removeAd(machinetype)
                }else{
                    maLog("$machinetype cache")
                    return
                }
            }
        }
        val parseAdList = parseAdList(machinetype)
        if(parseAdList.isEmpty()){
            return
        }
        loading.add(machinetype)
        startLoadAd(machinetype,parseAdList.iterator(),retryNum)
    }

    private fun startLoadAd(machinetype: String, iterator: Iterator<AdResBean>, retry:Int){
        loadAd(machinetype,iterator.next()){
            if(null!=it){
                loading.remove(machinetype)
                adResultMap[machinetype]=it
            }else{
                if(iterator.hasNext()){
                    startLoadAd(machinetype,iterator,retry)
                }else{
                    loading.remove(machinetype)
                    if(retry>0&&machinetype==OPEN){
                        load(machinetype, retryNum = 0)
                    }
                }
            }
        }
    }

    private fun loadAd(machinetype: String, adResBean: AdResBean, result: (bean: AdResultBean?) -> Unit){
        maLog("load ad $machinetype,${adResBean.machineadid}----${adResBean.machinetype}---${adResBean.machinenum}")
        if(adResBean.machinetype=="open"){
            AppOpenAd.load(
                myApp,
                adResBean.machineadid,
                AdRequest.Builder().build(),
                AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(p0: AppOpenAd) {
                        maLog("load ad success----$machinetype")
                        result.invoke(AdResultBean(time = System.currentTimeMillis(), ad = p0))
                    }

                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        maLog("load ad fail----$machinetype---${p0.message}")
                        result.invoke(null)
                    }
                }
            )
        }
        if(adResBean.machinetype=="int"){
            InterstitialAd.load(
                myApp,
                adResBean.machineadid,
                AdRequest.Builder().build(),
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        maLog("load ad fail----$machinetype---${p0.message}")
                        result.invoke(null)
                    }

                    override fun onAdLoaded(p0: InterstitialAd) {
                        maLog("load ad success----$machinetype")
                        result.invoke(AdResultBean(time = System.currentTimeMillis(), ad = p0))
                    }
                }
            )
        }
        if(adResBean.machinetype=="native"){
            AdLoader.Builder(
                myApp,
                adResBean.machineadid,
            ).forNativeAd {
                maLog("load ad success----$machinetype")
                result.invoke(AdResultBean(time = System.currentTimeMillis(), ad = it))
            }
                .withAdListener(object : AdListener(){
                    override fun onAdFailedToLoad(p0: LoadAdError) {
                        super.onAdFailedToLoad(p0)
                        maLog("load ad fail----$machinetype---${p0.message}")
                        result.invoke(null)
                    }

                    override fun onAdClicked() {
                        super.onAdClicked()
                        LimitManger.updatecc()
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(
                            NativeAdOptions.ADCHOICES_TOP_LEFT
                        )
                        .build()
                )
                .build()
                .loadAd(AdRequest.Builder().build())
        }

    }

    private fun parseAdList(key:String):List<AdResBean>{
        val list= arrayListOf<AdResBean>()
        try {
            val jsonArray = JSONObject(OnlineConfig.getAdStr()).getJSONArray(key)
            for (index in 0 until jsonArray.length()){
                val jsonObject = jsonArray.getJSONObject(index)
                list.add(
                    AdResBean(
                        jsonObject.optString("machineadid"),
                        jsonObject.optString("machinefrom"),
                        jsonObject.optString("machinetype"),
                        jsonObject.optInt("machinenum"),
                    )
                )
            }
        }catch (e:Exception){
        }
        return list.filter { it.machinefrom == "admob" }.sortedByDescending { it.machinenum }
    }

    fun getAd(machinetype:String)=adResultMap[machinetype]?.ad

    fun removeAd(machinetype:String){
        adResultMap.remove(machinetype)
    }

    fun preLoad(){
        load(OPEN)
        load(HOME_BOTTOM)
        load(RESULT_BOTTOM)
        load(CONNECT)
    }
}
