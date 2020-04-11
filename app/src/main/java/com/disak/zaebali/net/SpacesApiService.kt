package com.disak.zaebali.net

import com.disak.zaebali.BASE_URI
import com.disak.zaebali.PROXY_TIMEOUT
import com.disak.zaebali.vo.Auth
import com.disak.zaebali.vo.User
import com.google.gson.GsonBuilder
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import java.net.InetSocketAddress
import java.net.Proxy
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

interface SpacesApiService {
    companion object {

        fun create(proxyHost: String, proxyPort: Int): SpacesApiService {
            val httpClient = OkHttpClient.Builder()

            val sslContext= SSLContext.getInstance("SSL")
            val trustManager = arrayOf<TrustManager>(UnsafeTrustManager())
            sslContext.init(null, trustManager, SecureRandom())

            val sslSocketFactory = sslContext.socketFactory

            val builder = OkHttpClient.Builder()
            builder.sslSocketFactory(sslSocketFactory, trustManager[0] as X509TrustManager)
            builder.hostnameVerifier { _, _ -> true }

            val proxy = Proxy(Proxy.Type.HTTP,  InetSocketAddress(proxyHost, proxyPort))

            httpClient.writeTimeout(PROXY_TIMEOUT.toLong(), TimeUnit.SECONDS)
            httpClient.connectTimeout(PROXY_TIMEOUT.toLong(), TimeUnit.SECONDS)
            httpClient.readTimeout(PROXY_TIMEOUT.toLong(), TimeUnit.SECONDS)
            if(proxyHost.isNotEmpty()) httpClient.proxy(proxy)

            val gson = GsonBuilder()
                .setLenient()
                .create()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URI)
                .client(httpClient.build())
                .addCallAdapterFactory(CoroutineCallAdapterFactory())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return retrofit.create(SpacesApiService::class.java)
        }
    }

    @Headers("X-Proxy: spaces")
    @GET("/mysite/index/{userId}/")
    fun getUserById(
        @Path("userId") userId: Int
    ) : Deferred<User>

    @FormUrlEncoded
    @POST("/api/auth/")
    fun auth(
        @Field("method") method: String,
        @Field("login") login: String,
        @Field("password") password: String
    ) : Deferred<Auth>
}