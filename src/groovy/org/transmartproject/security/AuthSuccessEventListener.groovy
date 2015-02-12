package org.transmartproject.security

import org.springframework.context.ApplicationListener
import org.springframework.security.authentication.event.AuthenticationSuccessEvent

class AuthSuccessEventListener implements ApplicationListener<AuthenticationSuccessEvent> {

    BruteForceLoginLockService bruteForceLoginLockService

    @Override
    void onApplicationEvent(AuthenticationSuccessEvent event) {
        bruteForceLoginLockService.loginSuccess(event.authentication.name)
    }
}
