package com.recomdata.transmart.services

import org.codehaus.groovy.grails.commons.ApplicationHolder as AH
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.junit.Ignore

public class ClinicalDataServiceTests extends GroovyTestCase {

	def ctx = AH.application.mainContext
	def config = ConfigurationHolder.config
	String testingDirectory = config.RModules.tempFolderDirectory
		
	def postgresClinicalDataService
	
	/**
	 * This method will test the Clinical Data Export functionality for a specific patient list. 
	 * In order to generate the "result_instance_id" used in this test I went into the interface and ran a workflow for the following;
	 * Study : Breast_Cancer_Sorlie_GSE4382
	 * Workflow : Scatter Plot
	 * Independent Variable : \Sample Factors\End Points\Recurrence-Free Survival Time
	 * Dependent Variable : \Sample Factors\End Points\Survival Time (Months)
	 * 
	 * After the test was done running I extracted the required parameters from the jobInfo.txt file that is created in the jobs temporary directory.
	 */
//	@Ignore
	void testDataRetrieval()
	{
		//This is a list of studies.		
		def List studyList = ["GSE4382"];
		
		//This is a string array of concepts.
		def String[] conceptCodeList = ['27','26'];
		
		//Create the file object to pass into the service. This is the directory where we write the output file.
		def File studyDir = null
		studyDir = new File(testingDirectory, "integrationTesting")
		studyDir.mkdir()
		
		try
		{
			//Run the command to create the clinical data export file.
			postgresClinicalDataService.getData(studyList,
										studyDir,
										"clinicalIntegrationText.txt",
										"integrationTest",
										"18886",
										conceptCodeList,
										["CLINICAL.TXT"],
										false,
										false,
										[:],
										"1",
										[:],
										[],
										[] as String[],
										false)
		} catch (Exception e) {
			println(e.message)
			assert false;
		}
		
		//This is the file that should be created when we call the ClinicalDataService.
		File dataFile = new File(testingDirectory + "integrationTesting\\Clinical\\clinicalIntegrationText.txt")

		//Check for the existence of the exported data file.
		assert dataFile.exists()
		
		//Count how many lines are in the file.
		int num = 0;
		dataFile.eachLine { num++ }
		
		//Check to see if the count is equal to what we expect it to be.
		assert num == 234
		
	}

}
