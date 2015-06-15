import grails.converters.JSON
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.annotation.Secured
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.provider.OAuth2Authentication
import org.springframework.security.oauth2.provider.token.TokenStore

class OauthController {

    @Autowired
    private TokenStore tokenStore

    // This is just poorly named. There's nothing about "verification" here
    /** as per request of R client just return the code */
    @Secured('IS_AUTHENTICATED_REMEMBERED')
    def verify() {
        // is more needed? Returning the code from the request is actually
        // good enough as we are running oauth and the app in the same app
        render params.code
    }

    @Secured(['ROLE_CLIENT'])
    def inspectToken() {
        header 'Cache-Control', 'no-store'
        header 'Pragma', 'no-cache'

        Authentication auth = SecurityContextHolder.context.authentication
        if (!(auth instanceof OAuth2Authentication)) {
            throw new IllegalStateException(
                    'Method called without oauth2 authentication present')
        }
        OAuth2Authentication oauthAuth = auth

        Map res = [:]
        res['clientId'] = oauthAuth.storedRequest.clientId
        res['redirectUri'] = oauthAuth.storedRequest.redirectUri
        res['token'] = tokenStore.readAccessToken(oauthAuth.details.tokenValue)
        oauthAuth.principal.with {
            res['principal'] = [
                    id: id,
                    username: username,
                    userRealName: userRealName,
                    authorities: authorities*.toString(),
            ]
        }

        render res as JSON
    }

}
