/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/

import grails.util.Environment

def console
if (!Environment.isWarDeployed() && Environment.isWithinShell()) {
    console = grails.build.logging.GrailsConsole.instance
} else {
    console = [
            info: { println "[INFO] $it" },
            warn: { println "[WARN] $it" },
    ]
}

/**
 * Running externalized configuration
 * Assuming the following configuration files
 * - in the executing user's home at ~/.grails/<app_name>Config/[Config.groovy|DataSource.groovy]
 * - config location set path by system variable '<APP_NAME>_CONFIG_LOCATION'
 * - dataSource location set path by system environment variable '<APP_NAME>_DATASOURCE_LOCATION'
 */

/* For some reason, the externalized config files are run with a different
 * binding. None of the variables appName, userHome, appVersion, grailsHome
 * are available; the binding will actually be the root config object.
 * So store the current binding in the config object so the externalized
 * config has access to the variables mentioned.
 */
org.transmart.originalConfigBinding = getBinding()

grails.config.locations = []
def defaultConfigFiles = [
        "${userHome}/.grails/${appName}Config/Config.groovy",
        "${userHome}/.grails/${appName}Config/RModulesConfig.groovy",
        "${userHome}/.grails/${appName}Config/DataSource.groovy"
]
defaultConfigFiles.each { filePath ->
    def f = new File(filePath)
    if (f.exists()) {
        if (f.name == 'RModulesConfig.groovy') {
            console.warn "RModulesConfig.groovy is deprecated, it has been merged into Config.groovy. " +
                    "Loading it anyway."
        }
        grails.config.locations << "file:${filePath}"
    } else if (f.name != 'RModulesConfig.groovy') {
        console.info "Configuration file ${filePath} does not exist."
    }
}
String bashSafeEnvAppName = appName.toString().toUpperCase(Locale.ENGLISH).replaceAll(/-/, '_')

def externalConfig = System.getenv("${bashSafeEnvAppName}_CONFIG_LOCATION")
if (externalConfig) {
    grails.config.locations << "file:" + externalConfig
}
def externalDataSource = System.getenv("${bashSafeEnvAppName}_DATASOURCE_LOCATION")
if (externalDataSource) {
    grails.config.locations << "file:" + externalDataSource
}
grails.config.locations.each { console.info "Including configuration file [${it}] in configuration building." }

/*
 *  The following lines are copied from the previous COnfig.groovy
 * 
 */


grails.mime.file.extensions = true // enables the parsing of file extensions from URLs into the request format
grails.mime.types = [html: [
        'text/html',
        'application/xhtml+xml'
],
        xml: [
                'text/xml',
                'application/xml'
        ],
        text: 'text-plain',
        js: 'text/javascript',
        rss: 'application/rss+xml',
        atom: 'application/atom+xml',
        css: 'text/css',
        csv: 'text/csv',
        all: '*/*',
        json: [
                'application/json',
                'text/json'
        ],
        form: 'application/x-www-form-urlencoded',
        multipartForm: 'multipart/form-data',
        jnlp: 'application/x-java-jnlp-file'
]
// The default codec used to encode data with ${}
grails.views.default.codec = "none" // none, html, base64
grails.views.gsp.encoding = "UTF-8"
grails.converters.encoding = "UTF-8"
grails.converters.default.pretty.print = true

// enabled native2ascii conversion of i18n properties files
grails.enable.native2ascii = true

com.recomdata.search.autocomplete.max = 20
// default paging size
com.recomdata.search.paginate.max = 10
com.recomdata.search.paginate.maxsteps = 5
com.recomdata.admin.paginate.max = 20

//**************************
//This is the login information for the different i2b2 projects.
//SUBJECT Data.
com.recomdata.i2b2.subject.domain = 'i2b2demo'
com.recomdata.i2b2.subject.projectid = 'i2b2demo'
com.recomdata.i2b2.subject.username = 'Demo'
com.recomdata.i2b2.subject.password = 'demouser'

//SAMPLE Data.
com.recomdata.i2b2.sample.domain = 'i2b2demo'
com.recomdata.i2b2.sample.projectid = 'i2b2demo'
com.recomdata.i2b2.sample.username = 'sample'
com.recomdata.i2b2.sample.password = 'manager'

//core-db settings
org.transmartproject.i2b2.user_id = 'i2b2'
org.transmartproject.i2b2.group_id = 'Demo'
//**************************

// max genes to display after disease search
com.recomdata.search.gene.max = 250;

// set schema names for I2B2HelperService
com.recomdata.i2b2helper.i2b2hive = "i2b2hive"
com.recomdata.i2b2helper.i2b2metadata = "i2b2metadata"
com.recomdata.i2b2helper.i2b2demodata = "i2b2demodata"

com.recomdata.transmart.data.export.max.export.jobs.loaded = 20

com.recomdata.transmart.data.export.dataTypesMap = [
        'CLINICAL': 'Clinical & Low Dimensional Biomarker Data',
        'MRNA': 'Gene Expression Data',
        'SNP': 'SNP Data',
        'STUDY': 'Study Metadata',
        'ADDITIONAL': 'Additional Data'
        //,'GSEA':'Gene Set Enrichment Analysis (GSEA)'
];

// Data export FTP settings is Rserve running remote in relation to transmartApp
com.recomdata.transmart.data.export.ftp.server = 'localhost'
com.recomdata.transmart.data.export.ftp.serverport = ''
com.recomdata.transmart.data.export.ftp.username = 'transmartftp'
com.recomdata.transmart.data.export.ftp.password = 'transmartftp'
com.recomdata.transmart.data.export.ftp.remote.path = ''

// Control which gene/pathway search is used in Dataset Explorer
// A value of "native" forces Dataset Explorer's native algorithm.
// Abscence of this property or any other value forces the use of the Search Algorithm
//com.recomdata.search.genepathway="native"

// The tags in the Concept to indicate Progression-free Survival and Censor flags, used by Survival Analysis
com.recomdata.analysis.survival.survivalDataList = [
        '(PFS)',
        '(OS)',
        '(TTT)',
        '(DURTFI)'
];
com.recomdata.analysis.survival.censorFlagList = [
        '(PFSCENS)',
        '(OSCENS)',
        '(TTTCENS)',
        '(DURTFICS)'
];

com.recomdata.analysis.genepattern.file.dir = "data"; // Relative to the app root "web-app" - deprecated - replaced with data.file.dir

com.recomdata.analysis.data.file.dir = "data"; // Relative to the app root "web-app"

// Disclaimer
StringBuilder disclaimer = new StringBuilder()
disclaimer.append("<p></p>")
com.recomdata.disclaimer = disclaimer.toString()

// customization views
//com.recomdata.view.studyview='_clinicaltrialdetail'
com.recomdata.skipdisclaimer = true

grails.spring.bean.packages = []

org.transmart.security.spnegoEnabled = false

// Uncomment and edit the following lines to start using Grails encoding & escaping improvements

/* remove this line 
// GSP settings
grails {
    views {
        gsp {
            encoding = 'UTF-8'
            htmlcodec = 'xml' // use xml escaping instead of HTML4 escaping
            codecs {
                expression = 'html' // escapes values inside null
                scriptlet = 'none' // escapes output from scriptlets in GSPs
                taglib = 'none' // escapes output from taglibs
                staticparts = 'none' // escapes output from static template parts
            }
        }
        // escapes all not-encoded output at final stage of outputting
        filteringCodecForContentType {
            //'text/html' = 'html'
        }
    }
}
remove this line */
