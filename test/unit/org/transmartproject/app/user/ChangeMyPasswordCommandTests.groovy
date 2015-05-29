package org.transmartproject.app.user

import grails.test.mixin.TestMixin
import grails.test.mixin.web.ControllerUnitTestMixin
import org.junit.Before

import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

@TestMixin(ControllerUnitTestMixin)
class ChangeMyPasswordCommandTests {

    ChangePasswordCommand testee

    @Before
    void setup() {
        testee = mockCommandObject ChangePasswordCommand
        testee.springSecurityService = [
                passwordEncoder: [
                        isPasswordValid: { enc, raw, salt -> true }
                ],
                currentUser    : [
                        getPersistentValue: { psw -> 'test' }
                ]
        ]
        grailsApplication.config.clear()
    }

    void testOldPasswordDoesNotMatch() {
        testee.springSecurityService = [
                passwordEncoder: [
                        isPasswordValid: { enc, raw, salt -> false }
                ],
                currentUser    : [
                        getPersistentValue: { psw -> 'test' }
                ]
        ]

        testee.oldPassword = 'old'
        testee.newPassword = 'new'
        testee.newPasswordRepeated = 'new'

        assertFalse testee.validate()
        assertThat testee.errors.allErrors, contains(
                hasProperty('field', equalTo('oldPassword'))
        )
    }

    void testNewPasswordIsTheSameAsOldOne() {
        testee.oldPassword = 'test'
        testee.newPassword = 'test'
        testee.newPasswordRepeated = 'test'

        assertFalse testee.validate()
        assertThat testee.errors.allErrors, contains(
                hasProperty('field', equalTo('newPassword'))
        )
    }

    void testLowPasswordStrength() {
        grailsApplication.config.user.password.strength.regex = '^TEST.*$'
        testee.oldPassword = 'old'
        testee.newPassword = 'new'
        testee.newPasswordRepeated = 'new'

        assertFalse testee.validate()
        assertThat testee.errors.allErrors, contains(
                hasProperty('field', equalTo('newPassword'))
        )
    }

    void testRepeatDoesNotMatch() {
        testee.oldPassword = 'old'
        testee.newPassword = 'new'
        testee.newPasswordRepeated = 'new2'

        assertFalse testee.validate()
        assertThat testee.errors.allErrors, contains(
                hasProperty('field', equalTo('newPasswordRepeated'))
        )
    }

    void testSuccess() {
        grailsApplication.config.user.password.strength.regex = '^n.*w$'
        testee.oldPassword = 'old'
        testee.newPassword = 'new'
        testee.newPasswordRepeated = 'new'

        assertTrue testee.validate()
    }

}
