package com.pingidentity.ps;

import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;

import com.pingidentity.ps.helpers.OAuth2Helper;

@Controller
@SpringBootApplication
public class Application {

    /**
     * Main method.
     *
     * @param args       command line arguments.
     * @throws Exception The class {@code Exception} and its subclasses are a form of {@code Throwable} that indicates
     *                   conditions that a reasonable application might want to catch.
     */
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Home page.
     *
     * @return a string indicating which view to display.
     */
    @RequestMapping(value="/")
    private String index() {
        System.out.println("=== / route");

        return "index";
    }

    /**
     * A page to display the authentication code.
     *
     * @param request  the request object.
     * @param response the response object.
     * @param model    the model to be passed to the view.
     * @return         a string indicating which view to display.
     */
    @RequestMapping(value="/tokens/authcode")
    private String authcode(final HttpServletRequest request, final HttpServletResponse response, final Model model)
            throws IOException {

        System.out.println("=== /tokens/authcode route");

        redirectIfLoginRequired(request, response);

        model.addAttribute("authcode", request.getSession().getAttribute("authcode"));

        return "authcode";
    }

    /**
     * A page to display the access token.
     *
     * @param request  the request object.
     * @param response the response object.
     * @param model    the model to be passed to the view.
     * @return         a string indicating which view to display.
     */
    @RequestMapping(value="/tokens/access")
    private String access(final HttpServletRequest request, final HttpServletResponse response, final Model model)
            throws IOException {

        System.out.println("=== /tokens/access route");

        redirectIfLoginRequired(request, response);

        final DecodedJWT accessToken = (DecodedJWT) request.getSession().getAttribute("access_token");
        final Date expiresAt = accessToken.getExpiresAt();
        final String scopes = accessToken.getClaim("scope").asList(String.class).toString();
        final String clientId = accessToken.getClaim("client_id").asString();

        model.addAttribute("access_token", request.getSession().getAttribute("access_token"));
        model.addAttribute("expiresAt", expiresAt);
        model.addAttribute("subject", accessToken.getClaim("sub").asString());
        model.addAttribute("client_id", clientId);
        model.addAttribute("scopes", scopes.toString());

        return "access";
    }

    /**
     * A page to display the ID token.
     *
     * @param request  the request object.
     * @param response the response object.
     * @param model    the model to be passed to the view.
     * @return         a string indicating which view to display.
     */
    @RequestMapping(value="/tokens/id")
    private String id(final HttpServletRequest request, final HttpServletResponse response, final Model model)
            throws IOException {

        System.out.println("=== /tokens/id route");

        redirectIfLoginRequired(request, response);

        final DecodedJWT idToken = (DecodedJWT) request.getSession().getAttribute("id_token");

        final String audience = idToken.getAudience().toString();
        final Date issuedAt = idToken.getIssuedAt();
        final String issuer = idToken.getIssuer();
        final String subject = idToken.getSubject();

        model.addAttribute("id_token", request.getSession().getAttribute("id_token"));
        model.addAttribute("audience", audience);
        model.addAttribute("issuedAt", issuedAt);
        model.addAttribute("issuer", issuer);
        model.addAttribute("subject", subject);

        return "id";
    }

    @RequestMapping(value="/tokens/refresh")
    private String refresh(final HttpServletRequest request, final HttpServletResponse response, final Model model)
            throws IOException {

        System.out.println("=== /tokens/refresh route");

        redirectIfLoginRequired(request, response);

        model.addAttribute("refresh_token", request.getSession().getAttribute("refresh_token"));

        return "refresh";
    }

    /**
     * A page to receive the authentication code from the server.
     *
     * @param request      the request object.
     * @return             a string indicating which view to display.
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    @RequestMapping(value="/authentication/login")
    private String login(final HttpServletRequest request) throws IOException {
        System.out.println("=== /tokens/login route");

        final String authCode = request.getParameter("code");
        final String error = request.getParameter("error");
        final String errorDescription = request.getParameter("error_description");
        final String state = request.getParameter("state");
        final OAuth2Helper oauth2Helper = new OAuth2Helper(System.getenv("TOKEN_ENDPOINT"));
        String redirectUrl = "/";

        System.out.println("=== Code: " + authCode);

        final String clientId = System.getenv("CLIENT_ID");
        final String clientSecret = System.getenv("CLIENT_SECRET");
        final String redirectUri = System.getenv("REDIRECT_URI");
        final String scopes = System.getenv("SCOPES").replace(" ", "%20");

        final String rawToken = oauth2Helper.swapAuthenticationCode(
                clientId,
                clientSecret,
                redirectUri,
                scopes,
                authCode);

        // Parse the token
        final ObjectMapper mapper = new ObjectMapper();
        final JsonNode token = mapper.readTree(rawToken);

        final JsonNode accessToken = token.get("access_token");
        final JsonNode idToken = token.get("id_token");
        final JsonNode refreshToken = token.get("refresh_token");

        System.out.println();
        System.out.println("=== Access Token: " + accessToken.textValue());
        System.out.println();
        System.out.println("=== ID Token: " + idToken.textValue());



        final DecodedJWT accessJwt = JWT.decode(accessToken.asText());
        final DecodedJWT idJwt = JWT.decode(idToken.asText());

        request.getSession().setAttribute("authcode", authCode);
        request.getSession().setAttribute("access_token", accessJwt);
        request.getSession().setAttribute("id_token", idJwt);
        request.getSession().setAttribute("refresh_token", refreshToken.textValue());

        if (!state.isEmpty()) {
            // We have a state, redirect there
            redirectUrl = state;
        }

        return "redirect:" + redirectUrl;
    }

    /**
     * A page to process single logout.
     *
     * @return
     */
    @RequestMapping(value="/authentication/logout")
    private String logout() {
        System.out.println("=== /tokens/logout route");

        return "logout";
    }

    /**
     * Creates the redirect URL from configuration values.
     *
     * @param request                the request object.
     * @return                       the redirect URL to be used to initiate a login with Ping Federate.
     * @throws MalformedURLException Thrown to indicate that a malformed URL has occurred.
     */
    private String createRedirectUrl(final HttpServletRequest request) throws MalformedURLException {
        final String authorizationEndpoint = System.getenv("AUTHORIZATION_ENDPOINT");
        final String clientId = System.getenv("CLIENT_ID");
        final String adapterId = System.getenv("ADAPTER_ID");
        final String scopes = System.getenv("SCOPES").replace(" ", "%20");
        final String redirectUri = System.getenv("REDIRECT_URI");
        final String queryString = request.getQueryString();
        final String requestUrl = request.getRequestURL().toString();
        final URL url = new URL(requestUrl);

        final String state = url.getPath() + (queryString == null ? "" : "?" + queryString);

        final String redirectUrl = String.format(
                "%s?response_type=code&client_id=%s&pfidpadapterid=%s&scope=%s&redirect_uri=%s&state=%s",
                authorizationEndpoint,
                clientId,
                adapterId,
                scopes,
                redirectUri,
                state);

        return redirectUrl;
    }

    /**
     * Sends a redirect if the user needs to login.
     *
     * @param request      the request object.
     * @param response     the response object.
     * @throws IOException Signals that an I/O exception of some sort has occurred.
     */
    private void redirectIfLoginRequired(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        if (isLoginRequired(request)) {
            // Redirect to our authorization endpoint to begin the login process
            final String redirectUrl = createRedirectUrl(request);

            System.out.println("=== User is not logged in, redirecting: " + redirectUrl);

            response.sendRedirect(redirectUrl);
        }
    }

    /**
     * Determines if a user needs to login.
     *
     * @param request the request object.
     * @return        returns a boolean indicating that the user needs to login.
     */
    private boolean isLoginRequired(final HttpServletRequest request) {
        // If the access token cannot be found in the session then we need to login
        final DecodedJWT accessToken = (DecodedJWT) request.getSession().getAttribute("access_token");

        return accessToken == null;
    }
}