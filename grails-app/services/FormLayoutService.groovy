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
                                  )
                                  /*,
                                  new FormLayout(
                                          dataType: "string",
                                          displayName: "Institution",
                                          column: "Sanofi - Oncology BD"
                                  )
                                  */
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