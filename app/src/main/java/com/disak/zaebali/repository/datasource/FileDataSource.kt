package com.disak.zaebali.repository.datasource

import android.content.Context
import android.net.Uri

class FileDataSource(private val context: Context) {

    fun openInputStream(file: String) =
        context.contentResolver.openInputStream(Uri.parse(file))

    fun openOutputStream(file: String, mode: String) =
        context.contentResolver.openOutputStream(Uri.parse(file), mode)
}