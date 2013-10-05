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
  

package de

import org.apache.commons.lang.builder.HashCodeBuilder

class DeMrnaAnnotation implements Serializable{
	String gplId
	String probeId
	String geneSymbol
	Long probesetId
	Long geneId
	String organism
	
	static mapping = {
		table 'DE_MRNA_ANNOTATION'
		version false
		id column: 'DE_MRNA_ANNOTATION_ID'
		columns {
			gplId column:'GPL_ID'
		   probeId column:'PROBE_ID'
		   geneSymbol column:'GENE_SYMBOL'
		   probesetId column:'PROBESET_ID'
		   geneId column:'GENE_ID'
		   organism column:'ORGANISM'
		   }
	   }
	static constraints = {
		geneSymbol(nullable:true)
		geneId(nullable:true)
	}
}
