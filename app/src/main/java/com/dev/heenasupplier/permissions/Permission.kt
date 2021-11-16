package com.dev.heenasupplier.permissions

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.PackageManager.*
import android.os.Build
import android.provider.Telephony.Carriers.MNC


class Permission {

    private val MNC = "MNC"

    // Calendar group.
    val READ_CALENDAR = Manifest.permission.READ_CALENDAR
    val WRITE_CALENDAR = Manifest.permission.WRITE_CALENDAR

    // Camera group.
    val CAMERA = Manifest.permission.CAMERA

    // Contacts group.
    val READ_CONTACTS = Manifest.permission.READ_CONTACTS
    val WRITE_CONTACTS = Manifest.permission.WRITE_CONTACTS

    /*@Deprecated("")
    val WRITE_PROFILE: String = Manifest.permission.WRITE_PROFILE

    @Deprecated("")
    val READ_PROFILE: String = Manifest.permission.READ_PROFILE*/

    // Location group.
    val ACCESS_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION
    val ACCESS_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION

    // Microphone group.
    val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO

    // Phone group.
    val READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
    val CALL_PHONE = Manifest.permission.CALL_PHONE
    val READ_CALL_LOG = Manifest.permission.READ_CALL_LOG
    val WRITE_CALL_LOG = Manifest.permission.WRITE_CALL_LOG
    val ADD_VOICEMAIL = Manifest.permission.ADD_VOICEMAIL
    val USE_SIP = Manifest.permission.USE_SIP
    val PROCESS_OUTGOING_CALLS = Manifest.permission.PROCESS_OUTGOING_CALLS

    // Sensors group.
    val BODY_SENSORS = Manifest.permission.BODY_SENSORS
    val USE_FINGERPRINT = Manifest.permission.USE_FINGERPRINT

    // SMS group.
    val SEND_SMS = Manifest.permission.SEND_SMS
    val RECEIVE_SMS = Manifest.permission.RECEIVE_SMS
    val READ_SMS = Manifest.permission.READ_SMS
    val RECEIVE_WAP_PUSH = Manifest.permission.RECEIVE_WAP_PUSH
    val RECEIVE_MMS = Manifest.permission.RECEIVE_MMS
    val READ_CELL_BROADCASTS = "android.permission.READ_CELL_BROADCASTS"

    // Bookmarks group.
    val READ_HISTORY_BOOKMARKS = "com.android.browser.permission.READ_HISTORY_BOOKMARKS"
    val WRITE_HISTORY_BOOKMARKS = "com.android.browser.permission.WRITE_HISTORY_BOOKMARKS"

    // Social Info group.
    /*@Deprecated("")
    val WRITE_SOCIAL_STREAM: String = Manifest.permission.WRITE_SOCIAL_STREAM

    // User Dictionary group.
    val READ_USER_DICTIONARY: String = Manifest.permission.READ_USER_DICTIONARY
    val WRITE_USER_DICTIONARY: String = Manifest.permission.WRITE_USER_DICTIONARY*/

    /**
     * Create an array from a given permissions.
     *
     * @throws IllegalArgumentException
     */
    fun asArray(vararg permissions: String): Array<String?>? {
        require(permissions.size != 0) { "There is no given permission" }
        val dest = arrayOfNulls<String>(permissions.size)
        var i = 0
        val len = permissions.size
        while (i < len) {
            dest[i] = permissions[i]
            i++
        }
        return dest
    }

    /**
     * Check that given permission have been granted.
     */
    fun hasGranted(grantResult: Int): Boolean {
        return grantResult == PERMISSION_GRANTED
    }

    /**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value [PackageManager.PERMISSION_GRANTED].
     */
    fun hasGranted(grantResults: IntArray): Boolean {
        for (result in grantResults) {
            if (!hasGranted(result)) {
                return false
            }
        }
        return true
    }

    /**
     * Returns true if the Context has access to a given permission.
     * Always returns true on platforms below M.
     */
    fun hasSelfPermission(context: Context, permission: String): Boolean {
        return if (isMNC()) {
            permissionHasGranted(context, permission)
        } else true
    }

    /**
     * Returns true if the Context has access to all given permissions.
     * Always returns true on platforms below M.
     */
    fun hasSelfPermissions(context: Context, permissions: Array<String>): Boolean {
        if (!isMNC()) {
            return true
        }
        for (permission in permissions) {
            if (!permissionHasGranted(context, permission)) {
                return false
            }
        }
        return true
    }

    /**
     * Requests permissions to be granted to this application.
     */
    fun requestAllPermissions(activity: Activity, permissions: Array<String>, requestCode: Int) {
        if (isMNC()) {
            internalRequestPermissions(activity, permissions, requestCode)
        }
    }


    private fun internalRequestPermissions(
        activity: Activity?,
        permissions: Array<String>,
        requestCode: Int
    ) {
        requireNotNull(activity) { "Given activity is null." }
        activity.requestPermissions(permissions, requestCode)
    }


    private fun permissionHasGranted(context: Context, permission: String): Boolean {
        return hasGranted(context.checkSelfPermission(permission))
    }

    private fun isMNC(): Boolean {
        return MNC == Build.VERSION.CODENAME
    }
}