package com.disak.zaebali.ui
import androidx.lifecycle.*
import com.disak.zaebali.R
import com.disak.zaebali.extensions.singleArgViewModelFactory
import com.disak.zaebali.net.ProxyProcessor
import com.disak.zaebali.repository.SpacesRepository
import com.disak.zaebali.vo.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.disak.zaebali.models.Result
import com.disak.zaebali.repository.ResourceProvider
import java.util.*

class MainViewModel(private val resourceProvider: ResourceProvider) : ViewModel() {
    companion object {
        val FACTORY = singleArgViewModelFactory(::MainViewModel)
    }
    private var spacesRepository: SpacesRepository = SpacesRepository.getInstance()

    var userLiveData = spacesRepository.userLiveData
    var authLiveData = spacesRepository.authLiveData

    private val _currentUserId = MutableLiveData<Int>()
    val currentUserId: LiveData<Int> = _currentUserId
    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> = _errorLiveData
    private val _log = MutableLiveData<String>()
    val log: LiveData<String> = _log
    val isProgress = MutableLiveData<Boolean>()

    var passwords = mutableListOf<String>()
    var extraPasswords = mutableListOf<String>()
    private var currentPassword = 0
    var loginList = mutableListOf<String>()
    var login = ""
    var targetName: String? = null

    private fun getUserById(userId: Int) {
        _currentUserId.value = userId

        CoroutineScope(Dispatchers.IO).launch {
            spacesRepository.getUserById(userId)
        }
    }

    fun nextProxy() {
        viewModelScope.launch(Dispatchers.IO) {
            ProxyProcessor.changeIp()

            beginUser()
        }
    }

    fun addLoginPassword(lower: Boolean) {
        if(login.startsWith("_")) {
            val index = login.indexOf("_", 2)
            val pass= login.substring(index + 1, login.length)

            val lowerPassword = pass.toLowerCase(Locale.getDefault())
            if(lower && extraPasswords.indexOf(lowerPassword) == -1) extraPasswords.add(lowerPassword)
            else if(extraPasswords.indexOf(pass) == -1) extraPasswords.add(pass)
        } else {
            val lowerPassword = login.toLowerCase(Locale.getDefault())
            if(lower && extraPasswords.indexOf(lowerPassword) == -1) extraPasswords.add(lowerPassword)
            else if(extraPasswords.indexOf(login) == -1) extraPasswords.add(login)
        }
    }

    fun removeLoginPassword() {
        passwords.removeAll(extraPasswords)
        extraPasswords.clear()
    }

    private fun login(login: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
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
        passwords.addAll(extraPasswords)
        if(passwords.isEmpty()) {
            _errorLiveData.postValue(resourceProvider.getString(R.string.need_passwords))
        } else login(login, passwords[currentPassword])
    }

    fun begin(userId: Int) {
        if(targetName.isNullOrBlank()) {
            _errorLiveData.postValue(resourceProvider.getString(R.string.target_not_set))
        } else {
            isProgress.postValue(true)
            getUserById(userId)
        }
    }

    fun stop() {
        isProgress.postValue(false)
    }

    fun nextUser() {
        if (isProgress.value != true) return

        if(loginList.isEmpty()) {
            val uid = (_currentUserId.value ?: 0) + 1
            getUserById(uid)
        } else {
            val login = loginList.first()
            loginList.remove(login)
            userLiveData.postValue(Result.Success(User(login)))
        }
    }
}