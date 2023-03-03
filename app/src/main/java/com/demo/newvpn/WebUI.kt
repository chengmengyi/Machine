package com.demo.newvpn

import com.demo.newvpn.base.BaseUI
import com.demo.newvpn.conf.LocalConfig
import kotlinx.android.synthetic.main.activity_web.*

class WebUI:BaseUI() {
    override fun layout(): Int = R.layout.activity_web

    override fun initView() {
        immersionBar.statusBarView(top).init()
        iv_back.setOnClickListener { finish() }

        web_view.apply {
            settings.javaScriptEnabled=true
            loadUrl(LocalConfig.url)
        }
    }
}