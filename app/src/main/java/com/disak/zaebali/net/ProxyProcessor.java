package com.disak.zaebali.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;

import cz.msebera.android.httpclient.HttpHeaders;
import cz.msebera.android.httpclient.HttpResponse;
import cz.msebera.android.httpclient.client.HttpClient;
import cz.msebera.android.httpclient.client.entity.UrlEncodedFormEntity;
import cz.msebera.android.httpclient.client.methods.HttpGet;
import cz.msebera.android.httpclient.client.methods.HttpPost;
import cz.msebera.android.httpclient.client.protocol.HttpClientContext;
import cz.msebera.android.httpclient.config.Registry;
import cz.msebera.android.httpclient.config.RegistryBuilder;
import cz.msebera.android.httpclient.conn.socket.ConnectionSocketFactory;
import cz.msebera.android.httpclient.impl.client.HttpClients;
import cz.msebera.android.httpclient.impl.conn.PoolingHttpClientConnectionManager;
import cz.msebera.android.httpclient.ssl.SSLContexts;

import static com.disak.zaebali.BruteAppKt.onionProxyManager;

public class ProxyProcessor {

    private static HttpClient getNewHttpClient() {
        Registry<ConnectionSocketFactory> reg = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", new MyConnectionSocketFactory())
                .register("https", new MySSLConnectionSocketFactory(SSLContexts.createSystemDefault()))
                .build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(reg);
        return HttpClients.custom()
                .setConnectionManager(cm)
                .build();
    }

    public static void changeIp() {
        try {
            onionProxyManager.enableNetwork(false);
            onionProxyManager.enableNetwork(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static HttpResponse executeGetRequest(String url, Map<String, String> headers, UrlEncodedFormEntity params) throws IOException {
        HttpClient httpClient = getNewHttpClient();
        int port = onionProxyManager.getIPv4LocalHostSocksPort();
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", port);
        HttpClientContext clientContext = HttpClientContext.create();
        clientContext.setAttribute("socks.address", socketAddress);

        HttpGet request = new HttpGet(url);

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }

        request.setHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        request.setHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, sdch");
        request.setHeader(HttpHeaders.ACCEPT_LANGUAGE, "ru,en-US;q=0.8,en;q=0.6");
        request.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");

        return httpClient.execute(request, clientContext);

    }

    public static HttpResponse executePostRequest(String url, Map<String, String> headers, UrlEncodedFormEntity params) throws IOException {
        HttpClient httpClient = getNewHttpClient();
        int port = onionProxyManager.getIPv4LocalHostSocksPort();
        InetSocketAddress socketAddress = new InetSocketAddress("127.0.0.1", port);
        HttpClientContext clientContext = HttpClientContext.create();
        clientContext.setAttribute("socks.address", socketAddress);

        HttpPost request = new HttpPost(url);

        if (params != null) {
            request.setEntity(params);
        }
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                request.setHeader(entry.getKey(), entry.getValue());
            }
        }

        request.setHeader(HttpHeaders.ACCEPT, "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        request.setHeader(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, sdch");
        request.setHeader(HttpHeaders.ACCEPT_LANGUAGE, "ru,en-US;q=0.8,en;q=0.6");
        request.setHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.87 Safari/537.36");

        return httpClient.execute(request, clientContext);

    }

}
