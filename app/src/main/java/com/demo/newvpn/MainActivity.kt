package com.demo.newvpn

import android.animation.ValueAnimator
import android.content.Intent
import android.view.KeyEvent
import android.view.animation.LinearInterpolator
import androidx.core.animation.doOnEnd
import com.blankj.utilcode.util.ActivityUtils
import com.demo.newvpn.admob.LoadAd
import com.demo.newvpn.admob.ShowOpenAd
import com.demo.newvpn.base.BaseUI
import com.demo.newvpn.util.LimitManger
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseUI() {
    private var animator:ValueAnimator?=null

    private val showOpenAd by lazy {  ShowOpenAd(this,LoadAd.OPEN) }

    override fun layout(): Int = R.layout.activity_main

    override fun initView() {

        LimitManger.resetRefresh()
        LimitManger.readNum()
        LoadAd.preLoad()

        animator = ValueAnimator.ofInt(0, 100).apply {
            duration = 10000L
            interpolator = LinearInterpolator()
            addUpdateListener {
                val progress = it.animatedValue as Int
                progress_view.progress = progress
                val pro = (10 * (progress / 100.0F)).toInt()
                if(pro in 2..9){
                    showOpenAd.showOpenAd(
                        showing = {
                            stopAnimator()
                            progress_view.progress = 100
                        },
                        close = {
                            toHome()
                        }
                    )
                }else if (pro>=10){
                    toHome()
                }
            }
            start()
        }
    }

    private fun toHome(){
        val activityExistsInStack = ActivityUtils.isActivityExistsInStack(HomeUI::class.java)
        if (!activityExistsInStack){
            startActivity(Intent(this, HomeUI::class.java))
        }
        finish()
    }

    private fun stopAnimator(){
        animator?.removeAllUpdateListeners()
        animator?.cancel()
        animator=null
    }

    override fun onResume() {
        super.onResume()
        if (animator?.isPaused==true){
            animator?.resume()
        }
    }

    override fun onPause() {
        super.onPause()
        animator?.pause()

    }

    override fun onDestroy() {
        super.onDestroy()
        stopAnimator()
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode== KeyEvent.KEYCODE_BACK){
            return true
        }
        return false
    }
}