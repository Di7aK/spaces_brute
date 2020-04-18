package com.disak.zaebali.repository

import androidx.annotation.StringRes
import com.disak.zaebali.repository.datasource.ResourceDataSource

class ResourceRepository(private val resourceDataSource: ResourceDataSource) {

    fun getString(@StringRes resId: Int) = resourceDataSource.getString(resId)

    fun getString(@StringRes resId: Int, vararg objects: Any) = resourceDataSource.getString(resId, *objects)
}