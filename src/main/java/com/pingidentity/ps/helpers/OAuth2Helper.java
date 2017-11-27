package com.pingidentity.ps.helpers;

import okhttp3.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * A class to help exchange the authentication code for a set of tokens.
 */
public class OAuth2Helper {
    private static final MediaType X_WWW_FORM_URLENCODED = MediaType.parse("application/x-www-form-urlencoded");
    private String tokenUrl;

    /**
     * Constructor.
     *
     * @param tokenUrl the token URL for Ping Federate.
     */
    public OAuth2Helper(final String tokenUrl) {
        this.tokenUrl = tokenUrl;
    }

    /**
     * Returns the token URL.
     *
     * @return the token URL.
     */
    public String getTokenUrl() {
        return tokenUrl;
    }

    /**
     * Swaps the authentication code for an OpenID Connect token.
     *
     * @param clientId                  the client ID.
     * @param clientSecret              the client secret.
     * @param redirectUri               the redirect URI of the client.
     * @param scope                     the list of scopes requested.
     * @param authorizationCode         the authentication code returned by the server.
     * @return                          returns a string of the entire set of tokens.
     * @throws IOException              Signals that an I/O exception of some sort has occurred.
     */
    public String swapAuthenticationCode(final String clientId, final String clientSecret, final String redirectUri,
                                         final String scope, final String authorizationCode)
            throws IOException {

        String token = null;

        final String basicAuthString = clientId + ":" + clientSecret;
        final String basicAuth = Base64.getEncoder().encodeToString(basicAuthString.getBytes(StandardCharsets.UTF_8));
        final RequestBody body = RequestBody.create(X_WWW_FORM_URLENCODED, String.format(
                "grant_type=authorization_code&code=%s&redirect_uri=%s&scope=%s",
                authorizationCode, redirectUri, scope));

        // Don't ever do this in production because it allows any certificate!
        final OkHttpClient client = OkHttpHelper.getUnsafeOkHttpClient();

        final Request request = new Request.Builder()
                .url(tokenUrl)
                .post(body)
                .addHeader("Authorization", "Basic " + basicAuth)
                .addHeader("Content-Type", "application/x-www-form-urlencoded")
                .build();

        final Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            token = response.body().string();
        }

        return token;
    }
}
