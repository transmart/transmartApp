grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

//grails.plugin.location.rmodules="..//Rmodules"

grails.project.dependency.resolver = 'maven'

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") { }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'

    repositories {
        grailsCentral()
        mavenCentral()

        mavenRepo name: 'repo.thehyve.nl-public',
                  url:  'https://repo.thehyve.nl/content/repositories/public/'
    }

    dependencies {
		compile 'antlr:antlr:2.7.7'
        compile 'net.sf.opencsv:opencsv:2.3'
        compile 'org.apache.lucene:lucene-core:2.4.0',
                'org.apache.lucene:lucene-demos:2.4.0',
                'org.apache.lucene:lucene-highlighter:2.4.0'
        compile 'org.transmartproject:transmart-core-api:1.0-SNAPSHOT'

        test    'org.hamcrest:hamcrest-library:1.3',
                'org.hamcrest:hamcrest-core:1.3'
    }

    plugins {
        build ':tomcat:7.0.41'
        build ':build-info:1.2.4'
        build ':release:3.0.0', ':rest-client-builder:1.0.3'

        compile ':hibernate:3.6.10.1'
        compile ':quartz:1.0-RC2'
        compile ':spring-security-core:1.2.7.3'
        compile ':spring-security-kerberos:0.1'
        compile ':spring-security-ldap:1.0.6'
        compile ':rdc-rmodules:0.3-SNAPSHOT'

        runtime ':prototype:1.0'
        runtime ":transmart-core:1.0-SNAPSHOT"

        //This version of code coverage plugin does not work with grails 2.3
        //Despite specified test scope declaration of this plugin appears in grails.xml of prod war
        //cause ClassNotFoundationException: CodeCoverageGrailsPlugin
        //test ":code-coverage:1.2.6"
    }
}

def buildConfigFile = new File("${userHome}/.grails/${appName}Config/" +
        "BuildConfig.groovy")
if (buildConfigFile.exists()) {
    println "Processing external build config at $buildConfigFile"

    def slurpedBuildConfig = new ConfigSlurper(Environment.current.name).
            parse(buildConfigFile.toURL())

    /* For development, it's interesting to use the plugins in-place.
      * This allows the developer to put the grails.plugin.location.* assignments
      * in an out-of-tree BuildConfig file if they want to.
      * Snippet from https://gist.github.com/acreeger/910438
      */
    slurpedBuildConfig.grails.plugin.location.each { String k, v ->
        if (!new File(v).exists()) {
            println "WARNING: Cannot load in-place plugin from ${v} as that " +
                    "directory does not exist."
        } else {
            println "Loading in-place plugin $k from $v"
            grails.plugin.location."$k" = v
        }
        if (grailsSettings.projectPluginsDir?.exists()) {
            grailsSettings.projectPluginsDir.eachDir { dir ->
                // remove optional version from inline definition
                def dirPrefix = k.replaceFirst(/:.+/, '') + '-'
                if (dir.name.startsWith(dirPrefix)) {
                    println "WARNING: Found a plugin directory at $dir that is a " +
                            "possible conflict and may prevent grails from using " +
                            "the in-place $k plugin."
                }
            }
        }
    }

    /* dependency resolution in external BuildConfig */
    Closure originalDepRes = grails.project.dependency.resolution;
    if (slurpedBuildConfig.grails.project.dependency.resolution) {
        Closure extraDepRes = slurpedBuildConfig.grails.project.dependency.resolution;
        grails.project.dependency.resolution = {
            originalDepRes.delegate        = extraDepRes.delegate        = delegate
            originalDepRes.resolveStrategy = extraDepRes.resolveStrategy = resolveStrategy
            originalDepRes.metaClass.skipTransmartFoundationRepo = { true }
            originalDepRes.call(it)
            extraDepRes.call(it)
        }
    }
}

// vim: set et sw=4 ts=4:
