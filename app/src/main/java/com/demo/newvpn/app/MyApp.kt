package com.demo.newvpn.app

import android.app.ActivityManager
import android.app.Application
import com.demo.newvpn.HomeUI
import com.demo.newvpn.conf.OnlineConfig
import com.github.shadowsocks.Core
import com.tencent.mmkv.MMKV

class MyApp:Application() {
    override fun onCreate() {
        super.onCreate()
        Core.init(this,HomeUI::class)
        if (!packageName.equals(processName(this))){
            return
        }
        MMKV.initialize(this)
        OnlineConfig.getOnlineConfig()
        AcRegister.register(this)
    }

    private fun processName(applicationContext: Application): String {
        val pid = android.os.Process.myPid()
        var processName = ""
        val manager = applicationContext.getSystemService(Application.ACTIVITY_SERVICE) as ActivityManager
        for (process in manager.runningAppProcesses) {
            if (process.pid === pid) {
                processName = process.processName
            }
        }
        return processName
    }
}