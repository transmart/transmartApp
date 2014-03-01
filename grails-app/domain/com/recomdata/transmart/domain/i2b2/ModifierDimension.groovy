package com.recomdata.transmart.domain.i2b2;

import java.sql.Clob;

class ModifierDimension {

	String id 
	String modifierPath
	String nameChar
	Clob modifierBlob
	Date updateDate
	Date downloadDate
	Date importData
	String sourcesystemCd
	Long uploadId
	Long modifierLevel
	String modifierNodeType
		
	static mapping = {
		table 'I2B2DEMODATA.MODIFIER_DIMENSION'
		version false
		
		id column: 'MODIFIER_CD', type: 'string'
		modifierPath column: 'MODIFIER_PATH', type: 'string'
		nameChar column: 'NAME_CHAR', type: 'string'
		modifierBlob column: 'MODIFIER_BLOB', type: 'string'
		updateDate column: 'UPDATE_DATE', type: 'string'
		downloadDate column: 'DOWNLOAD_DATE', type: 'string'
		importData column: 'IMPORT_DATE', type: 'string'
		sourcesystemCd column: 'SOURCESYSTEM_CD', type: 'string'
		uploadId column: 'UPLOAD_ID', type: 'string'
		modifierLevel column: 'MODIFIER_LEVEL', type: 'integer'
		modifierNodeType column: 'MODIFIER_NODE_TYPE', type: 'string'
	}


}