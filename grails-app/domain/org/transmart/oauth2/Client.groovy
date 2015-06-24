package org.transmart.oauth2

class Client {

    private static final String NO_CLIENT_SECRET = ''

    transient springSecurityService

    String clientId
    String clientSecret

    Integer accessTokenValiditySeconds
    Integer refreshTokenValiditySeconds

    Map<String, Object> additionalInformation

    static hasMany = [
            authorities: String,
            authorizedGrantTypes: String,
            resourceIds: String,
            scopes: String,
            autoApproveScopes: String,
            redirectUris: String
    ]

    static transients = ['springSecurityService']

    static mapping = {
        datasource 'oauth2'
    }

    static constraints = {
        clientId blank: false, unique: true
        clientSecret nullable: true

        accessTokenValiditySeconds nullable: true
        refreshTokenValiditySeconds nullable: true

        authorities nullable: true
        authorizedGrantTypes nullable: true

        resourceIds nullable: true

        scopes nullable: true
        autoApproveScopes nullable: true

        redirectUris nullable: true
        additionalInformation nullable: true
    }

    def beforeInsert() {
        encodeClientSecret()
    }

    def beforeUpdate() {
        if (isDirty('clientSecret')) {
            encodeClientSecret()
        }
    }

    protected void encodeClientSecret() {
        clientSecret = clientSecret ?: NO_CLIENT_SECRET
        clientSecret = springSecurityService?.passwordEncoder ? springSecurityService.encodePassword(clientSecret) : clientSecret
    }
}
