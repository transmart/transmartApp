package com.recomdata.security

import org.transmart.searchapp.AuthUser

/**
 * Date: 07.07.2015
 * Time: 19:39
 */
class UsernameUtils {
    static final String FEDERATED_ID_PLACEHOLDER = '<FEDERATED_ID>'
    static final String ID_PLACEHOLDER = '<ID>'

    static String randomName() {
        return UUID.randomUUID().toString()
    }

    static String patternHasId(String pattern) {
        pattern.indexOf(ID_PLACEHOLDER) != -1
    }

    static String evaluatePattern(AuthUser user, String pattern) {
        return pattern.
                replace(FEDERATED_ID_PLACEHOLDER, user.federatedId).
                replace(ID_PLACEHOLDER, user.id.toString())
    }
}
