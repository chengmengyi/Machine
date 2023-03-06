package com.demo.newvpn.app

import android.content.Context
import android.net.ConnectivityManager
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.demo.newvpn.R


fun maLog(string: String){
    Log.e("qwer",string)
}

fun getServerLogo(name:String)=when(name){
    "Australia"->R.drawable.australia
    "Belgium"->R.drawable.belgium
    "Brazil"->R.drawable.brazil
    "Canada"->R.drawable.canada
    "France"->R.drawable.france
    "Germany"->R.drawable.germany
    "India"->R.drawable.india
    "Ireland"->R.drawable.ireland
    "Italy"->R.drawable.italy
    "KoreaSouth"->R.drawable.koreasouth
    "Netherlands"->R.drawable.netherlands
    "NewZealand"->R.drawable.newzealand
    "Norway"->R.drawable.norway
    "Singapore"->R.drawable.singapore
    "Sweden"->R.drawable.sweden
    "Switzerland"->R.drawable.switzerland
    "Turkey"->R.drawable.turkey
    "UnitedKingdom"->R.drawable.unitedkingdom
    "UnitedStates"->R.drawable.unitedstates
    else-> R.drawable.fast
}

fun View.show(show:Boolean){
    visibility=if (show) View.VISIBLE else View.GONE
}

fun Context.showToast(string: String){
    Toast.makeText(this,string,Toast.LENGTH_LONG).show()
}

fun Context.getNetStatus(): Int {
    val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val activeNetworkInfo = connectivityManager.activeNetworkInfo
    if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
        if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
            return 2
        } else if (activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE) {
            return 0
        }
    } else {
        return 1
    }
    return 1
}

fun Context.showNoNetDialog(){
    AlertDialog.Builder(this).apply {
        setMessage("You are not currently connected to the network")
        setPositiveButton("sure", null)
        show()
    }
}