package com.demo.newvpn.bean

import com.github.shadowsocks.database.Profile
import com.github.shadowsocks.database.ProfileManager

class ServerBean(
    val pwd:String="",
    val account:String="",
    val port:Int=0,
    val country:String="Smart Servers",
    val city:String="",
    val ip:String=""
) {



    fun isSuperFast()=country=="Smart Servers"&&ip.isEmpty()

    fun getServerId():Long{
        ProfileManager.getActiveProfiles()?.forEach {
            if (it.host==ip&&it.remotePort==port){
                return it.id
            }
        }
        return 0L
    }

    fun writeServerId(){
        val profile = Profile(
            id = 0L,
            name = "$country - $city",
            host = ip,
            remotePort = port,
            password = pwd,
            method = account
        )

        var id:Long?=null
        ProfileManager.getActiveProfiles()?.forEach {
            if (it.remotePort==profile.remotePort&&it.host==profile.host){
                id=it.id
                return@forEach
            }
        }
        if (null==id){
            ProfileManager.createProfile(profile)
        }else{
            profile.id=id!!
            ProfileManager.updateProfile(profile)
        }
    }

    override fun toString(): String {
        return "ServerBean(pwd='$pwd', account='$account', port=$port, country='$country', city='$city', ip='$ip')"
    }
}