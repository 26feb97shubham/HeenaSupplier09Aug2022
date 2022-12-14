package com.heena.supplier.utils

import android.content.Context
import android.content.SharedPreferences
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.heena.supplier.application.MyApp
import com.heena.supplier.models.Membership
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class SharedPreferenceUtility {

    private val sharedPreferences: SharedPreferences

    private val editor: SharedPreferences.Editor
        get() = sharedPreferences.edit()

    init {
        instance = this
        sharedPreferences = MyApp.instance!!.getSharedPreferences(
            SHARED_PREFS_NAME,
            Context.MODE_PRIVATE
        )

    }

    fun hideSoftKeyBoard(context: Context, view: View) {
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            if (imm.isAcceptingText){
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }else{
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    fun isUserNameValid(username: String): Boolean {
        var isValid = false
        val expression = "^[a-zA-Z0-9]([._](?![._])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]\$"
        val inputStr: CharSequence = username
        val pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE)
        val matcher: Matcher = pattern.matcher(inputStr)
        if (matcher.matches()) {
            isValid = true
        }
        return isValid
    }

    fun isCharacterAllowed(validateString: String): Boolean {
        var containsInvalidChar = false
        for (element in validateString) {
            val type = Character.getType(element)
            containsInvalidChar = !(type == Character.SURROGATE.toInt() || type == Character.OTHER_SYMBOL.toInt())
        }
        return containsInvalidChar
    }
    fun isPasswordValid(str: String): Boolean {
        var ch: Char
        var capitalFlag = false
        var lowerCaseFlag = false
        var numberFlag = false
        var lengthFlag = false
        if(str.length>=6) {
            lengthFlag=true
            for (element in str) {
                ch = element
                when {
                    Character.isDigit(ch) -> {
                        numberFlag = true
                    }
                    Character.isUpperCase(ch) -> {
                        capitalFlag = true
                    }
                    Character.isLowerCase(ch) -> {
                        lowerCaseFlag = true
                    }
                }
                if (numberFlag && capitalFlag && lowerCaseFlag) return true
            }
        }

        return false
    }

    fun delete(key: String) {
        if (sharedPreferences.contains(key)) {
            editor.remove(key).commit()
        }
    }

    fun save(key: String, value: Any?) {
        val editor = editor
        when {
            value is Boolean -> {
                editor.putBoolean(key, (value as Boolean?)!!)
            }
            value is Int -> {
                editor.putInt(key, (value as Int?)!!)
            }
            value is Float -> {
                editor.putFloat(key, (value as Float?)!!)
            }
            value is Long -> {
                editor.putLong(key, (value as Long?)!!)
            }
            value is String -> {
                editor.putString(key, value as String?)
            }
            value is Enum<*> -> {
                editor.putString(key, value.toString())
            }
            value is HashSet<*> -> {
                editor.putStringSet(key, value as Set<String>?)
            }
            value != null -> {
                throw RuntimeException("Attempting to save non-supported preference")
            }
        }
        editor.commit()
    }

    operator fun <T> get(key: String): T {
        return (sharedPreferences.all[key] as T?)!!
    }

    operator fun <T> get(key: String, defValue: T): T {
        val returnValue = sharedPreferences.all[key] as T?
        return returnValue ?: defValue
    }

    fun isEmailValid(email: String): Boolean {
        return Pattern.compile(
            "^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]|[\\w-]{2,}))@"
                    + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                    + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                    + "[0-9]{1,2}|25[0-5]|2[0-4][0-9]))|"
                    + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$"
        ).matcher(email).matches()
    }

    fun saveMembershipInfo(context: Context, membershipInfo: Membership){
        val gson = Gson()
        val json = gson.toJson(membershipInfo)
        val editor = editor
        editor.putString(MembershipInfo, json).apply()
    }

    fun isIbanValid(iban: String, accountNumber: String): Boolean {
        val IBAN_MIN_SIZE = 15
        val IBAN_MAX_SIZE = 34
        val IBAN_MAX: Long = 999999999
        val IBAN_MODULUS: Long = 97
        val trimmed = iban.trim { it <= ' ' }
        if (trimmed.length < IBAN_MIN_SIZE || trimmed.length > IBAN_MAX_SIZE) {
            return false
        }
        val countryCode = trimmed.substring(0, 2)
        return if (countryCode.equals("AE", false)){
            val checkDigits = trimmed.substring(2, 4)
            if (checkDigits.equals("26", false)){
                val myAccountNumber = trimmed.substring(4)
                myAccountNumber.matches(accountNumber.toRegex())
            }else{
                false
            }
        }else{
            false
        }

       /* val reformat = trimmed.substring(4) + trimmed.substring(0, 4)
        var total: Long = 0
        for (element in reformat) {
            val charValue = Character.getNumericValue(element)
            if (charValue < 0 || charValue > 35) {
                return false
            }
            total = (if (charValue > 9) total * 100 else total * 10) + charValue
            if (total > IBAN_MAX) {
                total %= IBAN_MODULUS
            }
        }
        return total % IBAN_MODULUS == 1L*/
    }

    fun isAccountNoValid(accountNo : String) : Boolean{
        val ACCOUNT_NO_MIN_SIZE = 15
        val ACCOUNT_NO_MAX_SIZE = 30
        val trimmed = accountNo.trim { it <= ' ' }
        return !(trimmed.length < ACCOUNT_NO_MIN_SIZE || trimmed.length > ACCOUNT_NO_MAX_SIZE)
    }

    companion object {
        private const val SHARED_PREFS_NAME = "Hena Supplier"
        private var instance: SharedPreferenceUtility? = null
        const val FCMTOKEN = "fcmToken"
        const val TOKEN = "token"
        const val DeviceId = "device_id"
        const val UserId = "getUserId"
        const val PhoneNumber = "getPhoneNumber"
        const val MembershipId = "membership_Id"
        const val SelectedLang = "selectedLang"
        const val DocPath = "docPath"
        const val UserEmail = "userEmail"
        const val IsLogin = "isLogin"
        const val IsWelcomeShow = "IsWelcomeShow"
        const val ISINTRODUCTION = "isIntroduction"
        const val SavedAddress= "savedAddress"
        const val FIRSTTIME = "isFirstTime"
        const val ISSELECTLANGUAGE = "isSelectLanguage"
        const val IsRemembered = "isRemembered"
        const val Username = "username"
        const val Fullname = "fullname"
        const val Address = "address"
        const val Password= "password"
        const val OldPassword= "oldPassword"
        const val IsBankAdded= "bankAdded"
        const val SavedLat= "savedLat"
        const val SavedLng= "savedLng"
        const val ProfilePic= "profilepic"
        const val IsVerified = "isVerified"
        const val IsResend = "isResend"
        const val MembershipTimeLimit = "membershipTimeLimit"
        const val MembershipName = "membershipName"
        const val MembershipPrice = "membershipPrice"
        const val MembershipInfo = "membershipInfo"
        const val PLACECLICK = "placeClick"
        const val ISFEATURED = "isFeatured"
        const val isNotificationsClicked = ""

        @Synchronized
        fun getInstance(): SharedPreferenceUtility {
            if (instance == null) {
                instance = SharedPreferenceUtility()
            }
            return instance as SharedPreferenceUtility
        }

    }
}