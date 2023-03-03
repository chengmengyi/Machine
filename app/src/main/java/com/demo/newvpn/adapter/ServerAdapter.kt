package com.demo.newvpn.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.demo.newvpn.R
import com.demo.newvpn.app.getServerLogo
import com.demo.newvpn.bean.ServerBean
import com.demo.newvpn.server.ConnectServer
import com.demo.newvpn.server.ServerInfoManger
import kotlinx.android.synthetic.main.item_server.view.*

class ServerAdapter(
    private val context: Context,
    private val click:(bean:ServerBean)->Unit
):RecyclerView.Adapter<ServerAdapter.ServerView>() {
    private val list= arrayListOf<ServerBean>()

    init {
        list.add(ServerBean())
        list.addAll(ServerInfoManger.getAllList())
    }

    inner class ServerView(view:View):RecyclerView.ViewHolder(view){
        init {
            view.setOnClickListener { click.invoke(list[layoutPosition]) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerView {
        return ServerView(LayoutInflater.from(context).inflate(R.layout.item_server,parent,false))
    }

    override fun onBindViewHolder(holder: ServerView, position: Int) {
        with(holder.itemView){
            val serverBean = list[position]
            tv_name.text=serverBean.country
            iv_logo.setImageResource(getServerLogo(serverBean.country))
            item_layout.isSelected=ConnectServer.currentServer.country==serverBean.country
        }
    }

    override fun getItemCount(): Int = list.size
}