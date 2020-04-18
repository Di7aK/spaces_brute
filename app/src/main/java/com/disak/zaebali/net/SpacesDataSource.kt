package com.disak.zaebali.net

import com.disak.zaebali.BASE_URI
import com.disak.zaebali.models.Result
import com.disak.zaebali.vo.Auth
import com.disak.zaebali.vo.User
import com.google.gson.GsonBuilder
import cz.msebera.android.httpclient.HttpException
import cz.msebera.android.httpclient.NameValuePair
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity
import cz.msebera.android.httpclient.message.BasicNameValuePair
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpacesDataSource {
    private val gson = GsonBuilder().setLenient().create()

    suspend fun getUserById(userId: Int): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = ProxyProcessor.executeGetRequest(
                "$BASE_URI/mysite/index/$userId/",
                mapOf("X-Proxy" to "spaces"),
                null
            )

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
    }

    companion object {
        fun newInstance() = SpacesDataSource()
    }
}