package com.heena.supplier.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.arasthel.spannedgridlayoutmanager.BuildConfig
import com.heena.supplier.BuildConfig.APPLICATION_ID
import com.heena.supplier.R
import com.heena.supplier.adapters.MessagesTypeAdapter
import com.heena.supplier.application.MyApp
import com.heena.supplier.application.MyApp.Companion.sharedPreferenceInstance
import com.heena.supplier.application.MyApp.Companion.socket
import com.heena.supplier.custom.FetchPath
import com.heena.supplier.models.*
import com.heena.supplier.rest.APIClient
import com.heena.supplier.utils.LogUtils
import com.heena.supplier.utils.SharedPreferenceUtility
import com.heena.supplier.utils.Utility
import com.heena.supplier.utils.Utility.apiInterface
import com.heena.supplier.utils.Utility.setSafeOnClickListener
import io.socket.client.IO
import io.socket.client.Socket.*
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_home2.*
import kotlinx.android.synthetic.main.fragment_chat_with_admin.view.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
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

    var status = 0
    private var uri: Uri? = null
    private var imagePath = ""
    private var chat_file = ""
    private var mime_type = ""
    private var image_url = ""
    private val PICK_IMAGE_FROM_GALLERY = 10
    private val MEDIA_TYPE_IMAGE = 1
    private val CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100
    private var message_type = ""
    private var is_share_clicked = false

    private val PERMISSIONS =  arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)


    private var activityResultLauncher: ActivityResultLauncher<Array<String>> =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()) { result ->
            var allAreGranted = true
            for(b in result.values) {
                allAreGranted = allAreGranted && b
            }
            if(allAreGranted) {
                Log.e("Granted", "Permissions")
                openCameraDialog()
            }else{
                Utility.showSnackBarValidationError(mView!!.chatWithAdminFragmentConstraintLayout,
                    requireContext().getString(R.string.please_allow_permissions),
                    requireContext())
            }
        }

    private var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { it ->
        if (status == CAMERA_CAPTURE_IMAGE_REQUEST_CODE){
            if (it.resultCode == Activity.RESULT_OK){
                if (uri != null) {
                    imagePath = ""
                    Log.e("uri", uri.toString())
                    imagePath = uri!!.path!!
                    Log.e("image_path", imagePath)
                    /* Perform API For Image Upload*/
                    is_share_clicked = true
                    postImage(imagePath, room)
                } else {
                    Utility.showSnackBarValidationError(mView!!.chatWithAdminFragmentConstraintLayout,
                        requireContext().getString(R.string.something_went_wrong),
                        requireContext())
                }
            }
        }else if (status.equals(PICK_IMAGE_FROM_GALLERY)){
            if (it.resultCode== Activity.RESULT_OK){
                val data: Intent? = it.data
                if (data!!.data != null) {
                    imagePath = ""
                    val uri = data.data
                    (if (uri.toString().startsWith("content")) {
                        FetchPath.getPath(requireContext(), uri!!)!!
                    } else {
                        uri!!.path!!
                    }).also { imagePath = it }
                    Log.e("image_path", imagePath)
                    /* Perform API For Image Upload*/
                    is_share_clicked = true
                    postImage(imagePath, room)
                }
            }
        }
    }

    private fun openCameraDialog() {
        val items = arrayOf<CharSequence>(getString(R.string.camera), getString(R.string.gallery), getString(R.string.cancel))
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.add_photo))
        builder.setItems(items) { dialogInterface, i ->
            when {
                items[i] == getString(R.string.camera) -> {
                    captureImage()
                }
                items[i] == getString(R.string.gallery) -> {
                    chooseImage()
                }
                items[i] == getString(R.string.cancel) -> {
                    dialogInterface.dismiss()
                }
            }
        }
        builder.show()
    }

    private fun chooseImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        status = PICK_IMAGE_FROM_GALLERY
        resultLauncher.launch(intent)
    }


    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        uri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        status = CAMERA_CAPTURE_IMAGE_REQUEST_CODE
        resultLauncher.launch(intent)
    }


    fun getOutputMediaFileUri(type: Int): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(requireContext(),
                "${APPLICATION_ID}.provider", getOutputMediaFile(type)!!)
        } else {
            Uri.fromFile(getOutputMediaFile(type))
        }
    }

    private fun getOutputMediaFile(type: Int): File? {
        val mediaStorageDir = File(
            Environment
                .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), Utility.IMAGE_DIRECTORY_NAME
        )
        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            mediaStorageDir.mkdirs()
        }
        // Create a media file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss",
            Locale.getDefault()).format(Date())
        val mediaFile: File = when (type) {
            MEDIA_TYPE_IMAGE -> {
                File(mediaStorageDir.path + File.separator
                        + "IMG_" + timeStamp + ".png")
            }
            MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO -> {
                File(mediaStorageDir.path + File.separator
                        + "VID_" + timeStamp + ".mp4")
            }
            else -> {
                return null
            }
        }
        return mediaFile
    }

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
        Utility.changeLanguage(
            requireContext(),
            sharedPreferenceInstance!!.get(SharedPreferenceUtility.SelectedLang, "")
        )
        return mView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().iv_back.setSafeOnClickListener {
            requireActivity().iv_back.startAnimation(
                AlphaAnimation(1f, 0.5f)
            )
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_back)
            findNavController().popBackStack()
        }

        requireActivity().iv_notification.setSafeOnClickListener {
            requireActivity().iv_notification.startAnimation(AlphaAnimation(1F,0.5F))
            sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(), requireActivity().iv_notification)
            findNavController().navigate(R.id.notificationsFragment)
        }

        mView!!.title.text=help_category!!.title
        mView!!.tv_title.text=sub_help_category!!.title

        if (Utility.isNetworkAvailable()){
            initSocket()
            getOldMessageList()
        }
        currentDate

        mView!!.send_msg.setSafeOnClickListener {
            if (mView!!.et_enter_message.text.toString().trim { it<=' '}.isNotEmpty() ||
                !mView!!.et_enter_message.text.toString().trim { it<=' '}.equals("", true)){
                val strMessage = mView!!.et_enter_message.text.toString().trim { it<=' '}
                message_type = "1"
                is_share_clicked = false
                sharedPreferenceInstance!!.hideSoftKeyBoard(requireContext(),mView!!.send_msg)

                val messageObject = JSONObject()
                try {
                    messageObject.put("message",strMessage)
                    messageObject.put("room", "" + room)
                    messageObject.put("sender_id", ""+sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0])
                    messageObject.put("receiver_id", ""+admin_id)
                    messageObject.put("help_id",""+help_category!!.id )
                    messageObject.put("sub_help_id",""+sub_help_category!!.id )
                    messageObject.put("type", ""+type)
                    messageObject.put("message_type", message_type)
                    messageObject.put("mime_type", mime_type)
                    messageObject.put("url", image_url)
                    messageObject.put("created_at", "" + date_time)
                    mSocket.emit("chatMessage", messageObject)
                    Log.e("emitMessage", messageObject.toString())
                    mView!!.et_enter_message.setText("")
                    if (type == "direct" && directMessageStatus == 0){
                        disableSendMsg()
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }else {
                Utility.showSnackBarValidationError(mView!!.chatWithAdminFragmentConstraintLayout,
                    requireContext().getString(R.string.message_field_cannot_be_empty),
                    requireContext())
            }
        }

        mView!!.share.setSafeOnClickListener {
            mView!!.share.startAnimation(AlphaAnimation(1f, 0.5f))
            activityResultLauncher.launch(PERMISSIONS)
        }

        mView!!.imgRight.setSafeOnClickListener {
            mView!!.imgRight.visibility = View.GONE
            mView!!.imgDown.visibility = View.VISIBLE
            mView!!.sub_main_card.visibility = View.GONE
        }

        mView!!.imgDown.setSafeOnClickListener {
            mView!!.imgRight.visibility = View.VISIBLE
            mView!!.imgDown.visibility = View.GONE
            mView!!.sub_main_card.visibility = View.VISIBLE
        }
    }

    private fun getOldMessageList() {
        mView!!.frag_chat_progressBar.visibility = View.VISIBLE
        val builder = APIClient.createBuilder(arrayOf("user_id","room", "lang"),
            arrayOf(sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0].toString(), room.toString(), sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))
        val call = apiInterface.getOldMessageList(builder.build())
        call?.enqueue(object : Callback<OldMessagesResponse?> {
            override fun onResponse(
                call: Call<OldMessagesResponse?>,
                response: Response<OldMessagesResponse?>
            ) {
                mView!!.frag_chat_progressBar.visibility = View.GONE
                if (response.isSuccessful){
                    if (response.body()!=null){
                        messagesList.clear()
                        messagesList = response.body()!!.messages as ArrayList<Message>
                        if (messagesList.size>0){
                            mView!!.msgs_list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                            messagesTypeAdapter = MessagesTypeAdapter(requireContext(),messagesList)
                            mView!!.msgs_list.adapter=messagesTypeAdapter
                            scrollToBottom()
                        }else{
                            Utility.showSnackBarOnResponseError(mView!!.chatWithAdminFragmentConstraintLayout,
                                requireContext().getString(R.string.no_messages_found),
                                requireContext())
                        }
                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.chatWithAdminFragmentConstraintLayout,
                            response.body()!!.message,
                            requireContext())
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.chatWithAdminFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<OldMessagesResponse?>, t: Throwable) {
                mView!!.frag_chat_progressBar.visibility = View.GONE
                Utility.showSnackBarOnResponseError(mView!!.chatWithAdminFragmentConstraintLayout,
                    t.message!!,
                    requireContext())
            }

        })
    }

    private val currentDate:
    //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Unit
        get() {
            Locale.setDefault(Locale.ENGLISH)
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
            joinRoom.put("userid", "" + sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0])
            joinRoom.put("room", room)
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
            }
        }
    }
    private val onDisconnect = Emitter.Listener {
        requireActivity().runOnUiThread {
            Log.i(ContentValues.TAG, "disconnected")
            isConnected = false
        }
    }
    private val onConnectError = Emitter.Listener { args ->
        requireActivity().runOnUiThread {
            Log.d("ErrorConnecting", "" + args[0].toString())
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
                val message_type = jsonObject.getString("message_type")
                val receiver_id = jsonObject.getString("receiver_id")
                val created_at = jsonObject.getString("created_at")
                val mime_type = jsonObject.getString("mime_type")

                addMessage_newMsg(chat_id = 0, sender_id.toInt(), receiver_id.toInt(), message = message, message_type, mime_type,room, created_at)

                if (receiver_id.toInt() == sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0]){
                    if (type == "direct" && directMessageStatus == 0){
                        directMessageStatus = 1
                        enableSendMsg()
                    }
                }

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
        message_type: String,
        mime_type: String,
        chat_room: String,
        date_time: String
    ) {
        messagesList.add(Message(0,"",date_time,0,0,"","",0,message,message_type, mime_type, "",receiver_id, chat_room, sender_id, 0,0,""))
        mView!!.msgs_list.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        messagesTypeAdapter = MessagesTypeAdapter(requireContext(),messagesList)
        mView!!.msgs_list.adapter=messagesTypeAdapter
        messagesTypeAdapter!!.notifyItemInserted(messagesList.size-1)
        scrollToBottom()

    }

    private val GetChatMessage = Emitter.Listener { args ->
        requireActivity().runOnUiThread {
            Log.e("GetChatMessage", "" + args.size)

        }
    }

    private fun postImage(imagePath: String, room: String?) {
        val builder = APIClient.createMultipartBodyBuilder(arrayOf("room", "lang"),
            arrayOf(room.toString(),
            sharedPreferenceInstance!![SharedPreferenceUtility.SelectedLang, ""]))
        val file = File(imagePath)
        val requestBody = RequestBody.create(MediaType.parse("image/*"), file)
        builder!!.addFormDataPart("chat_file", file.name, requestBody)
        val call = apiInterface.chatFileUpload(builder.build())
        call!!.enqueue(object : Callback<ChatFileUploadResponse?>{
            override fun onResponse(
                call: Call<ChatFileUploadResponse?>,
                response: Response<ChatFileUploadResponse?>
            ) {
                if (response.isSuccessful){
                    if (response.body()!=null){
                        chat_file = response.body()!!.chat_file.chat_file
                        mime_type = response.body()!!.chat_file.mime_type
                        image_url = response.body()!!.chat_file.url
                        message_type = "2"
                        val messageObject = JSONObject()
                        try {
                            messageObject.put("message",chat_file)
                            messageObject.put("room", "" + room)
                            messageObject.put("sender_id", ""+sharedPreferenceInstance!![SharedPreferenceUtility.UserId, 0])
                            messageObject.put("receiver_id", ""+admin_id)
                            messageObject.put("help_id",""+help_category!!.id )
                            messageObject.put("sub_help_id",""+sub_help_category!!.id )
                            messageObject.put("type", ""+type)
                            messageObject.put("message_type", message_type)
                            messageObject.put("mime_type", mime_type)
                            messageObject.put("url", image_url)
                            messageObject.put("created_at", "" + date_time)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                        mSocket.emit("chatMessage", messageObject)
                        Log.e("emitMessage", messageObject.toString())
                        mView!!.et_enter_message.setText("")
                        if (type == "direct" && directMessageStatus == 0){
                            disableSendMsg()
                        }

                    }else{
                        Utility.showSnackBarOnResponseError(mView!!.chatWithAdminFragmentConstraintLayout,
                            response.body()!!.message,
                            requireContext())
                    }
                }else{
                    Utility.showSnackBarOnResponseError(mView!!.chatWithAdminFragmentConstraintLayout,
                        requireContext().getString(R.string.response_isnt_successful),
                        requireContext())
                }
            }

            override fun onFailure(call: Call<ChatFileUploadResponse?>, t: Throwable) {
                Utility.showSnackBarOnResponseError(mView!!.chatWithAdminFragmentConstraintLayout,
                    t.message!!,
                    requireContext())
            }
        })
    }

    private fun scrollToBottom() {
        mView!!.msgs_list.scrollToPosition(messagesTypeAdapter!!.itemCount-1)
    }
    override fun onResume() {
        super.onResume()
        if (Utility.hasConnection(requireContext())){
            initSocket()
            getOldMessageList()
        }
    }

    public override fun onDestroy() {
        disconnectSocket()
        super.onDestroy()
    }

    private fun disconnectSocket(){
        if(Utility.hasConnection(requireContext()) && mSocket!=null) {
            mSocket.disconnect()
            mSocket.off(EVENT_CONNECT, onConnect)
            mSocket.off(EVENT_DISCONNECT, onDisconnect)
            mSocket.off(EVENT_CONNECT_ERROR, onConnectError)
            mSocket.off(EVENT_CONNECT_TIMEOUT, onConnectError)
            mSocket.off("getMessage", GetChatMessage)
            mSocket.off("newChatMessage", NewChatMessage)
            //mSocket!!.off("messages", NewChatMessage)
            //mSocket!!.off("blocked", BlockedUserListener)
        }
    }
}