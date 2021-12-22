package com.heena.supplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.models.Message
import com.heena.supplier.utils.Utility.Companion.message_type
import kotlinx.android.synthetic.main.row_chat_partner.view.*
import kotlinx.android.synthetic.main.row_chat_user.view.message

class MessagesTypeAdapter(
    private val context : Context,
    private val messageList : ArrayList<Message>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (message_type) {
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
            itemView.message.text=message.message
        }

    }

    inner class AdminChatAdapterVH(private val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(message: Message, position: Int, context: Context) {
            itemView.message_admin.text=message.message
        }
    }

    override fun getItemViewType(position: Int): Int {
        return message_type
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messageList[position]
        if (message.id<message.sender_id){
            message_type=1
        }else{
            message_type=2
        }
        when (message_type) {
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