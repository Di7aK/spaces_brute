package com.disak.zaebali.utils

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri

private const val REQUEST_FILE_PICKER = 2

class FilePicker {
    private var listener: FilePickerListener? = null

    fun pickFile(activity: Activity, type: String, listener: FilePickerListener) {
        this.listener = listener
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = type
        activity.startActivityForResult(intent, REQUEST_FILE_PICKER)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == REQUEST_FILE_PICKER && resultCode == RESULT_OK && data?.data != null) {
            listener?.onFilePick(data.data!!)
        }
    }

    interface FilePickerListener {
        fun onFilePick(uri: Uri)
    }
}