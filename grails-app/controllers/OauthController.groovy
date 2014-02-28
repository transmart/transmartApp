import org.springframework.security.access.annotation.Secured

class OauthController {

    /** as per request of R client just return the code */
    @Secured('IS_AUTHENTICATED_REMEMBERED')
    def verify() {
        // is more needed? Returning the code from the request is actually
        // good enough as we are running oauth and the app in the same app
        render params.code
    }

}
