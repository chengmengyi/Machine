package com.demo.newvpn

import com.demo.newvpn.app.getServerLogo
import com.demo.newvpn.base.BaseUI
import com.demo.newvpn.interfaces.IConnectTimeInterface
import com.demo.newvpn.server.ConnectServer
import com.demo.newvpn.server.ConnectTimeManager
import kotlinx.android.synthetic.main.activity_result.*

class ResultUI:BaseUI(), IConnectTimeInterface {
    private var connect=false

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

    override fun onDestroy() {
        super.onDestroy()
        if(connect){
            ConnectTimeManager.setInterface(this)
        }
    }

    override fun connectTime(time: String) {
        tv_connect_time.text=time
    }
}