# Java/Spring OpenID Connect Sample

### Overview

The project is a Java/Spring sample of the [OpenID Connect Authorization Code Flow](http://openid.net/specs/openid-connect-core-1_0.html#CodeFlowAuth) with Ping Federate.

### Configuration

This website is meant to work with [Heroku](https://www.heroku.com/) and requires a `.env` file with the following values:

* AUTHORIZATION_ENDPOINT - the authorization endpoint for Ping Federate
* TOKEN_ENDPOINT - the token endpoint for Ping Federate
* CLIENT_ID - your configured client ID for this website
* CLIENT_SECRET - your configured client secret for this website
* ADAPTER_ID - the adapter ID to be used by this website
* REDIRECT_URI - the redirect URI. This should be the `/authentication/login` endpoint
* SCOPES - the scopes to be sent to Ping Federate. If including multiple scopes, separate with a space; this string is URL encoded before being sent to the server

```
AUTHORIZATION_ENDPOINT=https://localhost:9031/as/authorization.oauth2
TOKEN_ENDPOINT=https://localhost:9031/as/token.oauth2
CLIENT_ID=<your client id>
CLIENT_SECRET=<your client secret>
ADAPTER_ID=<your adapter id>
REDIRECT_URI=http://localhost:5000/authentication/login
SCOPES=openid profile
```

### Disclaimer

This software is open sourced by Ping Identity but not supported commercially as such. Any questions/issues should go to the Github issues tracker or discuss on the [Ping Identity Developer Communities](https://community.pingidentity.com/collaborate). See also the DISCLAIMER file in this directory.

Ping Identity Developer Communities: https://community.pingidentity.com/collaborate
<br/>
Ping Identity Developer Site: https://developer.pingidentity.com/connect
