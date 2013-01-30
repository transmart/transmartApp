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
  


import org.jfree.util.Log;

import i2b2.Concept;

class ConceptService {
	def dataSource;
	
	public Concept getConceptByBaseCode(String baseCode) throws Exception {
		return Concept.findByBaseCode(baseCode);
	}
	
	public List<Concept> getChildrenConcepts(Concept concept) throws Exception {
		if (concept == null || concept.id == null || concept.getLevel() == null) return null;
		List<Concept> conceptList = 
			Concept.findAll("from Concept as c where c.fullName like :fullNameLike and level = :levelNew",
				[fullNameLike: concept.getFullName() + "%", levelNew: concept.getLevel().intValue() + 1]);
		return conceptList;
	}
	
}
