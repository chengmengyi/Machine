package com.demo.newvpn

import android.content.Intent
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.demo.newvpn.adapter.ServerAdapter
import com.demo.newvpn.admob.LoadAd
import com.demo.newvpn.admob.ShowOpenAd
import com.demo.newvpn.base.BaseUI
import com.demo.newvpn.bean.ServerBean
import com.demo.newvpn.server.ConnectServer
import kotlinx.android.synthetic.main.activity_choose_server.*

class ChooseServerUI:BaseUI() {
    private val showOpenAd by lazy { ShowOpenAd(this,LoadAd.BACK) }

    override fun layout(): Int = R.layout.activity_choose_server

    override fun initView() {
        LoadAd.load(LoadAd.BACK)
        immersionBar.statusBarView(top).init()
        rv_list.apply {
            layoutManager=LinearLayoutManager(this@ChooseServerUI)
            adapter=ServerAdapter(this@ChooseServerUI){ clickItem(it) }
        }
        iv_back.setOnClickListener { onBackPressed() }
    }

    private fun clickItem(serverBean: ServerBean){
        val current = ConnectServer.currentServer
        val connected = ConnectServer.isConnected()
        if(connected&&current.ip!=serverBean.ip){
            AlertDialog.Builder(this).apply {
                setMessage("If you want to connect to another VPN, you need to disconnect the current connection first. Do you want to disconnect the current connection?")
                setPositiveButton("sure") { _, _ ->
                    chooseBack(serverBean,"duankai")
                }
                setNegativeButton("cancel",null)
                show()
            }
        }else{
            if (connected){
                chooseBack(serverBean,"")
            }else{
                chooseBack(serverBean,"lianjie")
            }
        }
    }

    private fun chooseBack(serverBean: ServerBean,result:String){
        ConnectServer.currentServer=serverBean
        setResult(1000, Intent().apply {
            putExtra("result",result)
        })
        finish()
    }

    override fun onBackPressed() {
        showOpenAd.showOpenAd(
            back = true,
            showing = {},
            close = {
                finish()
            }
        )
    }
}