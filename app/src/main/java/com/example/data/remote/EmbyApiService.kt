package com.example.data.remote

import com.example.data.model.AuthRequest
import com.example.data.model.AuthResponse
import com.example.data.model.ItemsResponse
import com.example.data.model.PlaybackInfoResponse
import com.example.data.model.PlaybackProgressReport
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface EmbyApiService {

    @POST("Users/AuthenticateByName")
    suspend fun authenticate(
        @Header("X-Emby-Authorization") authorization: String,
        @Body request: AuthRequest
    ): Response<AuthResponse>

    @GET("Users/{userId}/Views")
    suspend fun getUserViews(
        @Path("userId") userId: String
    ): Response<ItemsResponse>

    @GET("Users/{userId}/Items/Resume")
    suspend fun getResumeItems(
        @Path("userId") userId: String,
        @Query("Limit") limit: Int = 12,
        @Query("Fields") fields: String = "Overview,UserData,MediaSources,Genres,PrimaryImageAspectRatio,ImageTags,BackdropImageTags"
    ): Response<ItemsResponse>

    @GET("Users/{userId}/Items/Latest")
    suspend fun getLatestItems(
        @Path("userId") userId: String,
        @Query("ParentId") parentId: String? = null,
        @Query("Limit") limit: Int = 16,
        @Query("Fields") fields: String = "Overview,UserData,MediaSources,Genres,PrimaryImageAspectRatio,ImageTags,BackdropImageTags"
    ): Response<List<com.example.data.model.EmbyItemDto>>

    @GET("Users/{userId}/Items")
    suspend fun getItems(
        @Path("userId") userId: String,
        @Query("ParentId") parentId: String? = null,
        @Query("IncludeItemTypes") includeItemTypes: String? = null,
        @Query("SortBy") sortBy: String? = "SortName",
        @Query("SortOrder") sortOrder: String? = "Ascending",
        @Query("Recursive") recursive: Boolean = true,
        @Query("SearchTerm") searchTerm: String? = null,
        @Query("PersonIds") personIds: String? = null,
        @Query("Limit") limit: Int = 50,
        @Query("StartIndex") startIndex: Int = 0,
        @Query("Fields") fields: String = "Overview,UserData,MediaSources,Genres,PrimaryImageAspectRatio,ImageTags,BackdropImageTags,Chapters,People"
    ): Response<ItemsResponse>

    @GET("Users/{userId}/Items/{itemId}")
    suspend fun getItemDetails(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String,
        @Query("Fields") fields: String = "Overview,UserData,MediaSources,Genres,PrimaryImageAspectRatio,ImageTags,BackdropImageTags,Chapters,People"
    ): Response<com.example.data.model.EmbyItemDto>

    @GET("Shows/{seriesId}/Seasons")
    suspend fun getSeasons(
        @Path("seriesId") seriesId: String,
        @Query("userId") userId: String
    ): Response<ItemsResponse>

    @GET("Shows/{seriesId}/Episodes")
    suspend fun getEpisodes(
        @Path("seriesId") seriesId: String,
        @Query("seasonId") seasonId: String? = null,
        @Query("userId") userId: String,
        @Query("Fields") fields: String = "Overview,UserData,MediaSources,Genres,PrimaryImageAspectRatio,ImageTags,BackdropImageTags,Chapters,People"
    ): Response<ItemsResponse>

    @GET("Items/{itemId}/Similar")
    suspend fun getSimilarItems(
        @Path("itemId") itemId: String,
        @Query("UserId") userId: String,
        @Query("Limit") limit: Int = 12,
        @Query("Fields") fields: String = "Overview,UserData,MediaSources,Genres,PrimaryImageAspectRatio,ImageTags,BackdropImageTags"
    ): Response<ItemsResponse>

    @GET("LiveTv/Channels")
    suspend fun getLiveTvChannels(
        @Query("UserId") userId: String,
        @Query("Limit") limit: Int = 100,
        @Query("Fields") fields: String = "Overview,CurrentProgram,ImageTags"
    ): Response<com.example.data.model.LiveTvChannelsResponse>

    @GET("Items/{itemId}/PlaybackInfo")
    suspend fun getPlaybackInfo(
        @Path("itemId") itemId: String,
        @Query("UserId") userId: String
    ): Response<PlaybackInfoResponse>

    @POST("Sessions/Playing")
    suspend fun reportPlaying(
        @Body report: PlaybackProgressReport
    ): Response<ResponseBody>

    @POST("Sessions/Playing/Progress")
    suspend fun reportProgress(
        @Body report: PlaybackProgressReport
    ): Response<ResponseBody>

    @POST("Sessions/Playing/Stopped")
    suspend fun reportStopped(
        @Body report: PlaybackProgressReport
    ): Response<ResponseBody>

    @POST("Users/{userId}/PlayedItems/{itemId}")
    suspend fun markPlayed(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String
    ): Response<ResponseBody>

    @DELETE("Users/{userId}/PlayedItems/{itemId}")
    suspend fun markUnplayed(
        @Path("userId") userId: String,
        @Path("itemId") itemId: String
    ): Response<ResponseBody>
}
