def forkSettingsRun = [
        minMemory: 1536,
        maxMemory: 4096,
        maxPerm:   384,
        debug:     false,
]
def forkSettingsOther = [
        minMemory: 256,
        maxMemory: 1024,
        maxPerm:   384,
        debug:     false,
]

grails.project.fork = [
        test:    forkSettingsOther,
        run:     forkSettingsRun,
        war:     false,
        console: forkSettingsOther]

grails.project.war.file = "target/${appName}.war"

/* we need at least servlet-api 2.4 because of HttpServletResponse::setCharacterEncoding */
grails.servlet.version = "2.5"

grails.project.dependency.resolver = 'maven'

def dm, dmClass
try {
    dmClass = new GroovyClassLoader().parseClass(
            new File('../transmart-dev/DependencyManagement.groovy'))
} catch (Exception e) {}
if (dmClass) {
    dm = dmClass.newInstance()
}

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {}
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    if (!dm) {
        repositories {
            grailsCentral()
            mavenCentral()

            mavenRepo "https://repo.transmartfoundation.org/content/repositories/public/"
            mavenRepo "https://repo.thehyve.nl/content/repositories/public/"
        }
    } else {
        dm.configureRepositories delegate
    }

    dependencies {
        // you can remove whichever you're not using
        runtime 'org.postgresql:postgresql:9.3-1100-jdbc4'
        runtime 'com.oracle:ojdbc7:12.1.0.1'

        runtime 'org.javassist:javassist:3.16.1-GA'

        compile 'org.transmartproject:transmart-core-api:1.2.1'

        compile 'antlr:antlr:2.7.7'
        compile 'net.sf.opencsv:opencsv:2.3'
        compile "org.apache.lucene:lucene-core:2.4.0"
        compile "org.apache.lucene:lucene-demos:2.4.0"
        compile "org.apache.lucene:lucene-highlighter:2.4.0"
        compile 'commons-net:commons-net:3.3' // used for ftp transfers
        compile 'org.apache.commons:commons-math:2.2' //>2MB lib briefly used in ChartController
        compile 'org.codehaus.groovy.modules.http-builder:http-builder:0.5.1', {
            excludes 'groovy', 'nekohtml'
        }
        compile 'org.rosuda:Rserve:1.7.3'
        compile 'com.google.guava:guava:14.0.1'

        /* we need at least servlet-api 2.4 because of HttpServletResponse::setCharacterEncoding */
        compile "javax.servlet:servlet-api:$grails.servlet.version" /* delete from the WAR afterwards */

        /* for GeneGo web services: */
        compile 'axis:axis:1.4'


        test('junit:junit:4.11') {
            transitive = false /* don't bring hamcrest */
            export     = false
        }

        test 'org.hamcrest:hamcrest-core:1.3',
                'org.hamcrest:hamcrest-library:1.3'

        test 'org.gmock:gmock:0.9.0-r435-hyve2', {
            transitive = false
        }
        test 'org.hamcrest:hamcrest-library:1.3',
                'org.hamcrest:hamcrest-core:1.3'
    }

    plugins {
        build ':release:3.0.1'
        build ':rest-client-builder:2.0.1'
        build ':tomcat:7.0.52.1'

        compile ':build-info:1.2.5'
        compile ':hibernate:3.6.10.10'
        compile ':quartz:1.0-RC2'
        // Not compatible with spring security 3.2 yet
        //compile ':spring-security-kerberos:0.1'
        compile ':spring-security-ldap:2.0-RC2'
        compile ':spring-security-core:2.0-RC2'
        compile ':spring-security-oauth2-provider:1.0.5.2'

        runtime ':prototype:1.0'
        runtime ':jquery:1.7.1'

        runtime ':resources:1.2.1'

        if (!dm) {
            compile ':rdc-rmodules:1.2.1'
            runtime ':transmart-core:1.2.1'
            compile ':transmart-gwas:1.2.1'
            // transmart-gwas includes directly, or indirectly
            //compile ':transmart-legacy-db:1.2.1'
            //compile ':folder-management:1.2.1'
            //compile ':search-domain:1.2.1'
            //compile ':biomart-domain:1.2.1'
            //compile ':transmart-java:1.2.1'
            runtime ':transmart-rest-api:1.2.1'

            runtime ':dalliance-plugin:0.2'
            runtime ':transmart-mydas:0.1-SNAPSHOT'
            runtime ':blend4j-plugin:1.2.1'
            runtime ':transmart-metacore-plugin:1.2.1'

            test ':transmart-core-db-tests:1.2.1'
        } else {
            dm.internalDependencies delegate
        }

        // Doesn't work with forked tests yet
        //test ":code-coverage:1.2.6"
    }
}

dm?.with {
    configureInternalPlugin 'compile', 'rdc-rmodules'
    configureInternalPlugin 'runtime', 'transmart-core'
    configureInternalPlugin 'test',    'transmart-core-db-tests'
    configureInternalPlugin 'compile', 'transmart-gwas'
    configureInternalPlugin 'compile', 'transmart-java'
    configureInternalPlugin 'compile', 'biomart-domain'
    configureInternalPlugin 'compile', 'search-domain'
    configureInternalPlugin 'compile', 'folder-management'
    configureInternalPlugin 'compile', 'transmart-legacy-db'
    configureInternalPlugin 'runtime', 'dalliance-plugin'
    configureInternalPlugin 'runtime', 'transmart-mydas'
    configureInternalPlugin 'runtime', 'transmart-rest-api'
}

dm?.inlineInternalDependencies grails, grailsSettings

grails.war.resources = { stagingDir ->
    delete(file: "${stagingDir}/WEB-INF/lib/servlet-api-${grails.servlet.version}.jar")
}

// Use new NIO connector in order to support sendfile
// This is a lovely thought, but with Tomcat running Grails 2.3.6+ NIO does not function in run-war mode
// Official bug number : GRAILS-11376
if (!grails.util.Environment.isWarDeployed()) {
    grails.tomcat.nio = true
}

// vim: set et ts=4 sw=4:
