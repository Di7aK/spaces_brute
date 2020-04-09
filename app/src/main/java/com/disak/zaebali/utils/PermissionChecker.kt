package com.disak.zaebali.utils

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

private const val REQUEST_PERMISSIONS = 1

class PermissionChecker(
    private val activity: AppCompatActivity,
    private val listener: PermissionCheckerListener
) {

    fun requestPermissions(permissions: Array<String>) {
        ActivityCompat.requestPermissions(activity, permissions, REQUEST_PERMISSIONS)
    }

    fun onPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if(requestCode == REQUEST_PERMISSIONS) {
            val granted = grantResults.none { it != PackageManager.PERMISSION_GRANTED }
            listener.onResult(granted)
        }
    }

    interface PermissionCheckerListener {
        fun onResult(granted: Boolean)
    }
}