/**
* This is the config file used by the RModules plugin
* @author SMunikuntla
*
*/
RModules 
{
	// plugin script directory
       pluginScriptDirectory = "/usr/local/tomcat-6.0.35/webapps/transmartPfizer/plugins/rdc-rmodules-0.1/Rscripts/"
	   defaultStatusList = ["Started",
			"Validating Cohort Information",
			"Triggering Data-Export Job",
			"Gathering Data",
			"Running Conversions",
			"Running Analysis",
			"Rendering Output"]
	   
	   /**
	    * Define a different statusList for each module if it varies from the defaultStatusList
	    * Make sure the moduleName is the jobType
	    */
	   survivalAnalysis.StatusList = ["Started",
		   "Validating Cohort Information",
		   "Triggering Data-Export Job",
		   "Gathering Data",
		   "Running Conversions",
		   "Running Analysis",
		   "Rendering Output"]
	   
	   //Configuration for plugins.
	   tempFolderDirectory = "/home/transmart/jobs/"
	   //I use this to do local development. It causes the analysis controller
	   transferImageFile = true
	   //This is the system path where we move the image file to so we can serve it.
	   temporaryImageFolder = "/usr/local/tomcat-6.0.35/webapps/transmartPfizer/images/tempImages"
	   //This is the path that we use to render the image.
	   imageURL = "/transmartPfizer/images/tempImages/"
	   
	// whether or not to enable ftp
	   ftpFlag = "FALSE"
	   
	   //Make sure there is groovy class with the name+'Definition' 
	   //(For ex:BoxPlotDefinition) in the com.recomdata.transmart.rmodules
	   //Add new modules here, as module-registration will happen only if it is defined here

	   modules = ["SurvivalAnalysis", "BoxPlot", "ScatterPlot", "TableWithFisher","Heatmap"]
}

