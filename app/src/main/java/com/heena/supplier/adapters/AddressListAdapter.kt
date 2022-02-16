package com.heena.supplier.adapters

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.heena.supplier.R
import com.heena.supplier.`interface`.ClickInterface
import com.heena.supplier.models.AddressItem
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import kotlinx.android.synthetic.main.saved_address_listing.view.*

class AddressListAdapter(
    private val context: Context, private val addressList : ArrayList<AddressItem>,
    private val OnAddressItemClick : ClickInterface.OnAddressItemClick
): RecyclerView.Adapter<AddressListAdapter.AddressListAdapterVH>() {
    inner class AddressListAdapterVH(val itemView : View) : RecyclerView.ViewHolder(itemView){
        fun bind(addressItem: AddressItem, position: Int) {
            if(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.SelectedLang, "").toString().equals("ar")){
                itemView.tv_address_title.gravity = Gravity.END
                itemView.tv_address.gravity = Gravity.END
            }
            itemView.tv_address_title.text = addressItem.title
            itemView.tv_address.text = addressItem.flat + "," + addressItem.street

            itemView.tv_address_edit.setSafeOnClickListener {
                OnAddressItemClick.editAdddress(position)
            }

            itemView.tv_address_delete.setSafeOnClickListener {
                OnAddressItemClick.deleteAddress(position)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddressListAdapterVH {
        val mView = LayoutInflater.from(context).inflate(R.layout.saved_address_listing, parent, false)
        return AddressListAdapterVH(mView)
    }

    override fun onBindViewHolder(holder: AddressListAdapterVH, position: Int) {
        holder.bind(addressList[position], position)
    }

    override fun getItemCount(): Int {
        return addressList.size
    }

}