package com.demo.newvpn

import com.demo.newvpn.admob.LoadAd
import com.demo.newvpn.admob.ShowBottomAd
import com.demo.newvpn.app.getServerLogo
import com.demo.newvpn.base.BaseUI
import com.demo.newvpn.interfaces.IConnectTimeInterface
import com.demo.newvpn.server.ConnectServer
import com.demo.newvpn.server.ConnectTimeManager
import com.demo.newvpn.util.LimitManger
import kotlinx.android.synthetic.main.activity_result.*

class ResultUI:BaseUI(), IConnectTimeInterface {
    private var connect=false
    private val showBottomAd by lazy { ShowBottomAd(this, LoadAd.RESULT_BOTTOM) }


    override fun layout(): Int = R.layout.activity_result

    override fun initView() {
        immersionBar.statusBarView(top).init()
        iv_back.setOnClickListener { finish() }

        connect=intent.getBooleanExtra("connect",false)
        item_layout.isSelected=connect
        tv_title.text=if (connect) "Connect success" else "Disconnected"
        tv_connect_time.isSelected=connect
        iv_logo.setImageResource(getServerLogo(ConnectServer.lastServer.country))
        if(connect){
            ConnectTimeManager.setInterface(this)
        }else{
            tv_connect_time.text=ConnectTimeManager.getTotalTime()
        }
    }
    override fun onResume() {
        super.onResume()
        if(LimitManger.canRefresh(LoadAd.RESULT_BOTTOM)){
            showBottomAd.showBottomAd()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(connect){
            ConnectTimeManager.setInterface(this)
        }
        showBottomAd.stopShow()
        LimitManger.setRefreshStatus(LoadAd.RESULT_BOTTOM,true)
    }

    override fun connectTime(time: String) {
        tv_connect_time.text=time
    }
}