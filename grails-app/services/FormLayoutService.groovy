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

/**
 * $Id: DetailsService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 */
//import org.dom4j.Document;
//import org.dom4j.Element;
//import org.dom4j.DocumentException;
//import org.dom4j.io.SAXReader;

class FormLayoutService {

    def getLayout(String key) {

        def columns = FormLayout.createCriteria().list() {
            eq('key', key)
            order('sequence', 'asc')
        }
        return columns
    }


    def getProgramLayout =
        {

            def formLayout = [new FormLayout(
                    dataType: "string",
                    displayName: "Program title",
                    column: "Oncology_pan-PI3K inhibition"
            ),

                    new FormLayout(
                            dataType: "string",
                            displayName: "Program description",
                            column: "pan-PI3K inhibition is a strategy to target all four of the class one PI3K isoforms (alpha, beta, gamma, delta) since the activity of any class 1A PI3K isoform appears to sustain cell proliferation and survival."
                    ),
                    new FormLayout(
                            dataType: "string",
                            displayName: "Program target",
                            column: "pan-PI3K inhibition (PIK3CA, PIK3CB, PIK3CD, PIK3CG)"
                    ),
                    new FormLayout(
                            dataType: "string",
                            displayName: "Therapeutic domain",
                            column: "Oncology"
                    ),
                    new FormLayout(
                            dataType: "string",
                            displayName: "Institution",
                            column: "Sanofi - Oncology BD"
                    )
            ]
            return formLayout
        }
/*
	def getStudyGridLayout =
	{
			yes	
		Institution	yes	Sanofi - Oncology BD
		Country	yes	USA
		Study PubMed ID	yes	none
		Study link	yes	http://clinicaltrials.gov/ct2/show/NCT01013324
		
		
		
		def formLayout = [ new FormLayout(
			dataType: "string",
			displayName : "Study title",
			column : "XL147_Endometrial cancer_ARD11436"
			),
		
		new FormLayout(
			dataType: "string",
			displayName : "Program description",
			column : "pan-PI3K inhibition is a strategy to target all four of the class one PI3K isoforms (alpha, beta, gamma, delta) since the activity of any class 1A PI3K isoform appears to sustain cell proliferation and survival."
			),
		new FormLayout(
			dataType: "string",
			displayName : "Program target",
			column : "pan-PI3K inhibition (PIK3CA, PIK3CB, PIK3CD, PIK3CG)"
			),
		new FormLayout(
			dataType: "string",
			displayName : "Therapeutic domain",
			column : "Oncology"
			),
		new FormLayout(
			dataType: "string",
			displayName : "Institution",
			column : "Sanofi - Oncology BD"
			)
		]
		return formLayout
	}
*/

}