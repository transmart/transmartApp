package org.transmartproject.security

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import grails.test.GrailsUnitTestCase

import java.util.concurrent.TimeUnit

class BruteForceLoginLockServiceTests extends GrailsUnitTestCase {

    BruteForceLoginLockService service

    void setUp() {
        super.setUp()
        service = new BruteForceLoginLockService(allowedNumberOfAttempts: 2, lockTimeInMinutes: 10)
        service.init()
    }

    void testLockAfterAllowedNumberOfAttempts() {
        assertFalse(service.isLocked('test'))
        service.failLogin('test')
        assertFalse(service.isLocked('test'))
        service.failLogin('test')
        assertTrue(service.isLocked('test'))
    }

    void testSuccessfulTrialDoesNotUnlock() {
        service.allowedNumberOfAttempts = 1

        service.failLogin('test')
        assertTrue(service.isLocked('test'))
        service.loginSuccess('test')
        assertTrue(service.isLocked('test'))
    }

    void testSuccessfulTrialRemovesBadTrialsCount() {
        assertEquals(2, service.remainedAttempts('test'))
        service.failLogin('test')
        assertEquals(1, service.remainedAttempts('test'))
        service.loginSuccess('test')
        assertEquals(2, service.remainedAttempts('test'))
    }

    void testTimeRemovesBadTrialsCount() {
        service.allowedNumberOfAttempts = 1
        //0 - expires immediately
        service.@failedAttempts = CacheBuilder.newBuilder()
                .expireAfterWrite(0, TimeUnit.MINUTES)
                .build({ 0 } as CacheLoader)

        service.failLogin('test')
        assertFalse(service.isLocked('test'))
    }

    void testInvalidAllowedNumberOfAttemptsSetting() {
        service.allowedNumberOfAttempts = -1
        shouldFail(IllegalArgumentException, {
            service.init()
        })
    }

    void testInvalidLockTimeInMinutesSetting() {
        service.lockTimeInMinutes = -1
        shouldFail(IllegalArgumentException, {
            service.init()
        })
    }

}
