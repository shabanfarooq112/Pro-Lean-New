package com.horizam.pro.elean.data.api

import androidx.lifecycle.MutableLiveData
import com.horizam.pro.elean.data.model.AnalyticModel
import com.horizam.pro.elean.data.model.BuyerActionRequestMultipart
import com.horizam.pro.elean.data.model.requests.*
import com.horizam.pro.elean.data.model.response.*
import com.horizam.pro.elean.ui.main.viewmodel.FirebaseNotificationRequest
import retrofit2.http.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.PartMap
import retrofit2.http.POST
import retrofit2.http.Multipart


interface ApiService {

    @POST("register")
    suspend fun registerUser(
        @Body request: RegisterRequest,
        @Query("referal_code") referralCode: String
    ): RegisterResponse
    //create and cancel dispute
    @POST("buyer/orders/:{order_id}/cancel_and_create_dispute")
    suspend fun buyerActions(
        @Path("order_id") orderId:String,
        @Body request: BuyerActionRequestMultipart):
            GeneralResponse
    //cancelDispute



    //Completed
    @POST("buyer/orders/:{order_id}/completed")
    suspend fun buyerCompleted(
        @Path ("order_id") orderId: String,
        @Body request: BuyerActionRequestMultipart):GeneralResponse

    @POST("buyer/orders/{order_id}/order_revision")
    suspend fun buyerRevision(
        @Path ("order_id") orderId: String,
        @Body request: BuyerRevisionAction):GeneralResponse




    @POST("seller/orders/:{order_id}/reject_dispute")
    suspend fun cancelDispute(
        @Path ("order_id") orderId: String):GeneralResponse
    //Reject_Dispute
    @POST("seller/orders/:{order_id}/reject_dispute")
    suspend fun rejectDispute(
        @Path ("order_id") orderId: String):GeneralResponse
    //Accept_Dispute
    @POST("seller/orders/:{order_id}/accept_dispute")
    suspend fun acceptDispute(
        @Path ("order_id") orderId: String):GeneralResponse



    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): LoginResponse

    @POST("seller/withdrawal_account")
    suspend fun addAccountDetail(@Body request: BankDetail): GeneralResponse

    @GET("seller/withdrawal_account")
    suspend fun getAccountDetail(): BankDetailResponse

    @POST("forgot_password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): GeneralResponse

    @POST("change_password")
    suspend fun forgotChangePassword(@Body request: ForgetChangePasswordRequest): GeneralResponse

    @POST("seller/send_offer")
    suspend fun sendOffer(@Body request: SendOfferRequest): BuyerRequest

    @POST("buyer/jobs")
    suspend fun postJob(@Body request: PostJobRequest): PostedJobResponse

    @POST("buyer/offer_order")
    suspend fun acceptOrder(@Body request: AcceptOrderRequest): GeneralResponse

    @Multipart
    @POST("become_freelancer")
    suspend fun becomeFreelancer(
        @PartMap partMap: HashMap<String, RequestBody>,
        @Part file: MultipartBody.Part
    ): GeneralResponse

    @Multipart
    @POST("seller/services")
    suspend fun createService(
        @PartMap partMap: HashMap<String, RequestBody>,
        @Part images: ArrayList<MultipartBody.Part>
    ): ServiceResponse

    @Multipart
    @POST("seller/services/{id}")
    suspend fun updateService(
        @PartMap partMap: HashMap<String, RequestBody>,
        @Part images: ArrayList<MultipartBody.Part>,
        @Part("delete[]") deletedImages: ArrayList<String>,
        @Path("id") id: String
    ): ServiceResponse

    @Multipart
    @POST("update_profile")
    suspend fun updateProfile(
        @PartMap partMap: HashMap<String, RequestBody>,
        @Part image: MultipartBody.Part?
    ): ProfileInfo

    @POST("logout")
    suspend fun logout(): GeneralResponse

    @GET("home")
    suspend fun getHomeData(): HomeDataResponse

    @GET("categories")
    suspend fun getCategoriesDays(): CategoriesDaysResponse

    @GET("countries&categories")
    suspend fun getCategoriesCountries(): CategoriesCountriesResponse

    @GET("profile")
    suspend fun getNonFreelancerProfile(): ProfileInfo

    @GET("seller/serviceReviews")
    suspend fun getUserReviews(): UserReviewsResponse

    @GET("term_condition")
    suspend fun getPrivacyTerms(): PrivacyPolicyResponse

    @GET("lang-currency")
    suspend fun getLanguageAndCurrency(): LanguageAndCurrencyResponse

    @GET("notification")
    suspend fun getNotifications(): NotificationsResponse

    @GET("notifications/read")
    suspend fun getNotificationsRead(): GeneralResponse



    @GET("profile")
    suspend fun getFreelancerProfile(@Query("user") id: String): ProfileInfo

    @GET("subcategories/{id}")
    suspend fun getSpinnerSubcategories(@Path("id") id: String): SubcategoriesDataResponse

    @GET("subcategories/{id}")
    suspend fun getSubcategories(
        @Path("id") id: String,
        @Query("page") page: Int,
        @Query("q") query: String
    ): SubcategoriesResponse

    @GET("categories/{id}/services")
    suspend fun getServicesBySubCategories(
        @Path("id") id: String,
        @Query("page") page: Int,
        @Query("filter") filter: String,
        @Query("filter_value") filter_value: String,
    ): ServicesResponse

    @GET("search")
    suspend fun searchGigs(
        @Query("q") query: String,
//        @Query("distance") distance: String,
        @Query("filter") filter: String,
        @Query("filter_value") filter_value: String,
        @Query("category") category: String,
        @Query("page") page: Int
    ): ServicesResponse
    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("category") category: String,
        @Query("page") page: Int
    ): ServicesResponse

    @GET("service/{id}/reviews")
    suspend fun getReviews(
        @Path("id") query: String,
        @Query("page") page: Int
    ): ServiceReviewsResponse

    @GET("buyer/jobs/{id}/offers")
    suspend fun getJobOffers(@Path("id") id: String, @Query("page") page: Int): JobOffersResponse

    @GET("seller/buyer_requests")
    suspend fun getBuyerRequests(
        @Query("page") page: Int,
        @Query("status") status: String
    ): BuyerRequestsResponse

    @GET("seller/services")
    suspend fun getManageServices(
        @Query("status") status: String,
    ): ServicesResponse

    @GET("seller/analytics")
    suspend fun getSellerData(): AnalyticModel

    @GET("seller/services")
    suspend fun getSellerServicesByID(
        @Query("seller") userID: String,
    ): ServicesResponse

    @GET("get_wishlist")
    suspend fun getSavedGigs(@Query("page") page: Int): ServicesResponse

    @GET("buyer/jobs")
    suspend fun getPostedJobs(
        @Query("page") page: Int,
        @Query("status") status: String
    ): PostedJobsResponse

    @GET("seller/services/{uid}")
    suspend fun getGigDetails(@Path("uid") uid: String): ServiceResponse

    @GET("seller/services/{uid}")
    suspend fun getFeaturedGigDetails(@Path("uid") uid: String): ServiceResponse

    @POST("customer_support")
    suspend fun submitQuery(@Body request: SubmitQueryRequest): GeneralResponse

    @POST("buyer/custom_order")
    suspend fun customOrder(@Body request: CustomOrderRequest): GeneralResponse

    @POST("token")
    suspend fun getToken(@Body request: CardModel): TokenModel

    @POST("buyer/chat_order")
    suspend fun chatOrder(@Body request: ChatOfferRequest): GeneralResponse

    @GET("order/{order_no}/activity")
    suspend fun orderTimeline(@Path("order_no") orderNo: String): OrderTimelineResponse

    @GET("chat/{id}")
    suspend fun chatOrder(@Path("id") id: String):ProfileInfo

    @POST("store_user_info")
    suspend fun storeUserInfo(@Body request: StoreUserInfoRequest): GeneralResponse

    @POST("wishlist")
    suspend fun addRemoveWishlist(@Body request: FavouriteRequest): GeneralResponse

    @FormUrlEncoded
    @POST("serviceclicks")
    suspend fun addClicksGigsRequest(@Field("service_id") request: String): GeneralResponse

    @FormUrlEncoded
    @POST("seller/manage_order")
    suspend fun sellerActions(
        @FieldMap sellerHashMap: HashMap<String, Any>
    ): GeneralResponse

    @Multipart
    @POST("seller/orders/:{order_id}/deliver_order")
    suspend fun sellerActionsWithFile(
        @Path("order_id") order_id:String,
        @Part("description") deliveryNote:String,
        @Part image: MultipartBody.Part,
    ): GeneralResponse

    @DELETE("buyer/jobs/{id}")
    suspend fun deletePostedJob(@Path("id") id: String): GeneralResponse

    @GET("buyer/orders")
    suspend fun getBuyerOrders(@Query("status") status: String,@Query("page") page: Int): OrdersResponse

    @GET("seller/analytics")
    suspend fun getEarnings(): AnalyticModel

    @FormUrlEncoded
    @POST("seller/withdrawRequest")
    suspend fun withdrawalAmount(@Field("amount") request: Double): GeneralResponse

    @GET("seller/orders")
    suspend fun getSellersOrders(@Query("status") status:String,@Query("page") page: Int): OrdersResponse

    @DELETE("buyer/delete_job_requests/{id}")
    suspend fun deleteJobOffer(@Path("id") id: String): GeneralResponse

    @DELETE("seller/services/{id}")
    suspend fun deleteUserService(@Path("id") id: String): GeneralResponse

    @DELETE("seller/cancel_offer/{uid}")
    suspend fun cancelBuyerRequests(@Path("uid") uid: String): GeneralResponse

    @POST("buyer/orders/:{order_id}/review")
    suspend fun ratingOrder(
        @Path ("order_id") orderId: String,
        @Body request: RatingOrderRequest): GeneralResponse

    @POST("update_password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): GeneralResponse

    @GET("buyer/orders/{id}/order_details")
    suspend fun orderByID(@Path("id") orderId: Int): Order

    @POST("seller/orders/:{order_id}/extend_order_request")
    suspend fun extendTime(
        @Path ("order_id") orderId: String,
        @Body request: ExtendDeliveryTimeModel): GeneralResponse


    @GET("buyer/orders/:{order_id}/acceptExtensionRequest")
    suspend fun acceptExtension(
        @Path ("order_id") orderId: String
    ):GeneralResponse

    @GET("buyer/orders/:{order_id}/rejectExtensionRequest")
    suspend fun rejectExtension(
        @Path ("order_id") orderId: String
    ):GeneralResponse

    @POST("sendNotifications")
    suspend fun sendFirebaseNotification(
        @Body rootModel: FirebaseNotificationRequest?
    ): GeneralResponse

}