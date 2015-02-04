/**
 * 	Explodes the plugins in the TranSMART plugin folder.
 */
Ant = new AntBuilder();
grailsHome = Ant.project.properties."environment.GRAILS_HOME"
basedir = System.getProperty("base.dir")
pluginFiles = "${basedir}/plugins/ext-2.2.zip"
jsDir = "${basedir}/web-app/js/plugin"
gspDir = "${basedir}/grails-app/views/plugin"
rDir = ""

//This is our plugin zip directory.
def zipDir = new File("${basedir}/plugins/")

//For each of the zips in this directory, instruct ant to unzip the file and move the contents to the directory specified by their file extension.
zipDir.eachFile { zipFile ->

    //If we find a file.
    if (zipFile.isFile()) {
        //Unzip file to a temp directory.
        Ant.unzip(src: "${zipFile}", dest: "${basedir}/plugins/temp/")

        fileDir = new File("${basedir}/plugins/temp/")

        fileDir.eachFile { file ->
            if (file.isFile()) {
                if (file.name.contains(".gsp")) Ant.move(file: "${file}", tofile: "${gspDir}/${file.name}")
                if (file.name.contains(".js")) Ant.move(file: "${file}", tofile: "${jsDir}/${file.name}")
                if (file.name.contains(".r")) Ant.move(file: "${rDir}", tofile: "${rDir}/${file.name}")
            }
        }

        //TODO: Delete temp directory?

    }
}
