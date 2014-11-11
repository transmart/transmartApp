


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
    private HashMap<String, List<String>> converter;

    public HashMap<String, List<String>> getConverter() {
        return converter;
    }

    public void setConverter(HashMap<String, List<String>> converter) {
        this.converter = converter;
    }

    /**
     * Hash denoting what processing method to use, and a list of the steps in the process.
     */
    private HashMap<String, List<String>> processor;

    public HashMap<String, List<String>> getProcessor() {
        return processor;
    }

    public void setProcessor(HashMap<String, List<String>> processor) {
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
    private HashMap<String, List<String>> dataTypes;

    public HashMap<String, List<String>> getDataTypes() {
        return dataTypes;
    }

    public void setDataTypes(HashMap<String, List<String>> dataTypes) {
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
    private HashMap<String, List<String>> renderer;

    public HashMap<String, List<String>> getRenderer() {
        return renderer;
    }

    public void setRenderer(HashMap<String, List<String>> renderer) {
        this.renderer = renderer;
    }

    /**
     * Map of parameters to the names of the HTML inputs. We will run a replace statement to sub in the value from the form for the string used as the key here.
     */
    private HashMap<String, String> variableMapping;

    public HashMap<String, String> getVariableMapping() {
        return variableMapping;
    }

    public void setVariableMapping(HashMap<String, String> variableMapping) {
        this.variableMapping = variableMapping;
    }

    /**
     * Map of data types to the names of the HTML inputs. We will evaluate the key to see if it is true, if it is, then we add the value to the list of data files to retrieve.
     */
    private HashMap<String, String> dataFileInputMapping;

    public HashMap<String, String> getDataFileInputMapping() {
        return dataFileInputMapping;
    }

    public void setDataFileInputMapping(HashMap<String, String> dataFileInputMapping) {
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
