import grails.util.Metadata

eventWebXmlStart = { webXmlFile ->
    ant.echo message: "Change display-name for web.xml"
    def tmpWebXmlFile = new File(projectWorkDir, webXmlFile)
    ant.replace(file: tmpWebXmlFile, token: "@grails.app.name.version@",
            value: "${grailsAppName}-${grailsAppVersion}")
}

eventCreateWarStart = {warname, stagingDir ->
    event("BuildInfoAddPropertiesStart", [warname, stagingDir])
    writeProperties(getEnvProperties(), "${stagingDir}/WEB-INF/classes/application.properties")
    event("BuildInfoAddPropertiesEnd", [warname, stagingDir])
}

eventCompileStart = { kind ->
    // Unfortunately during "run-app", the application metadata file loaded is not the one of the staging directory
    // We do not want to modify the local metadata file do avoid SCM mess.
    // We might still want these info displayed into the console at compile time for the main application
    getEnvProperties().each { k,v ->
        println(k + ' : ' + v)
    }
}

private void writeProperties(Map properties, String propertyFile) {
    Ant.propertyfile(file: propertyFile) {
        properties.each { k,v->
            entry(key: k, value: v)
        }
    }
}

def getEnvProperties() {
    def environment = [:]
    Ant.antProject.properties.findAll({k,v-> k.startsWith('environment')}).each { k,v->
        environment[k] = v
    }

    def properties = [:]
    properties['scm.version'] = getRevision()?.trim() ?: '-'
    properties['build.date'] = new Date().format('dd/MMM/yyyy; kk:mm:ss')
    properties['build.timezone'] = Calendar.getInstance().getTimeZone().getID()
    properties['build.java'] = System.getProperty('java.version')
    properties['build.groovy'] = GroovySystem.version

    //Add a selection of useful environment variables
    properties['env.os'] = environment['environment.OS'] ?: environment['environment.OSTYPE'] ?: '-'
    properties['env.username'] = environment['environment.USERNAME'] ?: environment['environment.USER'] ?: '-'
    properties['env.computer'] = environment['environment.COMPUTERNAME'] ?: environment['environment.HOSTNAME'] ?: '-'
    properties['env.proc.type'] = environment['environment.PROCESSOR_ARCHITECTURE'] ?: environment['environment.HOSTTYPE'] ?: '-'
    properties['env.proc.cores'] = environment['environment.NUMBER_OF_PROCESSORS'] ?: '-'

    getPluginEnvProperties(properties)

    return properties
}

def getRevision() {
    // client provided closure to determine revision
    def determineRevisionClosure = buildConfig.buildinfo.determineRevision
    if (determineRevisionClosure instanceof Closure) {
        return determineRevisionClosure()
    }

    // try to get revision from Hudson
    def scmVersion = Ant.antProject.properties."environment.SVN_REVISION"

    if (!scmVersion) {
        scmVersion = Ant.antProject.properties."environment.GIT_COMMIT"
    }

    // maybe a local git?
    if (!scmVersion) {
        try {
            def command = """git rev-parse HEAD"""
            def proc = command.execute()
            proc.waitFor()
            if (proc.exitValue() == 0) {
                scmVersion = proc.in.text
            }
        } catch (IOException e) {
            // oh well
        }
    }

    if (!scmVersion) {
        scmVersion = getEstimateRevisionFromGitFolder()
    }

    if (!scmVersion) {
        scmVersion = getRevisionFromSvnCli()
    }

    // if Hudson/Jenkins env variable not found, try file system (for SVN)
    if (!scmVersion) {
        File entries = new File(basedir, '.svn/entries')
        if (entries.exists() && entries.text.split('\n').length>3) {
            scmVersion = entries.text.split('\n')[3].trim()
        }
    }

    return scmVersion ?: '-'
}

private String getRevisionFromSvnCli() {
    try {
        def command = 'svn info --xml'
        def proc = command.execute()
        def out = new ByteArrayOutputStream()
        proc.consumeProcessOutput(out, null) //prevent blocking in Windows due to a full output buffer
        int exitVal = proc.waitFor()
        if (exitVal == 0) {
            def slurper = new XmlSlurper().parseText(out.toString())
            return slurper.entry.@revision
        }
    } catch (ignore) {
        return null
    }
}

def getEstimateRevisionFromGitFolder() {
    try {
        //on system which do not have git in the PATH, try the file system
        //the head this might not always provide accurate commit hash
        def headFile = new File(".git/HEAD")
        def refsHeadPath = ''
        if (headFile.exists()) {
            def headContents = headFile.text.trim()
            refsHeadPath = headContents.split(':')[1].trim()
            def refsHeadFile = new File(".git/${refsHeadPath}")
            if (refsHeadFile.isFile()) {
                return refsHeadFile.text.trim()
            }
        }
    } catch (ignore) {
        return null
    }
}

def getPluginEnvProperties(properties) {
    def pluginMeta
    pluginSettings.getSupportedPluginInfos().each { p ->
        pluginMeta = Metadata.getInstance(new File("${p.getPluginDir()}/application.properties")).getFromMap()
        if (pluginMeta?.'scm.version' != null) {
            pluginMeta.each { k, v ->
                properties."plugin.${p.getName()}.${k}" = v
            }
        }
    }
}