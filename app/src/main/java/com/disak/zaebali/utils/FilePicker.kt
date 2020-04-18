package com.disak.zaebali.utils

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import com.jaiselrahman.filepicker.activity.FilePickerActivity
import com.jaiselrahman.filepicker.config.Configurations
import com.jaiselrahman.filepicker.model.MediaFile

private const val REQUEST_FILE_PICKER = 2

class FilePicker {
    private var listener: FilePickerListener? = null

    fun pickFile(activity: Activity, listener: FilePickerListener) {
        this.listener = listener

        val intent = Intent(activity, FilePickerActivity::class.java)
        intent.putExtra(
            FilePickerActivity.CONFIGS, Configurations.Builder()
                .setCheckPermission(true)
                .setShowFiles(true)
                .setShowImages(false)
                .setShowVideos(false)
                .setSingleChoiceMode(true)
                .setSkipZeroSizeFiles(false)
                .setSuffixes("txt")
                .setSingleClickSelection(true)
                .build()
        )
        activity.startActivityForResult(intent, REQUEST_FILE_PICKER)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_FILE_PICKER && resultCode == RESULT_OK) {
            val files: ArrayList<MediaFile>? =
                data?.getParcelableArrayListExtra(FilePickerActivity.MEDIA_FILES)
            files?.firstOrNull()?.let {
                listener?.onFilePick(it)
            }
        }
    }

    interface FilePickerListener {
        fun onFilePick(file: MediaFile)
    }
}