package com.disak.zaebali.ui

import android.app.Application
import androidx.lifecycle.*
import com.disak.zaebali.CAPTCHA_TIMEOUT
import com.disak.zaebali.R
import com.disak.zaebali.models.ProxyItem
import com.disak.zaebali.repository.SpacesRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel(private val app: Application) : AndroidViewModel(app) {
    private var spacesRepository: SpacesRepository = SpacesRepository.getInstance()

    var userLiveData = spacesRepository.userLiveData
    var authLiveData = spacesRepository.authLiveData

    private val _currentUserId = MutableLiveData<Int>()
    val currentUserId: LiveData<Int> = _currentUserId
    private val _log = MutableLiveData<String>()
    val log: LiveData<String> = _log
    val isProgress = MutableLiveData<Boolean>()

    var passwords: Array<String> = arrayOf()
    private var currentPassword = 0
    var proxy: MutableList<ProxyItem> = mutableListOf()
    private var currentProxy = 0
    var login = ""

    private fun getUserById(userId: Int) {
        _currentUserId.value = userId

        viewModelScope.launch {
            spacesRepository.getUserById(userId)
        }
    }

    fun parseProxy(proxyString: String) {
        val proxyList = proxyString.split("\n")
        proxyList.forEach {
            ProxyItem.parse(it)?.let { proxyItem -> proxy.add(proxyItem) }
        }
    }

    fun nextProxy() {
        getCurrentProxyItem().lastUsed = System.currentTimeMillis()

        val proxy = getNextProxyItem()

        val await = proxy.lastUsed + CAPTCHA_TIMEOUT - System.currentTimeMillis()
        if(await > 0) {
            viewModelScope.launch {
                _log.postValue(app.applicationContext.getString(R.string.proxy_wait, await / 1000))
                delay(await)
                if(isProgress.value == true) {
                    spacesRepository.create(proxy.host, proxy.port)
                    beginUser()
                }
            }
        } else {
            spacesRepository.create(proxy.host, proxy.port)
            beginUser()
        }
    }

    private fun getCurrentProxyItem() : ProxyItem {
        return proxy[currentProxy]
    }

    private fun getNextProxyItem() : ProxyItem {
        currentProxy ++
        currentProxy %= proxy.size
        return proxy[currentProxy]
    }

    private fun login(login: String, password: String) {
        viewModelScope.launch {
            spacesRepository.login(login, password)
        }
    }

    fun nextPassword() {
        currentPassword++
        currentPassword %= passwords.size

        if (currentPassword == 0) nextUser()
        else login(login, passwords[currentPassword])
    }

    fun beginUser() {
        login(login, passwords[currentPassword])
    }

    fun begin(userId: Int) {
        isProgress.postValue(true)
        getUserById(userId)
    }

    fun stop() {
        isProgress.postValue(false)
    }

    fun nextUser() {
        val uid = (_currentUserId.value ?: 0) + 1
        if (isProgress.value == true) getUserById(uid)
    }
}