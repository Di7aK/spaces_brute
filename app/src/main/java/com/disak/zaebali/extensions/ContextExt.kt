package com.disak.zaebali.extensions

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.toast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

fun Context.toast(@StringRes message: Int) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()