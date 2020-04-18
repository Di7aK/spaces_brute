package com.disak.zaebali.repository

import com.disak.zaebali.repository.datasource.FileDataSource

class FileRepository(private val fileDataSource: FileDataSource) {

    fun openInputStream(file: String) =
        fileDataSource.openInputStream(file)

    fun openOutputStream(file: String, mode: String) =
        fileDataSource.openOutputStream(file, mode)
}