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



class AmTagValue {
	
	Long id
	String value
	String uniqueId
	
		
	static transients = ['uniqueId']
	
//	static belongsTo=[amTagItem: AmTagItem]
	
	/**
	 * Use transient property to support unique ID for tagValue.
	 * @return tagValue's uniqueId
	 */
	String getUniqueId() {
		if (uniqueId == null) {
			if(id)
			{
				AmData data = AmData.get(id);
				if (data != null) {
					uniqueId = data.uniqueId
					return data.uniqueId;
				}
				return null;
			}
			else
			{
				return null;
			}
		}
		return uniqueId;
	}
		
	/**
	 * Find tagValue by its uniqueId
	 * @param uniqueId
	 * @return tagValue with matching uniqueId or null, if match not found.
	 */
	static AmTagValue findByUniqueId(String uniqueId) {
		AmTagValue tagValue;
		AmData data = AmData.findByUniqueId(uniqueId);
		if (data != null) {
			tagValue = AmTagValue.get(data.id);
		}
		return tagValue;
	}
	
	
	static mapping = {
		table 'am_tag_value'
		version false
		cache true
		sort "value"
		columns { id column:'tag_value_id' }
//		amTagItem joinTable: [name: 'am_tag_template',  key:'tag_item_id', column: 'tag_value_id'], lazy: false
//		amTagItem column: 'tag_item_id'

	}

	static constraints = {
		value(maxSize:2000)
	}

}
