package com.disak.zaebali.net

import com.disak.zaebali.models.Result
import com.disak.zaebali.vo.Auth
import com.disak.zaebali.vo.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class SpacesDataSource (private val spacesApiService: SpacesApiService) {

    suspend fun getUserById(userId: Int) : Result<User> = withContext(Dispatchers.IO) {
        val request = spacesApiService.getUserById(userId)
        try {
            val response = request.await()

            Result.Success(response)
        } catch (ex: HttpException) {
            Result.Error(ex)
        } catch (ex: Throwable) {
            Result.Error(ex)
        }
    }

    suspend fun auth(login: String, password: String) : Result<Auth> = withContext(Dispatchers.IO) {
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
        }
    }

    companion object {
        fun newInstance(proxyHost: String, proxyPort: Int) = SpacesDataSource(SpacesApiService.create(proxyHost, proxyPort))

        fun recreate(proxyHost: String, proxyPort: Int) = SpacesDataSource(SpacesApiService.create(proxyHost, proxyPort))
    }
}