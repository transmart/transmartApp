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

includeTargets << new File("${grailsHome}/scripts/Init.groovy")

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