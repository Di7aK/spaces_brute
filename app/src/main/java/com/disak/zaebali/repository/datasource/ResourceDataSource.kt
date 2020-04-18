package com.disak.zaebali.repository.datasource

import android.content.Context
import androidx.annotation.StringRes

class ResourceDataSource(private val context: Context) {

    fun getString(@StringRes resId: Int) = context.getString(resId)

    fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String {
        return context.getString(resId, *formatArgs)
    }
}