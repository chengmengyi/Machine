package com.demo.newvpn.server

import com.demo.newvpn.bean.ServerBean

object ServerInfoManger {

    val cityList= arrayListOf<String>()
    val localServerList= arrayListOf<ServerBean>()
    val onlineServerList= arrayListOf<ServerBean>()

    fun getAllList()=onlineServerList.ifEmpty { localServerList }

    fun getFastServer():ServerBean{
        val serverList = getAllList()
        if (!cityList.isNullOrEmpty()){
            val filter = serverList.filter { cityList.contains(it.city) }
            if (!filter.isNullOrEmpty()){
                return filter.random()
            }
        }
        return serverList.random()
    }
}