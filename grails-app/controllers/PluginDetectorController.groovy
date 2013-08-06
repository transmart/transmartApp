import org.codehaus.groovy.grails.plugins.PluginManagerHolder

class PluginDetectorController {

    def checkPlugin = {

        // get plugin name
        def pluginName = request.getParameter("pluginName")
        def result = false

        if (PluginManagerHolder.getPluginManager().hasGrailsPlugin(pluginName)) { // check if plugin is installed
            result = true
        }

        render result
    }
}
