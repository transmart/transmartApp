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
  

import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU

/**
 * This script installs the javascript library extjs available from
 * http://www.extjs.com. Requires the zip file be present in the
 * plugins directory. After running grails install-extjs, the library
 * will be copied to the webapp/scripts/ext folder.
 */
grailsHome = Ant.project.properties."environment.GRAILS_HOME"
basedir = System.getProperty("base.dir")
extjsFile = "${basedir}/plugins/ext-2.2.zip"
jsDir = "${basedir}/web-app/js"
extDir = "${jsDir}/ext"
props = null

includeTargets << new File ( "${grailsHome}/scripts/Init.groovy" )

target('default': "The description of the script goes here!") {
    depends(setup)
    clean()
    install()
}

target(setup: "Sets up access to the Grails Config properites script") {
    Ant.property(file: "${basedir}/grails-app/conf/Config.groovy")
    props = Ant.antProject.properties
}

target(clean: "Removes any previous versions") {
    Ant.delete(dir: "${extDir}")
}

target(install: "Installs the latest version of extjs into the scripts folder") {
    Ant.unzip(src: "${extjsFile}", dest: "$jsDir")
    Ant.move(file: "${jsDir}/ext-2.2", tofile: "${extDir}")
    Ant.delete(dir: "${extDir}/docs")
    Ant.delete(dir: "${extDir}/examples")
    Ant.delete(dir: "${extDir}/source")
}