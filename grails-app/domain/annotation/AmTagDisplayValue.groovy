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
  

package annotation

import java.io.Serializable;


class AmTagDisplayValue implements Serializable {

	AmTagItem amTagItem
	String subjectUid
	String displayValue
	String objectType
	String objectUid
	Long objectId
	String uniqueId
	String codeTypeName
	
	static transients = ['uniqueId','codeTypeName']
	
	/**
	 * Use transient property to support unique ID for tagValue.
	 * @return tagValue's uniqueId
	 */
	String getUniqueId() {
		if (uniqueId == null) {
			uniqueId = objectUid
		}
		
		return uniqueId;
	}

	String getCodeTypeName() {
		if (codeTypeName == null) {
			codeTypeName = displayValue
		}
		
		return codeTypeName;
	}

	
		
	static mapping = {
		table 'am_tag_display_vw'
		version false
		cache true
		sort "value"
		id composite: ["subjectUid","objectUid","amTagItem"]
		amTagItem column: 'tag_item_id'

	}

	static constraints = {
	}

	static AmTagDisplayValue get(String subjectUid, long objectId) {
		find 'from AmTagDisplayValue where subjectUid=:subjectUid and objectId=:objectId',
			[subjectUid: subjectUid, objectId: objectId]
	}

	static boolean remove(String objectUid, long objectId, boolean flush = false) {
	//	AmTagDisplayValue instance = FmFolderAssociation.findByObjectUidAndFmFolder(objectUid, fmFolder)
	//	instance ? instance.delete(flush: flush) : false
		
		false
	}
	
	 static Collection<Object> findAllDisplayValue(String subjectUid, long amTagItemId) {
		findAll 'from AmTagDisplayValue where subjectUid=:subjectUid and amTagItem.id=:amTagItemId',
			[subjectUid: subjectUid, amTagItemId: amTagItemId]
		
		
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Subject UID: ").append(subjectUid).append(", ");
		sb.append("Object UID: ").append(objectUid).append(", ");
		sb.append("Display Value: ").append(displayValue);
		return sb.toString();
	}

}
