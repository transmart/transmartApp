package org.transmartfoundation.config

class ConfigInfoController {

    ConfigService configService

    def index() {

        ConfigParams configParams = configService.getConfigParams()

        [configParams: configParams, paramDone: [:] ]
    }

    def authProviders() {

        def configAuthProviders = configService.getAuthProviders()

        [providers: configAuthProviders]
    }

    def oauthClients() {

        def oauthClients = configService.getOauthClients()

        [clients: oauthClients]
    }

    def sampleMapping() {

        def sampleMapping = configService.getSampleMapping()

        [mapping: sampleMapping]
    }
}