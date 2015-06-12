package org.transmart.oauth2

class AuthorizationCode {

    byte[] authentication
    String code

    static constraints = {
        code nullable: false, blank: false, unique: true
        authentication nullable: false, minSize: 1, maxSize: 1024 * 4
    }

    static mapping = {
        datasource 'oauth2'
        version false
    }
}
