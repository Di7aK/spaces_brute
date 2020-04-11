package com.disak.zaebali.net

import com.disak.zaebali.BASE_URI
import com.disak.zaebali.PROXY_TIMEOUT
import com.disak.zaebali.models.ProxyItem
import com.disak.zaebali.models.Result
import com.disak.zaebali.vo.Auth
import com.disak.zaebali.vo.User
import com.google.gson.GsonBuilder
import cz.msebera.android.httpclient.NameValuePair
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity
import cz.msebera.android.httpclient.message.BasicNameValuePair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.InetSocketAddress
import java.net.Proxy
import java.net.URL

class SpacesDataSource(private val spacesApiService: SpacesApiService) {
    private val gson = GsonBuilder().setLenient().create()
    var proxy: ProxyItem? = null

    suspend fun getUserById(userId: Int): Result<User> = withContext(Dispatchers.IO) {
        //val request = spacesApiService.getUserById(userId)
        try {
            /*val response = ProxyProcessor.executeRequest(
                "$BASE_URI/mysite/index/$userId/",
                mapOf("X-Proxy" to "spaces"),
                null
            )*/
            val response = ProxyProcessor.executeGetRequest(
                "$BASE_URI/mysite/index/$userId/",
                mapOf("X-Proxy" to "spaces"),
                null
            )
            /*
            val url = URL("$BASE_URI/mysite/index/$userId/")
            val connection = if (proxy != null) {
                url.openConnection(
                    Proxy(
                        Proxy.Type.SOCKS,
                        InetSocketAddress(proxy!!.host, proxy!!.port)
                    )
                )
            } else {
                url.openConnection()
            }
            connection.setRequestProperty("X-Proxy", "spaces")
            val data = connection.inputStream.bufferedReader().readText()
            val user = gson.fromJson<User>(data, User::class.java)

             */
            val data = response.entity.content.bufferedReader().use { it.readText() }
            val user = gson.fromJson<User>(data, User::class.java)

            Result.Success(user)
        } catch (ex: HttpException) {
            Result.Error(ex)
        } catch (ex: Throwable) {
            Result.Error(ex)
        }
    }

    suspend fun auth(login: String, password: String): Result<Auth> = withContext(Dispatchers.IO) {
        try {
            val params = mutableListOf<NameValuePair>()
            params.add(BasicNameValuePair("method", "login"))
            params.add(BasicNameValuePair("login", login))
            params.add(BasicNameValuePair("password", password))
            val response = ProxyProcessor.executePostRequest(
                "$BASE_URI/api/auth/",
                mapOf(),
                UrlEncodedFormEntity(params)
            )

            val data = response.entity.content.bufferedReader().use { it.readText() }

            val result = gson.fromJson<Auth>(data, Auth::class.java)

            Result.Success(result.apply {
                this.login = login
                this.password = password
            })
        } catch (ex: HttpException) {
            Result.Error(ex)
        } catch (ex: Throwable) {
            Result.Error(ex)
        }
        /*
        val request = spacesApiService.auth(METHOD_LOGIN, login, password)
        try {
            val response = request.await()

            Result.Success(response.apply {
                this.login = login
                this.password = password
            })
        } catch (ex: HttpException) {
            Result.Error(ex)
        } catch (ex: Throwable) {
            Result.Error(ex)
        }*/
    }

    fun post(url: String, body: String): String {
        return URL(url).let {
            if (proxy != null) {
                it.openConnection(
                    Proxy(
                        Proxy.Type.SOCKS,
                        InetSocketAddress(proxy!!.host, proxy!!.port)
                    )
                )
            } else {
                it.openConnection()
            }
        }.let {
            it as HttpURLConnection
        }.apply {
            connectTimeout = PROXY_TIMEOUT
            readTimeout = PROXY_TIMEOUT
            setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
            requestMethod = "POST"

            doOutput = true
            val outputWriter = OutputStreamWriter(outputStream)
            outputWriter.write(body)
            outputWriter.flush()
        }.let {
            if (it.responseCode == 200) it.inputStream else it.errorStream
        }.let { streamToRead ->
            BufferedReader(InputStreamReader(streamToRead)).use {
                val response = StringBuffer()

                var inputLine = it.readLine()
                while (inputLine != null) {
                    response.append(inputLine)
                    inputLine = it.readLine()
                }
                it.close()
                response.toString()
            }
        }
    }

    companion object {
        fun newInstance(proxyHost: String, proxyPort: Int) =
            SpacesDataSource(SpacesApiService.create(proxyHost, proxyPort))
    }
}