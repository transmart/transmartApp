package com.recomdata.transmart.services

import java.io.File;
import java.util.List;
import java.util.Map;
import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.ConfigurationHolder;

public class GeneExpressionDataServiceTests extends GroovyTestCase {
	
	//def ctx = GeneExpressionDataServiceTests.domainClass.grailsApplication;
	
	def config = ConfigurationHolder.config;
	String testingDirectory = config.RModules.tempFolderDirectory
	
	def geneExpressionDataService
	
	def i2b2HelperService
	
	/**
	 * 
	 */
	void testDataRetrieval()
	{
		
		def resultInstanceId = "1855652";
		
		try {
			def concepts = i2b2HelperService.getConcepts(resultInstanceId);
		}
		catch (org.xml.sax.SAXParseException e) {
			/**
			 * If there is no test data 
			 * then the i2b2 helper throws an exception trying to parse nothing...
			 */
			println("testDataRetrieval: No data to test.");
			return;
		}
		
		//This is a list of studies.
		def List studyList = ["GSE4382"];
		
		//Create the file object to pass into the service. This is the directory where we write the output file.
		def File studyDir = null
		studyDir = new File(testingDirectory, "integrationTesting")
		studyDir.mkdir()
		
		def gplString   = "GSE4382PDM"
		def List gplIds	= gplString.tokenize(",")
		
		try
		{
			//List studyList,
			//File studyDir,
			//String fileName,
			//String jobName,
			//String resultInstanceId,
			//boolean pivot,
			//List gplIds,
			//String pathway,
			//String timepoint,
			//String sampleTypes,
			//String tissueTypes,
			//Boolean splitAttributeColumn
			
			geneExpressionDataService.getData(	studyList,
												studyDir,
												"geneExpressionIntegrationText.txt",
												"integrationTest",
												"18886",
												false,
												gplIds,
												resultInstanceId,
												null,
												null,
												null,
												true)

		}
		catch(Exception e)
		{
			println(e.message)
			assert false;
		}
		
		//This is the file that should be created when we call the ClinicalDataService.
		File dataFile = new File(testingDirectory + "integrationTesting/mRNA/Processed_Data/geneExpressionIntegrationText.txt")

		//Check for the existence of the exported data file.
		assert dataFile.exists()
		
	}

}
