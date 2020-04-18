package com.disak.zaebali.repository

import com.disak.zaebali.repository.datasource.SpacesDataSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SpacesRepository(private var spacesDataSource: SpacesDataSource) {

    suspend fun getUserById(userId: Int) = withContext(Dispatchers.IO) {
        spacesDataSource.getUserById(userId)
    }

    suspend fun login(login: String, password: String) = withContext(Dispatchers.IO) {
        spacesDataSource.auth(login, password)
    }
}