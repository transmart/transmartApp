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
  

package transmartpfizer

import grails.converters.XML

class WebserviceController {

	def webserviceService
	
    def computeGeneBounds = {
		def results = webserviceService.computeGeneBounds(params.geneSymbol, "GRCh37")
		renderDataSet(results)
	}
	
	def getGeneByPosition = {
		def results = webserviceService.getGeneByPosition(params.chromosome, params.long('start'), params.long('stop'))
		renderDataSet(results)
	}
	
	def getSnpSources = {
		renderDataSet([[18,"HG18",18,"03-2006","http://www.example.com"], [19,"HG19",19,"02-2009","http://www.example.com"]])
	}
	
	def getGeneSources = {
		renderDataSet([[0,"GRCh37",0,"01-2001","http://www.example.com"]])
	}
	
	def renderDataSet(results) {
		
		render(contentType:"text/xml",encoding:"UTF-8") {
			rows() {
				for (result in results) {
				row() {
					for (dat in result) {
					data(dat)
					}
				}
				}
			}
		}
		
	}
}
