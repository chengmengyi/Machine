package com.demo.newvpn.app

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.util.ActivityUtils
import com.demo.newvpn.HomeUI
import com.demo.newvpn.MainActivity
import com.demo.newvpn.interfaces.IAppFrontInterface
import com.google.android.gms.ads.AdActivity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

object AcRegister {
    var isFront=true
    private var hotReload=false
    private var job: Job?=null
    private var iAppFrontInterface:IAppFrontInterface?=null

    fun register(application: Application){
        application.registerActivityLifecycleCallbacks(callback)
    }

    private val callback=object : Application.ActivityLifecycleCallbacks{
        private var pages=0
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

        override fun onActivityStarted(activity: Activity) {
            pages++
            job?.cancel()
            job=null
            if (pages==1){
                isFront=true
                iAppFrontInterface?.appFront(true)
                if (hotReload){
                    if (ActivityUtils.isActivityExistsInStack(HomeUI::class.java)){
                        activity.startActivity(Intent(activity, MainActivity::class.java))
                    }
                }
                hotReload=false
            }
        }

        override fun onActivityResumed(activity: Activity) {}

        override fun onActivityPaused(activity: Activity) {}

        override fun onActivityStopped(activity: Activity) {
            pages--
            if (pages<=0){
                isFront=false
                iAppFrontInterface?.appFront(false)
                job= GlobalScope.launch {
                    delay(3000L)
                    hotReload=true
                    ActivityUtils.finishActivity(MainActivity::class.java)
                    ActivityUtils.finishActivity(AdActivity::class.java)
                }
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

        override fun onActivityDestroyed(activity: Activity) {}
    }

    fun setIAppFrontInterface(iAppFrontInterface:IAppFrontInterface?){
        this.iAppFrontInterface=iAppFrontInterface
    }
}