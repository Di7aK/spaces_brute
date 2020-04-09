package com.disak.zaebali.models

import androidx.core.text.isDigitsOnly

data class ProxyItem(val host: String, val port: Int, var lastUsed: Long = 0) {
    companion object {
        fun parse(proxyString: String) : ProxyItem? {
            val segments = proxyString.split(":")
            if(segments.size == 2 && segments[1].isDigitsOnly()) return ProxyItem(segments[0].trim(), segments[1].toInt())
            return null
        }
    }

    override fun toString(): String {
        return "$host:$port"
    }
}