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
  

import org.junit.Ignore
import org.transmart.biomart.BioAssayAnalysisData
import org.transmart.biomart.BioAssayData

/**
 * @author JLiu
 *
 * NOTE: these tests fail by side effect; database unavailable; table unavailable; etc
 * The tests are meaningless if the tables are empty (hence the guard function)
 */

// TODO: generalize away from the dependency on specific database keys

public class BioAssayAnalysisDataTests extends GroovyTestCase{
	static transactional = false

	def targetIdForBioMarker = "8037275"
	def targetIdForDisease = "8021799"

//	@Ignore // see JIRA item THRONE-103
	void testAnalysisData(){
		def tables = ["org.transmart.biomart.BioAssayAnalysisData","org.transmart.biomart.BioMarker","org.transmart.biomart.Disease"]
		if (!dataAvailable(tables)) return
		
		if (!targetIdAvailable("org.transmart.biomart.BioMarker",targetIdForBioMarker)) return

		def c = BioAssayAnalysisData.executeQuery(
			"SELECT COUNT (DISTINCT baad.id) " +
			"FROM org.transmart.biomart.BioAssayAnalysisData baad JOIN baad.markers bm JOIN baad.diseases bd " +
			"WHERE bm.id = " + targetIdForBioMarker + " ")

		//def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM org.transmart.biomart.BioAssayAnalysisData baad, org.transmart.biomart.BioDataOmicFact bm,  org.transmart.biomart.BioDataDiseaseFact bd "+
		//			" WHERE baad.id = bm.bioDataId AND bm.bioMarkerId = 8037275 "+
		//			" AND baad.id = bd.bioDataId AND bd.bioDiseaseId = 8021799 ")
		//def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM org.transmart.biomart.BioAssayAnalysisData baad JOIN baad.markers bm JOIN baad.diseases bd "+
		//	" WHERE bm.id = 8037275 "+
		//	" AND bd.id = 8021799 ")
		//def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM org.transmart.biomart.BioAssayAnalysisData baad JOIN baad.markerFacts bm JOIN baad.diseaseFacts bd "+
		//			" WHERE bm.bioMarkerId = 8037275 "+
		//			" AND bd.bioDiseaseId = 8021799 ")
		
		print("testAnalysisData - count: ")
		println(c)
	}

//	@Ignore // see JIRA item THRONE-104
	void testAssayData(){
	
		def tables = ["org.transmart.biomart.BioAssayData","org.transmart.biomart.BioMarker","org.transmart.biomart.Disease"]

		if (!dataAvailable(tables)) return
		
		if (!targetIdAvailable("org.transmart.biomart.BioMarker",targetIdForBioMarker)) return
		if (!targetIdAvailable("org.transmart.biomart.Disease",targetIdForDisease)) return

		def c = BioAssayData.executeQuery(
			"SELECT COUNT (DISTINCT bad.id) " + 
			"FROM org.transmart.biomart.BioAssayData bad JOIN bad.markers bm JOIN bad.diseases bd " +
			"WHERE bm.id = " + targetIdForBioMarker + " " +
			"AND bd.id = " + targetIdForDisease + " " )

		//def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM org.transmart.biomart.BioAssayAnalysisData baad, org.transmart.biomart.BioDataOmicFact bm,  org.transmart.biomart.BioDataDiseaseFact bd "+
		//				" WHERE baad.id = bm.bioDataId AND bm.bioMarkerId = 8037275 "+
		//				" AND baad.id = bd.bioDataId AND bd.bioDiseaseId = 8021799 ")
		//	def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM org.transmart.biomart.BioAssayAnalysisData baad JOIN baad.markerFacts bm JOIN baad.diseaseFacts bd "+
		//				" WHERE bm.bioMarkerId = 8037275 "+
		//				" AND bd.bioDiseaseId = 8021799 ")

		print("testAssayData - count: ")
		println(c)
	}

//	@Ignore // see JIRA item THRONE-105
	void testLiteratureData(){
		def tables = ["org.transmart.biomart.Literature","org.transmart.biomart.BioMarker","org.transmart.biomart.Disease"]

		if (!dataAvailable(tables)) return
		
		if (!targetIdAvailable("org.transmart.biomart.BioMarker",targetIdForBioMarker)) return
		if (!targetIdAvailable("org.transmart.biomart.Disease",targetIdForDisease)) return
		
		def c = BioAssayData.executeQuery(
			"SELECT COUNT (DISTINCT ldf.id) " + 
			"FROM org.transmart.biomart.Literature ldf JOIN ldf.markers bm JOIN ldf.diseases bd "+
			"WHERE bm.id =" + targetIdForBioMarker + " " +
			"AND bd.id = " + targetIdForDisease + " ")
		
		// def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM org.transmart.biomart.BioAssayAnalysisData baad, org.transmart.biomart.BioDataOmicFact bm,  org.transmart.biomart.BioDataDiseaseFact bd "+
		//				" WHERE baad.id = bm.bioDataId AND bm.bioMarkerId = 8037275 "+
		//				" AND baad.id = bd.bioDataId AND bd.bioDiseaseId = 8021799 ")
		//	def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM org.transmart.biomart.BioAssayAnalysisData baad JOIN baad.markerFacts bm JOIN baad.diseaseFacts bd "+
		//				" WHERE bm.bioMarkerId = 8037275 "+
		//				" AND bd.bioDiseaseId = 8021799 ")

		print("testLiteratureData - count: ")
		println(c)
	}
	
	boolean dataAvailable(sources) {
		boolean ret = true;
		for (String name in sources) {
			def boolean probe = dataInTable(name)
			if (!probe) log.info("No data in test table: " + name)
			ret &= probe
		}
		if (!ret) {
			println("The integration tests in BioAssayAnalysisDataTests can not be run; some test tables are empty!")
		}
		return ret
	}
	
	boolean dataInTable(String target){
		def c = BioAssayData.executeQuery("SELECT COUNT(id) FROM " + target)
		print (target + ": ")
		println(c)
		return (!c.isEmpty() & (c[0] != 0))
	}

	boolean targetIdAvailable(String target, String id){
		def c = BioAssayData.executeQuery("SELECT COUNT(id) FROM " + target + " where id=" + id);
		print (target + "(" + id +  "): ")
		println(c)
		boolean ret = (!c.isEmpty() & (c[0] != 0))
		if (!ret) {
			println("The integration tests in BioAssayAnalysisDataTests can not be run: " + 
				"the target id, " + id + ", for the entity " + target + " is not available.")
		}
	}
}
