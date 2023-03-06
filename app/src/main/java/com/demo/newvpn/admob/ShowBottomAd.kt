package com.demo.newvpn.admob

import android.graphics.Outline
import android.view.View
import android.view.ViewOutlineProvider
import android.widget.ImageView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.utils.widget.ImageFilterView
import com.blankj.utilcode.util.SizeUtils
import com.demo.newvpn.R
import com.demo.newvpn.app.maLog
import com.demo.newvpn.app.show
import com.demo.newvpn.base.BaseUI
import com.demo.newvpn.util.LimitManger
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import kotlinx.coroutines.*

class ShowBottomAd(
    private val baseUI: BaseUI,
    private val machinetype:String
) {

    private var loop=true
    private var lastNativeAd:NativeAd?=null
    private var showJob:Job?=null

    fun showBottomAd(){
        LoadAd.load(machinetype)
        stopShow()
        loop=true
        showJob=GlobalScope.launch(Dispatchers.Main)  {
            delay(300L)
            if (!baseUI.resume){
                return@launch
            }
            while (loop){
                if(!isActive){
                    break
                }
                val ad = LoadAd.getAd(machinetype)
                if(baseUI.resume&&null!=ad&&ad is NativeAd){
                    cancel()
                    lastNativeAd?.destroy()
                    lastNativeAd=ad
                    loop=false
                    show(ad)
                }
                delay(1000L)
            }
        }
    }

    private fun show(ad:NativeAd){
        maLog("show $machinetype ad ")
        val viewNative = baseUI.findViewById<NativeAdView>(R.id.machine_native)
        viewNative.iconView=baseUI.findViewById(R.id.machine_logo)
        (viewNative.iconView as ImageFilterView).setImageDrawable(ad.icon?.drawable)

        viewNative.callToActionView=baseUI.findViewById(R.id.machine_btn)
        (viewNative.callToActionView as AppCompatTextView).text=ad.callToAction

        viewNative.mediaView=baseUI.findViewById(R.id.machine_media)
        ad.mediaContent?.let {
            viewNative.mediaView?.apply {
                setMediaContent(it)
                setImageScaleType(ImageView.ScaleType.CENTER_CROP)
                outlineProvider = object : ViewOutlineProvider() {
                    override fun getOutline(view: View?, outline: Outline?) {
                        if (view == null || outline == null) return
                        outline.setRoundRect(
                            0,
                            0,
                            view.width,
                            view.height,
                            SizeUtils.dp2px(8F).toFloat()
                        )
                        view.clipToOutline = true
                    }
                }
            }
        }

        viewNative.bodyView=baseUI.findViewById(R.id.machine_desc)
        (viewNative.bodyView as AppCompatTextView).text=ad.body


        viewNative.headlineView=baseUI.findViewById(R.id.machine_title)
        (viewNative.headlineView as AppCompatTextView).text=ad.headline

        viewNative.setNativeAd(ad)
        baseUI.findViewById<AppCompatImageView>(R.id.machine_cover).show(false)

        LimitManger.updatecs()
        LoadAd.removeAd(machinetype)
        LoadAd.load(machinetype)
        LimitManger.setRefreshStatus(machinetype,false)
    }


    fun stopShow(){
        loop=false
        showJob?.cancel()
        showJob=null
    }
}