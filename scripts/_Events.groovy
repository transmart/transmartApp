includeTargets << grailsScript("_GrailsInit")

target('default': "Event manager for Grails container") {
    eventWebXmlStart = { webXmlFile ->
        ant.echo message: "Change display-name for web.xml"
        def tmpWebXmlFile = new File(projectWorkDir, webXmlFile)
        ant.replace(file: tmpWebXmlFile, token: "@grails.app.name.version@",
                value: "${grailsAppName}-${grailsAppVersion}")
    }
}