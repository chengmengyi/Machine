package com.demo.newvpn.conf

import com.demo.newvpn.bean.ServerBean
import com.demo.newvpn.server.ServerInfoManger
import com.demo.newvpn.util.LimitManger
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.tencent.mmkv.MMKV
import org.json.JSONArray
import org.json.JSONObject

object OnlineConfig {
    var isLimitUser=false
    var showGuide=true

    fun getOnlineConfig(){
        checkIsLimitUser()
        parseLocalServer()
//        val remoteConfig = Firebase.remoteConfig
//        remoteConfig.fetchAndActivate().addOnCompleteListener {
//            if (it.isSuccessful){
//                saveAdJson(remoteConfig.getString("machineAd"))
//            }
//        }
    }

    private fun saveAdJson(string: String){
        MMKV.defaultMMKV().encode("machineAd",string)
        LimitManger.setNum(string)
    }

    fun getAdStr():String{
        val machineAd = MMKV.defaultMMKV().decodeString("machineAd") ?: ""
        if(machineAd.isNullOrEmpty()){
            return LocalConfig.localAd
        }
        return machineAd
    }

    private fun parseLocalServer(){
        try {
            val jsonArray = JSONArray(LocalConfig.localServer)
            for (index in 0 until jsonArray.length()){
                val jsonObject = jsonArray.getJSONObject(index)
                ServerInfoManger.localServerList.add(
                    ServerBean(
                        pwd = jsonObject.optString("machine_ma"),
                        account = jsonObject.optString("machine_hao"),
                        port = jsonObject.optInt("machine_kou"),
                        country =jsonObject.optString("machine_ji"),
                        city =jsonObject.optString("machine_ty"),
                        ip=jsonObject.optString("machine_ip")
                    )
                )
            }
            ServerInfoManger.localServerList.forEach { it.writeServerId() }
        }catch (e:Exception){

        }
    }

    private fun checkIsLimitUser(){
        OkGo.get<String>("https://api.myip.com/")
            .execute(object : StringCallback(){
                override fun onSuccess(response: Response<String>?) {
//                        ipJson="""{"ip":"89.187.185.11","country":"United States","cc":"IR"}"""
                    try {
                        isLimitUser = JSONObject(response?.body()?.toString()).optString("cc").limitArea()
                    }catch (e:Exception){

                    }
                }
            })
    }

    private fun String.limitArea()=contains("IR")||contains("MO")||contains("HK")||contains("CN")
}