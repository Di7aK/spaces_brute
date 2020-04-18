package com.disak.zaebali

import android.app.Application
import com.disak.zaebali.sl.appModules
import com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager
import com.msopentech.thali.toronionproxy.OnionProxyManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

private const val FILE_STORAGE_LOCATION = "torfiles"
lateinit var onionProxyManager: OnionProxyManager

class BruteApp : Application() {

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            onionProxyManager = AndroidOnionProxyManager(this@BruteApp, FILE_STORAGE_LOCATION)
        }

        startKoin {
            androidContext(this@BruteApp)
            modules(appModules)
        }
    }
}