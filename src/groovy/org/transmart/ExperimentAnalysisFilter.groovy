package org.transmart
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
 * @author $Author: mmcduffie $
 * $Id: ExperimentAnalysisFilter.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @version $Revision: 9178 $
 *
 */

public class ExperimentAnalysisFilter {

	String dataSource
	Long bioDiseaseId
	String species
	String expDesign
	String expType
	Long bioCompoundId
	String tissueType
	String cellLine
	String expDescrKeyword
	//String platformOrganism
	//Double foldChange = 1.2
	//Double pValue = 0.05
	Double foldChange
	Double pValue

	def isUsed(){
		return validString(species) || validString(expDesign)||validString(expType)||validString(dataSource)||bioCompoundId!=null || bioDiseaseId!=null || foldChange!=null || pValue!=null || validString(cellLine);
	}
	def filterFoldChange(){
		return foldChange!=null && foldChange>0;
	}

	def filterPValue(){
		return pValue!=null && pValue>0;
	}

	def filterDisease(){
		return bioDiseaseId!=null && bioDiseaseId>0;
	}

	def filterCompound(){
		return bioCompoundId!=null && bioCompoundId>0;
	}

	def filterSpecies(){
		return validString(species);
	}

	def filterExpDesign(){
		return validString(expDesign);
	}

	def filterExpType(){
		return validString(expType);
	}

	def filterDataSource(){
		return validString(dataSource)
	}

	def validString(String s){
		return s!=null && s.length()>0;
	}
}
