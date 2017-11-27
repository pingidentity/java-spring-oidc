package com.pingidentity.ps.helpers;

import okhttp3.OkHttpClient;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

/**
 * Utility class to help with creating clients for the OkHttp framework.
 */
public final class OkHttpHelper {
    /**
     * Private constructor.
     */
    private OkHttpHelper() {
        // Private constructor for CheckStyle
    }

    /**
     * Create an OkHttp client that trusts all certificates.
     *
     * @return an OkHttpClient that trusts all certificates.
     *
     * @throws KeyManagementException   This is the general key management exception for all operations dealing with
     *                                  key management.
     * @throws NoSuchAlgorithmException This exception is thrown when a particular cryptographic algorithm is requested
     *                                  but is not available in the environment.
     */
    public static OkHttpClient getUnsafeOkHttpClient() {
        // Create a trust manager that does not validate certificate chains
        final TrustManager[] trustAllCerts = new TrustManager[] {
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(final java.security.cert.X509Certificate[] chain,
                                                   final String authType)
                            throws CertificateException {
                        // Empty Method
                    }

                    @Override
                    public void checkServerTrusted(final java.security.cert.X509Certificate[] chain,
                                                   final String authType)
                            throws CertificateException {
                        // Empty method
                    }

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return new java.security.cert.X509Certificate[]{};
                    }
                }
        };

        try {
            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            final OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(final String hostname, final SSLSession session) {
                    return true;
                }
            });

            final OkHttpClient okHttpClient = builder.build();

            return okHttpClient;
        } catch (KeyManagementException | NoSuchAlgorithmException ex) {
            throw new IllegalArgumentException(ex.getMessage(), ex);
        }
    }
}
