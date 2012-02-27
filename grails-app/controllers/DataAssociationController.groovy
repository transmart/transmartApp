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
import com.recomdata.plugins.PluginDescriptor;

class DataAssociationController {
	
	def pluginService

	/**
	 * Display the initial DataAssociation page.
	 */
	def defaultPage =
	{

		Map pluginMap = pluginService.register()
	
		session.setAttribute(PluginDescriptor.PLUGIN_MAP, pluginMap);
		
		List pluginList = new ArrayList()
		Set pluginKeySet = pluginMap.keySet()
		pluginKeySet.each {pluginId->
			
		List availablePlugins = grailsApplication.config.com.recomdata.plugins.available
				if(availablePlugins.contains(pluginId))
				{
					pluginList.add(pluginMap.get(pluginId))
				}
			
			}
		
		render(template: "dataAssociation", model:[analysisList:pluginList])
	}
	
	def variableSelection = {
		def analysis = params['analysis']
		PluginDescriptor plugin = ((Map)session.getAttribute(PluginDescriptor.PLUGIN_MAP)).get(analysis)
		render(view:"../plugin/"+plugin.getView())
	}
	
	
}