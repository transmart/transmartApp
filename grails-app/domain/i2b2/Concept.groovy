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
  

package i2b2

class Concept {
	Long id;
	
	String fullName;
	Integer level;
	String name;
	String synonym;
	String visualAttributes;
	Integer totalNum;
	String baseCode;
	String metaDataXml;
	String factTableColumn;
	String tableName;
	String columnName;
	String columnDataType;
	String operator;
	String dimCode;
	String comment;
	String toolTip;
	Date updateDate;
	Date downloadDate;
	Date importDate;
	String sourceSystem;
	String valueType;
	
	static mapping = {
		table 'i2b2'
		version false
		
		id generator: 'sequence', column: "i2b2_id", params:[sequence:'I2B2_ID_SEQ']
		fullName column: 'C_FULLNAME', type: 'string'
		level column: 'C_HLEVEL', type: 'integer'
		name column: 'C_NAME', type: 'string'
		synonym column: 'C_SYNONYM_CD', type: 'string'
		visualAttributes column: 'C_VISUALATTRIBUTES', type: 'string'
		totalNum column: 'C_TOTALNUM', type: 'integer'
		baseCode column: 'C_BASECODE', type: 'string'
		metaDataXml column: 'C_METADATAXML', type: 'string'
		factTableColumn column: 'C_FACTTABLECOLUMN', type: 'string'
		tableName column: 'C_TABLENAME', type: 'string'
		columnName column: 'C_COLUMNNAME', type: 'string'
		columnDataType column: 'C_COLUMNDATATYPE', type: 'string'
		operator column: 'C_OPERATOR', type: 'string'
		dimCode column: 'C_DIMCODE', type: 'string'
		comment column: 'C_COMMENT', type: 'string'
		toolTip column: 'C_TOOLTIP', type: 'string'
		updateDate column: 'UPDATE_DATE', type: 'date'
		downloadDate column: 'DOWNLOAD_DATE', type: 'date'
		importDate column: 'IMPORT_DATE', type: 'date'
		sourceSystem column: 'SOURCESYSTEM_CD', type: 'string'
		valueType column: 'VALUETYPE_CD', type: 'string'
	}
}
