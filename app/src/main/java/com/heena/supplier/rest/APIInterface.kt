package com.heena.supplier.rest

import com.heena.supplier.models.*
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface APIInterface {
    @POST(APIUtils.REGISTER)
    fun signUp(@Body body: RequestBody?): Call<SignUpResponse?>?

    @POST(APIUtils.LOGIN)
    fun login(@Body body: RequestBody?) : Call<LoginResponse?>?

    @GET(APIUtils.COUNTRIES)
    fun getCountries(@Path("lang") lang: String): Call<CountryResponse?>?

    @POST(APIUtils.FORGOTPASSWORD)
    fun forgotPassword(@Body body: RequestBody?): Call<ForgotPasswordResponse?>?

    @POST(APIUtils.VERIFYOTP)
    fun verifyotp(@Body body: RequestBody?): Call<RegistrationVerifyResponse?>?

    @POST(APIUtils.FORGOTPASSVERIFYOTP)
    fun forgotpassverifyotp(@Body body: RequestBody?): Call<ForgotPasswordVerifyResponse?>?

    @POST(APIUtils.RESETPASSWORD)
    fun resetpassword(@Body body: RequestBody?): Call<ResetPasswordResponse?>?

    @POST(APIUtils.REGISTERVERIFYRESEND)
    fun registerverivyresend(@Body body: RequestBody?) : Call<RegisterVerifyResendResponse?>?

   @GET(APIUtils.CATEGORYLIST)
   fun categoryList() : Call<CategoryListResponse?>?

   @GET(APIUtils.MEMBERSHIPLIST)
   fun membershipList(@Path("user_id") user_id: Int) : Call<MembershipListResponse?>?

    @FormUrlEncoded
    @POST(APIUtils.BUYMEMBERSHIP)
    fun buyMembership(@Field("user_id") user_id : String,
    @Field("membership_id") membership_id : String) : Call<BuyMembership?>?


    @GET(APIUtils.LOGOUT)
    fun logout(@Path("user_id") user_id: Int)  :Call<LogoutResponse?>?

    @GET(APIUtils.GALLERYLISTING)
    fun galleryListing (@Query("user_id") user_id: Int) : Call<GalleryListResponse?>?

    @POST(APIUtils.UPLOADGALLERY)
    fun uploadGallery(@Body body: RequestBody?) : Call<UploadGalleryResponse?>?

    @GET(APIUtils.DELETEGALLERYIMAGE)
    fun deletegalleryimage(@Path("gallery_id") gallery_id: Int) : Call<DeleteGalleryImage?>?

    @POST(APIUtils.ADDSERVICE)
    fun addService(@Body body: RequestBody?) : Call<AddServiceResponse?>?

    @GET(APIUtils.SERVICESLISTING)
    fun serviceslisting(@Path("user_id") user_id: Int, @Query("lang") lang : String) : Call<ServiceListingResponse?>?

    @GET(APIUtils.FEATUREDSERVICESLISTING)
    fun featuredserviceslisting(@Path("user_id") user_id: Int, @Query("lang") lang : String) : Call<FeaturedServicesListingResponse?>?


    @GET(APIUtils.DELETESERVICE)
    fun deleteservice(@Path("service_id") service_id : Int) : Call<DeleteServiceResponse?>?

    @POST(APIUtils.EDITSERVICE)
    fun editService(@Body body: RequestBody?) : Call<EditServiceResponse?>?

    @GET(APIUtils.SHOWSERVICE)
    fun showService(@Path("sevice_id") service_id : Int) : Call<ShowServiceResponse?>?

    @GET(APIUtils.GETNOTIFICATIONS)
    fun getNotifications(@Path("user_id") user_id: Int) : Call<NotificationResponse?>?

    @POST(APIUtils.ADDOFFERS)
    fun addOffers(@Body body: RequestBody?) : Call<AddOfferResponse?>?

    @GET(APIUtils.GETOFFERS)
    fun getOffersListing(@Path("manager_id") manager_id : Int) : Call<OffersListingResponse?>?

    @GET(APIUtils.DELETEOFFER)
    fun deleteoffer(@Path("offer_id") offer_id : Int) : Call<OfferDeleteResponse?>?

    @GET(APIUtils.SHOWOFFER)
    fun showOffer(@Path("offer_id") offer_id : Int) : Call<ShowOfferResponse?>?

    @POST(APIUtils.CONTACTUS)
    fun contactUs(@Body body: RequestBody?) : Call<ContactUsResponse?>?

    @POST(APIUtils.CHANGEPASSWORD)
    fun changePassword(@Body body: RequestBody?) : Call<ChangePasswordResponse?>?

    @GET(APIUtils.SHOWPROFILE)
    fun showProfile(@Path("user_id") user_id : Int) : Call<ProfileShowResponse?>?

    @GET(APIUtils.SHOWCOMMENTS)
    fun showComments(@Path("user_id") user_id : Int) : Call<ShowCommentsResponse?>?

    @POST(APIUtils.UPDATEPROFILE)
    fun updateProfile(@Body body: RequestBody?) : Call<UpdateProfileResponse?>?

    @POST(APIUtils.ADDEDITBANKS)
    fun addEditBanks(@Body body: RequestBody?) : Call<AddEditBankResponse?>?

    @GET(APIUtils.SHOWBANKS)
    fun showBanks(@Path("user_id") user_id  :Int) : Call<BankDetailsResponse?>?

    @GET(APIUtils.DASHBOARD)
    fun getDashboard(@Path("user_id") user_id  :Int) : Call<DashboardResponse?>?

    @GET(APIUtils.BOOKINGLISTING)
    fun getBookingsList(@QueryMap query : HashMap<String, String>) : Call<BookingsListingResponse?>?

    @POST(APIUtils.ACCEPTBOOKING)
    fun acceptBooking(@Body body: RequestBody?) : Call<AcceptRejectBookingResponse?>?

    @POST(APIUtils.REJECTBOOKING)
    fun rejectBooking(@Body body: RequestBody?) : Call<AcceptRejectBookingResponse?>?

    @GET(APIUtils.SHOWBOOKING)
    fun showBooking(@Path("booking_id") booking_id : String) : Call<BookingDetailsResponse?>?

    @GET(APIUtils.TRANSACTIONSLISTING)
    fun getTransactionsList(@Path("manager_id") manager_id : Int, @QueryMap query : HashMap<String, String>) : Call<TransactionsListingResponse?>?

    @GET(APIUtils.SERVICEIMAGEDELETE)
    fun deleteServiceImage(@Path("gallery_id") gallery_id : String) : Call<ServiceImageDeleteResponse?>?

    @POST(APIUtils.BUYSUBSCRIPTION)
    fun buySubscription(@Body body: RequestBody?) : Call<BuySubscriptionResponse?>?

    @GET(APIUtils.SHOWADDRESS)
    fun showAddress(@Path("address_id") address_id: Int, @Query("lang") lang : String) : Call<ShowAddressResponse?>?

    @POST(APIUtils.ADDADDRESS)
    fun addAddress(@Body body: RequestBody?) : Call<AddEditDeleteAddressResponse?>?

    @POST(APIUtils.EDITADDRESS)
    fun editAddress(@Body body: RequestBody?) : Call<AddEditDeleteAddressResponse?>?

    @GET(APIUtils.ADDRESSLISTING)
    fun getAddressList(@Path("user_id") user_id: Int, @Query("lang") lang : String) : Call<AddressListingResponse?>?

    @GET(APIUtils.DELETEADDRESS)
    fun deleteAddress(@Path("user_id") user_id: Int, @Query("lang") lang : String) : Call<AddEditDeleteAddressResponse?>?

    @POST(APIUtils.UPDATEOFFERS)
    fun updateOffers(@Body body: RequestBody?) : Call<OfferUpdateResponse?>?

    @POST(APIUtils.ADDDELETECARD)
    fun addDeleteCard(@Body body: RequestBody?) : Call<AddDeleteCardResponse?>?

    @GET(APIUtils.SHOWCARDS)
    fun showCards(@Path("user_id") user_id  :Int) : Call<ViewCardResponse?>?

    @POST(APIUtils.DASH_HELP_CATEGORY)
    fun dashHelpCategory(@Body body: RequestBody?) : Call<DashHelpCategoryResponse?>?

    @POST(APIUtils.GET_OLD_MESSAGE_LIST)
    fun getOldMessageList(@Body body: RequestBody?) : Call<OldMessagesResponse?>?

    @POST(APIUtils.CHAT_FILE_UPLOAD)
    fun chatFileUpload(@Body body: RequestBody?) : Call<ChatFileUploadResponse?>?

    @GET(APIUtils.SUBSCRIPTION_PLANS)
    fun subscription_plans(@Path("user_id") user_id: Int, @Query("lang") lang : String) : Call<SubscriptionPlansResponse?>
}