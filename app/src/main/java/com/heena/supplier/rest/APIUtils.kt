package com.heena.supplier.rest

object APIUtils {
        //const val BASE_URL:String = "https://henna.devtechnosys.info/api/manager/"
        const val BASE_URL:String = "https://alniqasha.ae/api/manager/"
        const val LOGIN:String = "access/login"
        const val REGISTER:String = "access/register"
        const val FORGOTPASSWORD:String = "access/forget-password"
        const val VERIFYOTP:String = "access/register-verify"
        const val FORGOTPASSVERIFYOTP:String = "access/forget-password-verify"
        const val RESETPASSWORD:String = "access/reset-password"
        const val REGISTERVERIFYRESEND:String = "access/register-verify-resend"
        const val COUNTRIES:String = "option/country/{lang}"
        const val CATEGORYLIST : String = "option/category"
        const val MEMBERSHIPLIST : String = "membership/{user_id}"
        const val BUYMEMBERSHIP : String = "membership"
        const val LOGOUT : String = "access/logout/{user_id}"
        const val SERVICESLISTING : String = "service/{user_id}"
        const val GALLERYLISTING : String = "gallery"
        const val UPLOADGALLERY : String = "gallery"
        const val DELETEGALLERYIMAGE : String = "gallery/delete/{gallery_id}"
        const val ADDSERVICE : String = "service"
        const val DELETESERVICE = "service/delete/{service_id}"
        const val EDITSERVICE = "service/edit"
        const val GETNOTIFICATIONS = "notification/{user_id}"
        const val ADDOFFERS : String = "offer"
        const val UPDATEOFFERS : String = "offer/edit"
        const val GETOFFERS : String = "offer/{manager_id}"
        const val SHOWOFFER : String = "offer/show/{offer_id}"
        const val SHOWSERVICE : String = "service/show/{sevice_id}"
        const val DELETEOFFER = "offer/delete/{offer_id}"
        const val CONTACTUS : String = "dashboard/contact"
        const val CHANGEPASSWORD : String = "profile/change-password"
        const val SHOWPROFILE : String = "profile/{user_id}"
        const val SHOWCOMMENTS : String = "comment/{user_id}"
        const val UPDATEPROFILE : String = "profile"
        const val ADDEDITBANKS : String = "bank"
        const val SHOWBANKS : String = "bank/{user_id}"
        const val DASHBOARD : String = "dashboard/{user_id}"
        const val BOOKINGLISTING : String = "booking"
        const val ACCEPTBOOKING : String = "booking/accept"
        const val REJECTBOOKING : String = "booking/cancel"
        const val TRANSACTIONSLISTING : String = "transaction/{manager_id}"
        const val SHOWBOOKING : String = "booking/{booking_id}"
        const val SERVICEIMAGEDELETE : String = "service/image/delete/{gallery_id}"
        const val BUYSUBSCRIPTION : String = "subsciptions"
        const val SHOWADDRESS : String = "address/show/{address_id}"
        const val ADDADDRESS : String = "address"
        const val EDITADDRESS : String = "address/edit"
        const val DELETEADDRESS : String = "address/delete/{user_id}"
        const val ADDRESSLISTING : String = "address/{user_id}"
        const val FEATUREDSERVICESLISTING : String = "featured_service/{user_id}"
        const val ADDDELETECARD : String = "card"
        const val SHOWCARDS : String = "card/{user_id}"
        const val DASH_HELP_CATEGORY : String = "dashboard/get_help_category"
        const val GET_OLD_MESSAGE_LIST : String = "get_old_messages_list"
        const val CHAT_FILE_UPLOAD : String = "chat_file_upload"
        const val SUBSCRIPTION_PLANS : String = "subscription_plans/{user_id}"
        const val NOTIFICATION : String = "notification/{user_id}"
        const val createCharge = "create_charge"
        const val createChargeSubscriptions = "create_charge_subscriptions"
        const val transaction_excel = "transaction_excel/{user_id}"
}