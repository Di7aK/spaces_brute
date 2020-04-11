package com.disak.zaebali.ui

import android.app.Application
import androidx.lifecycle.*
import com.disak.zaebali.net.ProxyProcessor
import com.disak.zaebali.repository.SpacesRepository
import kotlinx.coroutines.Dispatchers
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

    var passwords = mutableListOf<String>()
    private var currentPassword = 0
    var login = ""

    private fun getUserById(userId: Int) {
        _currentUserId.value = userId

        viewModelScope.launch {
            spacesRepository.getUserById(userId)
        }
    }

    fun nextProxy() {
        viewModelScope.launch(Dispatchers.IO) {
            ProxyProcessor.changeIp()

            beginUser()
        }
    }

    private var loginPasswordIndex = -1

    fun addLoginPassword() {
        val segments = login.split("_")
        loginPasswordIndex = passwords.size
        if(segments.size == 3) {
            passwords.add(segments[2])
        } else {
            passwords.add(segments[0])
        }
    }

    fun removeLoginPassword() {
        if(loginPasswordIndex != -1) {
            passwords.removeAt(loginPasswordIndex)
            loginPasswordIndex = -1
        }
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