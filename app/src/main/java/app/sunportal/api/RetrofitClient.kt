package app.sunportal.api

import android.content.Context
import app.sunportal.data.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private var baseUrl: String = "http://ha4a.pp.ua:8080/"

    fun init(context: Context) {
        val repo = SettingsRepository(context)
        // Читаем URL сервера не в главном потоке — иначе чтение SharedPreferences
        // блокирует UI и выводит систему в ANR ("Application Not Responding").
        // Это блочит все клики, в том числе по бургеру/меню drawer.
        val executor = java.util.concurrent.Executors.newSingleThreadExecutor()
        executor.execute {
            try {
                baseUrl = runBlocking { repo.serverUrl.first() }
            } catch (e: Exception) { /* оставляем адрес по умолчанию */ }
            executor.shutdown()
        }
    }

    fun updateBaseUrl(newUrl: String) {
        baseUrl = newUrl
        rebuild()
    }

    fun getBaseUrl(): String = baseUrl

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private var retrofit: Retrofit = buildRetrofit()

    private fun buildRetrofit(): Retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private fun rebuild() {
        retrofit = buildRetrofit()
        _api = retrofit.create(ApiService::class.java)
    }

    private var _api: ApiService = retrofit.create(ApiService::class.java)
    val api: ApiService get() = _api
}
