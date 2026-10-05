package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.PlaybackQuality
import com.example.data.model.ServerConnection
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ServerPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("emby_prefs", Context.MODE_PRIVATE)

    private val _connectionFlow = MutableStateFlow(getConnection())
    val connectionFlow: StateFlow<ServerConnection?> = _connectionFlow.asStateFlow()

    fun getConnection(): ServerConnection? {
        val url = prefs.getString("server_url", null) ?: return null
        val token = prefs.getString("access_token", "") ?: ""
        val userId = prefs.getString("user_id", "") ?: ""
        val username = prefs.getString("username", "User") ?: "User"
        val serverName = prefs.getString("server_name", "Emby Server") ?: "Emby Server"
        val isDemo = prefs.getBoolean("is_demo", false)
        return ServerConnection(
            url = url,
            username = username,
            userId = userId,
            token = token,
            serverName = serverName,
            isDemo = isDemo
        )
    }

    fun saveConnection(connection: ServerConnection) {
        prefs.edit()
            .putString("server_url", connection.url.trimEnd('/'))
            .putString("access_token", connection.token)
            .putString("user_id", connection.userId)
            .putString("username", connection.username)
            .putString("server_name", connection.serverName)
            .putBoolean("is_demo", connection.isDemo)
            .apply()
        _connectionFlow.value = connection
    }

    fun clearConnection() {
        prefs.edit()
            .remove("server_url")
            .remove("access_token")
            .remove("user_id")
            .remove("username")
            .remove("server_name")
            .remove("is_demo")
            .apply()
        _connectionFlow.value = null
    }

    // Transcode and Playback settings
    var hardwareAcceleration: Boolean
        get() = prefs.getBoolean("hw_accel", true)
        set(value) = prefs.edit().putBoolean("hw_accel", value).apply()

    var defaultQuality: PlaybackQuality
        get() {
            val name = prefs.getString("default_quality", PlaybackQuality.DIRECT_PLAY.name)
            return try {
                PlaybackQuality.valueOf(name ?: PlaybackQuality.DIRECT_PLAY.name)
            } catch (e: Exception) {
                PlaybackQuality.DIRECT_PLAY
            }
        }
        set(value) = prefs.edit().putString("default_quality", value.name).apply()

    var preferredSubtitleLanguage: String
        get() = prefs.getString("preferred_sub_lang", "English") ?: "English"
        set(value) = prefs.edit().putString("preferred_sub_lang", value).apply()

    var autoSubtitles: Boolean
        get() = prefs.getBoolean("auto_subtitles", true)
        set(value) = prefs.edit().putBoolean("auto_subtitles", value).apply()

    var audioChannels: Int
        get() = prefs.getInt("audio_channels", 2)
        set(value) = prefs.edit().putInt("audio_channels", value).apply()
}
