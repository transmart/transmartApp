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
  

package transmartapp

import org.transmart.biomart.Disease;
import org.transmart.biomart.Observation;
import grails.converters.JSON

class DiseaseController {

	/**
	 * Find the top 15 diseases with a case-insensitive LIKE
	 */
    def extSearch = {
		def paramMap = params
		def value = params.term.toUpperCase();
		
		def observations = null;
		//eQTL requires just disease - GWAS types need diseases and observations
		def diseases = Disease.executeQuery("SELECT meshCode, disease FROM Disease d WHERE upper(d.disease) LIKE '%' || :term || '%'", [term: value], [max: 10]);
		if (!params.type.equals("eqtl")) {
			observations = Observation.executeQuery("SELECT code, name, codeSource FROM Observation o WHERE upper(o.name) LIKE '%' || :term || '%'", [term: value], [max: 10]);
		}
		
		def itemlist = [];
		for (disease in diseases) {
			itemlist.add([id:disease[0], keyword:disease[1], sourceAndCode:"MESH:"+disease[0], category:"DISEASE", display:"Disease"]);
		}
		for (observation in observations) {
			itemlist.add([id:observation[0], keyword:observation[1], sourceAndCode:observation[2] + ":" + observation[0], category:"OBSERVATION", display:"Observation"]);
		}
		
		render itemlist as JSON;
	}
}
