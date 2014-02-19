class FilterQueryServiceTests extends GroovyTestCase {

	def filterQueryService

//	@Ignore // JIRA - THRONE-111
    void testExpDisease() {
		def results = filterQueryService.experimentDiseaseFilter("Clinical Trial");
		assertTrue(results.size()>0)
    }
	
//	@Ignore // JIRA - THRONE-112
	 void testExpCompound() {
			def results = filterQueryService.experimentDiseaseFilter("Clinical Trial");
			assertTrue(results.size()>0)
	  }

}
