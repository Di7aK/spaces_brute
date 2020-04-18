package com.disak.zaebali.extensions

import android.content.Context
import android.content.Intent
import android.net.Uri

fun String.openAsWeb(context: Context) {
    context.startActivity(Intent(Intent.ACTION_VIEW).apply {
        data = Uri.parse(this@openAsWeb)
    })
}