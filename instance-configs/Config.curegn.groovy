/*
 * NOTE
 * ----
 * This configuration assumes that the development environment will be used with
 * run-app and the production environment will be used with the application
 * packaged as a WAR and deployed to tomcat. Running grails run-war or otherwise
 * running a WAR with the development profile set up or activating the
 * production environment when running grails run-app are scenarios that have
 * NOT been tested.
 */

quickStartURL = "../../files/CureGN_tranSMART_Quick_Start_Guide.pdf"
dataAttestationText = """
<p>
It is the responsibility of all users to protect the privacy of individuals who are subjects in the data; to not use or disclose the data other than as permitted; and to appropriately secure the data.</p>
<p>
By clicking “I agree” below, users agree to the following:
<ul style="list-style-type: square; list-style-position: outside; padding-left: 18px;">
<li>No attempt shall be made to link subject data to a CureGN participant.</li>
<li>Any disclosure of data, analysis, or results from tranSMART must be in accordance with appropriate CureGN policies and procedures.</li>
<li>Further data analysis on hypotheses generated via tranSMART will be done via existing CureGN policies and procedures.</li>
<li>Data may not be disclosed, downloaded, or shared unless appropriate Material Transfer Agreements are in place.</li>
<li>As CureGN data are still being collected and cleaned, there will be periodic, announced updates to the data in tranSMART, with resulting possible changes in analysis results.</li>
</ul>
</p>
"""

// if running as a WAR, we need these
def catalinaBase      = System.getProperty('catalina.base') ?: '.'

def explodedWarDir    = catalinaBase + '/webapps/transmart'
def solrPort          = 8983 //port of appserver where solr runs (under ctx path /solr)
def searchIndex       = catalinaBase + '/searchIndex' //create this directory
// for running transmart as WAR, create this directory and then create an alias
def jobsDirectory     = "/var/tmp/jobs/"
def oauthEnabled      = true
def samlEnabled       = false
def gwavaEnabled      = false
def transmartURL      = "http://localhost:${System.getProperty('server.port', '8080')}/transmart"

motd {
    motd_title = "<center>Welcome<center>"
    motd_text = """\
<center>
Welcome to the CureGN instance of tranSMART.<br />
 <br />

Upon login, new users will be asked to agree to the data usage and <br />
attribution policy of the site. Renewal of that agreement will be <br/>
requested every 90 days.<br>
<br>
<a href="${quickStartURL}" target="_blank">Click here for CureGN-specific help on using tranSMART.</a>
</center>
"""
}

org.transmartproject.enableAcrossTrials = false

//Disabling/Enabling UI tabs
ui {
    tabs {
        //Search was not part of 1.2. It's not working properly. You need to set `show` to `true` to see it on UI
        search.show = false
        browse.hide = true
        //Note: analyze tab is always shown
        sampleExplorer.hide = true
        geneSignature.hide = false
        gwas.hide = false
        uploadData.hide = false
        datasetExplorer {
            gridView.hide = true
            dataExport.hide = true
            dataExportJobs.hide = true
            // Note: by default the analysisJobs panel is NOT shown
            // Currently, it is only used in special cases
            analysisJobs.show = false
            workspace.hide = false
	    rawDataExport.enabled = true
        }
    }
}

// I001 – Insertion point 'post-WAR-variables'

/* Other things you may want to change:
 * – Log4j configuration
 * – 'Personalization & login' section
 * – Location of Solr instance in 'Faceted Search Configuration'
 * – For enabling SAML, editing the corresponding section is mandatory
 */

/* If you want to be able to regenerate this file easily, instead of editing
 * the generated file directly, create a Config-extra.groovy file in the root of
 * the transmart-data checkout. That file will be appended to this one whenever
 * the Config.groovy target is run */

environments { production {
    if (transmartURL.startsWith('http://localhost:')) {
        println "[WARN] transmartURL not overridden. Some settings (e.g. help page) may be wrong"
    }
} }

/* {{{ Log4J Configuration */
log4j = {
    environments {
        development {
            root {
                info 'stdout'
            }

            // for a less verbose startup & shutdown
            warn  'org.codehaus.groovy.grails.commons.spring'
            warn  'org.codehaus.groovy.grails.orm.hibernate.cfg'
            warn  'org.codehaus.groovy.grails.domain.GrailsDomainClassCleaner'

            debug 'org.transmartproject'
            debug 'com.recomdata'
            debug 'grails.app.services.com.recomdata'
            debug 'grails.app.services.org.transmartproject'
            debug 'grails.app.controllers.com.recomdata'
            debug 'grails.app.controllers.org.transmartproject'
            debug 'grails.app.domain.com.recomdata'
            debug 'grails.app.domain.org.transmartproject'
            // debug 'org.springframework.security'
            // (very verbose) debug  'org.grails.plugin.resource'
        }

        production {
            def logDirectory = "${catalinaBase}/logs".toString()
            appenders {
                rollingFile(name: 'transmart',
                            file: "${logDirectory}/transmart.log",
                            layout: pattern(conversionPattern: '%d{dd-MM-yyyy HH:mm:ss,SSS} %5p %c{1} - %m%n'),
                            maxFileSize: '100MB')
            }
            root {
                warn 'transmart'
            }
        }
    }
}
/* }}} */

/* {{{ Faceted Search Configuration */
environments {
    development {
        com.rwg.solr.scheme = 'http'
        com.rwg.solr.host   = 'localhost:8983'
        com.rwg.solr.path   = '/solr/rwg/select/'
    }

    production {
        com.rwg.solr.scheme = 'http'
        com.rwg.solr.host   = 'localhost:' + solrPort
        com.rwg.solr.path   = '/solr/rwg/select/'
    }
}
/* }}} */

/* {{{ Data Upload Configuration - see GWAS plugin Data Upload page */
// This is the value that will appear in the To: entry of the e-mail popup 
// that is displayed when the user clicks the Email administrator button,
// on the GWAS plugin Data Upload page
com.recomdata.dataUpload.adminEmail = 'No data upload adminEmail value set - contact site administrator'
/* }}} */

/* {{{ Personalization */
// application logo to be used in the login page
com.recomdata.largeLogo = "CureGNLogo.jpg"

// application logo to be used in the search page
com.recomdata.smallLogo="no_graphic.png"

// contact email address
com.recomdata.contactUs = "CureGNtmSupport@umich.edu"

// site administrator contact email address
com.recomdata.adminEmail = "CureGNtmSupport@umich.edu"

// application title
com.recomdata.appTitle = "CureGN tranSMART"

//Quick Start Guide URL
quickStartURL = "../../files/NEPTUNE_tranSMART_Quick_Start_Guide.pdf"

// Location of the help pages. Should be an absolute URL.
// Currently, these are distribution with transmart,
// so it can also point to that location copy.
com.recomdata.adminHelpURL = "$transmartURL/help/adminHelp/default.htm"

environments { development {
    com.recomdata.bugreportURL = 'https://jira.transmartfoundation.org'
} }

// Keys without defaults (see Config-extra.php.sample):
// com.recomdata.projectName
// com.recomdata.providerName
// com.recomdata.providerURL
/* }}} */

/* {{{ Login */
// Session timeout and heartbeat frequency (ping interval)
com.recomdata.sessionTimeout = 1800
com.recomdata.heartbeatLaps = 300

environments { development {
    com.recomdata.sessionTimeout = Integer.MAX_VALUE / 1000 as int /* ~24 days */
    com.recomdata.heartbeatLaps = 900
} }

// Maximum concurrent sessions for a user (-1: unlimited)
// org.transmartproject.maxConcurrentUserSessions = 10

// Not enabled by default (see Config-extra.php.sample)
//com.recomdata.passwordstrength.pattern
//com.recomdata.passwordstrength.description

// Whether to enable guest auto login.
// If it's enabled no login is required to access tranSMART.
com.recomdata.guestAutoLogin = false
environments { development { com.recomdata.guestAutoLogin = true } }

// Guest account user name - if guestAutoLogin is true, this is the username of
// the account that tranSMART will automatically authenticate users as. This will
// control the level of access anonymous users will have (the access will match
// that of the account specified here).
com.recomdata.guestUserName = 'guest'
/* }}} */

/* {{{ Search tool configuration */

// Lucene index location for documentation search
com.recomdata.searchengine.index = searchIndex

/* }}} */

/* {{{ Sample Explorer configuration */

// This is an object to dictate the names and 'pretty names' of the SOLR fields.
// Optionally you can set the width of each of the columns when rendered.

sampleExplorer {
    fieldMapping = [
        columns:[
            [header:'Sample ID',dataIndex:'id', mainTerm: false, showInGrid: false],
            [header:'BioBank', dataIndex:'BioBank', mainTerm: true, showInGrid: true, width:10],
            [header:'Source Organism', dataIndex:'Source_Organism', mainTerm: true, showInGrid: true, width:10]
            // Continue as you have fields
        ]
    ]
    resultsGridHeight = 100
    resultsGridWidth = 100
    idfield = 'id'
}

edu.harvard.transmart.sampleBreakdownMap = [
    "id":"Aliquots in Cohort"
]

// Solr configuration for the Sample Explorer
com { recomdata { solr {
    maxNewsStories = 10
    maxRows = 10000
}}}

/* }}} */

/* {{{ Dataset Explorer configuration */
com { recomdata { datasetExplorer {
    // set to 'true' (quotes included) to enable gene pattern integration
    genePatternEnabled = 'false'
    // The tomcat URL that gene pattern is deployed within -usually it's proxyed through apache
    genePatternURL = 'http://23.23.185.167'
    // Gene Pattern real URL with port number
    genePatternRealURLBehindProxy = 'http://23.23.185.167:8080'
    // default Gene pattern user to start up - each tranSMART user will need a separate user account to be created in Gene Pattern
    genePatternUser = 'biomart'

    // Absolute path to PLINK executables
    plinkExcutable = '/usr/local/bin/plink'
} } }
// Metadata view
com.recomdata.view.studyview = 'studydetail'

com.recomdata.plugins.resultSize = 5000
/* }}} */

/* {{{ RModules & Data Export Configuration */
environments {
    // This is to target a remove Rserv. Bear in mind the need for shared network storage
    RModules.host = "127.0.0.1"
    RModules.port = 6311

    // This is not used in recent versions; the URL is always /analysisFiles/
    RModules.imageURL = "/tempImages/" //must end and start with /

    production {
        // The working directory for R scripts, where the jobs get created and
        // output files get generated
        RModules.tempFolderDirectory = jobsDirectory
    }
    development {
        RModules.tempFolderDirectory = "/tmp"

        /* we don't need to specify temporaryImageDirectory, because we're not copying */
    }

    // Used to access R jobs parent directory outside RModules (e.g. data export)
    com.recomdata.plugins.tempFolderDirectory = RModules.tempFolderDirectory
}
/* }}} */

/* {{{ GWAS Configuration */

com.recomdata.dataUpload.appTitle="Upload data to tranSMART"
com.recomdata.dataUpload.stageScript="run_analysis_stage"

// Directory path of com.recomdata.dataUpload.stageScript
def gwasEtlDirectory = new File(System.getenv('HOME'), '.grails/transmart-gwasetl')

// Directory to hold GWAS file uploads
def gwasUploadsDirectory = new File(System.getenv('HOME'), '.grails/transmart-datauploads')

// Directory to preload with template files with names <type>-template.txt
def gwasTemplatesDirectory = new File(System.getenv('HOME'), '.grails/transmart-templates')

com.recomdata.dataUpload.templates.dir = gwasTemplatesDirectory.absolutePath
com.recomdata.dataUpload.uploads.dir = gwasUploadsDirectory.absolutePath
com.recomdata.dataUpload.etl.dir = gwasEtlDirectory.absolutePath

[gwasTemplatesDirectory, gwasUploadsDirectory, gwasEtlDirectory].each {
    if (!it.exists()) {
        it.mkdir()
    }
}

/* }}} */

/* {{{ Misc Configuration */

// This can be used to debug JavaScript callbacks in the dataset explorer in
// Chrome. Unfortunately, it also sometimes causes chrome to segfault
com.recomdata.debug.jsCallbacks = 'false'

environments {
    production {
        com.recomdata.debug.jsCallbacks = 'false'
    }
}

grails.resources.adhoc.excludes = [ '/images' + RModules.imageURL + '**' ]

// Adding properties to the Build information panel
buildInfo { properties {
   include = [ 'app.grails.version', 'build.groovy' ]
   exclude = [ 'env.proc.cores' ]
} }

/* }}} */

/* {{{ Spring Security configuration */

grails { plugin { springsecurity {
    // You probably won't want to change these

    // customized user GORM class
    userLookup.userDomainClassName = 'org.transmart.searchapp.AuthUser'
    // customized password field
    userLookup.passwordPropertyName = 'passwd'
    // customized user /role join GORM class
    userLookup.authorityJoinClassName = 'org.transmart.searchapp.AuthUser'
    // customized role GORM class
    authority.className = 'org.transmart.searchapp.Role'
    // request map GORM class name - request map is stored in the db
    requestMap.className = 'org.transmart.searchapp.Requestmap'
    // requestmap in db
    securityConfigType = grails.plugin.springsecurity.SecurityConfigType.Requestmap
    // url to redirect after login in
    // just_rest branch provides alternative default via org.transmart.defaultLoginRedirect
    successHandler.defaultTargetUrl = org.transmart.defaultLoginRedirect ?: '/userLanding'
    // logout url
    logout.afterLogoutUrl = '/login/forceAuth'

    // configurable requestmap functionality in transmart is deprecated
    def useRequestMap = false

    if (useRequestMap) {
        // requestmap in db
        securityConfigType = 'Requestmap'
        // request map GORM class name - request map is stored in the db
        requestMap.className = 'org.transmart.searchapp.Requestmap'
    } else {
        securityConfigType = 'InterceptUrlMap'
        def oauthEndpoints = [
              '/oauth/authorize.dispatch': ["isFullyAuthenticated() and (request.getMethod().equals('GET') or request.getMethod().equals('POST'))"],
              '/oauth/token.dispatch':     ["isFullyAuthenticated() and request.getMethod().equals('POST')"],
        ]

        // This looks dangerous and it possibly is (would need to check), but
        // reflects the instructions I got from the developer.
        def gwavaMappings = [
             '/gwasWeb/**'                : ['IS_AUTHENTICATED_ANONYMOUSLY'],
        ]

        interceptUrlMap = [
            '/login/**'                   : ['IS_AUTHENTICATED_ANONYMOUSLY'],
            '/css/**'                     : ['IS_AUTHENTICATED_ANONYMOUSLY'],
            '/js/**'                      : ['IS_AUTHENTICATED_ANONYMOUSLY'],
            '/grails-errorhandler'        : ['IS_AUTHENTICATED_ANONYMOUSLY'],
            '/images/analysisFiles/**'    : ['IS_AUTHENTICATED_REMEMBERED'],
            '/images/**'                  : ['IS_AUTHENTICATED_ANONYMOUSLY'],
            '/static/**'                  : ['IS_AUTHENTICATED_ANONYMOUSLY'],
            '/search/loadAJAX**'          : ['IS_AUTHENTICATED_ANONYMOUSLY'],
            '/analysis/getGenePatternFile': ['IS_AUTHENTICATED_ANONYMOUSLY'],
            '/analysis/getTestFile'       : ['IS_AUTHENTICATED_ANONYMOUSLY'],
            '/requestmap/**'              : ['ROLE_ADMIN'],
            '/role/**'                    : ['ROLE_ADMIN'],
            '/authUser/**'                : ['ROLE_ADMIN'],
            '/secureObject/**'            : ['ROLE_ADMIN'],
            '/accessLog/**'               : ['ROLE_ADMIN'],
            '/authUserSecureAccess/**'    : ['ROLE_ADMIN'],
            '/secureObjectPath/**'        : ['ROLE_ADMIN'],
            '/userGroup/**'               : ['ROLE_ADMIN'],
            '/secureObjectAccess/**'      : ['ROLE_ADMIN'],
            '/oauthAdmin/**'              : ['ROLE_ADMIN'],
            *                             : (oauthEnabled ?  oauthEndpoints : [:]),
            *                             : (gwavaEnabled ?  gwavaMappings : [:]),
            '/**'                         : ['IS_AUTHENTICATED_REMEMBERED'], // must be last
        ]
        rejectIfNoRule = true
    }

    // Hash algorithm
    password.algorithm = 'bcrypt'
    // Number of bcrypt rounds
    password.bcrypt.logrounds = 14

    providerNames = [
        'daoAuthenticationProvider',
        'anonymousAuthenticationProvider',
        'rememberMeAuthenticationProvider',
    ]

    if (oauthEnabled) {
        providerNames << 'clientCredentialsAuthenticationProvider'

        def securedResourcesFilters = [
                'JOINED_FILTERS',
                '-securityContextPersistenceFilter',
                '-logoutFilter',
                '-rememberMeAuthenticationFilter',
                '-basicAuthenticationFilter',
                '-exceptionTranslationFilter',
        ].join(',')

        filterChain.chainMap = [
                '/oauth/token': [
                        'JOINED_FILTERS',
                        '-oauth2ProviderFilter',
                        '-securityContextPersistenceFilter',
                        '-logoutFilter',
                        '-rememberMeAuthenticationFilter',
                        '-exceptionTranslationFilter',
                ].join(','),
                '/studies/**': securedResourcesFilters,
                '/observations/**': securedResourcesFilters,
                '/patient_sets/**': securedResourcesFilters,
                '/oauth/inspectToken': securedResourcesFilters,
                '/**': [
                        'JOINED_FILTERS',
                        '-statelessSecurityContextPersistenceFilter',
                        '-oauth2ProviderFilter',
                        '-clientCredentialsTokenEndpointFilter',
                        '-basicAuthenticationFilter',
                        '-oauth2ExceptionTranslationFilter'
                ].join(','),
        ]

        grails.exceptionresolver.params.exclude = ['password', 'client_secret']

        def glowingBearRedirectUris = [
                transmartURL - ~/transmart\/?$/ + 'connections',
        ]
        // for dev, node reverse proxy runs on 8001
        glowingBearRedirectUris << 'http://localhost:8001/connections'

        oauthProvider {
            authorization.requireRegisteredRedirectUri = true
            authorization.requireScope = false

            clients = [
                    [
                        clientId: 'api-client',
                        clientSecret: 'api-client',
                        authorities: ['ROLE_CLIENT'],
                        scopes: ['read', 'write'],
                        authorizedGrantTypes: ['authorization_code', 'refresh_token'],
                        redirectUris: [(transmartURL - ~'\\/$') + '/oauth/verify'],
                    ],
                    [
                        clientId: 'glowingbear-js',
                        clientSecret: '',
                        authorities: ['ROLE_CLIENT'],
                        scopes: ['read', 'write'],
                        authorizedGrantTypes: ['implicit', 'password'],
                        redirectUris: glowingBearRedirectUris,
                    ],
            ]
        }
    }

} } }
/* }}} */

//{{{ SAML Configuration

if (samlEnabled) {
    // don't do assignment to grails.plugin.springsecurity.providerNames
    // see GRAILS-11730
    grails { plugin { springsecurity {
        providerNames << 'samlAuthenticationProvider'
    } } }
    // again, because of GRAILS-11730
    def ourPluginConfig
    grails {
        ourPluginConfig = plugin
    }

    org { transmart { security {
        setProperty('samlEnabled', true) // clashes with local variable
        ssoEnabled  = "true"

        // URL to redirect to after successful authentication
        successRedirectHandler.defaultTargetUrl = ourPluginConfig.springsecurity.successHandler.defaultTargetUrl
        // URL to redirect to after successful logout
        successLogoutHandler.defaultTargetUrl = ourPluginConfig.springsecurity.logout.afterLogoutUrl

        saml {
            /* {{{ Service provider details (we) */
            sp {
                // ID of the Service Provider
                id = "gustavo-transmart"

                // URL of the service provider. This should be autodected, but it isn't
                url = "http://localhost:8080/transmart"

                // Alias of the Service Provider
                alias = "transmart"

                // Alias of the Service Provider's signing key, see keystore details
                signingKeyAlias = "saml-signing"
                // Alias of the Service Provider's encryption key
                encryptionKeyAlias = "saml-encryption"
            }
            /* }}} */

            // Metadata file of the provider. We insist on keeping instead of just
            // retrieving it from the provider on startup to prevent transmart from
            // being unable to start due to provider being down. A copy will still be
            // periodically fetched from the provider
            idp.metadataFile = '/home/glopes/idp-local-metadata.xml'

            /* {{{ Keystore details */
            keystore {
                // Generate with:
                //  keytool -genkey -keyalg RSA -alias saml-{signing,encryption} \
                //    -keystore transmart.jks -storepass changeit \
                //    -validity 3602 -keysize 2048
                // Location of the keystore. You can use other schemes, like classpath:resource/samlKeystore.jks
                file = 'file:///home/glopes/transmart.jks'

                // keystore's storepass
                password="changeit"

                // keystore's default key
                defaultKey="saml-signing"

                // Alias of the encryption key in the keystore
                encryptionKey.alias="saml-encryption"
                // Password of the key with above alias in the keystore
                encryptionKey.password="changeit"

                // Alias of the signing key in the keystore
                signingKey.alias="saml-signing"
                // Password of the key with above alias in the keystore
                signingKey.password="changeit"
            }
            /* }}} */

            /* {{{ Creation of new users */
            createInexistentUsers = "true"
            attribute.username    = "urn:custodix:ciam:1.0:principal:username"
            attribute.firstName   = "urn:oid:2.5.4.42"
            attribute.lastName    = "urn:oid:2.5.4.4"
            attribute.email       = ""
            attribute.federatedId = "personPrincipalName"
            /* }}} */

            //
            // Except maybe for the binding, you probably won't want to change the rest:
            //

            // Suffix of the login filter, saml authentication is initiated when user browses to this url
            entryPoint.filterProcesses = "/saml/login"
            // SAML Binding to be used for above entry point url.
            entryPoint.binding = "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
            // This property must be set otherwise the default binding is used which, in this configuration, is HTTP-ARTIFACT
            entryPoint.defaultAssertionConsumerIndex = "1"

            // Suffix of the Service Provider's metadata, this url needs to be configured on IDP
            metadata.filterSuffix = "/saml/metadata"
            // Id of the spring security's authentication manager
            authenticationManager = "authenticationManager"
            // Whether sessions should be invalidated after logout
            logout.invalidateHttpSession = "true"
            // Id of the spring security user service that should be called to fetch users.
            saml.userService = "org.transmart.FederatedUserDetailsService"
        }
    } } }
} else { // if (!samlEnabled)
    org { transmart { security {
        setProperty('samlEnabled', false) // clashes with local variable
    } } }
}
/* }}} */

/* {{{ gwava */
if (gwavaEnabled) {
    // assume deployment alongside transmart
    com { recomdata { rwg { webstart {
        def url       = new URL(transmartURL)
        codebase      = "$url.protocol://$url.host${url.port != -1 ? ":$url.port" : ''}/gwava"
        jar           = './ManhattanViz2.1g.jar'
        mainClass     = 'com.pfizer.mrbt.genomics.Driver'
        gwavaInstance = 'transmartstg'
        transmart.url = transmartURL - ~'\\/$'
   } } } }
   com { recomdata { rwg { qqplots {
       cacheImages = new File(jobsDirectory, 'cachedQQplotImages').toString()
   } } } }
}
/* }}} */

/* {{{ Quartz jobs configuration */
// start delay for the sweep job
com.recomdata.export.jobs.sweep.startDelay =60000 // d*h*m*s*1000
// repeat interval for the sweep job
com.recomdata.export.jobs.sweep.repeatInterval = 86400000 // d*h*m*s*1000
// specify the age of files to be deleted (in days)
com.recomdata.export.jobs.sweep.fileAge = 3
/* }}} */

/* {{{ File store and indexing configuration */
com.rwg.solr.browse.path   = '/solr/browse/select/'
com.rwg.solr.update.path = '/solr/browse/dataimport/'
com.recomdata.solr.baseURL = "${com.rwg.solr.scheme}://${com.rwg.solr.host}" +
                             "${new File(com.rwg.solr.browse.path).parent}"

def fileStoreDirectory = new File(System.getenv('HOME'), '.grails/transmart-filestore')
def fileImportDirectory = new File(System.getProperty("java.io.tmpdir"), 'transmart-fileimport')
com.recomdata.FmFolderService.filestoreDirectory = fileStoreDirectory.absolutePath
com.recomdata.FmFolderService.importDirectory = fileImportDirectory.absolutePath

[fileStoreDirectory, fileImportDirectory].each {
    if (!it.exists()) {
        it.mkdir()
    }
}
/* }}} */

/* {{{ Sample Explorer configuration */

sampleExplorer {
    fieldMapping = [
        columns:[
            [header:'ID', dataIndex:'id', mainTerm: true, showInGrid: true, width:20],
            [header:'trial name', dataIndex:'trial_name', mainTerm: true, showInGrid: true, width:20],
            [header:'barcode', dataIndex:'barcode', mainTerm: true, showInGrid: true, width:20],
            [header:'plate id', dataIndex:'plate_id', mainTerm: true, showInGrid: true, width:20],
            [header:'patient id', dataIndex:'patient_id', mainTerm: true, showInGrid: true, width:20],
            [header:'external id', dataIndex:'external_id', mainTerm: true, showInGrid: true, width:20],
            [header:'aliquot id', dataIndex:'aliquot_id', mainTerm: true, showInGrid: true, width:20],
            [header:'visit', dataIndex:'visit', mainTerm: true, showInGrid: true, width:20],
            [header:'sample type', dataIndex:'sample_type', mainTerm: true, showInGrid: true, width:20],
            [header:'description', dataIndex:'description', mainTerm: true, showInGrid: true, width:20],
            [header:'comment', dataIndex:'comment', mainTerm: true, showInGrid: true, width:20],
            [header:'location', dataIndex:'location', mainTerm: true, showInGrid: true, width:20],
            [header:'organism', dataIndex:'source_organism', mainTerm: true, showInGrid: true, width:20]
        ]
    ]
    resultsGridHeight = 100
    resultsGridWidth = 100
    idfield = 'id'
}

edu.harvard.transmart.sampleBreakdownMap = [
    "aliquot_id":"Aliquots in Cohort"
]

com { recomdata { solr {
    maxNewsStories = 10
    maxRows = 10000
}}}

org.transmart.configFine = true

/* }}} */

// I002 – Insertion point 'end'

// vim: set fdm=marker et ts=4 sw=4 filetype=groovy ai:
