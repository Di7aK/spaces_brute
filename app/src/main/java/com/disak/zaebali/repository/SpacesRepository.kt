package com.disak.zaebali.repository

import com.disak.zaebali.models.Result
import androidx.lifecycle.MutableLiveData
import com.disak.zaebali.models.ProxyItem
import com.disak.zaebali.net.SpacesDataSource
import com.disak.zaebali.vo.Auth
import com.disak.zaebali.vo.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpacesRepository {
    private var spacesDataSource: SpacesDataSource = SpacesDataSource.newInstance("", 0)

    val userLiveData = MutableLiveData<Result<User>>()
    val authLiveData = MutableLiveData<Result<Auth>>()

    suspend fun getUserById(userId: Int) = withContext(Dispatchers.IO) {
        val result = spacesDataSource.getUserById(userId)
        userLiveData.postValue(result)
    }

    suspend fun login(login: String, password: String) = withContext(Dispatchers.IO) {
        val result = spacesDataSource.auth(login, password)
        authLiveData.postValue(result)
    }

    fun create(proxyHost: String, proxyPort: Int) {
        spacesDataSource = SpacesDataSource.newInstance(proxyHost, proxyPort)
        spacesDataSource.proxy = ProxyItem(proxyHost, proxyPort)
    }

    companion object {
        private var INSTANCE: SpacesRepository? = null

        @JvmStatic
        fun getInstance(): SpacesRepository {
            return INSTANCE ?: SpacesRepository().apply { INSTANCE = this }
        }

        @JvmStatic
        fun destroyInstance() {
            INSTANCE = null
        }
    }
}