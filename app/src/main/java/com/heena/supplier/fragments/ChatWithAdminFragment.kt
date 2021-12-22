package com.heena.supplier.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.heena.supplier.R
import com.heena.supplier.adapters.MessagesTypeAdapter
import com.heena.supplier.application.MyApp
import com.heena.supplier.application.MyApp.Companion.socket
import com.heena.supplier.models.HelpCategory
import com.heena.supplier.models.HelpSubCategory
import com.heena.supplier.models.Message
import com.heena.supplier.models.OldMessagesResponse
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.Companion.apiInterface
import io.socket.client.IO
import io.socket.client.Socket.*
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.fragment_chat_with_admin.view.*
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.Socket
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatWithAdminFragment : Fragment() {
    private var help_category : HelpCategory?=null
    private var sub_help_category : HelpSubCategory?=null
    private var mView : View?=null
    val TAG = ChatWithAdminFragment::class.java.simpleName
    lateinit var mSocket: io.socket.client.Socket
    lateinit var userName: String
    private var isConnected = true
    var currentDateTimeString: String? = null
    var timeCurrent: String? = null
    var date_time: String? = null
    private var room: String? = ""
    private var admin_id: String? = ""
    var type: String? = null
    private var directMessageStatus : Int = 1
    private var messagesList = ArrayList<Message>()
    private var messagesTypeAdapter:MessagesTypeAdapter?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            help_category = it.getSerializable("helpCategory") as HelpCategory?
            sub_help_category = it.getSerializable("subhelpCategory") as HelpSubCategory?
            room = it.getString("room")
            type = it.getString("type")
            admin_id = it.getString("admin_id")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_chat_with_admin, container, false)
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mView!!.title.text=help_category!!.title
        mView!!.tv_title.text=sub_help_category!!.title

        if (Utility.isNetworkAvailable()){
            initSocket()
        }
        currentDate

        mView!!.send_msg.setOnClickListener {
            if (mView!!.et_enter_message.text.toString().trim { it<=' '}.isNotEmpty() ||
                mView!!.et_enter_message.text.toString().trim { it<=' '}.equals("", true)){
                val strMessage = mView!!.et_enter_message.text.toString().trim { it<=' '}
                val messageObject = JSONObject()
                try {
                    messageObject.put("message",strMessage)
                    messageObject.put("room", "" + room)
                    messageObject.put("sender_id", ""+SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0])
                    messageObject.put("receiver_id", ""+admin_id)
                    messageObject.put("help_id",""+help_category!!.id )
                    messageObject.put("sub_help_id",""+sub_help_category!!.id )
                    messageObject.put("type", ""+type)
                    messageObject.put("message_type", "1")
                    messageObject.put("mime_type", "")
                    messageObject.put("created_at", "" + date_time)
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                mSocket!!.emit("chatMessage", messageObject)
                Log.e("emitMessage", messageObject.toString())
                mView!!.et_enter_message.setText("")
                if (type == "direct" && directMessageStatus == 0){
                    disableSendMsg()
                }
            }else {
                LogUtils.shortToast(requireContext(), "Please enter message")
            }
        }
    }

    private fun getOldMessageList() {
        val builder = APIClient.createBuilder(arrayOf("user_id","room"),
            arrayOf(SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0].toString(), room.toString()))
        val call = apiInterface.getOldMessageList(builder!!.build())
        call?.enqueue(object : Callback<OldMessagesResponse?> {
            override fun onResponse(
                call: Call<OldMessagesResponse?>,
                response: Response<OldMessagesResponse?>
            ) {
                if (response.isSuccessful){
                    messagesList = response.body()!!.messages as ArrayList<Message>
                    if (messagesList.size>0){
                        mView!!.msgs_list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                        messagesTypeAdapter = MessagesTypeAdapter(requireContext(),messagesList)
                        mView!!.msgs_list.adapter=messagesTypeAdapter
                        messagesTypeAdapter!!.notifyDataSetChanged()
                        scrollToBottom()

                    }else{
                        LogUtils.shortToast(requireContext(), "No Messages Found")
                    }
                }else{
                    LogUtils.shortToast(requireContext(), requireContext().getString(R.string.response_isnt_successful))
                }
            }

            override fun onFailure(call: Call<OldMessagesResponse?>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })
    }

    private val currentDate:
    //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Unit
        get() {
            val c = Calendar.getInstance()
            //SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            val hour = c[Calendar.HOUR_OF_DAY]
            val minute = c[Calendar.MINUTE]
            updateTime(hour, minute)
            val day = c[Calendar.DAY_OF_MONTH]
            val strday = String.format("%02d", day)
            val month = c[Calendar.MONTH] + 1
            val strmonth = String.format("%02d", month)
            val year = c[Calendar.YEAR]
            currentDateTimeString = "$strday-$strmonth-$year $timeCurrent"

            //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            date_time = sdf.format(Date())
        }

    private fun updateTime(hours_: Int, mins: Int) {
        var hours = hours_
        var timeSet = ""
        when {
            hours > 12 -> {
                hours -= 12
                timeSet = "PM"
            }
            hours == 0 -> {
                hours += 12
                timeSet = "AM"
            }
            hours == 12 -> timeSet = "PM"
            else -> timeSet = "AM"
        }
        var minutes = ""
        minutes = if (mins < 10) "0$mins" else mins.toString()

        // Append in a StringBuilder
        val aTime = StringBuilder().append(hours).append(':')
            .append(minutes).append(" ").append(timeSet).toString()
        timeCurrent = aTime
    }

    private fun initSocket() {
        val app = MyApp
        mSocket = app.socket!!
        val opts = IO.Options()
        opts.forceNew = true
        mSocket.disconnect()
        mSocket.off(EVENT_CONNECT, onConnect)
        mSocket.off(EVENT_DISCONNECT, onDisconnect)
        mSocket.off(EVENT_CONNECT_ERROR, onConnectError)
        mSocket.off(EVENT_CONNECT_TIMEOUT, onConnectError)
        mSocket.off("getMessage", GetChatMessage)
        mSocket.off("newChatMessage", NewChatMessage)

        mSocket.on(EVENT_CONNECT, onConnect)
        mSocket.on(EVENT_DISCONNECT, onDisconnect)
        mSocket.on(EVENT_CONNECT_ERROR, onConnectError)
        mSocket.on(EVENT_CONNECT_TIMEOUT, onConnectError)
        mSocket.on("getMessage", GetChatMessage)
        mSocket.on("newChatMessage", NewChatMessage)
        mSocket.connect()

        val joinRoom = JSONObject()
        try {
            joinRoom.put("userid", "" + SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0])
            joinRoom.put("room", room)
            getOldMessageList()
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        mSocket.emit("joinroom", joinRoom)
        Log.e("joinRoom", joinRoom.toString())

    }

    private val onConnect = Emitter.Listener {
        requireActivity().runOnUiThread {
            if (!isConnected) {
                isConnected = true
                Log.e("socketStatus", "isConnected")
                getOldMessageList()
            }
        }
    }
    private val onDisconnect = Emitter.Listener {
        requireActivity().runOnUiThread {
            Log.i(ContentValues.TAG, "disconnected")
            isConnected = false
            /* Toast.makeText(ChatActivity.this,
                            "disconnected", Toast.LENGTH_LONG).show();*/
        }
    }
    private val onConnectError = Emitter.Listener { args ->
        requireActivity().runOnUiThread {
            Log.d("ErrorConnecting", "" + args[0].toString())
            Toast.makeText(requireContext(), "connect to server", Toast.LENGTH_LONG).show()
        }
    }

    private val NewChatMessage = Emitter.Listener { args ->
        requireActivity().runOnUiThread {
            Log.d("NewChatMessage1", "" + args.size)

            Log.d("NewChatMessage", "a" + args[0] )
            val jsonObj = args[0].toString()
            try {
                val jsonObject = JSONObject(jsonObj)
                Log.e("roomData", jsonObj)
                val message = jsonObject.getString("message")
                val room = jsonObject.getString("room")
                val sender_id = jsonObject.getString("sender_id")
                val type = jsonObject.getString("type")
                val message_type = jsonObject.getString("message_type")
                val receiver_id = jsonObject.getString("receiver_id")
                val created_at = jsonObject.getString("created_at")
                val mime_type = jsonObject.getString("mime_type")

                addMessage_newMsg(chat_id = 0, sender_id.toInt(), receiver_id.toInt(), message = message, message_type, mime_type,room, created_at)

                if (receiver_id.toInt() == SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0]){
                    if (type == "direct" && directMessageStatus == 0){
                        directMessageStatus = 1
                        enableSendMsg()
                    }
                }

                updateMessageIsReadStatus()

            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }

    private fun disableSendMsg(){
        mView!!.llBottom.isEnabled = false
        mView!!.llBottom.isClickable = false
        mView!!.send_msg.isEnabled = false
        mView!!.share.isEnabled = false
        mView!!.send_msg.alpha = 0.5f
        mView!!.share.alpha = 0.5f
    }

    private fun enableSendMsg(){
        mView!!.llBottom.isEnabled = true
        mView!!.llBottom.isClickable = true
        mView!!.send_msg.isEnabled = true
        mView!!.share.isEnabled = true
        mView!!.send_msg.alpha = 1f
        mView!!.share.alpha = 1f
    }

    private fun addMessage_newMsg(
        chat_id: Int,
        sender_id: Int,
        receiver_id: Int,
        message: String,
        /*file: String,*/
        message_type: String,
        mime_type: String,
        chat_room: String,
        date_time: String
    ) {
        messagesList.add(Message(0,"",date_time,0,0,"",0,message,message_type, mime_type, "",receiver_id, chat_room, sender_id, 0,0,""))
        mView!!.msgs_list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        messagesTypeAdapter = MessagesTypeAdapter(requireContext(),messagesList)
        mView!!.msgs_list.adapter=messagesTypeAdapter
        messagesTypeAdapter!!.notifyItemInserted(messagesList.size-1)
        scrollToBottom()
//        messagesTypeAdapter!!.notifyItemInserted(messagesList.size-1)

    }

    private val GetChatMessage = Emitter.Listener { args ->
        requireActivity().runOnUiThread {
            Log.e("GetChatMessage", "" + args.size)

        }
    }

    private fun scrollToBottom() {
        /*   binding.recyclerView.scrollToPosition(adapter.itemCount - 1)*/
        mView!!.msgs_list.scrollToPosition(messagesTypeAdapter!!.itemCount-1)
    }
    override fun onResume() {
        super.onResume()
        if (Utility.hasConnection(requireContext()))
            initSocket()
    }

    public override fun onDestroy() {
        disconnectSocket()
        super.onDestroy()
    }

    private fun disconnectSocket(){
        if(Utility.hasConnection(requireContext()) && mSocket!=null) {
            mSocket!!.disconnect()
            mSocket!!.off(EVENT_CONNECT, onConnect)
            mSocket!!.off(EVENT_DISCONNECT, onDisconnect)
            mSocket!!.off(EVENT_CONNECT_ERROR, onConnectError)
            mSocket!!.off(EVENT_CONNECT_TIMEOUT, onConnectError)
            mSocket!!.off("getMessage", GetChatMessage)
            mSocket!!.off("newChatMessage", NewChatMessage)
            //mSocket!!.off("messages", NewChatMessage)
            //mSocket!!.off("blocked", BlockedUserListener)
        }
    }

    private fun updateMessageIsReadStatus(){
        if (Utility.isNetworkAvailable()) {
            val map: MutableMap<String, Any> = HashMap()
            map["api_key"] = "tribe123"
            map["user_id"] = "" + SharedPreferenceUtility.getInstance()[SharedPreferenceUtility.UserId, 0]
            map["room"] = "" + room
//            presenterList.updateMessageIsReadStatus(this@ChatActivity, map)
        }else{
//            simpleToast(this@ChatActivity, getString(R.string.no_network_message))
        }
    }

    companion object{
        private var instance: SharedPreferenceUtility? = null
        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }
    }
}