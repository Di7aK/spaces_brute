package com.disak.zaebali.models

sealed class Result<out T : Any> {

    data class Progress<out T : Any>(val current: Long, val total: Long) : Result<T>()

    data class Success<out T : Any>(val data: T) : Result<T>()

    data class Error(val exception: Throwable) : Result<Nothing>()

    class Ready<out T : Any> : Result<T>()
}