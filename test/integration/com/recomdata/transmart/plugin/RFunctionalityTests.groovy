package com.recomdata.transmart.plugin

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.junit.Ignore
import org.rosuda.REngine.REXP
import org.rosuda.REngine.Rserve.*

public class RFunctionalityTests extends GroovyTestCase {
	
	def ctx = AH.application.mainContext
	def config = ConfigurationHolder.config
	String testingDirectory = config.RModules.tempFolderDirectory
	String testDataDirectory = ""
	
	def pluginService
	
	/**
	 * This method will test our connection to the R Server.
	 */
//	@Ignore // JIRA - THRONE-108
	void testRConnection()
	{
		
		//We need to get the path to the test data within the grails application. This is a hacky way to get it. The real way is buried deep in confusing documentation somewhere.
		testDataDirectory = new File('.').absolutePath + "\\testData\\"
		
		println("Test Data Directory" + testDataDirectory)
		
		//**********************************
		//Test the actual connection to the R server.
		//Pull the configuration for the R Server IP and port.
		def String rServerAddress = config.RModules.rServerAddress
		def Integer rServerPort = config.RModules.rServerPort
		
		//Establish a connection to R Server.
		println("Attempting Connection to R Serve.")
		RConnection c = new RConnection(rServerAddress,rServerPort);
		println("Connection to R Serve made.")
		//**********************************
		
		//**********************************
		//Test setting the working directory.
		
		//Set the working directory to be our temporary location.
		String workingDirectoryCommand = "setwd('${testingDirectory}')".replace("\\","\\\\")
		
		println("Attempting following R Command : " + "setwd('${testingDirectory}')".replace("\\","\\\\"))
		//Run the R command to set the working directory to our temp directory.
		REXP x = c.eval(workingDirectoryCommand);
		println("Working directory set.")
		//**********************************
		
		//**********************************
		//Test running a scatter plot.
		//Fill the plugin service object.
		def pluginModuleInstance = pluginService.findPluginModuleByModuleName("scatterPlot")
		
		//Make sure we got the plugin module okay.
		assert pluginModuleInstance != null
		
		println("Gathered Plugin info.")
		
		//Pull the parameter object out of the database.
		def InputStream textStream = pluginModuleInstance?.params?.getAsciiStream()
		def moduleMap, moduleMapStr = null
		
		//Create a map for all the module parameters.
		moduleMapStr = pluginService.convertStreamToString(textStream).replace('\n',' ')
		moduleMap = new org.codehaus.groovy.grails.web.json.JSONObject(moduleMapStr) as Map
		
		//Make sure we got the module map correctly.
		assert moduleMap != null
		
		println("Got module map.")
		
		moduleMap.converter.each
		{
			currentStep ->

			currentStep.value.each
			{
				currentCommand ->
				
				//Need to escape backslashes for R commands.
				String reformattedCommand = currentCommand.replace("\\","\\\\")
				
				//Replace the working directory flag if it exists in the string.
				reformattedCommand = reformattedCommand.replace("||PLUGINSCRIPTDIRECTORY||", config.RModules.pluginScriptDirectory)
				reformattedCommand = reformattedCommand.replace("||TEMPFOLDERDIRECTORY||", testDataDirectory + "ScatterPlot\\").replace("\\","\\\\")
				reformattedCommand = reformattedCommand.replace("||TOPLEVELDIRECTORY||", config.RModules.pluginScriptDirectory).replace("\\","\\\\")
				reformattedCommand = reformattedCommand.replace("||DEPENDENT||", "\\\\Public Studies\\\\Breast_Cancer_Sorlie_GSE4382\\\\Sample Factors\\\\End Points\\\\Recurrence-free survival time (Months)\\\\")
				reformattedCommand = reformattedCommand.replace("||INDEPENDENT||", "\\\\Public Studies\\\\Breast_Cancer_Sorlie_GSE4382\\\\Sample Factors\\\\End Points\\\\Survival time (Months)\\\\")
				reformattedCommand = reformattedCommand.replace("||TYPEDEP||", "CLINICAL")
				reformattedCommand = reformattedCommand.replace("||TYPEIND||", "CLINICAL")
				
				reformattedCommand = reformattedCommand.replace("||TYPEIND||", "")
				reformattedCommand = reformattedCommand.replace("||TYPEIND||", "")
				reformattedCommand = reformattedCommand.replace("||GENESDEP||", "")
				reformattedCommand = reformattedCommand.replace("||GENESIND||", "")
				reformattedCommand = reformattedCommand.replace("||AGGREGATEDEP||", "")
				reformattedCommand = reformattedCommand.replace("||AGGREGATEIND||", "")
				reformattedCommand = reformattedCommand.replace("||SAMPLEDEP||", "")
				reformattedCommand = reformattedCommand.replace("||SAMPLEIND||", "")
				reformattedCommand = reformattedCommand.replace("||TIMEPOINTSDEP||", "")
				reformattedCommand = reformattedCommand.replace("||TIMEPOINTIND||", "")
				reformattedCommand = reformattedCommand.replace("||SNPTYPEDEP||", "")
				reformattedCommand = reformattedCommand.replace("||SNPTYPEIND||", "")
				reformattedCommand = reformattedCommand.replace("||GPLDEP||", "")
				reformattedCommand = reformattedCommand.replace("||GPLINDEP||", "")
				
				reformattedCommand = reformattedCommand.replace("||LOGX||", "false")
				
				log.debug("Attempting following R Command : " + reformattedCommand)
				println("Attempting following R Command : " + reformattedCommand)
				
				REXP r = c.parseAndEval("try("+reformattedCommand+",silent=TRUE)");
				
				//If there is an error running the script, the test fails.
				if (r.inherits("try-error"))
				{
					//Grab the error R gave us.
					String rError = r.asString()
					
					//This is the error we will eventually throw.
					RserveException newError = null
					newError = new RserveException(c,rError);
					
					throw newError
					assert false
				}
			}
		}
		
		println("Generated outputfile")
		
		File originalFile = new File(testDataDirectory + "ScatterPlot\\outputfile.original")
		File newFile = new File(testingDirectory + "\\outputfile")
		
		//Compare the size of the files as a means to roughly compare their contents.
		assert originalFile.size() == newFile.size()
		
		//Run the Analysis part of the R Script.
		
		moduleMap.processor.each
		{
			currentStep ->

			currentStep.value.each
			{
				currentCommand ->
				
				//Need to escape backslashes for R commands.
				String reformattedCommand = currentCommand.replace("\\","\\\\")
				reformattedCommand = reformattedCommand.replace("||PLUGINSCRIPTDIRECTORY||", config.RModules.pluginScriptDirectory)
				reformattedCommand = reformattedCommand.replace("||DEPENDENT||", "\\\\Public Studies\\\\Breast_Cancer_Sorlie_GSE4382\\\\Sample Factors\\\\End Points\\\\Recurrence-free survival time (Months)\\\\")
				reformattedCommand = reformattedCommand.replace("||INDEPENDENT||", "\\\\Public Studies\\\\Breast_Cancer_Sorlie_GSE4382\\\\Sample Factors\\\\End Points\\\\Survival time (Months)\\\\")
				reformattedCommand = reformattedCommand.replace("||TYPEDEP||", "CLINICAL")
				reformattedCommand = reformattedCommand.replace("||TYPEIND||", "CLINICAL")
				reformattedCommand = reformattedCommand.replace("||GENESDEPNAME||", "")
				reformattedCommand = reformattedCommand.replace("||GENESINDNAME||", "")
				reformattedCommand = reformattedCommand.replace("||SNPTYPEDEP||", "")
				reformattedCommand = reformattedCommand.replace("||SNPTYPEIND||", "")
				
				println(reformattedCommand)
				
				REXP r = c.parseAndEval("try("+reformattedCommand+",silent=TRUE)");
				
				//If there is an error running the script, the test fails.
				if (r.inherits("try-error"))
				{
					//Grab the error R gave us.
					String rError = r.asString()
					
					//This is the error we will eventually throw.
					RserveException newError = null
					newError = new RserveException(c,rError);
					
					throw newError
					assert false
				}
			}
		}
		
		//Verify we have an image file and a statistical output file.
		originalFile = new File(testDataDirectory + "ScatterPlot\\LinearRegression.txt.original")
		newFile = new File(testingDirectory + "\\LinearRegression.txt")
		
		//Compare the size of the files as a means to roughly compare their contents.
		assert originalFile.size() == newFile.size()
		
		originalFile = new File(testDataDirectory + "ScatterPlot\\ScatterPlot.png.original")
		newFile = new File(testingDirectory + "\\ScatterPlot.png")
		
		assert originalFile.size() == newFile.size()
		
		
		//Remove the test files we created.
		newFile = new File(testingDirectory + "\\LinearRegression.txt")
		newFile.delete()
		
		newFile = new File(testingDirectory + "\\ScatterPlot.png")
		newFile.delete()
	
		newFile = new File(testingDirectory + "\\outputfile")
		newFile.delete()
	}

}
