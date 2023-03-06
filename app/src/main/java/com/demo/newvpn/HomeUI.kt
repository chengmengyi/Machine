package com.demo.newvpn

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.net.VpnService
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.demo.newvpn.admob.LoadAd
import com.demo.newvpn.admob.ShowBottomAd
import com.demo.newvpn.admob.ShowOpenAd
import com.demo.newvpn.app.*
import com.demo.newvpn.base.BaseUI
import com.demo.newvpn.conf.LocalConfig
import com.demo.newvpn.conf.OnlineConfig
import com.demo.newvpn.interfaces.IAppFrontInterface
import com.demo.newvpn.interfaces.IConnectServerInterface
import com.demo.newvpn.interfaces.IConnectTimeInterface
import com.demo.newvpn.server.ConnectServer
import com.demo.newvpn.server.ConnectTimeManager
import com.demo.newvpn.util.LimitManger
import com.github.shadowsocks.utils.StartService
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.layout_home.*
import kotlinx.android.synthetic.main.layout_set.*
import kotlinx.coroutines.*
import java.lang.Exception

class HomeUI:BaseUI(), IConnectServerInterface, IConnectTimeInterface, IAppFrontInterface {
    private var time=0
    private var bottomIndex=0
    private var canClick=true
    private var permission=false
    private var connectServerJob: Job?=null
    private var objectAnimator: ObjectAnimator?=null
    private val showOpenAd by lazy { ShowOpenAd(this,LoadAd.CONNECT) }
    private val showBottomAd by lazy { ShowBottomAd(this,LoadAd.HOME_BOTTOM) }

    private val registerResult=registerForActivityResult(StartService()) {
        if (!it && permission) {
            permission = false
            startConnectServer()
        } else {
            canClick=true
            showToast("Connected fail")
        }
    }

    override fun layout(): Int = R.layout.activity_home

    override fun initView() {
        immersionBar.statusBarView(top).init()
        updateGuideUI(OnlineConfig.showGuide)
        updateBottomUI()
        ConnectServer.init(this,this)
        ConnectTimeManager.setInterface(this)
        AcRegister.setIAppFrontInterface(this)
        setClick()
        if(OnlineConfig.isLimitUser){
            AlertDialog.Builder(this).apply {
                setCancelable(false)
                setMessage("Due to the policy reason , this service is not available in your country")
                setPositiveButton("confirm") { _, _ ->
                    finish()
                }
                show()
            }
        }
    }

    private fun setClick(){
        llc_home.setOnClickListener {
            if(canClick&&!lottieIsShowing()){
                bottomIndex=0
                updateBottomUI()
            }

        }
        llc_set.setOnClickListener {
            if(canClick&&!lottieIsShowing()){
                bottomIndex=1
                updateBottomUI()
            }
        }
        iv_connect.setOnClickListener { clickConnectBtn() }
        llc_choose_server.setOnClickListener {
            if(canClick&&!lottieIsShowing()){
                startActivityForResult(Intent(this,ChooseServerUI::class.java),1000)
            }
        }


        llc_contact.setOnClickListener {
            if(canClick&&!lottieIsShowing()){
                try {
                    val uri = Uri.parse("mailto:${LocalConfig.email}")
                    val intent = Intent(Intent.ACTION_SENDTO, uri)
                    startActivity(intent)
                }catch (e: Exception){
                    showToast("Contact us by email：${LocalConfig.email}")
                }
            }
        }
        llc_agree.setOnClickListener {
            startActivity(Intent(this,WebUI::class.java))
        }
        llc_update.setOnClickListener {
            if(canClick&&!lottieIsShowing()){
                val packName = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).packageName
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/apps/details?id=$packName")
                }
                startActivity(intent)
            }
        }
        llc_share.setOnClickListener {
            if(canClick&&!lottieIsShowing()){
                val pm = packageManager
                val packageName=pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES).packageName
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = "text/plain"
                intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "https://play.google.com/store/apps/details?id=${packageName}"
                )
                startActivity(Intent.createChooser(intent, "share"))
            }
        }
    }

    private fun clickConnectBtn(){
        if(!canClick){
            return
        }
        LoadAd.load(LoadAd.CONNECT)
        LoadAd.load(LoadAd.RESULT_BOTTOM)
        canClick=false
        updateGuideUI(false)
        if(ConnectServer.isConnected()){
            startDisconnectServer()
        }else{
            updateServerInfoUI()
            if (getNetStatus()==1){
                showToast("Please check your network")
                canClick=true
                return
            }
            if (VpnService.prepare(this) != null) {
                permission = true
                registerResult.launch(null)
                return
            }

            startConnectServer()
        }
    }

    private fun startDisconnectServer(){
        updateConnectingUI()
        startConnectServerJob(false)
    }

    private fun startConnectServer(){
        updateConnectingUI()
        ConnectTimeManager.resetTime()
        startConnectServerJob(true)
    }

    private fun startConnectServerJob(connect:Boolean){
        time=0
        connectServerJob= GlobalScope.launch(Dispatchers.Main) {
//            var time = 0
            while (true) {
                if (!isActive) {
                    break
                }
                delay(1000)
                time++
                if (time==3){
                    if (connect){
                        ConnectServer.connect()
                    }else{
                        ConnectServer.disconnect()
                    }
                }

                if (time in 3..9){
                    if (connectServerSuccess(connect)){
                        if(LimitManger.hasLimit()){
                            cancel()
                            connectJobFinish(connect)
                        }
                        if(null!=LoadAd.getAd(LoadAd.CONNECT)){
                            cancel()
                            showOpenAd.showOpenAd(
                                showing = {
                                    connectJobFinish(connect,toResult = false)
                                },
                                close = {
                                    connectJobFinish(connect)
                                }
                            )
                        }
                    }
                }else if (time >= 10) {
                    cancel()
                    stopObjectAnimator()
                    connectJobFinish(connect)
                }
            }
        }
    }

    private fun connectServerSuccess(connect: Boolean)=if (connect) ConnectServer.isConnected() else ConnectServer.isDisconnected()

    private fun connectJobFinish(connect: Boolean,toResult:Boolean=true){
        runOnUiThread {
            if (connectServerSuccess(connect)){
                if (connect){
                    updateConnectedUI()
                }else{
                    updateStoppedUI()
                    updateServerInfoUI()
                }
                if (toResult&&AcRegister.isFront){
                    startActivity(Intent(this,ResultUI::class.java).apply {
                        putExtra("connect",connect)
                    })
                }
            }else{
                updateStoppedUI()
                showToast(if (connect) "Connect Fail" else "Disconnect Fail")
            }
            canClick=true
        }
    }

    private fun startObjectAnimator(){
        objectAnimator=ObjectAnimator.ofFloat(iv_connect_btn, "rotation", 0f, 360f).apply {
            duration=1000L
            repeatCount= ValueAnimator.INFINITE
            repeatMode=ObjectAnimator.RESTART
            start()
        }
    }

    private fun stopObjectAnimator(){
        objectAnimator?.cancel()
        objectAnimator=null
    }

    private fun updateConnectingUI(){
        tv_connect_time.isSelected=false
        iv_connect.setImageResource(R.drawable.home)
        iv_connect_btn.setImageResource(R.drawable.connecting)
        startObjectAnimator()
    }

    private fun updateConnectedUI(){
        stopObjectAnimator()
        tv_connect_time.isSelected=true
        iv_connect_btn.translationY=0F
        iv_connect_btn.setImageResource(R.drawable.connected)
        iv_connect.setImageResource(R.drawable.home2)
    }

    private fun updateStoppedUI(){
        stopObjectAnimator()
        tv_connect_time.isSelected=false
        iv_connect_btn.translationY=0F
        iv_connect_btn.setImageResource(R.drawable.connect)
        iv_connect.setImageResource(R.drawable.home)
        tv_connect_time.text="00:00:00"
    }

    private fun updateServerInfoUI(){
        val currentServer = ConnectServer.currentServer
        tv_name.text=currentServer.country
        iv_logo.setImageResource(getServerLogo(currentServer.country))
    }

    private fun updateBottomUI(){
        val homeSelect = bottomIndex == 0
        iv_home.isSelected=homeSelect
        iv_set.isSelected=!homeSelect
        layout_home.show(homeSelect)
        layout_set.show(!homeSelect)
    }

    override fun connectSuccess() {
        updateConnectedUI()
    }

    override fun disconnectSuccess() {
        if (canClick) {
            updateStoppedUI()
        }
    }

    override fun connectTime(time: String) {
        tv_connect_time.text=time
    }

    override fun onBackPressed() {
        if(canClick){
            if(OnlineConfig.showGuide&&lottieIsShowing()){
                updateGuideUI(false)
            }else{
                finish()
            }
        }
    }

    private fun updateGuideUI(show:Boolean){
        OnlineConfig.showGuide=show
        connecting_lottie_view.show(show)
        guide_view.show(show)
    }

    private fun lottieIsShowing()=connecting_lottie_view.visibility==View.VISIBLE

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==1000){
            when(data?.getStringExtra("result")){
                "duankai"->{
                    clickConnectBtn()
                }
                "lianjie"->{
                    updateServerInfoUI()
                    clickConnectBtn()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(LimitManger.canRefresh(LoadAd.HOME_BOTTOM)){
            showBottomAd.showBottomAd()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopObjectAnimator()
        connectServerJob?.cancel()
        connectServerJob=null
        ConnectServer.onDestroy()
        ConnectTimeManager.setInterface(this)
        showBottomAd.stopShow()
        AcRegister.setIAppFrontInterface(null)
        LimitManger.setRefreshStatus(LoadAd.HOME_BOTTOM,true)
    }

    override fun appFront(front: Boolean) {
//        if(!front&&time<3&&ConnectServer.isDisconnected()){
//            stopObjectAnimator()
//            updateStoppedUI()
//            connectServerJob?.cancel()
//            connectServerJob=null
//        }
    }
}