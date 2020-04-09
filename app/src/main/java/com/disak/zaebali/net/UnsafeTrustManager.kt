package com.disak.zaebali.net

import android.annotation.SuppressLint
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

class UnsafeTrustManager : X509TrustManager {
    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }

    @SuppressLint("TrustAllX509TrustManager")
    @Throws(CertificateException::class)
    override fun checkClientTrusted(
        chain: Array<X509Certificate>,
        authType: String
    ) {
    }

    @SuppressLint("TrustAllX509TrustManager")
    @Throws(CertificateException::class)
    override fun checkServerTrusted(
        chain: Array<X509Certificate>,
        authType: String
    ) {
    }
}