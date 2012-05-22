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
  

import bio.BioAssayAnalysisData
import bio.Literature
import bio.BioAssayData

/**
 * @author JLiu
 *
 */

public class BioAssayAnalysisDataTests extends GroovyTestCase{
	static transactional = false

	void testAnalysisData(){
	//def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM bio.BioAssayAnalysisData baad, bio.BioDataOmicFact bm,  bio.BioDataDiseaseFact bd "+
//			" WHERE baad.id = bm.bioDataId AND bm.bioMarkerId = 8037275 "+
//			" AND baad.id = bd.bioDataId AND bd.bioDiseaseId = 8021799 ")
	def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM bio.BioAssayAnalysisData baad JOIN baad.markers bm JOIN baad.diseases bd "+
			" WHERE bm.id = 8037275 "+
			" AND bd.id = 8021799 ")
//def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM bio.BioAssayAnalysisData baad JOIN baad.markerFacts bm JOIN baad.diseaseFacts bd "+
//			" WHERE bm.bioMarkerId = 8037275 "+
//			" AND bd.bioDiseaseId = 8021799 ")

println(c)

	}

	void testAssayData(){
		//def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM bio.BioAssayAnalysisData baad, bio.BioDataOmicFact bm,  bio.BioDataDiseaseFact bd "+
//				" WHERE baad.id = bm.bioDataId AND bm.bioMarkerId = 8037275 "+
//				" AND baad.id = bd.bioDataId AND bd.bioDiseaseId = 8021799 ")
		def c = BioAssayData.executeQuery("SELECT COUNT (DISTINCT bad.id) FROM bio.BioAssayData bad JOIN bad.markers bm JOIN bad.diseases bd "+
				" WHERE bm.id = 8037275 "+
				" AND bd.id = 8021799 ")
//	def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM bio.BioAssayAnalysisData baad JOIN baad.markerFacts bm JOIN baad.diseaseFacts bd "+
//				" WHERE bm.bioMarkerId = 8037275 "+
//				" AND bd.bioDiseaseId = 8021799 ")

	println(c)

		}

	void testLiteratureData(){
		//def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM bio.BioAssayAnalysisData baad, bio.BioDataOmicFact bm,  bio.BioDataDiseaseFact bd "+
//				" WHERE baad.id = bm.bioDataId AND bm.bioMarkerId = 8037275 "+
//				" AND baad.id = bd.bioDataId AND bd.bioDiseaseId = 8021799 ")
		def c = BioAssayData.executeQuery("SELECT COUNT (DISTINCT ldf.id) FROM bio.Literature ldf JOIN ldf.markers bm JOIN ldf.diseases bd "+
				" WHERE bm.id = 8037275 "+
				" AND bd.id = 8021799 ")
//	def c = BioAssayAnalysisData.executeQuery("SELECT COUNT (DISTINCT baad.id) FROM bio.BioAssayAnalysisData baad JOIN baad.markerFacts bm JOIN baad.diseaseFacts bd "+
//				" WHERE bm.bioMarkerId = 8037275 "+
//				" AND bd.bioDiseaseId = 8021799 ")

	println(c)

		}
}
