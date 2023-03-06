package com.demo.newvpn.util

import com.tencent.mmkv.MMKV
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

object LimitManger {

    private var machinesm=50
    private var machinecm=1

    private var cc=0
    private var cs=0

    private val refresh= hashMapOf<String,Boolean>()

    fun resetRefresh(){
        refresh.clear()
    }

    fun canRefresh(machinetype:String)=refresh[machinetype]?:true

    fun setRefreshStatus(machinetype:String,boolean: Boolean){
        refresh[machinetype]=boolean
    }

    fun setNum(string: String){
        try{
            val jsonObject = JSONObject(string)
            machinesm=jsonObject.optInt("machinesm")
            machinecm=jsonObject.optInt("machinecm")
        }catch (e:Exception){

        }
    }

    fun readNum(){
        cc= MMKV.defaultMMKV().decodeInt(key("machinecm"),0)
        cs= MMKV.defaultMMKV().decodeInt(key("machinesm"),0)
    }

    fun updatecc(){
        cc++
        MMKV.defaultMMKV().encode(key("machinecm"), cc)
    }

    fun updatecs(){
        cs++
        MMKV.defaultMMKV().encode(key("machinesm"), cs)
    }

    fun hasLimit()= cc>= machinecm|| cs>= machinesm

    private fun key(string:String)="${string}...${SimpleDateFormat("yyyy-MM-dd").format(Date(System.currentTimeMillis()))}"
}