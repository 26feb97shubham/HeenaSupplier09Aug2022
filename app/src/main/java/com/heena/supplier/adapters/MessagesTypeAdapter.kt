package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.heena.supplier.R
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.models.Message
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility.sender_admin
import kotlinx.android.synthetic.main.row_chat_partner.view.*
import kotlinx.android.synthetic.main.row_chat_user.view.*

class MessagesTypeAdapter(
    private val context : Context,
    private val messageList : ArrayList<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (sender_admin) {
            1 -> {
                val view  =
                    LayoutInflater.from(context).inflate(R.layout.row_chat_user, parent, false)
                UserChatAdapterVH(view)
            }
            2 -> {
                val view  =
                    LayoutInflater.from(context).inflate(R.layout.row_chat_partner, parent, false)
                AdminChatAdapterVH(view)
            }
            else -> {
                val view  =
                    LayoutInflater.from(context).inflate(R.layout.row_chat_user, parent, false)
                UserChatAdapterVH(view)
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    inner class UserChatAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(message: Message, position: Int, context: Context) {
            if (message.message_type.equals("1")){
                itemView.card_image.visibility = View.GONE
                itemView.message.visibility = View.VISIBLE
                itemView.message.text=message.message
            }else{
                itemView.card_image.visibility = View.VISIBLE
                itemView.message.visibility = View.GONE
                Glide.with(context).load(message.message).into(itemView.post_image)
            }
        }

    }

    inner class AdminChatAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(message: Message, position: Int, context: Context) {
            if (message.message_type.equals("1")){
                itemView.card_image_admin.visibility = View.GONE
                itemView.message_admin.visibility = View.VISIBLE
                itemView.message_admin.text=message.message
            }else{
                itemView.card_image_admin.visibility = View.VISIBLE
                itemView.message_admin.visibility = View.GONE
                Glide.with(context).load(message.message).into(itemView.post_image_admin)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if (messageList[position].sender_id==sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0]){
            sender_admin = 1
        }else{
            sender_admin = 2
        }
        return sender_admin
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        val booking_item_type =
            when (messageList[position].sender_id) {
                sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0] -> {
                    1
                }
                else -> {
                    2
                }
            }
        when (booking_item_type) {
            1 -> {
                val userchatholder = holder as UserChatAdapterVH
                userchatholder.bind(message, position, context)
            }
            2 -> {
                val adminchatholder = holder as AdminChatAdapterVH
                adminchatholder.bind(message, position, context)
            }
            else -> {
                val userchatholder = holder as UserChatAdapterVH
                userchatholder.bind(message, position, context)
            }
        }
    }
}