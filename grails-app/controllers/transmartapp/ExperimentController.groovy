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

import bio.Experiment;
import grails.converters.JSON

class ExperimentController {

	/**
	 * Find the top 20 experiments with a case-insensitive LIKE
	 */
    def extSearch = {
		def paramMap = params
		def value = params.query.toUpperCase();
		
		def experiments = Experiment.executeQuery("SELECT id, title FROM Experiment e WHERE upper(e.title) LIKE '%' || :term || '%'", [term: value], [max: 20]);
		
		def itemlist = [];
		for (exp in experiments) {
			itemlist.add([id:exp[0], keyword:exp[1], category:"STUDY", display:"Study"]);
		}
		
		def result = [rows:itemlist]
		def json = result as JSON;
		render (params.callback+"("+(result as JSON)+")");
	}
}
