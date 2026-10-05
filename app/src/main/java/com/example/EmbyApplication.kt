package com.example

import android.app.Application
import androidx.room.Room
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.download.MediaDownloadManager
import com.example.data.local.AppDatabase
import com.example.data.local.ServerPreferences
import com.example.data.repository.EmbyRepository
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class EmbyApplication : Application(), ImageLoaderFactory {
    lateinit var database: AppDatabase
        private set
    lateinit var serverPreferences: ServerPreferences
        private set
    lateinit var repository: EmbyRepository
        private set
    lateinit var downloadManager: MediaDownloadManager
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "embystream.db"
        ).fallbackToDestructiveMigration().build()

        serverPreferences = ServerPreferences(applicationContext)
        repository = EmbyRepository(applicationContext, serverPreferences, database)
        downloadManager = MediaDownloadManager(applicationContext, database, repository)
    }

    override fun newImageLoader(): ImageLoader {
        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val conn = serverPreferences.getConnection()
                val requestBuilder = chain.request().newBuilder()
                if (conn != null && conn.token.isNotBlank()) {
                    requestBuilder.header("X-Emby-Token", conn.token)
                    requestBuilder.header("X-Emby-Authorization", repository.authHeader)
                }
                chain.proceed(requestBuilder.build())
            }

        try {
            val trustAll = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAll, SecureRandom())
            clientBuilder.sslSocketFactory(sslContext.socketFactory, trustAll[0] as X509TrustManager)
            clientBuilder.hostnameVerifier { _, _ -> true }
        } catch (_: Exception) {}

        val okHttpClient = clientBuilder.build()

        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("emby_image_cache"))
                    .maxSizeBytes(80L * 1024 * 1024)
                    .build()
            }
            .crossfade(true)
            .build()
    }

    companion object {
        lateinit var instance: EmbyApplication
            private set
    }
}
