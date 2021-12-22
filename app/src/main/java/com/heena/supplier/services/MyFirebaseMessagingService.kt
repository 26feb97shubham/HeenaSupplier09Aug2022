package com.heena.supplier.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.heena.supplier.R
import com.heena.supplier.activities.HomeActivity
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.json.JSONException
import org.json.JSONObject

class MyFirebaseMessagingService : FirebaseMessagingService() {
    var channelId = "channel1"
    var channelName = "Channeln1"
    lateinit var broadcaster: LocalBroadcastManager

    override fun onCreate() {
        super.onCreate()
        broadcaster = LocalBroadcastManager.getInstance(this)
    }

    override fun onNewToken(s: String) {
        super.onNewToken(s)
        LogUtils.d("token", "token $s")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        if(remoteMessage.notification!=null){
            val messageData = remoteMessage.data.toString().replace("data=", "data:")
            LogUtils.e("message", "message $messageData")
            if(SharedPreferenceUtility.getInstance().get(SharedPreferenceUtility.UserId, 0) !=0){
                /*val intent =  Intent("MyData")
               broadcaster.sendBroadcast(intent)*/
                makeNotification(messageData)
            }
        }else{
            LogUtils.e("message", "no notification found")
        }

    }

    private fun makeNotification(messageData: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val attributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            val mChannel = NotificationChannel(
                channelId, channelName, importance)
            mChannel.setSound(soundUri, attributes)
            mChannel.enableLights(true)
            mChannel.enableVibration(true)
            mChannel.setShowBadge(true)
            notificationManager.createNotificationChannel(mChannel)
        }
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
        val jsonObject: JSONObject
        try {
            jsonObject = JSONObject(messageData)
            val jsonObject1 = jsonObject.getJSONObject("data")
            val title = jsonObject1.getString("title")
            builder.setContentTitle(title)
            // builder.setSubText("Tap to view the website.");
            val stackBuilder = TaskStackBuilder.create(this)

            val bodyObject = jsonObject1.getJSONObject("body")
            builder.setContentText(bodyObject.getString("msg"))


            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("type", bodyObject.getString("type"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            stackBuilder.addNextIntent(intent)

            val resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.setContentIntent(resultPendingIntent)


            notificationManager.notify(1, builder.build())
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }
}