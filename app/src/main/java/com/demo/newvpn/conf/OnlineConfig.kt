package com.demo.newvpn.conf

import com.demo.newvpn.bean.ServerBean
import com.demo.newvpn.server.ServerInfoManger
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import org.json.JSONArray
import org.json.JSONObject

object OnlineConfig {
    var isLimitUser=false
    var showGuide=true

    fun getOnlineConfig(){
        checkIsLimitUser()
        parseLocalServer()
    }

    private fun parseLocalServer(){
        try {
            val jsonArray = JSONArray(LocalConfig.localServer)
            for (index in 0 until jsonArray.length()){
                val jsonObject = jsonArray.getJSONObject(index)
                ServerInfoManger.localServerList.add(
                    ServerBean(
                        pwd = jsonObject.optString("pwd"),
                        account = jsonObject.optString("account"),
                        port = jsonObject.optInt("port"),
                        country =jsonObject.optString("country"),
                        city =jsonObject.optString("city"),
                        ip=jsonObject.optString("ip")
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