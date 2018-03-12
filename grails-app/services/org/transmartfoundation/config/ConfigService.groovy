package org.transmartfoundation.config

import grails.util.Holders

import java.util.Date

class ConfigService {

    def grailsApplication

    boolean transactional = true

    def getConfigParams() {

        Map adminParams = [
            'com.recomdata.admin.paginate.max': [
                desc:'Pages per screen in admin panels'],
            'org.transmart.config.showUsernames': [
                default:false,
                desc:'Unless true, replace known username parameters with \'(hidden)\' in configuration panel'],
            'org.transmart.config.showPasswords': [
                default:false,
                desc:'Unless true, replace known password parameters with \'(hidden)\' in configuration panel'],
            'com.recomdata.plugins.pluginScriptDirectory': [
                desc:'Script directory for plugins'],
        ]
        Map analysisParams = [
            'com.recomdata.datasetExplorer.genePatternEnabled': [
                desc:'If \'true\' (quoted string), GenePattern is enabled'],
            'com.recomdata.datasetExplorer.genePatternURL': [
                desc:'GenePattern URL for gene pattern service, usually proxyed through Apache'],
            'com.recomdata.datasetExplorer.genePatternRealURLBehindProxy': [
                desc:'GenePattern URL with port number for analysis in gene pattern service'],
            'com.recomdata.datasetExplorer.genePatternUser': [
                desc:'GenePattern username'],

            'com.recomdata.analysis.data.file.dir': [
                desc:'Analysis data directory for data export'],
            'com.recomdata.analysis.genepattern.file.dir': [
                desc:'GenePattern file directory'],
            'com.recomdata.analysis.survival.censorFlagList': [
                desc:'GenePattern survival analysis censor flags'],
            'com.recomdata.analysis.survival.survivalDataList': [
                desc:'Genepattern survival analysis data concepts'],
            'com.recomdata.datasetExplorer.plinkExcutable': [
                desc:'Full path to plink executable'],
        ]
        Map analyzeParams = [
            'com.recomdata.datasetExplorer.imageTempDir': [
                desc:'Temporary image directories for analysis results'],
            'com.recomdata.plugins.tempFolderDirectory': [
                desc:'Temporary directory for plugins to save files'],
        ]
        Map authParams = [
            'grails.plugin.springsecurity.providerNames': [
                desc:'List of loaded authentication providers'],
            'com.recomdata.guestAutoLogin': [
                default:false,
                desc:'If true, automatically login as guestUserName (logout to change to another user)'],
            'com.recomdata.guestUserName': [
                desc:'Guest username for autologin'],
            'com.recomdata.passwordstrength.pattern': [
                desc:'Regular expression to ensure password complexity. Good passwords must match. Usually includes test for upper case, lower case, number, and other character classes and a test for length'],
            'com.recomdata.passwordstrength.description': [
                desc:'Friendly readable description of the password complexity conditions'],
            'transmartproject.authUser.create.passwordRequired': [
                desc:'Require a password when creating a new user'],
            'org.transmart.security.sniValidation': [
                default:true,
                desc:'Server name indication extension'],
            'org.transmart.security.sslValidation': [
                default:true,
                desc:'SSL hostname and certification verification'],
            'org.transmart.security.spnegoEnabled': [
                desc:'Simple and Protected GSSAPI Negotiation enabled if true. Supports multiple security mechanisms'],
            'user.password.strength.regex': [
                desc:'Password strength regex that is used to test passwords that are entered by users'],
        ]
        Map browseParams = [
            'com.recomdata.FmFolderJob.cronExpression': [
                default:'0 0/5 * * * ?',
                desc:'File upload cron interval'],
            'com.recomdata.FmFolderJob.startDelayMs': [
                default:60000,
                desc:'File upload delay (msec)'],
            'com.recomdata.FmFolderService.filestoreDirectory': [
                desc:'Directory on server to store Browse tab files saved in folders, required for Browse file import'],
            'com.recomdata.FmFolderService.importDirectory': [
                desc:'Directory on server to temporarily save uploaded files, required for Browse file import'],
            'com.recomdata.FmFolderService.fileTypes': [
                default:'xml,json,csv,pdf,doc,docx,ppt,pptx,xls,xlsx,odt,odp,ods,ott,otp,ots,rtf,htm,html,txt,lo',
                desc:'List of known file types for saving in Browse tab folders'],
            'com.recomdata.solr.baseURL': [
                desc:'Browse URL for solR, required for Browse file import'],
            'com.rwg.solr.scheme': [
                desc:'Browse/Faceted search solR protocol'],
            'com.rwg.solr.host': [
                desc:'Browse/Faceted search solR host'],
            'com.rwg.solr.path': [
                desc:'Browse/Faceted search solR path (remainder of URL)'],
            'com.rwg.solr.browse.path': [
                desc:'Browse search path (remainder of URL)'],
            'com.rwg.solr.update.path': [
                desc:'Browse solR data import path (remainder of URL)'],
            'transmartproject.mongoFiles.enableMongo': [
                default:false,
                desc:'If true, store Browse tab files in MongoDB, else use server filesystem'],
            'transmartproject.mongoFiles.useDriver': [
                desc:'if true, use local MongoDB local server'],
            'transmartproject.mongoFiles.dbServer': [
                desc:'Local MongoDB server host'],
            'transmartproject.mongoFiles.dbPort': [
                desc:'Local MongoDB server port'],
            'transmartproject.mongoFiles.dbName': [
                desc:'Local MongoDB server dbname'],
            'transmartproject.mongoFiles.apiURL': [
                desc:'Remote MongoDB server URL'],
            'transmartproject.mongoFiles.apiKey': [
                desc:'Remote MongoDB server API key'],
            'ui.browse.delete.allowprogram': [
                default:false,
                desc:'If true, display \'Delete Program\' in Browse tab for admin users'],
            'ui.browse.delete.allowstudy': [
                default:false,
                desc:'If true, display \'Delete Study\' in Browse tab for admin users'],
        ]
        Map buildInfoParams = [
            'buildinfo.properties.include': [
                default:[],
                desc:'Groovy list of properties to add to the buildInfo panel e.g. \'app.grails.version\' and \'build.groovy\''],
            'buildinfo.properties.exclude': [
                default:[],
                desc:'Groovy list of properties to exclude from the buildInfo panel e.g. \'env.proc.cores\''],
        ]
        Map contactParams = [
            'com.recomdata.adminEmail': [
                desc:'Email address to request administrator support, or to request a login username'],
            'com.recomdata.contactUs': [
                desc:'contact email address'],
        ]
        Map dataSourceParams = [
            'dataSource.dialect' : [
                desc:'Data source dialect' ],
            'dataSource.driverClassName' : [
                desc:'Data source driver' ],
            'dataSource.dbCreate' : [
                desc:'Data source created if true' ],
            'dataSource.url' : [
                desc:'Data source URL' ],
            'dataSource.username' : [
                desc:'Data source username' ],
            'dataSource.password' : [
                desc:'Data source password' ],
            'dataSource.logSql' : [
                desc:'Data source logSql setting' ],
            'dataSource.formatSql' : [
                desc:'Data source formatSql setting' ],
            'dataSource.properties.initialSize' : [
                desc:'Data source initial size' ],
            'dataSource.properties.minIdle' : [
                desc:'Data source minimum idle' ],
            'dataSource.properties.maxIdle' : [
                desc:'Data source maximum idle' ],
            'dataSource.properties.maxActive' : [
                desc:'Data source maximum active' ],
            'dataSource.properties.maxWait' : [
                desc:'Data source maximum wait' ],
            'dataSource.properties.numTestsPerEvictionRun' : [
                desc:'Data source number of tests per eviction run' ],
            'dataSource.properties.minEvictableIdleTimeMillis' : [
                desc:'Data source mininum evictable idle time (msec)' ],
            'dataSource.properties.timeBetweenEvictionRunsMillis' : [
                desc:'Data source tme between eviction runs (msec)' ],
            'dataSource.properties.validationQuery' : [
                desc:'Data source simple validation query' ],
            'dataSource.properties.testOnBorrow' : [
                desc:'Data source test on borrow' ],
            'dataSource.properties.testWhileIdle' : [
                desc:'Data source test while idle' ],
            'dataSource.properties.testOnReturn' : [
                desc:'Data source test on return' ],
        ]
        Map exportParams = [
            'com.recomdata.export.jobs.sweep.startDelay': [
                desc:'start delay for the sweep job (msec)'],
            'com.recomdata.export.jobs.sweep.repeatInterval': [
                desc:'Repeat interval for file sweep job (msec)'],
            'com.recomdata.export.jobs.sweep.fileAge': [
                desc:'Delete files older than this (days)'],
            'com.recomdata.transmart.data.export.jobTmpDirectory': [
                desc:'Server directory for data export jobs'],
            'com.recomdata.transmart.data.export.rScriptDirectory': [
                desc:'Path to R scripts for data export'],
            'dataExport.bed.acgh.rgbColorScheme': [
                desc:'Alternative color scheme for aCGH BED tracks. Current colors are based on cghCall R package'],
            'com.recomdata.search.gene.max': [
                desc:'Maaximum number of genes in SNP data service'],
            'com.recomdata.search.genepathway': [
                desc:'Gene pathway in SNP data service'],
            'com.recomdata.plugins.resultSize': [
                desc:'Result size used in SNP data service'],
            'com.recomdata.transmart.data.export.ftp.server': [
                desc:'Data Export FTP server address'],
            'com.recomdata.transmart.data.export.ftp.serverport': [
                desc:'Data Export FTP server port'],
            'com.recomdata.transmart.data.export.ftp.username': [
                desc:'Data Export FTP username'],
            'com.recomdata.transmart.data.export.ftp.password': [
                desc:'Data Export FTP password'],
            'com.recomdata.transmart.data.export.ftp.remote.path': [
                desc:'Data Export FTP file path'],
            'com.recomdata.transmart.data.export.max.export.jobs.loaded': [
                desc:'Data Export maximum jobs loaded '],
        ]
        Map galaxyParams = [
            'com.galaxy.blend4j.galaxyEnabled': [
                default: false,
                desc:'Galaxy export plugin enabled if true'],
            'com.galaxy.blend4j.galaxyEnabled': [
                desc:'Galaxy server URL for Galaxy export tab'],
        ]
        Map generalParams = [
            'com.recomdata.appTitle': [
                desc:'Application title, usually including the release'],
            'com.recomdata.largeLogo': [
                desc:'application logo to be used in the login page (path appended to transmart URL)'],
            'com.recomdata.smallLogo': [
                desc:'application logo to be used in the search page (path appended to transmart URL)'],
            'com.recomdata.projectName': [
                desc:'Project name for welcome screen'],
            'com.recomdata.projectLogo': [
                desc:'Project logo for welcome screen'],
            'com.recomdata.providerName': [
                desc:'Provider name for welcome screen'],
            'com.recomdata.providerLogo': [
                desc:'Provider logo for welcome screen'],
            'com.recomdata.providerURL': [
                desc:'Provider URL for link on welcome page'],
            'com.recomdata.skipdisclaimer': [
                desc:'If true, do not show the disclaimer text on welcome screen'],
            'com.recomdata.disclaimer': [
                desc:'Disclaimer text for welcome screen'],
            'com.recomdata.view.studyview': [
                desc:'View to choose for study display'],
            'org.transmart.configFine': [
                desc:'Set to true on successful startup, last value in Config.groovy'],
        ]
        Map gwasParams = [
            'com.recomdata.rwg.manhattanplots.cacheImages': [
                desc:'If true, cache Manhattan plot mages for GWAS'],
            'com.recomdata.rwg.qqplots.cacheImages': [
                desc:'If true, cache QQ plot images for GWAS'],
            'com.recomdata.rwg.qqplots.temporaryImageFolder': [
                desc:'Server folder to store cached images while rendering to the user'],
            'com.recomdata.rwg.qqplots.temporaryImageFolderFullPath': [
                desc:'Server folder to store images while rendering to the user'],
            'com.recomdata.gwas.usehg19table': [ default: false,
                desc:'If true, use only hg19 human reference unless another version is specified'],
            'grails.plugin.transmartGwasPlink.plinkPath': [
                desc:'Full path to the plink executable for GWAS-plink plugin'],
        ]
        Map gwavaParams = [
            'com.recomdata.rwg.webstart.codebase': [
                desc:'GWAVA webstart parameters'],
            'com.recomdata.rwg.webstart.gwavaInstance': [
                desc:'GWAVA webstart parameters'],
            'com.recomdata.rwg.webstart.href': [
                desc:'GWAVA webstart parameters'],
            'com.recomdata.rwg.webstart.jar': [
                desc:'GWAVA webstart parameters'],
            'com.recomdata.rwg.webstart.mainClass': [
                desc:'GWAVA webstart parameters'],
            'com.recomdata.rwg.webstart.transmart.url': [
                desc:'GWAVA webstart parameters'],
        ]
        Map helpParams = [
            'com.recomdata.adminHelpURL': [
                desc:'Location of the help pages as an absolute URL. May be a remote copy.'],
            'org.transmartproject.helpUrls.hiDomePopUp': [
                desc:'URL of local help for HiDome usage to query high dimensional data'],
            'org.transmartproject.helpUrls.summaryStatistics': [
                desc:'URL of local help for Summary Statistics in Analyze tab'],
            'org.transmartproject.helpUrls.geneSignatureList': [
                desc:'URL of local help for Gene Signature List tab'],
            'org.transmartproject.helpUrls.rsIdSignatureList': [
                desc:'URL of local help for rsId Gene Signatures'],
            'org.transmartproject.helpUrls.boxPlot': [
                desc:'URL of local help for boxPlot workflow'],
            'org.transmartproject.helpUrls.correlationAnalysis': [
                desc:'URL of local help for Correlation workflow'],
            'org.transmartproject.helpUrls.heatMap': [
                desc:'URL of local help for Heatmap workflows'],
            'org.transmartproject.helpUrls.heatMapMaxRows': [
                desc:'URL of local help for Heatmap workflow MaxRows parameter'],
            'org.transmartproject.helpUrls.hierarchicalClustering': [
                desc:'URL of local help for HClust workflow'],
            'org.transmartproject.helpUrls.hierarchicalClusteringMaxRows': [
                desc:'URL of local help for HClust workflow MaxRows parameter'],
            'org.transmartproject.helpUrls.kMeansClustering': [
                desc:'URL of local help for kMeans workflow'],
            'org.transmartproject.helpUrls.kMeansClusteringMaxRows': [
                desc:'URL of local help for kMeans workflow MaxRows parameter'],
            'org.transmartproject.helpUrls.lineGraph': [
                desc:'URL of local help for Line Graph workflow'],
            'org.transmartproject.helpUrls.logisticRegression': [
                desc:'URL of local help for Logistic Regression workflow'],
            'org.transmartproject.helpUrls.markerSelection': [
                desc:'URL of local help for Marker Selection workflow'],
            'org.transmartproject.helpUrls.pca': [
                desc:'URL of local help for PCA workflow'],
            'org.transmartproject.helpUrls.scatterPlot': [
                desc:'URL of local help for Scatter Plot workflow'],
            'org.transmartproject.helpUrls.search': [
                desc:'URL of local help for Search'],
            'org.transmartproject.helpUrls.survivalAnalysis': [
                desc:'URL of local help for Survival Analysis workflow'],
            'org.transmartproject.helpUrls.tableWithFisher': [
                desc:'URL of local help for Table with Fisher workflow'],
        ]
        Map i2b2Params = [
            'com.recomdata.i2b2.subject.domain': [ default:'(undefined)',
                desc:'DatasetExplorer i2b2 server domain'],
            'com.recomdata.i2b2.subject.projectid': [ default:'(undefined)',
                desc:'DatasetExplorer i2b2 server projectID'],
            'com.recomdata.i2b2.subject.username': [ default:'(undefined)',
                desc:'DatasetExplorer i2b2 server username'],
            'com.recomdata.i2b2.subject.password': [ default:'(undefined)',
                desc:'DatasetExplorer i2b2 server password'],
            'org.transmartproject.i2b2.instance': [ default:'(undefined)',
                desc:'I2b2 plugin server instance'],
            'org.transmartproject.i2b2.instance.port': [ default:'(undefined)',
                desc:'I2b2 plugin server instance port'],
            'org.transmartproject.i2b2.user_id': [ default:'(undefined)',
                desc:'I2b2 server query userID'],
            'org.transmartproject.i2b2.group_id': [ default:'(undefined)',
                desc:'I2b2 server query groupID'],
            'org.transmartproject.i2b2.pool': [ default:'(undefined)',
                desc:'I2b2 plugin server pool'],
            'org.transmartproject.i2b2.waitTimeMilliseconds': [ default:600000,
                desc:'I2b2 plugin server wait time (ms)'],
        ]
        Map kerberosParams = [
            'grails.plugin.springsecurity.kerberos.active': [
                default:false,
                desc:'If true, and Kerberos and LDAP (also required) providers are included in providwerNames, Kerberos authentication is used'],
        ]
        Map ldapParams = [
            'grails.plugin.springsecurity.ldap.active': [
                default:false,
                desc:'If true, and LDAP provider is included in providerNames, LDAP authentication is used'],
            'org.transmart.security.ldap.newUsernamePattern':[
                desc:'pattern for newly created user, can include &lt;ID&gt; for record id or &lt;FEDERATED_ID&gt; for external user name'],
            'org.transmart.security.ldap.defaultAuthorities':[
                desc:'comma separated list of new user authorities'],
            'org.transmart.security.ldap.inheritPassword':[
                desc:'if inheritPassword == false specified user will not be able to login without LDAP'],
            'org.transmart.security.ldap.mappedUsernameProperty':[
                desc:'can be \'username\' or \'federatedId\''],
            'org.transmart.security.ldap.ad.domain':[
                desc:'Active Directory extension'],
            'org.transmart.security.ldap.context.server':[
                desc:'Active directory server'],
        ]
        Map logParams = [
            'log4j': [
                desc:'Closure to configure server logging'],
        ]
        Map loginParams = [
            'bruteForceLoginLock.allowedNumberOfAttempts': [
                default:2,
                desc:'Number of failed login attempts allowed'],
            'bruteForceLoginLock.lockTimeInMinutes': [
                default:10,
                desc:'Time to disable user login after too many failed attempts (min)'],
            'org.transmartproject.maxConcurrentUserSessions': [
                desc:'Maximum concurrent sessions for one user (-1 for unlimited)'],
            'ui.loginScreen.disclaimer': [
                desc:'Text displayed below login screen'],
        ]
        Map metacoreParams = [
            'com.thomsonreuters.transmart.metacoreAnalyticsEnable': [
                default: false,
                desc:'Enable metacore analytics'],
            'com.thomsonreuters.transmart.metacoreSettingsMode': [
                desc:'Metacore analytics setting: \'demo\', \'system\' or \'user\''],
            'com.thomsonreuters.transmart.demoEnrichmentURL': [
                desc:'Metacore analytics enrichment URL'],
            'com.thomsonreuters.transmart.demoMapBaseURL': [
                desc:'Metacore analytics mapbase URL for demo server'],
            'com.thomsonreuters.transmart.metacoreDefaultLogin': [
                desc:'Metacore analytics default (demo) username'],
            'com.thomsonreuters.transmart.metacoreDefaultPassword': [
                desc:'Metacore analytics default (demo) password'],
            'com.thomsonreuters.transmart.metacoreURL': [
                desc:'Metacore analytics URL, overrides demo settings'],
            'com.thomsonreuters.transmart.metacoreLogin': [
                desc:'Metacore analytics username'],
            'com.thomsonreuters.transmart.metacorePassword': [
                desc:'Metacore analytics password'],
        ]
        Map oauthParams = [
            'dataSource_oauth2.dbCreate': [
                desc:'If true, create new Oauth2 database' ],
            'dataSource_oauth2.driverClassName' : [
                desc:'Oauth2 database driver' ],
            'dataSource_oauth2.url': [
                desc:'Oauth2 database URL' ],
            'dataSource_oauth2.username': [
                desc:'Oauth2 database username' ],
            'dataSource_oauth2.password': [
                desc:'Oauth2 database password' ],
            'dataSource_oauth2.logSql': [
                desc:'Oauth2 database logSql setting' ],
            'dataSource_oauth2.formatSql': [
                desc:'Oauth2 database formatSql setting' ],
        ]
        Map rmodulesParams = [
            'RModules.host': [
                desc:'Host address for Rserve server'],
            'RModules.port': [
                desc:'Port for Rserve server'],
            'RModules.pluginScriptDirectory': [
                desc:'Path to R scripts'],
            'RModules.tempFolderDirectory': [
                desc:'working directory for R scripts, where the jobs are created and output files are generated'],
            'ui.tabs.datasetExplorer.analysisJobs.show': [
                default:true,
                desc:'If defined as true, display \'Analysis Jobs\' tab in the Analyze tab pages'],
        ]
        Map samlParams = [
            'org.transmart.security.samlEnabled': [
                default:false,
                desc:'If true, use SAML authentication and login screen presents SAML federated login prompt'],
            'org.transmart.security.saml.lb.scheme': [
                desc:'SAML connection protocol'],
            'org.transmart.security.saml.lb.serverName': [
                desc:'SAML server'],
            'org.transmart.security.saml.lb.serverPort': [
                desc:'SAML server port'],
            'org.transmart.security.saml.lb.includeServerPortInRequestURL': [
                desc:'Whether to include the serverPort in the request'],
            'org.transmart.security.saml.lb.contextPath': [
                desc:'SAML server path (rest of URL)'],
            'org.transmart.security.saml.createInexistentUsers': [
                desc:'If true, federated access can create federated users that exist on other servers'],
        ]
        Map sampleParams = [
            'sampleExplorer.idfield': [
                desc:'Sample explorer solr ID field name'],
            'edu.harvard.transmart.sampleBreakdownMap.id': [
                desc:'Identifier for sample breakdown'],
            'sampleExplorer.resultsGridHeight': [
                desc:'Sample explorer grid height'],
            'sampleExplorer.resultsGridWidth': [
                desc:'Sample explorer grid width'],
            'com.recomdata.solr.maxNewsStories': [
                desc:'Sample explorer maximum results per page'],
            'com.recomdata.solr.maxRows': [
                desc:'Sample explorer maximum results'],
            'sampleExplorer.fieldMapping.columns': [
                desc:'Names and pretty names of solR sample fields with optional width and other fields'],
       ]
        Map searchParams = [
            'com.recomdata.searchengine.index': [
                desc:'Search tool Lucene index location'],
            'com.recomdata.search.paginate.max': [
                default:10,
                desc:'Maximum to display on page in search results'],
            'com.recomdata.search.paginate.maxsteps': [
                default:10,
                desc:'Number to skip to start of next page of search results, shoulld be same as com.recomdata.search.paginate.max'],
            'com.recomdata.solr.solrFieldList': [
                desc:'Search (obsolete) field list to be indexed, separated by vertical bars'],
            'com.recomdata.solr.fieldExclusionList': [
                desc:'Search (obsolete) fields not displayed, separated by vertical bars'],
            'com.recomdata.solr.resultFields': [
                desc:'Search (obsolete) result fields - in alphabetical order, separated by commas'],
            'com.recomdata.solr.maxLinksDisplayed': [
                desc:'Search (obsolete) number of results before displaying \'More[+]\''],
            'com.recomdata.solr.numberOfSuggestions': [
                desc:'Search (obsolete) number of items in search suggestions box'],
        ]
        Map smartrParams = [
            'ipaConnector.username': [
                desc:'IPA connector username'],
            'ipaConnector.password': [
                desc:'IPA connector password'],
            'smartR.baseDir': [
                desc:'Base directory for smartR'],
            'smartR.remoteScriptDirectory': [
                desc:'Directory on R server to copy smartR scripts'],
            'grails.plugin.transmartGwasPlink.enabled': [
                desc:'GWAS plink enabled if true'],
        ]
        Map solrParams = [
            :
        ]
        Map springParams = [
            'grails.plugin.springsecurity.userLookup.userDomainClassName': [
                desc:'customized user GORM class (org.transmart.searchapp.AuthUser)'],
            'grails.plugin.springsecurity.userLookup.passwordPropertyName': [
                desc:'customized password field (passwd)'],
            'grails.plugin.springsecurity.userLookup.authorityJoinClassName': [
                desc:'customized user /role join GORM class (org.transmart.searchapp.AuthUser)'],
            'grails.plugin.springsecurity.authority.className': [
                desc:'customized role GORM class (org.transmart.searchapp.Role)'],
            'grails.plugin.springsecurity.requestMap.className': [
                desc:'(Obsolete) request map GORM class name - request map is stored in the db (org.transmart.searchapp.Requestmap)'],
            'grails.plugin.springsecurity.securityConfigType': [
                desc:'requestmap in db (InterceptUrlMap to use oauth)'],
            'grails.plugin.springsecurity.successHandler.defaultTargetUrl': [
                desc:'URL to redirect after login. Set to value of org.transmart.defaultLoginRedirect else /userLanding'],
            'grails.plugin.springsecurity.logout.afterLogoutUrl': [
                desc:'URL after logout (/login/forceAuth)'],
        ]
        Map uiParams = [
            'com.recomdata.sessionTimeout': [
                desc:'Session timeout (inactivity in seconds)'],
            'com.recomdata.heartbeatLaps': [
                desc:'Session heartneat frequency to check for activity'],
            'com.recomdata.bugreportURL': [
                desc:'Bug tracking system URL to enable Utilities menu \'Report a Bug\''],
            'com.recomdata.debug.jsCallbacks': [
                desc:'used to debug JavaScript callbacks in the dataset explorer in Chrome. Unfortunately, it also sometimes causes chrome to segfault'],
            'com.recomdata.defaults.landing': [
                desc:'Landing page after login. If undefined use Browse page or (if Browse is disabled) Analyze page'],
            'org.transmart.xnatImporterEnable': [
                default:false,
                desc:'If true, xnat importer is an option on the admin page'],
            'org.transmart.defaultLoginRedirect': [
                desc:'default page displayed after login'],
            'ui.jirareport.hide': [
                default:false,
                desc:'If true, remove \'Report a bug\' button on bottom left of every page'],
            'ui.loginScreen.disclaimer': [
                default:false,
                desc:'If defined, a text to display below the initial login'],
            'ui.tabs.browse.hide': [
                default:false,
                desc:'If true, disable the Browse tab page, remove Browse from the common header, use the Analyze page as the landing page after login'],
            'ui.tabs.datasetExplorer.dataExport.hide': [
                default:false,
                desc:'If true, remove the Data Export tab from the Analyze tab pages'],
            'ui.tabs.datasetExplorer.dataExportJobs.hide': [
                default:false,
                desc:'If true, remove the Data Export Jobs tab from the Analyze tab pages'],
            'ui.tabs.datasetExplorer.gridView.hide': [
                default:false,
                desc:'If true, remove the Grid View tab from the Analyze tab pages'],
            'ui.tabs.datasetExplorer.workspace.hide': [
                default:false,
                desc:'If true, remove the Workspace tab from the Analyze tab pages'],
            'ui.tabs.geneSignature.hide': [
                default:false,
                desc:'If true, remove the Gene Signature tab from the common header'],
            'ui.tabs.gwas.hide': [
                default:false,
                desc:'If true, remove the GWAS tab from the common header'],
            'ui.tabs.sampleExplorer.hide': [
                default:false,
                desc:'If true, remove the Sample Explorer tab from the common header'],
            'ui.tabs.search.show': [
                default:false,
                desc:'If true, restore the obsolete Search tab to the common header'],
            'ui.tabs.uploadData.hide': [
                default:false,
                desc:'If true, remove the Upload tab from the common header'],
        ]
        Map uploadParams = [
            'com.recomdata.dataUpload.uploads.dir': [
                desc:'GWAS data upload directory on server'],
            'com.recomdata.dataUpload.templates.dir': [
                desc:'GWAS data upload templates directory on server'],
            'com.recomdata.dataUpload.etl.dir': [
                desc:'GWAS data upload directory for analysis ETL'],
            'com.recomdata.dataUpload.adminEmail': [
                desc:'Contact email for analysis ETL administrator'],
            'com.recomdata.dataUpload.appTitle': [
                desc:'Title for data upload page'],
            'com.recomdata.dataUpload.stageScript': [
                desc:'GWAS analysis ETL script'],
        ]
        Map x509Params = [
            :
        ]
        Map xnatImportParams = [
            'org.transmart.data.location': [
                desc:'Local directory of transmart-data files for XNAT import scripts<br>Defaults to /transmart-data under /transmart'],
            'org.transmart.importxnatplugin.etldir': [
                desc:'Local directory to write imported XNAT clinical data<br>Defaults to end/data-integration/ under org.transmart.data.location'],
            'org.transmart.importxnatplugin.kettlehome': [
                desc:'Local Kettle home directory for XNAT import<br>Defaults to samples/postgres/kettle-home under transmart-data'],
            'org.transmart.importxnatplugin.location': [
                desc:'Local directory for XNAT importer scripts<br>Defaults to /xnattotransmartlink under /transmart'],
            'org.transmart.importxnatplugin.workingdir': [
                desc:'Working directory for XNAT import<br>Defaults to value of RModules.tempFolderDirectory'],
        ]
        Map xnatViewParams = [
            'org.transmart.xnatViewerEnable': [
                default:false,
                desc:'If true, enable the XNAT viewer in the Analyze tab'],
            'org.xnat.domain': [
                desc:'XNAT server domain name for XNAT image viewer'],
            'org.xnat.projectName': [
                desc:'XNAT project name for XNAT image viewer'],
            'org.xnat.username': [
                desc:'XNAT username for XNAT image viewer'],
            'org.xnat.password': [
                desc:'XNAT password (plain text) for XNAT image viewer'],
        ]

        Map Params = [:]

        def properties = grailsApplication.config.toProperties().sort()
        properties.each { k,v ->

            // admin
            if(k == "com.recomdata.admin.paginate.max"){addConfig(adminParams,k,v,'')}
            else if(k == "org.transmart.config.showUsernames"){addConfig(adminParams,k,v,'')}
            else if(k == "org.transmart.config.showPasswords"){addConfig(adminParams,k,v,'')}
            else if(k == "com.recomdata.plugin.pluginScriptDirectory"){addConfig(adminParams,k,v,'')}
            else if(k == "org.transmart.originalConfigBinding"){addConfig(adminParams,k,v,'Saved original configuration')}

            // analysis
            else if(k == "com.recomdata.datasetExplorer.genePatternEnabled"){addConfig(analysisParams,k,v,'')}
            else if(k == "com.recomdata.datasetExplorer.genePatternURL"){addConfig(analysisParams,k,v,'')}
            else if(k == "com.recomdata.datasetExplorer.genePatternRealURLBehindProxy"){addConfig(analysisParams,k,v,'')}
            else if(k == "com.recomdata.datasetExplorer.genePatternUser"){addConfig(analysisParams,k,encodeConfigUsername(v),'')}

            else if(k == "com.recomdata.analysis.data.file.dir"){addConfig(analysisParams,k,v,'')}
            else if(k == "com.recomdata.analysis.genepattern.file.dir"){addConfig(analysisParams,k,v,'')}
            else if(k == "com.recomdata.analysis.survival.censorFlagList"){addConfig(analysisParams,k,v,'')}
            else if(k == "com.recomdata.analysis.survival.survivalDataList"){addConfig(analysisParams,k,v,'')}
            else if(k == "com.recomdata.datasetExplorer.plinkExcutable"){addConfig(analysisParams,k,v,'')}

            // analyze tab
            else if(k == "com.recomdata.datasetExplorer.gridShortLabels"){addConfig(analyzeParams,k,v,'Grid view short labels for standard terms')}
            else if(k == "com.recomdata.datasetExplorer.hideAcrossTrialsPanel"){addConfig(analyzeParams,k,v,'Hide across trial panel')}
            else if(k == "com.recomdata.datasetExplorer.imageTempDir"){addConfig(analyzeParams,k,v,'')}
            else if(k == "com.recomdata.datasetExplorer.inforsense"){addConfig(analyzeParams,k,v,'Deprecated - leave it as false')}
            else if(k == "com.recomdata.datasetExplorer.pmServiceURL"){addConfig(analyzeParams,k,v,'I2b2 project management cell url')}
            else if(k == "com.recomdata.datasetExplorer.pmServiceProxy"){addConfig(analyzeParams,k,v,'Turn proxy on if the project management cell is deployed on a different server or not through the apache proxy')}
            else if(k == "com.recomdata.plugins.tempFolderDirectory"){addConfig(analyzeParams,k,v,'')}

            // auth
            else if(k == "grails.plugin.springsecurity.providerNames"){addConfigLink(authParams,k,linkAuthProviders(grailsApplication.config.grails.plugin.springsecurity.providerNames),'Click link to see full list')}
            else if(k == "com.recomdata.guestAutoLogin"){addConfig(authParams,k,v,'')}
            else if(k == "com.recomdata.guestUserName"){addConfig(authParams,k,v,'')}
            else if(k == "com.recomdata.passwordstrength.pattern"){addConfig(authParams,k,v,'')}
            else if(k == "com.recomdata.passwordstrength.description"){addConfig(authParams,k,v,'')}
            else if(k == "transmartproject.authUser.create.passwordRequired"){addConfig(authParams,k,v,'')}
            else if(k == "org.transmart.security.sniValidation"){addConfig(authParams,k,v,'')}
            else if(k == "org.transmart.security.spnegoEnabled"){addConfig(authParams,k,v,'')}
            else if(k == "org.transmart.security.sslValidation"){addConfig(authParams,k,v,'')}
            else if(k == "user.password.strength.regex"){addConfig(authParams,k,v,'')}

            // browse
            else if(k == "com.recomdata.FmFolderJob.cronExpression"){addConfig(browseParams,k,v,'')}
            else if(k == "com.recomdata.FmFolderJob.startDelayMs"){addConfig(browseParams,k,v,'')}
            else if(k == "com.recomdata.FmFolderService.filestoreDirectory"){addConfig(browseParams,k,v,'')}
            else if(k == "com.recomdata.FmFolderService.importDirectory"){addConfig(browseParams,k,v,'')}
            else if(k == "com.recomdata.FmFolderService.fileTypes"){addConfig(browseParams,k,v,'')}
            else if(k == "com.recomdata.solr.baseURL"){addConfig(browseParams,k,v,'')}
            else if(k == "com.rwg.solr.scheme"){addConfig(browseParams,k,v,'')}
            else if(k == "com.rwg.solr.host"){addConfig(browseParams,k,v,'')}
            else if(k == "com.rwg.solr.path"){addConfig(browseParams,k,v,'')}
            else if(k == "com.rwg.solr.browse.path"){addConfig(browseParams,k,v,'')}
            else if(k == "com.rwg.solr.update.path"){addConfig(browseParams,k,v,'')}
            else if(k == "transmartproject.mongoFiles.enableMongo"){addConfig(browseParams,k,v,'')}
            else if(k == "transmartproject.mongoFiles.useDriver"){addConfig(browseParams,k,v,'')}
            else if(k == "transmartproject.mongoFiles.dbServer"){addConfig(browseParams,k,v,'')}
            else if(k == "transmartproject.mongoFiles.dbPort"){addConfig(browseParams,k,v,'')}
            else if(k == "transmartproject.mongoFiles.dbName"){addConfig(browseParams,k,v,'')}
            else if(k == "transmartproject.mongoFiles.apiURL"){addConfig(browseParams,k,v,'')}
            else if(k == "transmartproject.mongoFiles.apiKey"){addConfig(browseParams,k,encodeConfigUsername(v),'')}
            else if(k == "ui.browse.delete.allowprogram"){addConfig(browseParams,k,v,'')}
            else if(k == "ui.browse.delete.allowstudy"){addConfig(browseParams,k,v,'')}

            // buildInfo
            else if(k == "buildInfo.properties.include"){addConfig(buildInfoParams,k,v,'')}
            else if(k == "buildInfo.properties.exclude"){addConfig(buildInfoParams,k,v,'')}

            // contact
            else if(k == "com.recomdata.adminEmail"){addConfig(contactParams,k,v,'')}
            else if(k == "com.recomdata.administrator"){addConfig(contactParams,k,v,'Obsolete administrator email address, use com.recomdata.adminEmail')}
            else if(k == "com.recomdata.contactUs"){addConfig(contactParams,k,v,'')}
            else if(k == "com.recomdata.searchtool.contactUs"){addConfig(contactParams,k,v,'Obsolete search panel contact email, use com.recomdata.contactUs')}

            // datasource
            else if(k == "dataSource.dialect"){addConfig(dataSourceParams,k,v,'')}
            else if(k == "dataSource.driverClassName"){addConfig(dataSourceParams,k,v,'')}
            else if(k == "dataSource.dbCreate"){addConfig(dataSourceParams,k,v,'')}
            else if(k == "dataSource.url"){addConfig(dataSourceParams,k,v,'')}
            else if(k == "dataSource.username"){addConfig(dataSourceParams,k,encodeConfigUsername(v),'')}
            else if(k == "dataSource.formatSql"){addConfig(dataSourceParams,k,v,'')}
            else if(k == "dataSource.logSql"){addConfig(dataSourceParams,k,v,'')}
            else if(k == "dataSource.password"){addConfig(dataSourceParams,k,encodeConfigPassword(v),'')}
            else if(k == "dataSource.properties.initialSize"){addConfig(dataSourceParams,k,v,'')}
	    else if(k == "dataSource.properties.minIdle"){addConfig(dataSourceParams,k,v,'')}
	    else if(k == "dataSource.properties.maxIdle"){addConfig(dataSourceParams,k,v,'')}
	    else if(k == "dataSource.properties.maxActive"){addConfig(dataSourceParams,k,v,'')}
	    else if(k == "dataSource.properties.maxWait"){addConfig(dataSourceParams,k,v,'')}
	    else if(k == "dataSource.properties.numTestsPerEvictionRun"){addConfig(dataSourceParams,k,v,'')}
	    else if(k == "dataSource.properties.minEvictableIdleTimeMillis"){addConfig(dataSourceParams,k,v,'')}
	    else if(k == "dataSource.properties.timeBetweenEvictionRunsMillis"){addConfig(dataSourceParams,k,v,'')}
	    else if(k == "dataSource.properties.validationQuery"){addConfig(dataSourceParams,k,v,'')}
	    else if(k == "dataSource.properties.testOnBorrow"){addConfig(dataSourceParams,k,v,'')}
	    else if(k == "dataSource.properties.testWhileIdle"){addConfig(dataSourceParams,k,v,'')}
	    else if(k == "dataSource.properties.testOnReturn"){addConfig(dataSourceParams,k,v,'')}

            // export
            else if(k == "com.recomdata.export.jobs.sweep.startDelay"){addConfig(exportParams,k,v,'')}
            else if(k == "com.recomdata.export.jobs.sweep.repeatInterval"){addConfig(exportParams,k,v,'')}
            else if(k == "com.recomdata.export.jobs.sweep.fileAge"){addConfig(exportParams,k,v,'')}

            else if(k.startsWith("com.recomdata.transmart.data.export.dataTypesMap.")){
                def kk = k - "com.recomdata.transmart.data.export.dataTypesMap."
                addConfig(exportParams,k,v,"Export datatypes map for ${kk}")
            }

            else if(k == "com.recomdata.transmart.data.export.ftp.password"){addConfig(exportParams,k,encodeConfigPassword(v),'')}
            else if(k == "com.recomdata.transmart.data.export.ftp.remote.path"){addConfig(exportParams,k,v,'')}
            else if(k == "com.recomdata.transmart.data.export.ftp.server"){addConfig(exportParams,k,v,'')}
            else if(k == "com.recomdata.transmart.data.export.ftp.serverport"){addConfig(exportParams,k,v,'')}
            else if(k == "com.recomdata.transmart.data.export.ftp.username"){addConfig(exportParams,k,encodeConfigUsername(v),'')}
            else if(k == "com.recomdata.transmart.data.export.max.export.jobs.loaded"){addConfig(exportParams,k,v,'')}
            else if(k == "com.recomdata.transmart.data.export.jobTmpDirectory"){addConfig(exportParams,k,v,'')}
            else if(k == "com.recomdata.transmart.data.export.rScriptDirectory"){addConfig(exportParams,k,v,'')}
            else if(k == "dataExport.bed.acgh.rgbColorScheme"){addConfig(exportParams,k,v,'')}
            else if(k == "com.recomdata.search.gene.max"){addConfig(exportParams,k,v,'')}
            else if(k == "com.recomdata.search.genepathway"){addConfig(exportParams,k,v,'')}
            else if(k == "com.recomdata.plugins.resultSize"){addConfig(exportParams,k,v,'')}

            // galaxy
            else if(k == "com.galaxy.blend4j.galaxyEnabled"){addConfig(galaxyParams,k,v,'')}
            else if(k == "com.galaxy.blend4j.galaxyURL"){addConfig(galaxyParams,k,v,'')}

            // general
            else if(k == "com.recomdata.appTitle"){addConfig(generalParams,k,v,'')}
            else if(k == "com.recomdata.largeLogo"){addConfig(generalParams,k,v,'')}
            else if(k == "com.recomdata.smallLogo"){addConfig(generalParams,k,v,'')} 
            else if(k == "com.recomdata.projectName"){addConfig(generalParams,k,v,'')}
            else if(k == "com.recomdata.projectLogo"){addConfig(generalParams,k,v,'')}
            else if(k == "com.recomdata.providerName"){addConfig(generalParams,k,v,'')}
            else if(k == "com.recomdata.providerLogo"){addConfig(generalParams,k,v,'')}
            else if(k == "com.recomdata.providerURL"){addConfig(generalParams,k,v,'')}
            else if(k == "com.recomdata.skipdisclaimer"){addConfig(generalParams,k,v,'')}
            else if(k == "com.recomdata.disclaimer"){addConfig(generalParams,k,v,'')}
            else if(k == "com.recomdata.view.studyview"){addConfig(generalParams,k,v,'')}
            else if(k == "org.transmart.configFine"){addConfig(generalParams,k,v,'')}

            else if(k == "grails.cache.ehcache.ehcacheXmlLocation"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.cache.ehcache.reloadable"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.cache.enabled"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.config.locations"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.converters.default.pretty.print"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.converters.encoding"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.databinding.convertEmptyStringsToNull"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.databinding.trimStrings"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.enable.native2ascii"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.exceptionresolver.params.exclude"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.mime.disable.accept.header.userAgents"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.mime.file.extensions"){addConfig(generalParams,k,v,'')}

            else if(k.startsWith("grails.mime.types.")){
                def kk = k - "grails.mime.types."
                addConfig(generalParams,k,v,"Mime type ${kk}")
            }

            else if(k == "grails.plugins.sendfile.tomcat"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.resources.adhoc.excludes"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.spring.bean.packages"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.views.default.codec"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.views.gsp.encoding"){addConfig(generalParams,k,v,'')}
            else if(k == "grails.views.javascript.library"){addConfig(generalParams,k,v,'')}

            else if(k == "hibernate.cache.region.factory_class"){addConfig(generalParams,k,v,'')}
            else if(k == "hibernate.cache.use_query_cache"){addConfig(generalParams,k,v,'')}
            else if(k == "hibernate.cache.use_second_level_cache"){addConfig(generalParams,k,v,'')}

            // gwas
            else if(k == "com.recomdata.rwg.manhattanplots.cacheImages"){addConfig(gwasParams,k,v,'')}
            else if(k == "com.recomdata.rwg.qqplots.cacheImages"){addConfig(gwasParams,k,v,'')}
            else if(k == "com.recomdata.rwg.qqplots.temporaryImageFolder"){addConfig(gwasParams,k,v,'')}
            else if(k == "com.recomdata.rwg.qqplots.temporaryImageFolderFullPath"){addConfig(gwasParams,k,v,'')}
            else if(k == "com.recomdata.gwas.usehg19table"){addConfig(gwasParams,k,v,'')}
            else if(k == "grails.plugin.transmartGwasPlink.plinkPath"){addConfig(gwasParams,k,v,'')}

            // gwava
            else if(k == "com.recomdata.rwg.webstart.codebase"){addConfig(gwavaParams,k,v,'')}
            else if(k == "com.recomdata.rwg.webstart.gwavaInstance"){addConfig(gwavaParams,k,v,'')}
            else if(k == "com.recomdata.rwg.webstart.href"){addConfig(gwasParams,k,v,'')}
            else if(k == "com.recomdata.rwg.webstart.jar"){addConfig(gwasParams,k,v,'')}
            else if(k == "com.recomdata.rwg.webstart.mainClass"){addConfig(gwasParams,k,v,'')}
            else if(k == "com.recomdata.rwg.webstart.transmart.url"){addConfig(gwavaParams,k,v,'')}

            // help
            else if(k == "com.recomdata.adminHelpURL"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.hiDomePopUp"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.summaryStatistics"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.geneSignatureList"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.rsIdSignatureList"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.boxPlot"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.correlationAnalysis"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.heatMap"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.heatMapMaxRows"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.hierarchicalClustering"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.hierarchicalClusteringMaxRows"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.kMeansClustering"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.kMeansClusteringMaxRows"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.lineGraph"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.logisticRegression"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.markerSelection"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.pca"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.scatterPlot"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.search"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.survivalAnalysis"){addConfig(helpParams,k,v,'')}
            else if(k == "org.transmartproject.helpUrls.tableWithFisher"){addConfig(helpParams,k,v,'')}

            // i2b2
            else if(k == "com.recomdata.i2b2.subject.domain"){addConfig(i2b2Params,k,v,'')}
            else if(k == "com.recomdata.i2b2.subject.projectid"){addConfig(i2b2Params,k,v,'')}
            else if(k == "com.recomdata.i2b2.subject.username"){addConfig(i2b2Params,k,encodeConfigUsername(v),'')}
            else if(k == "com.recomdata.i2b2.subject.password"){addConfig(i2b2Params,k,encodeConfigPassword(v),'')}
            else if(k == "org.transmartproject.i2b2.instance"){addConfig(i2b2Params,k,v,'')}
            else if(k == "org.transmartproject.i2b2.instance.port"){addConfig(i2b2Params,k,v,'')}
            else if(k == "org.transmartproject.i2b2.user_id"){addConfig(i2b2Params,k,encodeConfigUsername(v),'')}
            else if(k == "org.transmartproject.i2b2.group_id"){addConfig(i2b2Params,k,v,'')}
            else if(k == "org.transmartproject.i2b2.pool"){addConfig(i2b2Params,k,v,'')}
            else if(k == "org.transmartproject.i2b2.waitTimeMilliseconds"){addConfig(i2b2Params,k,v,'')}

            else if(k == "com.recomdata.i2b2.sample.domain"){addConfig(i2b2Params,k,v,'')}
            else if(k == "com.recomdata.i2b2.sample.projectid"){addConfig(i2b2Params,k,v,'')}
            else if(k == "com.recomdata.i2b2.sample.username"){addConfig(i2b2Params,k,encodeConfigUsername(v),'')}
            else if(k == "com.recomdata.i2b2.sample.password"){addConfig(i2b2Params,k,encodeConfigPassword(v),'')}

            else if(k == "com.recomdata.i2b2helper.i2b2demodata"){addConfig(i2b2Params,k,v,'')}
            else if(k == "com.recomdata.i2b2helper.i2b2hive"){addConfig(i2b2Params,k,v,'')}
            else if(k == "com.recomdata.i2b2helper.i2b2metadata"){addConfig(i2b2Params,k,v,'')}

            // kerberos
            else if(k == "grails.plugin.springsecurity.kerberos.active"){addConfig(kerberosParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.kerberos.debug"){addConfig(kerberosParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.kerberos.client.debug"){addConfig(kerberosParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.kerberos.ticketValidator.debug"){addConfig(kerberosParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.kerberos.skipIfAlreadyAuthenticated"){addConfig(kerberosParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.kerberos.successHandler.headerName"){addConfig(kerberosParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.kerberos.successHandler.headerPrefix"){addConfig(kerberosParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.kerberos.ticketValidator.holdOnToGSSContext"){addConfig(kerberosParams,k,v,'')}

            // ldap
            else if(k == "grails.plugin.springsecurity.ldap.active"){addConfig(ldapParams,k,v,'')}
            else if(k == "org.transmart.security.ldap.newUsernamePattern"){addConfig(ldapParams,k,v,'')}
            else if(k == "org.transmart.security.ldap.defaultAuthorities"){addConfig(ldapParams,k,v,'')}
            else if(k == "org.transmart.security.ldap.inheritPassword"){addConfig(ldapParams,k,v,'')}
            else if(k == "org.transmart.security.ldap.mappedUsernameProperty"){addConfig(ldapParams,k,v,'')}
            else if(k == "org.transmart.security.ldap.ad.domain"){addConfig(ldapParams,k,v,'')}
            else if(k == "org.transmart.security.ldap.context.server"){addConfig(ldapParams,k,v,'')}

            else if(k == "grails.plugin.springsecurity.ldap.auth.hideUserNotFoundExceptions"){addConfig(ldapParams,k,v,'If true, throw BadCredentialsExceptionelse throw explicit UsernameNotFoundException')}
            else if(k == "grails.plugin.springsecurity.ldap.auth.useAuthPassword"){addConfig(ldapParams,k,v,'If true, use supplied password, else try to obtain password from Userdetails object')}
            else if(k == "grails.plugin.springsecurity.ldap.authenticator.attributesToReturn"){addConfig(ldapParams,k,v,'Names of attribute ids to return; use null to return all and an empty list to return none')}
            else if(k == "grails.plugin.springsecurity.ldap.authenticator.passwordAttributeName"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.authenticator.useBind"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.authorities.clean.dashes"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.authorities.clean.uppercase"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.authorities.groupRoleAttribute"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.authorities.groupSearchBase"){addConfig(ldapParams,k,v,'The base DN from which the search for group membership should be performed')}
            else if(k == "grails.plugin.springsecurity.ldap.authorities.groupSearchFilter"){addConfig(ldapParams,k,v,'The pattern to be used for the user search. {0} is the user DN')}
            else if(k == "grails.plugin.springsecurity.ldap.authorities.ignorePartialResultException"){addConfig(ldapParams,k,v,'Whether PartialResultExceptions should be ignored in searches, typically used with Active Directory since AD servers often have a problem with referrals')}
            else if(k == "grails.plugin.springsecurity.ldap.authorities.prefix"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.authorities.retrieveDatabaseRoles"){addConfig(ldapParams,k,v,'Whether to retrieve additional roles from the database using the User/Role many-to-many')}
            else if(k == "grails.plugin.springsecurity.ldap.authorities.retrieveGroupRoles"){addConfig(ldapParams,k,v,'Whether to infer roles based on group membership')}
            else if(k == "grails.plugin.springsecurity.ldap.authorities.searchSubtree"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.context.anonymousReadOnly"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.context.cacheEnvironmentProperties"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.context.contextFactoryClassName"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.context.dirObjectFactoryClassName"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.context.managerDn"){addConfig(ldapParams,k,v,'LDAP DN to authenticate with')}
            else if(k == "grails.plugin.springsecurity.ldap.context.managerPassword"){addConfig(ldapParams,k,encodeConfigPassword(v),'LDAP manager password for authentication (plain text)')}
            else if(k == "grails.plugin.springsecurity.ldap.context.server"){addConfig(ldapParams,k,v,'Address of the LDAP server (see org.transmart.security.ldap.context.server)')}
            else if(k == "grails.plugin.springsecurity.ldap.mapper.convertToUpperCase"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.mapper.passwordAttributeName"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.rememberMe.detailsManager.groupMemberAttributeName"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.rememberMe.detailsManager.groupRoleAttributeName"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.rememberMe.detailsManager.groupSearchBase"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.rememberMe.detailsManager.passwordAttributeName"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.search.base"){addConfig(ldapParams,k,v,'Context name to search in, relative to the base of the configured ContextSource')}
            else if(k == "grails.plugin.springsecurity.ldap.search.derefLink"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.search.filter"){addConfig(ldapParams,k,v,'The filter expression used in the user search')}
            else if(k == "grails.plugin.springsecurity.ldap.search.searchSubtree"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.search.timeLimit"){addConfig(ldapParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ldap.useRememberMe"){addConfig(ldapParams,k,v,'')}
            else if(k == "org.transmart.security.ldap.inheritPassword"){addConfig(ldapParams,k,v,'')}
            else if(k == "org.transmart.security.ldap.mappedUsernameProperty"){addConfig(ldapParams,k,v,'')}

            // log
	    else if(k == "log4j"){addConfig(logParams,k,v,'')}

            // login
            else if(k == "bruteForceLoginLock.allowedNumberOfAttempts"){addConfig(loginParams,k,v,'')}
            else if(k == "bruteForceLoginLock.lockTimeInMinutes"){addConfig(loginParams,k,v,'')}
            else if(k == "org.transmartproject.maxConcurrentUserSessions"){addConfig(loginParams,k,v,'')}
            else if(k == "ui.loginScreen.disclaimer"){addConfig(loginParams,k,v,'')}

            // metacore
            else if(k == "com.thomsonreuters.transmart.metacoreAnalyticsEnable"){addConfig(metacoreParams,k,v,'')}
            else if(k == "com.thomsonreuters.transmart.metacoreSettingsMode"){addConfig(metacoreParams,k,v,'')}
            else if(k == "com.thomsonreuters.transmart.demoEnrichmentURL"){addConfig(metacoreParams,k,v,'')}
            else if(k == "com.thomsonreuters.transmart.demoMapBaseURL"){addConfig(metacoreParams,k,v,'')}
            else if(k == "com.thomsonreuters.transmart.metacoreDefaultLogin"){addConfig(metacoreParams,k,encodeConfigUsername(v),'')}
            else if(k == "com.thomsonreuters.transmart.metacoreDefaultPassword"){addConfig(metacoreParams,k,encodeConfigPassword(v),'')}
            else if(k == "com.thomsonreuters.transmart.metacoreURL"){addConfig(metacoreParams,k,v,'')}
            else if(k == "com.thomsonreuters.transmart.metacoreLogin"){addConfig(metacoreParams,k,encodeConfigUsername(v),'')}
            else if(k == "com.thomsonreuters.transmart.metacorePassword"){addConfig(metacoreParams,k,encodeConfigPassword(v),'')}

            // oauth
            else if(k == "dataSource_oauth2.dbCreate"){addConfig(oauthParams,k,v,'')}
            else if(k == "dataSource_oauth2.driverClassName"){addConfig(oauthParams,k,v,'')}
            else if(k == "dataSource_oauth2.formatSql"){addConfig(oauthParams,k,v,'')}
            else if(k == "dataSource_oauth2.logSql"){addConfig(oauthParams,k,v,'')}
            else if(k == "dataSource_oauth2.password"){addConfig(oauthParams,k,encodeConfigPassword(v),'')}
            else if(k == "dataSource_oauth2.url"){addConfig(oauthParams,k,v,'')}
            else if(k == "dataSource_oauth2.username"){addConfig(oauthParams,k,encodeConfigUsername(v),'')}

            else if(k == "grails.plugin.springsecurity.oauthProvider.accessTokenLookup.additionalInformationPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.accessTokenLookup.authenticationKeyPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.accessTokenLookup.authenticationPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.accessTokenLookup.className"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.accessTokenLookup.clientIdPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.accessTokenLookup.expirationPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.accessTokenLookup.refreshTokenPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.accessTokenLookup.scopePropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.accessTokenLookup.tokenTypePropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.accessTokenLookup.usernamePropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.accessTokenLookup.valuePropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.active"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.approval.approvalValiditySeconds"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.approval.auto"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.approval.handleRevocationAsExpiry"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.approval.scopePrefix"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.approvalLookup.approvedPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.approvalLookup.clientIdPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.approvalLookup.expirationPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.approvalLookup.lastModifiedPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.approvalLookup.scopePropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.approvalLookup.usernamePropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.authorization.requireRegisteredRedirectUri"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.authorization.requireScope"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.authorizationCodeLookup.authenticationPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.authorizationCodeLookup.className"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.authorizationCodeLookup.codePropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.authorizationEndpointUrl"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.basicAuthenticationFilterStartPosition"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clientFilterStartPosition"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clientLookup.accessTokenValiditySecondsPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clientLookup.additionalInformationPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clientLookup.authoritiesPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clientLookup.authorizedGrantTypesPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clientLookup.autoApproveScopesPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clientLookup.className"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clientLookup.clientIdPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clientLookup.clientSecretPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clientLookup.redirectUrisPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clientLookup.refreshTokenValiditySecondsPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clientLookup.resourceIdsPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clientLookup.scopesPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.clients"){addConfigLink(oauthParams,k,linkOauthClients(grailsApplication.config.grails.plugin.springsecurity.oauthProvider.clients),'Click link to see full list')}

            else if(k == "grails.plugin.springsecurity.oauthProvider.credentialsCharset"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.defaultClientConfig.authorities"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.defaultClientConfig.authorizedGrantTypes"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.defaultClientConfig.autoApproveScopes"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.defaultClientConfig.resourceIds"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.defaultClientConfig.scope"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.exceptionTranslationFilterStartPosition"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.filterStartPosition"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.grantTypes.authorizationCode"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.grantTypes.clientCredentials"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.grantTypes.implicit"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.grantTypes.password"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.grantTypes.refreshToken"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.refreshTokenLookup.authenticationPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.refreshTokenLookup.className"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.refreshTokenLookup.expirationPropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.refreshTokenLookup.valuePropertyName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.registerBasicAuthenticationFilter"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.errorEndpointUrl"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.realmName"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.registerExceptionTranslationFilter"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.registerStatelessFilter"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.statelessFilterStartPosition"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.tokenEndpointUrl"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.tokenServices.accessTokenValiditySeconds"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.tokenServices.refreshTokenValiditySeconds"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.tokenServices.registerTokenEnhancers"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.tokenServices.reuseRefreshToken"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.tokenServices.supportRefreshToken"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.userApprovalEndpointUrl"){addConfig(oauthParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.oauthProvider.userApprovalParameter"){addConfig(oauthParams,k,v,'')}

            // rmodules
            else if(k == "RModules.host"){addConfig(rmodulesParams,k,v,'')}
            else if(k == "RModules.port"){addConfig(rmodulesParams,k,v,'')}
            else if(k == "RModules.pluginScriptDirectory"){addConfig(rmodulesParams,k,v,'')}
            else if(k == "RModules.tempFolderDirectory"){addConfig(rmodulesParams,k,v,'')}
            else if(k == "RModules.imageURL"){addConfig(rmodulesParams,k,v,'(Obsolete) URL path for images from analysis jobs - now fixed as /analysisfiles/')}
            else if(k == "ui.tabs.datasetExplorer.analysisJobs.show"){addConfig(rmodulesParams,k,v,'')}
            else if(k == "quartz._properties.org.quartz.scheduler.skipUpdateCheck"){addConfig(rmodulesParams,k,v,'')}
            else if(k == "quartz.autoStartup"){addConfig(rmodulesParams,k,v,'')}
            else if(k == "quartz.exposeSchedulerInRepository"){addConfig(rmodulesParams,k,v,'')}
            else if(k == "quartz.jdbcStore"){addConfig(rmodulesParams,k,v,'')}
            else if(k == "quartz.props.scheduler.skipUpdateCheck"){addConfig(rmodulesParams,k,v,'')}
            else if(k == "quartz.waitForJobsToCompleteOnShutdown"){addConfig(rmodulesParams,k,v,'')}

            // saml
            else if(k == "org.transmart.security.samlEnabled"){addConfig(samlParams,k,v,'')}
            else if(k == "org.transmart.security.lb.scheme"){addConfig(samlParams,k,v,'')}
            else if(k == "org.transmart.security.lb.serverName"){addConfig(samlParams,k,v,'')}
            else if(k == "org.transmart.security.lb.serverPort"){addConfig(samlParams,k,v,'')}
            else if(k == "org.transmart.security.lb.includeServerPortInRequestURL"){addConfig(samlParams,k,v,'')}
            else if(k == "org.transmart.security.lb.contextPath"){addConfig(samlParams,k,v,'')}
            else if(k == "org.transmart.security.lb.createInexistentUsers"){addConfig(samlParams,k,v,'')}


            // sampleExplorer
            else if(k == "sampleExplorer.idfield"){addConfig(sampleParams,k,v,'')}
            else if(k == "edu.harvard.transmart.sampleBreakdownMap.aliquot_id"){addConfig(sampleParams,k,v,'')}
            else if(k == "sampleExplorer.resultsGridHeight"){addConfig(sampleParams,k,v,'')}
            else if(k == "sampleExplorer.resultsGridWidth"){addConfig(sampleParams,k,v,'')}
            else if(k == "com.recomdata.solr.maxNewsStories"){addConfig(sampleParams,k,v,'')}
            else if(k == "com.recomdata.solr.maxRows"){addConfig(sampleParams,k,v,'')}

            else if(k == "sampleExplorer.fieldMapping.clone"){addConfig(sampleParams,k,v,'')}
            else if(k == "sampleExplorer.fieldMapping.columns"){addConfigLink(sampleParams,k,linkSampleMapping(grailsApplication.config.sampleExplorer.fieldMapping.columns),'Click link to see full list')}

            // search
            else if(k == "com.recomdata.searchengine.index"){addConfig(searchParams,k,v,'')}
            else if(k == "com.recomdata.search.paginate.max"){addConfig(searchParams,k,v,'')}
            else if(k == "com.recomdata.search.paginate.maxsteps"){addConfig(searchParams,k,v,'')}
            else if(k == "com.recomdata.solr.solrFieldList"){addConfig(searchParams,k,v,'')}
            else if(k == "com.recomdata.solr.fieldExclusionList"){addConfig(searchParams,k,v,'')}
            else if(k == "com.recomdata.solr.resultFields"){addConfig(searchParams,k,v,'')}
            else if(k == "com.recomdata.solr.maxLinksDisplayed"){addConfig(searchParams,k,v,'')}
            else if(k == "com.recomdata.solr.numberOfSuggestions"){addConfig(searchParams,k,v,'')}
            else if(k == "com.recomdata.search.gene.max"){addConfig(searchParams,k,v,'')}
            else if(k == "com.recomdata.search.autocomplete.max"){addConfig(searchParams,k,v,'')}
            else if(k == "com.recomdata.searchtool.appTitle"){addConfig(searchParams,k,v,'application "About" text from Utilities tab (obsolete)')}
            else if(k == "com.recomdata.searchtool.datasetExplorerURL"){addConfig(searchParams,k,v,'Relative context path from Search to Analyze tab')}
            else if(k == "com.recomdata.searchtool.hideInternalTabs"){addConfig(searchParams,k,v,'Hide internal tabs including doc and jubilant tabs')}
            else if(k == "com.recomdata.searchtool.genegoURL"){addConfig(searchParams,k,v,'')}

            // smartr
            else if(k == "smartR.remoteScriptDirectory"){addConfig(smartrParams,k,v,'')}
            else if(k == "grails.plugin.transmartGwasPlink.enabled"){addConfig(smartrParams,k,v,'')}

            // solr

            // spring
            else if(k == "grails.plugin.springsecurity.active"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.adh.ajaxErrorPage"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.adh.errorPage"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.adh.useForward"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.afterInvocationManagerProviderNames"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.ajaxHeader"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.anon.key"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.apf.allowSessionCreation"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.apf.continueChainBeforeSuccessfulAuthentication"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.apf.filterProcessesUrl"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.apf.passwordParameter"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.apf.postOnly"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.apf.storeLastUsername"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.apf.usernameParameter"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.atr.anonymousClass"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.atr.rememberMeClass"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.auth.ajaxLoginFormUrl"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.auth.forceHttps"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.auth.loginFormUrl"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.auth.useForward"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.authority.className"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.authority.nameField"){addConfig(springParams,k,v,'')}
            
            else if(k == "grails.plugin.springsecurity.basic.credentialsCharset"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.basic.realmName"){addConfig(springParams,k,v,'')}

            else if(k == "grails.plugin.springsecurity.cacheUsers"){addConfig(springParams,k,v,'')}

            else if(k == "grails.plugin.springsecurity.dao.hideUserNotFoundExceptions"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.debug.useFilter"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.digest.createAuthenticatedToken"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.digest.key"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.digest.nonceValiditySeconds"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.digest.passwordAlreadyEncoded"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.digest.realmName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.digest.useCleartextPasswords"){addConfig(springParams,k,v,'')}
            
            else if(k == "grails.plugin.springsecurity.failureHandler.ajaxAuthFailUrl"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.failureHandler.allowSessionCreation"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.failureHandler.defaultFailureUrl"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.failureHandler.useForward"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.fii.alwaysReauthenticate"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.fii.observeOncePerRequest"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.fii.publishAuthorizationSuccess"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.fii.rejectPublicInvocations"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.fii.validateConfigAttributes"){addConfig(springParams,k,v,'')}

            else if(k.startsWith("grails.plugin.springsecurity.filterChain.chainMap.")){
                def kk = k - "grails.plugin.springsecurity.filterChain.chainMap."
                addConfig(springParams,k,v,"Filter chainMap for ${kk}")
            }

            else if(k.startsWith("grails.plugin.springsecurity.interceptUrlMap.")){
                def kk = k - "grails.plugin.springsecurity.interceptUrlMap."
                addConfig(springParams,k,v,"Intercept UrlMap for ${kk}")
            }
            
            else if(k == "grails.plugin.springsecurity.logout.clearAuthentication"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.logout.afterLogoutUrl"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.logout.alwaysUseDefaultTargetUrl"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.logout.filterProcessesUrl"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.logout.handlerNames"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.logout.invalidateHttpSession"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.logout.postOnly"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.logout.redirectToReferer"){addConfig(springParams,k,v,'')}

            else if(k == "grails.plugin.springsecurity.password.algorithm"){addConfig(springParams,k,v,'Password encryption algorithm')}
            else if(k == "grails.plugin.springsecurity.password.bcrypt.logrounds"){addConfig(springParams,k,v,'Password bcrypt number of rounds')}
            else if(k == "grails.plugin.springsecurity.password.encodeHashAsBase64"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.password.hash.iterations"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.portMapper.httpPort"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.portMapper.httpsPort"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.printStatusMessages"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.providerManager.eraseCredentialsAfterAuthentication"){addConfig(springParams,k,v,'')}

            else if(k == "grails.plugin.springsecurity.redirectStrategy.contextRelative"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.registerLoggerListener"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.rejectIfNoRule"){addConfig(springParams,k,v,'If true, reject URLs with no intercept URL map')}
            else if(k == "grails.plugin.springsecurity.rememberMe.alwaysRemember"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.rememberMe.cookieName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.rememberMe.createSessionOnSuccess"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.rememberMe.key"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.rememberMe.parameter"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.rememberMe.persistent"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.rememberMe.persistentToken.seriesLength"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.rememberMe.persistentToken.tokenLength"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.rememberMe.tokenValiditySeconds"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.requestCache.createSession"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.requestMap.className"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.requestMap.configAttributeField"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.requestMap.httpMethodField"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.requestMap.urlField"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.roleHierarchy"){addConfig(springParams,k,v,'')}

            else if(k == "grails.plugin.springsecurity.sch.strategyName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.scpf.forceEagerSessionCreation"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.scr.allowSessionCreation"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.scr.disableUrlRewriting"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.scr.springSecurityContextKey"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.secureChannel.insecureHeaderName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.secureChannel.insecureHeaderValue"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.secureChannel.secureHeaderName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.secureChannel.secureHeaderValue"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.secureChannel.useHeaderCheckChannelSecurity"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.securityConfigType"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.sessionFixationPrevention.alwaysCreateSession"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.sessionFixationPrevention.migrate"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.successHandler.ajaxSuccessUrl"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.successHandler.alwaysUseDefault"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.successHandler.defaultTargetUrl"){addConfig(springParams,k,v,'URL to redirect after logging in, default config uses value of org.transmart.defaultLoginredirect')}
            else if(k == "grails.plugin.springsecurity.successHandler.targetUrlParameter"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.successHandler.useReferer"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.switchUser.exitUserUrl"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.switchUser.switchUserUrl"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.switchUser.usernameParameter"){addConfig(springParams,k,v,'')}

            else if(k == "grails.plugin.springsecurity.useBasicAuth"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.useDigestAuth"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.useExternalClasses"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.useHttpSessionEventPublisher"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.userLookup.accountExpiredPropertyName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.userLookup.accountLockedPropertyName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.userLookup.authoritiesPropertyName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.userLookup.enabledPropertyName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.userLookup.passwordExpiredPropertyName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.userLookup.userDomainClassName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.userLookup.authorityJoinClassName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.userLookup.passwordPropertyName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.userLookup.usernamePropertyName"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.useRoleGroups"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.useSecurityEventListener"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.useSessionFixationPrevention"){addConfig(springParams,k,v,'')}
            else if(k == "grails.plugin.springsecurity.useSwitchUserFilter"){addConfig(springParams,k,v,'')}

            else if(k == "grails.plugin.springsecurity.voterNames"){addConfig(springParams,k,v,'')}

            // ui
            else if(k == "com.recomdata.hideSampleExplorer"){addConfig(uiParams,k,v,'Disable sample explorer (obsolete?)')}
            else if(k == "com.recomdata.sessionTimeout"){addConfig(uiParams,k,v,'')}
            else if(k == "com.recomdata.debug.jsCallbacks"){addConfig(uiParams,k,v,'')}
            else if(k == "com.recomdata.plugins.resultSize"){addConfig(uiParams,k,v,'')}
            else if(k == "com.recomdata.heartbeatLaps"){addConfig(uiParams,k,v,'')}
            else if(k == "org.transmart.xnatViewerEnable"){addConfig(uiParams,k,v,'')}
            else if(k == "ui.jirareport.hide"){addConfig(uiParams,k,v,'')}
            else if(k == "ui.tabs.browse.hide"){addConfig(uiParams,k,v,'')}
            else if(k == "ui.tabs.datasetExplorer.dataExport.hide"){addConfig(uiParams,k,v,'')}
            else if(k == "ui.tabs.datasetExplorer.dataExportJobs.hide"){addConfig(uiParams,k,v,'')}
            else if(k == "ui.tabs.datasetExplorer.gridView.hide"){addConfig(uiParams,k,v,'')}
            else if(k == "ui.tabs.datasetExplorer.workspace.hide"){addConfig(uiParams,k,v,'')}
            else if(k == "ui.tabs.geneSignature.hide"){addConfig(uiParams,k,v,'')}
            else if(k == "ui.tabs.gwas.hide"){addConfig(uiParams,k,v,'')}
            else if(k == "ui.tabs.sampleExplorer.hide"){addConfig(uiParams,k,v,'')}
            else if(k == "ui.tabs.search.show"){addConfig(uiParams,k,v,'')}
            else if(k == "ui.tabs.uploadData.hide"){addConfig(uiParams,k,v,'')}

            // upload
            else if(k == "com.recomdata.dataUpload.appTitle"){addConfig(uploadParams,k,v,'')}
            else if(k == "com.recomdata.dataUpload.etl.dir"){addConfig(uploadParams,k,v,'')}
            else if(k == "com.recomdata.dataUpload.adminEmail"){addConfig(contactParams,k,v,'')}
            else if(k == "com.recomdata.dataUpload.stageScript"){addConfig(uploadParams,k,v,'')}
            else if(k == "com.recomdata.dataUpload.templates.dir"){addConfig(uploadParams,k,v,'')}
            else if(k == "com.recomdata.dataUpload.uploads.dir"){addConfig(uploadParams,k,v,'')}

            // x509
            else if(k == "grails.plugin.springsecurity.useX509"){addConfig(x509Params,k,v,'')}
            else if(k == "grails.plugin.springsecurity.x509.checkForPrincipalChanges"){addConfig(x509Params,k,v,'')}
            else if(k == "grails.plugin.springsecurity.x509.continueFilterChainOnUnsuccessfulAuthentication"){addConfig(x509Params,k,v,'')}
            else if(k == "grails.plugin.springsecurity.x509.invalidateSessionOnPrincipalChange"){addConfig(x509Params,k,v,'')}
            else if(k == "grails.plugin.springsecurity.x509.subjectDnRegex"){addConfig(x509Params,k,v,'')}
            else if(k == "grails.plugin.springsecurity.x509.throwExceptionWhenTokenRejected"){addConfig(x509Params,k,v,'')}

            // xnatImport
            else if(k == "org.transmart.data.location"){addConfig(xnatImportParams,k,v,'')}
            else if(k == "org.transmart.xnatImporterEnable"){addConfig(xnatImportParams,k,v,'')}

            // xnatView
            else if(k == "org.xnat.domain"){addConfig(xnatViewParams,k,v,'')}
            else if(k == "org.xnat.password"){addConfig(xnatViewParams,k,encodeConfigPassword(v),'')}
            else if(k == "org.xnat.projectName"){addConfig(xnatViewParams,k,v,'')}
            else if(k == "org.xnat.username"){addConfig(xnatViewParams,k,encodeConfigUsername(v),'')}



            // all remaining params not known go here

            else {addConfig(Params,k,v,"Unknown")}

         }

        def settings = [
                        'adminParams'      : adminParams,
                        'analysisParams'   : analysisParams,
                        'analyzeParams'    : analyzeParams,
                        'authParams'       : authParams,
                        'browseParams'     : browseParams,
                        'buildInfoParams'  : buildInfoParams,
                        'contactParams'    : contactParams,
                        'dataSourceParams' : dataSourceParams,
                        'exportParams'     : exportParams,
                        'galaxyParams'     : galaxyParams,
                        'generalParams'    : generalParams,
                        'gwasParams'       : gwasParams,
                        'gwavaParams'      : gwavaParams,
                        'helpParams'       : helpParams,
                        'i2b2Params'       : i2b2Params,
                        'kerberosParams'   : kerberosParams,
                        'ldapParams'       : ldapParams,
                        'logParams'        : logParams,
                        'loginParams'      : loginParams,
                        'metacoreParams'   : metacoreParams,
                        'oauthParams'      : oauthParams,
                        'rmodulesParams'   : rmodulesParams,
                        'samlParams'       : samlParams,
                        'sampleParams'     : sampleParams,
                        'searchParams'     : searchParams,
                        'smartrParams'     : smartrParams,
                        'solrParams'       : solrParams,
                        'springParams'     : springParams,
                        'uiParams'         : uiParams,
                        'uploadParams'     : uploadParams,
                        'x509Params'       : x509Params,
                        'xnatImportParams' : xnatImportParams,
                        'xnatViewParams'   : xnatViewParams,

                        'unknownParams'    : Params
            ]
        ConfigParams configParams = new ConfigParams(settings)
        return configParams
    }

    def getAuthProviders() {
        def result = [:]
        def providers = ['ldapAuthenticationProvider':'LDAP',
                         'samlAuthenticationProvider':'SAML',
                         'rememberMeAuthenticationProvider':'Use a rememberMe cookie',
                         'daoAuthenticationProvider':'User and Role database tables',
                         'preAuthenticatedAuthenticationProvider':'Pre-authenticated',
                         'kerberosServiceAuthenticationProvider':'Kerberos service',
                         'anonymousAuthenticationProvider':'Anonymous authentication if no other method succeeds',
                         'clientCredentialsAuthenticationProvider': 'Client credentials',
                         'customAuthenticationProvider': 'Custom authentication']

        grailsApplication.config.grails.plugin.springsecurity.providerNames.each{ it ->
            result.put(it, providers[it]?:"${it - 'AuthenticationProvider'}")
        }

        result
    }

    def getOauthClients() {
        def result = []

        grailsApplication.config.grails.plugin.springsecurity.oauthProvider.clients.each{ it ->
            if(it.clientId == "api-client") {
                it.desc = "REST API client"
            } else if (it.clientId == "glowingbear-js") {
                it.desc = "Glowing Bear UI"
            } else {
                it.desc = "Unknown client '${it.client_id}'"
            }
            result.add(it)
        }

        result
    }

    def getSampleMapping() {
        def result = []

        grailsApplication.config.sampleExplorer.fieldMapping.columns.each{ it ->
            if(it.header == "ID") {
                it.desc = "Identifier"
            } else if (it.header == "trial name") {
                it.desc = "Trial name"
            } else if (it.header == "barcode") {
                it.desc = "Bar code"
            } else if (it.header == "plate id") {
                it.desc = "Plate ID"
            } else if (it.header == "patient id") {
                it.desc = "Patient ID"
            } else if (it.header == "external id") {
                it.desc = "External ID"
            } else if (it.header == "aliquot id") {
                it.desc = "Aliquot ID"
            } else if (it.header == "visit") {
                it.desc = "Visit"
            } else if (it.header == "sample type") {
                it.desc = "Sample type"
            } else if (it.header == "description") {
                it.desc = "Description"
            } else if (it.header == "comment") {
                it.desc = "Comment"
            } else if (it.header == "location") {
                it.desc = "Location"
            } else if (it.header == "tissue type") {
                it.desc = "Tissue type"
            } else if (it.header == "data types") {
                it.desc = "Data types"
            } else if (it.header == "disease") {
                it.desc = "Disease"
            } else if (it.header == "tissue state") {
                it.desc = "Tissue state"
            } else if (it.header == "biobank") {
                it.desc = "Biobank"
            } else if (it.header == "organism") {
                it.desc = "Organism"
            } else if (it.header == "treatment") {
                it.desc = "Treatment"
            } else if (it.header == "sample treatment") {
                it.desc = "Sample treatment"
            } else if (it.header == "subject treatment") {
                it.desc = "Subject treatment"
            } else if (it.header == "timepoint") {
                it.desc = "Time point"
            } else {
                it.desc = "Unknown column '${it.header}'"
            }
            result.add(it)
        }

        result
    }

    def private addConfig(Map paramData, String paramName, String paramValue, String paramDescription) {
        if(paramData.containsKey(paramName)){
            paramData[(paramName)].put('value',paramValue.encodeAsHTML())
            if(paramDescription != "") {
                if(paramData[(paramName)].desc == "") {
                    paramData[(paramName)].desc = paramDescription
                } else {
                    paramData[(paramName)].desc = paramData[(paramName)].desc + '<br>' + paramDescription
                }
            } else {
                if(paramData[(paramName)].desc == "") {
                    paramData[(paramName)].desc = '(Defined but undocumented)'
                }
            }
        }
        else {
            paramData.put(paramName, [value:paramValue.encodeAsHTML(), desc:paramDescription])
        }
    }

    def private addConfigLink(Map paramData, String paramName, String paramValue, String paramDescription) {
        if(paramData.containsKey(paramName)){
            paramData[(paramName)].put('value',paramValue)
            if(paramDescription != "") {
                if(paramData[(paramName)].desc == "") {
                    paramData[(paramName)].desc = paramDescription
                } else {
                    paramData[(paramName)].desc = paramData[(paramName)].desc + '<br>' + paramDescription
                }
            } else {
                if(paramData[(paramName)].desc == "") {
                    paramData[(paramName)].desc = '(Defined but undocumented)'
                }
            }
        }
        else {
            paramData.put(paramName, [value:paramValue, desc:paramDescription])
        }
    }

    def private encodeConfigPassword(String password) {
        if(!grailsApplication.config.org.transmart.config.showPasswords) {
            '(hidden)'
        } else {
            password
        }
    }
    
    def private encodeConfigUsername(String username) {
        if(!grailsApplication.config.org.transmart.config.showUsernames) {
            '(hidden)'
        } else {
            username
        }
    }
    
    def private linkAuthProviders(List providers) {
       "Defined<a href=\"authProviders\">${providers.size()} providers</a>"
    }
    
    def private linkOauthClients(List clients) {
       "Defined<a href=\"oauthClients\">${clients.size()} clients</a>"
    }
    
    def private linkSampleMapping(List mappings) {
       "Defined<a href=\"sampleMapping\">${mappings.size()} mappings</a>"
    }
    
}

