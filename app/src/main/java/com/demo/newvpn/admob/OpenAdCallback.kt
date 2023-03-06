package com.demo.newvpn.admob

import com.demo.newvpn.base.BaseUI
import com.demo.newvpn.util.LimitManger
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class OpenAdCallback(
    private val baseUI: BaseUI,
    private val machinetype:String,
    private val close:()->Unit
): FullScreenContentCallback()  {

    override fun onAdDismissedFullScreenContent() {
        super.onAdDismissedFullScreenContent()
        LoadAd.openAdShowing =false
        clickCloseAd()
    }

    override fun onAdShowedFullScreenContent() {
        super.onAdShowedFullScreenContent()
        LoadAd.openAdShowing  =true
        LimitManger.updatecs()
        LoadAd.removeAd(machinetype)
    }

    override fun onAdFailedToShowFullScreenContent(p0: AdError) {
        super.onAdFailedToShowFullScreenContent(p0)
        LoadAd.openAdShowing  =false
        LoadAd.removeAd(machinetype)
        clickCloseAd()
    }


    override fun onAdClicked() {
        super.onAdClicked()
        LimitManger.updatecc()
    }

    private fun clickCloseAd(){
        if (machinetype!= LoadAd.OPEN){
            LoadAd.load(machinetype)
        }
        GlobalScope.launch(Dispatchers.Main) {
            delay(200L)
            if (baseUI.resume){
                close.invoke()
            }
        }
    }
}