package org.transmartproject.security

import javax.net.ssl.HostnameVerifier
import javax.net.ssl.HttpsURLConnection
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSession
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class SSLCertificateValidation {

    public static void disable() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS")
            TrustManager[] trustManagerArray = [ new NullX509TrustManager() ]
            sslContext.init(null, trustManagerArray, null)
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory())
            HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier())
        } catch(Exception e) {
            e.printStackTrace()
        }
    }

    private static class NullX509TrustManager implements X509TrustManager {
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException { }
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0]
        }
    }

    private static class NullHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true
        }
    }

}