package com.bank.notifications.data.cloud

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface NotificationHttpService {
    @POST("api/v1/user/auth")
    suspend fun authUser(): Response<UserAuthedResponse>

    @GET("api/v1/user/profile")
    suspend fun getUserProfile(): Response<UserProfileResponse>

    @POST("api/v1/user/profile")
    suspend fun updateUserProfile(@Body userProfile: UserProfileRequest): Response<UserProfileResponse>

    @GET("api/v1/notifications/channels")
    suspend fun getUserNotificationChannels(): Response<NotificationChannelsResponse>

    @GET("api/v1/notifications/channels/{channelId}")
    suspend fun getUserNotificationChannel(@Path("channelId") channelId: String): Response<NotificationChannelResponse>

    @POST("api/v1/notifications/channels/{channelId}")
    suspend fun upsertUserNotificationChannel(
        @Path("channelId") channelId: String, @Body notificationChannel: NotificationChannelRequest
    ): Response<NotificationChannelResponse>
}