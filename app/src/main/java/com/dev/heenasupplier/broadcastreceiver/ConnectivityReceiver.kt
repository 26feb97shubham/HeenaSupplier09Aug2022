package com.dev.heenasupplier.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import com.dev.heenasupplier.utils.Utility.Companion.isNetworkAvailable

class ConnectivityReceiver : BroadcastReceiver() {
    var connectivityReceiverListener: ConnectivityReceiverListener? = null
    override fun onReceive(context: Context, intent: Intent?) {
        val status: Boolean = isNetworkAvailable()
        if (connectivityReceiverListener != null) {
            connectivityReceiverListener!!.onNetworkConnectionChanged(status)
        }
    }
    interface ConnectivityReceiverListener {
        fun onNetworkConnectionChanged(isConnected: Boolean)
    }

    fun NetworkChangeReceiver(connectivityReceiverListener: ConnectivityReceiverListener?) {
        this.connectivityReceiverListener = connectivityReceiverListener
    }
}