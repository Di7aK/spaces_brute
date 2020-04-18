package com.disak.zaebali.repository

import android.content.Context
import androidx.annotation.StringRes

class ResourceProvider(private val context: Context) {

    fun getString(@StringRes resId: Int) = context.getString(resId)
}