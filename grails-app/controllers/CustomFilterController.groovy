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
  

/*
 * $Id: CustomFilterController.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 */
import org.transmart.GlobalFilter;
import org.transmart.searchapp.AuthUser;

import org.transmart.searchapp.SearchKeyword
import org.transmart.searchapp.CustomFilter
import org.transmart.searchapp.CustomFilterItem
import grails.converters.*

/**
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class CustomFilterController {

	def springSecurityService
    static def allowedMethods = [save:'POST', update:'POST']

    def list = {
        if(!params.max) params.max = 10
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        def customFilters = CustomFilter.findAllBySearchUserId(user.id)
        for (customFilter in customFilters) {
        	def keywordMap = createKeywordMapForCustomFilter(customFilter)
        	def summary = createSummaryWithLinks(keywordMap)
        	customFilter.summary = summary
        }
        [ customFilterInstanceList: customFilters ]
    }

    def delete = {
        def customFilterInstance = CustomFilter.get( params.id )
        if (!customFilterInstance) {
            flash.message = "CustomFilter not found with id ${params.id}"
            redirect(action:list)
        } else if (!canUpdate(customFilterInstance)) {
        	flash.message = "You are not authorized to delete the custom filter with ID ${params.id}."
            redirect(action:list)
        } else {
            customFilterInstance.delete()
            flash.message = "CustomFilter ${params.id} deleted"
            redirect(action:list, params:[ts:new Date().getTime()])
        }
    }

    def edit = {
        def customFilterInstance = CustomFilter.get( params.id )

        if (!customFilterInstance) {
            flash.message = "CustomFilter not found with id ${params.id}"
            redirect(action:list)
        } else if (!canUpdate(customFilterInstance)) {
        	flash.message = "You are not authorized to edit the custom filter with ID ${params.id}."
            redirect(action:list)
        } else {
        	def keywordMap = createKeywordMapForCustomFilter(customFilterInstance)
        	def summary = createSummaryWithLinks(keywordMap)
        	customFilterInstance.summary = summary
            return [ customFilterInstance : customFilterInstance ]
        }
    }

    def update = {
		params.privateFlag = (params?.privateFlag != "on") ? 'N' : 'Y'
        def customFilterInstance = CustomFilter.get( params.id )

        if (customFilterInstance) {
            customFilterInstance.properties = params
            if(!customFilterInstance.hasErrors() && customFilterInstance.save()) {
                flash.message = "CustomFilter ${params.id} updated"
                redirect(action:list, params:[ts:new Date().getTime(), lastFilterID:params.id])
            } else {
                render(view:'edit',model:[customFilterInstance:customFilterInstance])
            }
        } else {
            flash.message = "CustomFilter not found with id ${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
        def filter = new CustomFilter()
		filter.properties.searchUserId = user.id
		filter.properties.privateFlag = 'N'
		def map = createKeywordMapForGlobalFilter(session.searchFilter.globalFilter)
		filter.properties.summary = createSummaryWithLinks(map)
        return ['customFilterInstance':filter]
    }

    def save = {

	//	println("private flag:"+params.privateFlag);
		params.privateFlag = (params?.privateFlag != "on") ? 'N' : 'Y'
        def filter = new CustomFilter(params)
		//println(filter)
		def map = createKeywordMapForGlobalFilter(session.searchFilter.globalFilter)
        for (key in map.keySet()) {
        	def keywords = map[(key)]
        	for (keyword in keywords) {
	        	CustomFilterItem item = new CustomFilterItem()
	        	item.uniqueId = keyword.uniqueId
	        	item.bioDataType = keyword.dataCategory
	        	filter.addToItems(item)
        	}
        }
        if(!filter.hasErrors() && filter.save()) {
            flash.message = "CustomFilter ${filter.id} created"
            redirect(action:list, params:[ts:new Date().getTime(), lastFilterID:filter.id])
        }
        else {
            render(view:'create',model:[customFilterInstance:filter])
        }
    }

	boolean canUpdate(customFilter) {
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
		if (customFilter != null && customFilter.searchUserId == user.id) {
			return true
		}
		return false
	}

	boolean canSelect(customFilter) {
		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
		if (customFilter != null && (customFilter.privateFlag != 'Y' || customFilter.searchUserId == user.id)) {
			return true
		}
		return false
	}

	def createKeywordMapForGlobalFilter(GlobalFilter gfilter) {
        def map = [:]
        def list
        list = gfilter.getGeneFilters()
        if (list.size() > 0) {
        	map.put("GENE", list)
        }
        list = gfilter.getPathwayFilters()
        if (list.size() > 0) {
        	map.put("PATHWAY", list)
        }
        list = gfilter.getCompoundFilters()
        if (list.size() > 0) {
        	map.put("COMPOUND", list)
        }
        list = gfilter.getDiseaseFilters()
        if (list.size() > 0) {
        	map.put("DISEASE", list)
        }
        list = gfilter.getTrialFilters()
        if (list.size() > 0) {
        	map.put("TRIAL", list)
        }
        list = gfilter.getStudyFilters()
        if (list.size() > 0) {
        	map.put("STUDY", list)
        }
        list = gfilter.getGeneSignatureFilters()
        if (list.size() > 0) {
        	map.put("GENESIG", list)
        }
        list = gfilter.getTextFilters()
        if (list.size() > 0) {
        	map.put("TEXT", list)
        }

        return map
	}

	def createKeywordMapForCustomFilter(CustomFilter filter) {

		def map = [:]
		def uniqueIds = []
		for (item in filter.items) {
			def id = item.uniqueId
			if (item.bioDataType == "TEXT") {
				def list
				if (map.containsKey("TEXT")) {
					list = map["TEXT"]
				} else {
					list = []
					map["TEXT"] = list
				}
				def keyword = new SearchKeyword()
				keyword.keyword = id.substring(id.indexOf(":") + 1)
				keyword.id = -1
				keyword.uniqueId = id
				keyword.dataCategory = "TEXT"
				list.add(keyword)
			} else {
				uniqueIds.add(item.uniqueId)
			}
		}
		if (uniqueIds.size() > 0) {
			def keywords = SearchKeyword.findAllByUniqueIdInList(uniqueIds)
			for (keyword in keywords) {
				def list
				if (map.containsKey(keyword.dataCategory)) {
					list = map[(keyword.dataCategory)]
				} else {
					list = []
					map[(keyword.dataCategory)] = list
				}
				list.add(keyword)
			}
		}

		return map

	}

	/**
	 * Creates link to detatils for specified filter keyword.
	 */
	def createSummaryFilter(SearchKeyword keyword){

		def link = new StringBuilder()
		def type = keyword.dataCategory.toLowerCase()

		link.append("<nobr>")
		if (type == "text") {
			link.append(createFilterDetailsLink(id: keyword.keyword, label: keyword.keyword, type: type))
		} else {
			def label = keyword.keyword
			if (type == "pathway" && keyword.dataSource != null && keyword.dataSource != "") {
				label = keyword.dataSource + "-" + label
			}
			link.append(createFilterDetailsLink(id: keyword.bioDataId, label: label, type: type))
		}
		link.append("</nobr>")
		return link.toString()

	}

	/**
	 * Creates section in summary for given categories filters.
	 */
	def createSummarySection(category, keywordMap) {

		def section = new StringBuilder()
		def filters = keywordMap[(category)]
		for (filter in filters) {
			if (section.length() > 0) {
				section.append(" OR ")
			}
			section.append(createSummaryFilter(filter))
		}

		if (section.length() == 0) {
			return ""
		}

		def span = new StringBuilder()
		span.append("<span class=\"filter-item filter-item-")
		span.append(category.toLowerCase())
		span.append("\">")
		span.append(formatCategory(category))
		if (filters.size() > 1) {
			span.append("s")
		}
		span.append("&gt;&nbsp;</span>")
		span.append(section)

		return span.toString()

	}
	/**
	 * Creates summary of filters with links to details for filters.
	 */
	def createSummaryWithLinks(keywordMap) {

		def genes = createSummarySection("GENE", keywordMap)
		def pathways = createSummarySection("PATHWAY", keywordMap)
		def compounds = createSummarySection("COMPOUND", keywordMap)
		def diseases = createSummarySection("DISEASE", keywordMap)
		def trials = createSummarySection("TRIAL", keywordMap)
		def genesigs = createSummarySection("GENESIG", keywordMap)
		def studies = createSummarySection("STUDY", keywordMap)
		def texts = createSummarySection("TEXT", keywordMap)
		def summary = new StringBuilder()

		if (genes) {
			summary.append(genes)
		}

		if (summary.length() > 0 && pathways.length() > 0) {
			summary.append(" OR ")
		}
		summary.append(pathways)

		if (summary.length() > 0 && genesigs.length() > 0) {
			summary.append(" OR ")
		}
		summary.append(genesigs)

		if (summary.length() > 0 && compounds.length() > 0) {
			summary.append(" AND ")
		}
		summary.append(compounds)

		if (summary.length() > 0 && diseases.length() > 0) {
			summary.append(" AND ")
		}
		summary.append(diseases)

		if (summary.length() > 0 && trials.length() > 0) {
			summary.append(" AND ")
		}
		summary.append(trials)

		if (summary.length() > 0 && studies.length() > 0) {
			summary.append(" AND ")
		}
		summary.append(studies)

		if (summary.length() > 0 && texts.length() > 0) {
			summary.append(" AND ")
		}
		summary.append(texts)

		return summary.toString()

	}

	def formatCategory(category) {
		return category.substring(0, 1).toUpperCase() + category.substring(1).toLowerCase()
	}

}
