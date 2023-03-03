package com.demo.newvpn.server

import android.util.Log
import com.demo.newvpn.base.BaseUI
import com.demo.newvpn.bean.ServerBean
import com.demo.newvpn.interfaces.IConnectServerInterface
import com.github.shadowsocks.Core
import com.github.shadowsocks.aidl.IShadowsocksService
import com.github.shadowsocks.aidl.ShadowsocksConnection
import com.github.shadowsocks.bg.BaseService
import com.github.shadowsocks.preference.DataStore
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object ConnectServer: ShadowsocksConnection.Callback {
    private var baseUI:BaseUI?=null
    private var state = BaseService.State.Stopped
    var currentServer= ServerBean()
    var lastServer=ServerBean()
    var fastServer=ServerBean()
    private val sc= ShadowsocksConnection(true)
    private var iConnectServerInterface: IConnectServerInterface?=null

    fun init(baseUI:BaseUI,iConnectServerInterface: IConnectServerInterface){
        this.baseUI=baseUI
        this.iConnectServerInterface=iConnectServerInterface
        sc.connect(baseUI,this)
    }

    fun connect(){
        state= BaseService.State.Connecting
        GlobalScope.launch {
            if (currentServer.isSuperFast()){
                fastServer=ServerInfoManger.getFastServer()
                Log.e("qwer", fastServer.toString())
                DataStore.profileId = fastServer.getServerId()
            }else{
                DataStore.profileId = currentServer.getServerId()
            }
            Core.startService()
        }
    }

    fun disconnect(){
        state= BaseService.State.Stopping
        GlobalScope.launch {
            Core.stopService()
        }
    }

    fun isConnected()= state== BaseService.State.Connected

    fun isDisconnected()= state== BaseService.State.Stopped

    override fun stateChanged(state: BaseService.State, profileName: String?, msg: String?) {
        this.state=state
        if (isConnected()){
            lastServer= currentServer
            ConnectTimeManager.start()
        }
        if (isDisconnected()){
            ConnectTimeManager.end()
            iConnectServerInterface?.disconnectSuccess()
        }
    }

    override fun onServiceConnected(service: IShadowsocksService) {
        val state = BaseService.State.values()[service.state]
        this.state=state
        if (isConnected()){
            lastServer= currentServer
            ConnectTimeManager.start()
            iConnectServerInterface?.connectSuccess()
        }
    }

    override fun onBinderDied() {
        baseUI?.let {
            sc.disconnect(it)
        }
    }

    fun onDestroy(){
        onBinderDied()
        baseUI=null
        iConnectServerInterface=null
    }
}