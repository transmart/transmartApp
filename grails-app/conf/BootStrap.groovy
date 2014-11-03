import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.slf4j.LoggerFactory

class BootStrap {

    final static logger = LoggerFactory.getLogger(this)

    def securityContextPersistenceFilter

    def grailsApplication

    def init = { servletContext ->
        securityContextPersistenceFilter.forceEagerSessionCreation = true

        SpringSecurityUtils.clientRegisterFilter('concurrentSessionFilter', SecurityFilterPosition.CONCURRENT_SESSION_FILTER)

        if (grailsApplication.config.org.transmart.security.samlEnabled) {
            SpringSecurityUtils.clientRegisterFilter(
                    'metadataGeneratorFilter', SecurityFilterPosition.FIRST)
            SpringSecurityUtils.clientRegisterFilter(
                    'samlFilter', SecurityFilterPosition.BASIC_AUTH_FILTER)
        }

        if (!grailsApplication.config.org.transmart.configFine.is(true)) {
            logger.error("Something wrong happened parsing the externalized " +
                    "Config.groovy, because we could not find the " +
                    "configuration setting 'org.transmart.configFine " +
                    "set to true.\n" +
                    "Tip: on ~/.grails/transmartConfig, run\n" +
                    "groovy -e 'new ConfigSlurper().parse(new File(\"Config.groovy\").toURL())'\n" +
                    "to detect compile errors. Other errors can be detected " +
                    "with a breakpoing on the catch block in ConfigurationHelper::mergeInLocations().\n" +
                    "Alternatively, you can change the console logging settings by editing " +
                    "\$GRAILS_HOME/scripts/log4j.properties, adding a proper appender and log " +
                    "org.codehaus.groovy.grails.commons.cfg.ConfigurationHelper at level WARN")
            throw new GrailsConfigurationException("Configuration magic setting not found")
        }

        fixupConfig()

        // force marshaller registrar initialization
        grailsApplication.mainContext.getBean 'marshallerRegistrarService'
    }

    private void fixupConfig() {
        def c = grailsApplication.config
        def val

        /* rScriptDirectory */
        val = c.com.recomdata.transmart.data.export.rScriptDirectory
        if (val) {
            logger.warn("com.recomdata.transmart.data.export.rScriptDirectory " +
                    "should not be explicitly set, value '$val' ignored")
        }

        def servletContext = grailsApplication.mainContext.servletContext
        logger.error("Zero :: " + servletContext.contextPath)

        def tsAppRScriptsDir = servletContext.getRealPath('dataExportRScripts')
        logger.error("First :: " + tsAppRScriptsDir)
        if (tsAppRScriptsDir) {
            tsAppRScriptsDir = new File(tsAppRScriptsDir)
            logger.error("Second :: " + tsAppRScriptsDir?.absolutePath)
        }
        if (!tsAppRScriptsDir || !tsAppRScriptsDir.isDirectory()) {
            tsAppRScriptsDir = servletContext.getRealPath('.') +
                    '/../dataExportRScripts'
            logger.error("Third :: " + tsAppRScriptsDir)
            if (tsAppRScriptsDir) {
                tsAppRScriptsDir = new File(tsAppRScriptsDir)
                logger.error("Fourth :: " + tsAppRScriptsDir?.absolutePath)
            }
        }
//        logger.error("Fifth :: ${appName}" )
        logger.error("Fifth :: " + servletContext.contextPath)
        if (!tsAppRScriptsDir || !tsAppRScriptsDir.isDirectory()) {
            tsAppRScriptsDir = new File('webapps' + servletContext.contextPath, 'dataExportRScripts')
            logger.error("Fifth :: " + tsAppRScriptsDir?.absolutePath)
        }
        if (!tsAppRScriptsDir || !tsAppRScriptsDir.isDirectory()) {
            tsAppRScriptsDir = new File('web-app', 'dataExportRScripts')
            logger.error("Fifth :: " + tsAppRScriptsDir?.absolutePath)
        }
        logger.error("Sixth :: " + tsAppRScriptsDir?.absolutePath)
        if (!tsAppRScriptsDir.isDirectory()) {
            throw new RuntimeException('Could not determine proper for ' +
                    'com.recomdata.transmart.data.export.rScriptDirectory')
        }
        c.com.recomdata.transmart.data.export.rScriptDirectory = tsAppRScriptsDir.canonicalPath

        logger.info("com.recomdata.transmart.data.export.rScriptDirectory = " +
                "${c.com.recomdata.transmart.data.export.rScriptDirectory}")

        /* RModules.pluginScriptDirectory */
        val = c.RModules.pluginScriptDirectory
        if (val) {
            logger.warn("RModules.pluginScriptDirectory " +
                    "should not be explicitly set, value '$val' ignored")
        }
        File rdcModulesDir = GrailsPluginUtils.getPluginDirForName('rdc-rmodules')?.file
        if (rdcModulesDir == null) {
            // it actually varies...
            rdcModulesDir = GrailsPluginUtils.getPluginDirForName('rdcRmodules')?.file
        }
        if (!rdcModulesDir) {
            String version = grailsApplication.mainContext.pluginManager.allPlugins.find {
                it.name == 'rdc-rmodules' || it.name == 'rdcRmodules'
            }.version
            def pluginsDir = servletContext.getRealPath('plugins')
            if (pluginsDir) {
                rdcModulesDir = new File(pluginsDir, "rdc-rmodules-$version")
            }
            if (!rdcModulesDir || !rdcModulesDir.isDirectory()) {
                rdcModulesDir = new File('webapps' + servletContext.contextPath + '/plugins', "rdc-rmodules-$version")
            }
        }
        if (!rdcModulesDir) {
            throw new RuntimeException('Could not determine directory for ' +
                    'rdc-rmodules plugin')
        }

        File rScriptsDir = new File(rdcModulesDir, 'Rscripts')
        if (!rScriptsDir || !rScriptsDir.isDirectory()) {
            rScriptsDir = new File(rdcModulesDir, 'web-app/Rscripts')
        }
        if (!rScriptsDir.isDirectory()) {
            throw new RuntimeException('Could not determine proper for ' +
                    'RModules.pluginScriptDirectory')
        }
        c.RModules.pluginScriptDirectory = rScriptsDir.canonicalPath + '/'

        logger.info("RModules.pluginScriptDirectory = " +
                "${c.RModules.pluginScriptDirectory}")
    }


    def destroy = {
    }
}
