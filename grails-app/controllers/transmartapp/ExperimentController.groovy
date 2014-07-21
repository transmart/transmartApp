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

import grails.converters.JSON
import org.transmart.biomart.Experiment

class ExperimentController {

	/**
	 * Find the top 20 experiments with a case-insensitive LIKE
	 */
    def extSearch = {
		def paramMap = params
		def value = params.term.toUpperCase();
        def studyType = params.studyType?.toUpperCase();
		
		def experiments = Experiment.executeQuery("SELECT accession, title FROM Experiment e WHERE upper(e.title) LIKE '%' || :term || '%' AND upper(e.type) = :studyType", [term: value, studyType: studyType], [max: 20]);

        def category = "STUDY"
        def categoryDisplay = "Study"
        if (studyType.equals('I2B2')) {
            category="i2b2"
            categoryDisplay = "i2b2"
        }
		def itemlist = [];
		for (exp in experiments) {
			itemlist.add([id:exp[0], keyword:exp[1], category:category, display:categoryDisplay]);
		}
		
		render itemlist as JSON;
	}
	
	/**
	 * This will display a list of the available studies in the system to the user. The user will only be able to select one item from the dropdown.
	 */
	def browseExperimentsSingleSelect = {

        def experiments

        if (params.type) {
            experiments = Experiment.findAllByType(params.type)
            experiments = getSortedList(experiments)
        }
        else {
            experiments = Experiment.list()
            experiments = getSortedList(experiments)
        }

        render(template:'browseSingle',model:[experiments:experiments])
	}
	
	/**
	 * This will render a UI where the user can pick an experiment from a list of all the experiments in the system. Selection of multiple studies is allowed.
	 */
	def browseExperimentsMultiSelect = {

        def experiments

        if (params.type) {
            experiments = Experiment.findAllByType(params.type)
            experiments = getSortedList(experiments)
        }
        else {
            experiments = Experiment.list()
            experiments = getSortedList(experiments)
        }

		render(template:'browseMulti',model:[experiments:experiments])
	}
	
	def getSortedList(experiments) {

		experiments.sort({a, b ->
			return a.title.trim().compareToIgnoreCase(b.title.trim());
		})
		
		return experiments
	}
	
}
