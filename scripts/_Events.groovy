import grails.build.logging.GrailsConsole

eventConfigureTomcat = { tomcat ->
    def ctx = tomcat.host.findChild(serverContextPath)
    def goAhead = true
    def console = GrailsConsole.getInstance()
    def err = console.&error

    def checkSetting = { name ->
        if (config.RModules?.get(name) == null) {
            err "Setting RModules.$name is not set!"
            goAhead = false
        }
    }

    checkSetting 'transferImageFile'
    checkSetting 'imageURL'
    checkSetting 'tempFolderDirectory'

    if (goAhead && !config.RModules.transferImageFile) {
        String imageUrl = "/images" + config.RModules.imageURL

        /* ending / has to be stripped */
        if (imageUrl.endsWith("/"))
            imageUrl = imageUrl.substring(0, imageUrl.length() - 1)

        /*
         * Prevent the resource plugin from handling the resources in this
         * so that it can be served directly.
         * Note that this will allow .gsp files under the "tempFolderDirectory"
         * to be executed! I'm not sure how this should be handled; maybe adding
         * a filter to deny access to such urls.
         * In any case, this is a relatively minor problem since the jobs
         * directory should not have gsp files (relatively for instance to
         * straightforward arbitrary code execution flaws that currently
         * exist...).
         */
        def curExcludes = config.grails.resources.adhoc.excludes
        config.grails.resources.adhoc.excludes = [imageUrl + "/**"]
                + (curExcludes ?: [])


        def alias = imageUrl + "=" + config.RModules.tempFolderDirectory
        if (ctx.aliases) {
            ctx.aliases += ',' + alias
        } else {
            ctx.aliases = alias
        }

        console.info("[INFO] Set context aliases to ${ctx.aliases}")
    }
}

eventWebXmlStart = { webXmlFile ->
    ant.echo message: "Change display-name for web.xml"
    def tmpWebXmlFile = new File(projectWorkDir, webXmlFile)
    ant.replace(file: tmpWebXmlFile, token: "@grails.app.name.version@",
            value: "${grailsAppName}-${grailsAppVersion}")
}
