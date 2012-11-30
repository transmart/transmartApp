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
 * $Id: LiteratureAssocMoleculeDetailsData.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package org.transmart.biomart
class LiteratureAssocMoleculeDetailsData {
	Long id
	Long bioLitAltDataId
	String etlId
	String molecule
	String moleculeType
	String totalExpPercent
	String totalExpNumber
	String totalExpValue
	String totalExpSd
	String totalExpUnit
	String overExpPercent
	String overExpNumber
	String overExpValue
	String overExpSd
	String overExpUnit
	String coExpPercent
	String coExpNumber
	String coExpValue
	String coExpSd
	String coExpUnit
	String mutationType
	String mutationSites
	String mutationChange
	String mutationPercent
	String mutationNumber
	String targetExpPercent
	String targetExpNumber
	String targetExpValue
	String targetExpSd
	String targetExpUnit
	String targetOverExpPercent
	String targetOverExpNumber
	String targetOverExpValue
	String targetOverExpSd
	String targetOverExpUnit
	String techniques
	String description
	static mapping = {
		table 'BIO_LIT_AMD_DATA'
		version false
		id generator:'sequence', params:[sequence:'SEQ_BIO_DATA_ID']
		columns {
			id column:'BIO_LIT_AMD_DATA_ID'
			bioLitAltDataId column:'BIO_LIT_ALT_DATA_ID'
			etlId column:'ETL_ID'
			molecule column:'MOLECULE'
			moleculeType column:'MOLECULE_TYPE'
			totalExpPercent column:'TOTAL_EXP_PERCENT'
			totalExpNumber column:'TOTAL_EXP_NUMBER'
			totalExpValue column:'TOTAL_EXP_VALUE'
			totalExpSd column:'TOTAL_EXP_SD'
			totalExpUnit column:'TOTAL_EXP_UNIT'
			overExpPercent column:'OVER_EXP_PERCENT'
			overExpNumber column:'OVER_EXP_NUMBER'
			overExpValue column:'OVER_EXP_VALUE'
			overExpSd column:'OVER_EXP_SD'
			overExpUnit column:'OVER_EXP_UNIT'
			coExpPercent column:'CO_EXP_PERCENT'
			coExpNumber column:'CO_EXP_NUMBER'
			coExpValue column:'CO_EXP_VALUE'
			coExpSd column:'CO_EXP_SD'
			coExpUnit column:'CO_EXP_UNIT'
			mutationType column:'MUTATION_TYPE'
			mutationSites column:'MUTATION_SITES'
			mutationChange column:'MUTATION_CHANGE'
			mutationPercent column:'MUTATION_PERCENT'
			mutationNumber column:'MUTATION_NUMBER'
			targetExpPercent column:'TARGET_EXP_PERCENT'
			targetExpNumber column:'TARGET_EXP_NUMBER'
			targetExpValue column:'TARGET_EXP_VALUE'
			targetExpSd column:'TARGET_EXP_SD'
			targetExpUnit column:'TARGET_EXP_UNIT'
			targetOverExpPercent column:'TARGET_OVER_EXP_PERCENT'
			targetOverExpNumber column:'TARGET_OVER_EXP_NUMBER'
			targetOverExpValue column:'TARGET_OVER_EXP_VALUE'
			targetOverExpSd column:'TARGET_OVER_EXP_SD'
			targetOverExpUnit column:'TARGET_OVER_EXP_UNIT'
			techniques column:'TECHNIQUES'
			description column:'DESCRIPTION'
		}
	}
}
