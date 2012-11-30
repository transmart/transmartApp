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
 * $Id: Experiment.groovy 10303 2011-11-01 03:27:41Z jliu $
 * @author $Author: jliu $
 * @version $Revision: 10303 $
 */

package org.transmart.biomart

import org.transmart.biomart.BioData;

import com.recomdata.util.IExcelProfile

class Experiment implements IExcelProfile {
	Long id
	String type
	String title
	String description
	String design
	String status
	String overallDesign
	String accession
	Date startDate
	Date completionDate
	String primaryInvestigator
	String institution
	String country
	String bioMarkerType
	String target
	String accessType
	
	static hasMany =[compounds:Compound, diseases:Disease, files:ContentReference, uniqueIds:BioData, organisms:Taxonomy]
	static belongsTo=[Compound, Disease, Taxonomy, ContentReference]

	static mapping = {
		tablePerHierarchy false
		table 'BIO_EXPERIMENT'
		version false
		cache usage:'read-only'
		//	 id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		columns {
			id column:'BIO_EXPERIMENT_ID'
			type column:'BIO_EXPERIMENT_TYPE'
			title column:'TITLE'
			description column:'DESCRIPTION'
			design column:'DESIGN'
			startDate column:'START_DATE'
			completionDate column:'COMPLETION_DATE'
			overallDesign column:'OVERALL_DESIGN'
			accession column:'ACCESSION'
			institution column:'INSTITUTION'
			country column:'COUNTRY'
			accessType column:'ACCESS_TYPE'
			target column:'TARGET'
			bioMarkerType column:'BIOMARKER_TYPE'
			primaryInvestigator column:'PRIMARY_INVESTIGATOR'
			compounds joinTable:[name:'BIO_DATA_COMPOUND', key:'BIO_DATA_ID'], cache:true
			diseases joinTable:[name:'BIO_DATA_DISEASE', key:'BIO_DATA_ID'], cache:true
			organisms joinTable:[name:'BIO_DATA_TAXONOMY', key:'BIO_DATA_ID'],cache:true
			files joinTable:[name:'BIO_CONTENT_REFERENCE', key:'BIO_DATA_ID', column:'BIO_CONTENT_REFERENCE_ID'],cache:true
			uniqueIds joinTable:[name:'BIO_DATA_UID', key:'BIO_DATA_ID']
		}
	}
	
	static constraints = {
		type(nullable:true, maxSize:400)
		title(nullable:true, maxSize:2000)
		description(nullable:true, maxSize:4000)
		design(nullable:true, maxSize:4000)
		overallDesign(nullable:true, maxSize:4000)
		startDate(nullable:true)
		completionDate(nullable:true)
		primaryInvestigator(nullable:true, maxSize:800)
	}
	
	def getCompoundNames()	{
	    StringBuilder compoundNames = new StringBuilder()
	    compounds.each{
	        if (it.getName() != null)	{
	            if (compoundNames.length() > 0)	{
	                compoundNames.append("; ")
	            }
	            compoundNames.append(it.getName())
	        }
	    }
	    return compoundNames.toString()
	}
	
	def getDiseaseNames()	{
	    StringBuilder diseaseNames = new StringBuilder()
	    diseases.each{
	        if (it.disease != null)	{
	            if (diseaseNames.length() > 0)	{
	                diseaseNames.append("; ")
	            }
	            diseaseNames.append(it.disease)
	        }
	    }
	    return diseaseNames.toString()
	}
	
	def getOrganismNames()	{
		StringBuilder taxNames = new StringBuilder()
		organisms.each{
			if (it.name != null)	{
				if (taxNames.length() > 0)	{
					taxNames.append("; ")
				}
				taxNames.append(it.name)
			}
		}
		return taxNames.toString()
	}
	
	
	/**
	 * Get values to Export to Excel
	 */
	public List getValues() {	
		
		return [accession, type, title, description, design, status, overallDesign, startDate, completionDate, primaryInvestigator,  getCompoundNames(), getDiseaseNames()]
		}
	
	public List getExpValues() {
		
		return [accession, type, title, description, design, status, overallDesign, startDate, completionDate, primaryInvestigator,  getCompoundNames(), getDiseaseNames()]
		}
	
	
	def getUniqueId(){
		if(uniqueIds!=null && !uniqueIds.isEmpty())
			return uniqueIds.iterator().next();
		return null;
	}
	
	/**
	 * hack to get around gorm inheritance bug
	 */
	def getExpId() {
		return this.getId();
	}
	
	/**
	 * override display
	 */
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("id: ").append(getExpId()).append("; type: ").append(type).append("; title: ").append(title).append("; description: ").append(description).append("; accesion: ").append(accession);
		return sb.toString();		
	}
}
