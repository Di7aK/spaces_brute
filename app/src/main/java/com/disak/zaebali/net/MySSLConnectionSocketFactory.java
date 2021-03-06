package com.disak.zaebali.net;


import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import cz.msebera.android.httpclient.HttpHost;
import cz.msebera.android.httpclient.conn.ssl.SSLConnectionSocketFactory;
import cz.msebera.android.httpclient.protocol.HttpContext;
import cz.msebera.android.httpclient.util.Args;

public class MySSLConnectionSocketFactory extends SSLConnectionSocketFactory {

    MySSLConnectionSocketFactory(final SSLContext sslContext) {
        super(sslContext);
    }

    @Override
    public Socket createSocket(final HttpContext context) {
        return new Socket();
    }

    @Override
    public Socket connectSocket(
            int connectTimeout,
            Socket socket,
            final HttpHost host,
            final InetSocketAddress remoteAddress,
            final InetSocketAddress localAddress,
            final HttpContext context) throws IOException {
        Args.notNull(host, "HTTP host");
        Args.notNull(remoteAddress, "Remote address");
        InetSocketAddress socksaddr = (InetSocketAddress) context.getAttribute("socks.address");
        socket = new Socket();
        connectTimeout = 100000;
        socket.setSoTimeout(connectTimeout);
        socket.connect(new InetSocketAddress(socksaddr.getHostName(), socksaddr.getPort()), connectTimeout);
        DataOutputStream outputStream = new DataOutputStream(socket.getOutputStream());
        outputStream.write((byte) 0x04);
        outputStream.write((byte) 0x01);
        outputStream.writeShort((short) host.getPort());
        outputStream.writeInt(0x01);
        outputStream.write((byte) 0x00);
        outputStream.write(host.getHostName().getBytes());
        outputStream.write((byte) 0x00);

        DataInputStream inputStream = new DataInputStream(socket.getInputStream());
        if (inputStream.readByte() != (byte) 0x00 || inputStream.readByte() != (byte) 0x5a) {
            throw new IOException("SOCKS4a connect failed");
        } else
            Log.v("SSLConnectionSF", "SOCKS4a connect ok!");
        inputStream.readShort();
        inputStream.readInt();

        SSLConnectionSocketFactory sslSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createLayeredSocket(socket, host.getHostName(), host.getPort(), context);
        prepareSocket(sslSocket);
        return sslSocket;
    }

}
