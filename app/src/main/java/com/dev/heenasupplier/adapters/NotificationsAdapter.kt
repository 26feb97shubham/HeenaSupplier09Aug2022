package com.dev.heenasupplier.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import androidx.recyclerview.widget.RecyclerView
import com.dev.heenasupplier.R
import com.dev.heenasupplier.`interface`.ClickInterface
import com.dev.heenasupplier.models.Notification
import kotlinx.android.synthetic.main.item_notifications_list.view.*

class NotificationsAdapter(private val context: Context, private val data:ArrayList<Notification>): RecyclerView.Adapter<NotificationsAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_notifications_list, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.itemView.txtTitle.text = data[position].title
        holder.itemView.txtDetails.text = data[position].description
        holder.itemView.txtTime.text = data[position].create_at
        /*if(data[position].seen==0){
            holder.itemView.imgNotWell.setImageResource(R.drawable.not_well_01)
        }
        else{
            holder.itemView.imgNotWell.setImageResource(R.drawable.not05)
        }*/

    }

    override fun getItemCount(): Int {
        return data.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}