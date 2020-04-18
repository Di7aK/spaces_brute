package com.disak.zaebali.ui

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.disak.zaebali.LOG_SIZE
import com.disak.zaebali.R
import com.disak.zaebali.utils.FilePicker
import com.disak.zaebali.utils.PermissionChecker
import com.disak.zaebali.utils.TorProgressTask
import com.disak.zaebali.extensions.openAsWeb
import com.jaiselrahman.filepicker.model.MediaFile
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

val REQUIRED_PERMISSIONS =
    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

class MainActivity : AppCompatActivity(), PermissionChecker.PermissionCheckerListener {
    private val permissionChecker = PermissionChecker(this, this)
    private val filePicker = FilePicker()
    private val mainViewModel by viewModel<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
                    mainViewModel.target = file.uri.toString()

                    updateResultButton()
                }
            })
        }

        updateCustomLoginButton()
        updatePasswordsButton()
        updateResultButton()
        updateState()

        TorProgressTask(this@MainActivity).execute()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.menu_github) getString(R.string.source_url).openAsWeb(this)
        return super.onOptionsItemSelected(item)
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
        mainViewModel.passwordEqualLogin = loginPassword.isChecked
        mainViewModel.passwordEqualLoginLower = loginPasswordLower.isChecked

        mainViewModel.begin(from)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        filePicker.onActivityResult(requestCode, resultCode, data)
    }

    private fun stop() {
        mainViewModel.stop()
    }

    private fun subscribeLiveData() {
        mainViewModel.errorLiveData.observe(this, Observer {
            log(it)
            stop()
        })

        mainViewModel.currentUserId.observe(this, Observer {
            startFrom.setText(it.toString())
        })

        mainViewModel.log.observe(this, Observer {
            log(it)
        })

        mainViewModel.isProgress.observe(this, Observer {
            updateButton()
        })

        mainViewModel.checked.observe(this, Observer {
            updateState()
        })

        mainViewModel.success.observe(this, Observer {
            updateState()
        })
    }

    private fun updateState() {
        state.text = getString(R.string.state, mainViewModel.checked.value, mainViewModel.success.value)
    }

    override fun onResult(granted: Boolean) {
        if (!granted) log(getString(R.string.permissions_required))
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
