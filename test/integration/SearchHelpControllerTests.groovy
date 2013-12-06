

import static org.junit.Assert.*

import org.junit.*

class SearchHelpControllerTests {

	void testListAllTrials() {
		def expected = 0
		def shc = new SearchHelpController()
		shc.listAllTrials()
		def trials = shc.modelAndView.model.trials
		assertEquals(expected, trials.size())
	}
	
	void testListAllDiseases()	{
		def expected = 0
		def shc = new SearchHelpController()
		shc.listAllDiseases()
		def diseases = shc.modelAndView.model.diseases
		assertEquals(expected, diseases.size())
	}
	
	void testListAllCompounds()	{
		def expected = 0
		def shc = new SearchHelpController()
		shc.listAllCompounds()
		def compounds = shc.modelAndView.model.compounds
		assertEquals(expected, compounds.size())
	}
	
	void testListAllPathways()	{
		def expectedPathways = 0
		def shc = new SearchHelpController()
		shc.listAllPathways()
		def pathways = shc.modelAndView.model.pathways
		assertEquals(expectedPathways, pathways.size())
	}
}
