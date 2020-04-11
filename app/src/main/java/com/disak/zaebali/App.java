package com.disak.zaebali;

import android.app.Application;

import com.msopentech.thali.android.toronionproxy.AndroidOnionProxyManager;
import com.msopentech.thali.toronionproxy.OnionProxyManager;

public class App extends Application {
    public static OnionProxyManager onionProxyManager = null;

    @Override
    public void onCreate() {
        super.onCreate();

        final String fileStorageLocation = "torfiles";

        Thread initThread = new Thread() {
            @Override
            public void run() {
                onionProxyManager =
                        new AndroidOnionProxyManager(App.this, fileStorageLocation);
            }
        };
        initThread.start();
    }
}
