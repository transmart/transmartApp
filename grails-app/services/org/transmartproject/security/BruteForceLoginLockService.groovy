package org.transmartproject.security

import com.google.common.cache.Cache
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import org.springframework.util.Assert

import javax.annotation.PostConstruct
import java.util.concurrent.TimeUnit

/**
 * This class counts failed login attempts per account.
 * It has functionality for checking when given account should be locked and on how long.
 *
 * idea taken from
 * http://www.grygoriy.com/blog/2012/10/06/prevent-brute-force-attack-with-spring-security/
 */
class BruteForceLoginLockService {

    private Cache failedAttempts

    int allowedNumberOfAttempts

    int lockTimeInMinutes

    @PostConstruct
    void init() {
        Assert.isTrue(allowedNumberOfAttempts > 0, 'allowedNumberOfAttempts has to be greater than 0')
        Assert.isTrue(lockTimeInMinutes > 0, 'lockTimeInMinutes has to be greater than 0')

        failedAttempts = CacheBuilder.newBuilder()
                .expireAfterWrite(lockTimeInMinutes, TimeUnit.MINUTES)
                .build({ 0 } as CacheLoader)
    }

    /**
     * Triggers on each unsuccessful login attempt and increases number of failedAttempts in local accumulator
     * @param login - username which is trying to login
     * @return
     */
    def failLogin(String login) {
        def numberOfAttempts = failedAttempts.get(login)
        log.debug "fail login ${login} previous number for failedAttempts $numberOfAttempts"
        failedAttempts.put(login, numberOfAttempts + 1)
    }

    /**
     * Triggers on each successful login attempt and resets number of failedAttempts in local accumulator
     * @param login - username which is login
     */
    def loginSuccess(String login) {
        if (!isLocked(login)) {
            log.debug "successfull login for ${login}"
            failedAttempts.invalidate(login)
        }
    }

    /**
     * Check weather account is locked.
     * @param login
     * @return
     */
    boolean isLocked(String login) {
        remainedAttempts(login) <= 0
    }

    /**
     * Remained attempts
     * @param login
     * @return
     */
    int remainedAttempts(String login) {
        int result = allowedNumberOfAttempts - failedAttempts.get(login)
        result < 0 ? 0 : result
    }

}
