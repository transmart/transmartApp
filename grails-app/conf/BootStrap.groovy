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
 * 
 *
 ******************************************************************/

import org.codehaus.groovy.grails.exceptions.GrailsConfigurationException
import org.codehaus.groovy.grails.plugins.GrailsPluginUtils
import org.codehaus.groovy.grails.plugins.springsecurity.SecurityFilterPosition
import org.codehaus.groovy.grails.plugins.springsecurity.SpringSecurityUtils
import org.slf4j.LoggerFactory
import org.transmart.marshallers.MarshallerRegistrarService

class BootStrap {

    final static logger = LoggerFactory.getLogger(this)

	def securityContextPersistenceFilter

    def grailsApplication

	def init = { servletContext ->
		securityContextPersistenceFilter.forceEagerSessionCreation = true
		
		SpringSecurityUtils.clientRegisterFilter('concurrentSessionFilter', SecurityFilterPosition.CONCURRENT_SESSION_FILTER)

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

        def tsAppRScriptsDir = servletContext.getRealPath('dataExportRScripts')
        if (tsAppRScriptsDir) {
            tsAppRScriptsDir = new File(tsAppRScriptsDir)
        }
        if (!tsAppRScriptsDir || !tsAppRScriptsDir.isDirectory()) {
            tsAppRScriptsDir = servletContext.getRealPath('.') +
                    '/../dataExportRScripts'
            if (tsAppRScriptsDir) {
                tsAppRScriptsDir = new File(tsAppRScriptsDir)
            }
        }
        if (!tsAppRScriptsDir || !tsAppRScriptsDir.isDirectory()) {
            tsAppRScriptsDir = new File('web-app', 'dataExportRScripts')
        }
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
