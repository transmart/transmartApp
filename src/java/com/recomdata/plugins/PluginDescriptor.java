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
  

package com.recomdata.plugins;

import java.util.HashMap;
import java.util.List;

public class PluginDescriptor {
	public static final String PLUGIN_MAP = "PLUGIN_MAP"; 
	
	private String dataAccessObject;
	public String getDataAccessObject() {
		return dataAccessObject;
	}
	public void setDataAccessObject(String dataAccessObject) {
		this.dataAccessObject = dataAccessObject;
	}
	
	private String view;
	public String getView() {
		return view;
	}
	public void setView(String view) {
		this.view = view;
	}
	
	/**
	 * Hash denoting what converting method to use, and a list of the steps in the process.
	 */
	private HashMap<String,List<String>> converter;
	
	public HashMap<String,List<String>> getConverter() {
		return converter;
	}
	public void setConverter(HashMap<String,List<String>> converter) {
		this.converter = converter;
	}
	
	/**
	 * Hash denoting what processing method to use, and a list of the steps in the process.
	 */
	private HashMap<String,List<String>> processor;
	
	public HashMap<String,List<String>> getProcessor() {
		return processor;
	}
	public void setProcessor(HashMap<String,List<String>> processor) {
		this.processor = processor;
	}

	/**
	 * Name of the plugin.
	 */
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * List of data types this plugin utilizes.
	 */
	private HashMap<String,List<String>> dataTypes;
	public HashMap<String,List<String>> getDataTypes() {
		return dataTypes;
	}
	public void setDataTypes(HashMap<String,List<String>> dataTypes) {
		this.dataTypes = dataTypes;
	}	
	
	/**
	 * ID we use to pull the descriptor out of session.
	 */
	private String id;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	
	/**
	 * Hash denoting what processing method to use, and a list of the steps in the process.
	 */
	private HashMap<String,List<String>> renderer;
	
	public HashMap<String,List<String>> getRenderer() {
		return renderer;
	}
	public void setRenderer(HashMap<String,List<String>> renderer) {
		this.renderer = renderer;
	}	
	
	/**
	 * Map of parameters to the names of the HTML inputs. We will run a replace statement to sub in the value from the form for the string used as the key here.
	 */
	private HashMap<String,String> variableMapping;
	
	public HashMap<String,String> getVariableMapping() {
		return variableMapping;
	}
	public void setVariableMapping(HashMap<String,String> variableMapping) {
		this.variableMapping = variableMapping;
	}		
	
	/**
	 * Map of data types to the names of the HTML inputs. We will evaluate the key to see if it is true, if it is, then we add the value to the list of data files to retrieve.
	 */
	private HashMap<String,String> dataFileInputMapping;
	
	public HashMap<String,String> getDataFileInputMapping() {
		return dataFileInputMapping;
	}
	public void setDataFileInputMapping(HashMap<String,String> dataFileInputMapping) {
		this.dataFileInputMapping = dataFileInputMapping;
	}		
		
	/**
	 * This is a flag that tells us whether to pivot the clinical data or not.
	 */
	private boolean pivotData;
	public boolean getPivotData() {
		return pivotData;
	}
	public void setPivotData(boolean pivotData) {
		this.pivotData = pivotData;
	}			
	
}
