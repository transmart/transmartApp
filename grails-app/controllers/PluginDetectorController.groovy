import grails.util.Holders

class PluginDetectorController {

    def checkPlugin = {

        // get plugin name
        def pluginName = request.getParameter("pluginName")
        def result = false

        if (Holders.pluginManager.hasGrailsPlugin(pluginName)) { // check if plugin is installed
            result = true
        }

        render result
    }
}
