package com.demo.newvpn.admob

import com.demo.newvpn.app.maLog
import com.demo.newvpn.base.BaseUI
import com.demo.newvpn.util.LimitManger
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd

class ShowOpenAd(
    private val baseUI: BaseUI,
    private val machinetype:String
) {

    fun showOpenAd(back:Boolean=false,showing:()->Unit,close:()->Unit){
        val ad = LoadAd.getAd(machinetype)
        if (null!=ad){
            if (LoadAd.openAdShowing||!baseUI.resume){
                close.invoke()
                return
            }
            maLog("show $machinetype ad")
            showing.invoke()
            when(ad){
                is InterstitialAd ->{
                    ad.fullScreenContentCallback= OpenAdCallback(baseUI, machinetype, close)
                    ad.show(baseUI)
                }
                is AppOpenAd ->{
                    ad.fullScreenContentCallback= OpenAdCallback(baseUI, machinetype, close)
                    ad.show(baseUI)
                }
            }
        }else{
            if (back){
                LoadAd.load(machinetype)
                close.invoke()
            }
        }
    }
}