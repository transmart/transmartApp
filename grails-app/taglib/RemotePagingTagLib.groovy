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
  

import org.springframework.validation.Errors;
import org.springframework.context.NoSuchMessageException;
import org.springframework.web.servlet.support.RequestContextUtils as RCU;
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU
import com.opensymphony.module.sitemesh.PageParserSelector
import com.opensymphony.module.sitemesh.Factory
import org.codehaus.groovy.grails.web.pages.GroovyPagesTemplateEngine
import org.springframework.web.context.ServletConfigAware
import javax.servlet.ServletConfig
import org.springframework.beans.factory.InitializingBean;
import org.codehaus.groovy.grails.web.sitemesh.FactoryHolder


class RemotePagingTagLib {

	/**
	 * Creates next/previous links to support pagination for the current controller
	 *
	 * 
	 */
	def remotePaginate = { attrs ->

	  def writer = out
        if(attrs.total == null)
            throwTagError("Tag [paginate] is missing required attribute [total]")
		def messageSource = grailsAttributes.getApplicationContext().getBean("messageSource")
		def locale = RCU.getLocale(request)
		
		def total = attrs.total.toInteger()
		def action = (attrs.action ? attrs.action : (params.action ? params.action : "search"))
		def offset = params.offset?.toInteger()
		def max = params.max?.toInteger()
		def maxsteps = (attrs.maxsteps ? attrs.maxsteps.toInteger() : 10)

		if(!offset) offset = (attrs.offset ? attrs.offset.toInteger() : 0)
		if(!max) max = (attrs.max ? attrs.max.toInteger() : 10)

		def linkParams = [offset:offset - max, max:max]
		if(params.sort) linkParams.sort = params.sort
		if(params.order) linkParams.order = params.order
		if(attrs.params) linkParams.putAll(attrs.params)
		if(params.searchStr) linkParams.searchStr = params.searchStr
		if(params.searchDate) linkParams.searchDate = params.searchDate
		if(params.searchField) linkParams.searchField = params.searchField


		def linkTagAttrs = [action:action,'update':attrs.update]
		if(attrs.controller) {
			linkTagAttrs.controller = attrs.controller
		}
		if(attrs.id!=null) {
			linkTagAttrs.id = attrs.id
		}
		linkTagAttrs.params = linkParams
	
		// determine paging variables
		def steps = maxsteps > 0
		int currentstep = (offset / max) + 1
		int firststep = 1
		int laststep = Math.round(Math.ceil(total / max))

		// display previous link when not on firststep
		if(currentstep > firststep) {
			linkTagAttrs.class = 'prevLink'
			writer << remoteLink(linkTagAttrs.clone()) {
				(attrs.prev ? attrs.prev : messageSource.getMessage('paginate.prev', null, messageSource.getMessage('default.paginate.prev', null, 'Previous', locale), locale))
			 }
		}

		// display steps when steps are enabled and laststep is not firststep
		if(steps && laststep > firststep) {
			linkTagAttrs.class = 'step'

			// determine begin and endstep paging variables
			int beginstep = currentstep - Math.round(maxsteps / 2) + (maxsteps % 2)
			int endstep = currentstep + Math.round(maxsteps / 2) - 1

			if(beginstep < firststep) {
				beginstep = firststep
				endstep = maxsteps
			}
			if(endstep > laststep) {
				beginstep = laststep - maxsteps + 1
				if(beginstep < firststep) {
					beginstep = firststep
				}
				endstep = laststep
			}

			// display firststep link when beginstep is not firststep
			if(beginstep > firststep) {
				linkParams.offset = 0
				writer << remoteLink(linkTagAttrs.clone()) {firststep.toString()}
				writer << '<span class="step">..</span>'
			}

			// display paginate steps
			(beginstep..endstep).each { i ->
				if(currentstep == i) {
					writer << "<span class=\"currentStep\">${i}</span>"
				}
				else {
					linkParams.offset = (i - 1) * max
					writer << remoteLink(linkTagAttrs.clone()) {i.toString()}
				}
			}

			// display laststep link when endstep is not laststep
			if(endstep < laststep) {
				writer << '<span class="step">..</span>'
				linkParams.offset = (laststep -1) * max
				writer << remoteLink(linkTagAttrs.clone()) { laststep.toString() }
			}
		}

		// display next link when not on laststep
		if(currentstep < laststep) {
			linkTagAttrs.class = 'nextLink'
			linkParams.offset = offset + max
			writer << remoteLink(linkTagAttrs.clone()) {
				(attrs.next ? attrs.next : messageSource.getMessage('paginate.next', null, messageSource.getMessage('default.paginate.next', null, 'Next', locale), locale))
			}
		}

	}
	
	/**
	 * Creates ABC DEF ... links to support alpha pagination for the current controller
	 *
	 * 
	 */
	def remoteAlphaPaginate = { attrs ->

	    def steps = [ "A-C", "D-F", "G-I", "J-L", "M-O", "P-R", "S-V", "W-Z", "Other" ]
	  	def writer = out
		def messageSource = grailsAttributes.getApplicationContext().getBean("messageSource")
		def locale = RCU.getLocale(request)
		def action = (attrs.action ? attrs.action : (params.action ? params.action : "search"))
		def update = attrs.update
		def currentstep = params.step ? params.step : "A-C"

		def linkParams = [currentstep:currentstep]
	    if(attrs.params) linkParams.putAll(attrs.params)
		if(params.searchStr) linkParams.searchStr = params.searchStr
		if(params.searchDate) linkParams.searchDate = params.searchDate
		if(params.searchField) linkParams.searchField = params.searchField

		def linkTagAttrs = [action:action,'update':attrs.update]
		if(attrs.controller) {
			linkTagAttrs.controller = attrs.controller
		}
		if(attrs.id!=null) {
			linkTagAttrs.id = attrs.id
		}
		linkTagAttrs.params = linkParams
		linkTagAttrs.class = 'step'
		if (update != null) {
			linkTagAttrs.before = "toggleVisible('" + update + "_loading'); toggleVisible('" + update + "');"
			linkTagAttrs.onComplete = "toggleVisible('" + update + "'); toggleVisible('" + update + "_loading');"
		}

		for (step in steps) {
			if (currentstep == step) {
				writer << "<span class=\"currentStep\">${step}</span>"
			} else {
				linkParams.step = step
				writer << remoteLink(linkTagAttrs.clone()) {step}
			}
		}
		
	}

	/**
	 * Renders a sortable column to support sorting in list views
	 *
	 * Attributes:
	 *
	 * property - name of the property relating to the field
	 * defaultOrder (optional) - default order for the property; choose between asc (default if not provided) and desc
	 * title (optional*) - title caption for the column
	 * titleKey (optional*) - title key to use for the column, resolved against the message source
	 * params (optional) - a map containing request parameters
	 * action (optional) - the name of the action to use in the link, if not specified the list action will be linked
	 * Attribute title or titleKey is required. When both attributes are specified then titleKey takes precedence,
	 * resulting in the title caption to be resolved against the message source. In case when the message could
	 * not be resolved, the title will be used as title caption.
	 *
	 * Examples:
	 *
	 * <g:sortableColumn property="title" title="Title" />
	 * <g:sortableColumn property="title" title="Title" style="width: 200px" />
	 * <g:sortableColumn property="title" titleKey="book.title" />
	 * <g:sortableColumn property="releaseDate" defaultOrder="desc" title="Release Date" />
	 * <g:sortableColumn property="releaseDate" defaultOrder="desc" title="Release Date" titleKey="book.releaseDate" />
	 */
	def remoteSortableColumn = { attrs ->
		def writer = out
		if(!attrs.property)
			throwTagError("Tag [sortableColumn] is missing required attribute [property]")

		if(!attrs.title && !attrs.titleKey)
			throwTagError("Tag [sortableColumn] is missing required attribute [title] or [titleKey]")

		//println "sortable - > " + attrs	
		//println "params - > " + params	

		def property = attrs.remove("property")
		def action = attrs.action ? attrs.remove("action") : (params.action ? params.action : "list")

		def defaultOrder = attrs.remove("defaultOrder")
		if(defaultOrder != "desc") defaultOrder = "asc"

		// current sorting property and order
		def sort = params.sort	
		def order = params.order

		// add sorting property and params to link params
		def linkParams = [sort:property]
		if(params.id) linkParams.put("id",params.id)
		if(attrs.params) linkParams.putAll(attrs.remove("params"))

		// determine and add sorting order for this column to link params
		attrs.class = "sortable"
		if(property == sort) {
			attrs.class = attrs.class + " sorted " + order
			if(order == "asc") {
				linkParams.order = "desc"
			}
			else {
				linkParams.order = "asc"
			}
		}
		else {
			linkParams.order = defaultOrder
		}
		if (params.auditSearchStr){
		    linkParams.put('logSearchStr',params.auditSearchStr)
		}
		if (params.logSearchStr){
		    linkParams.put('logSearchStr',params.logSearchStr)
		}
		if (params.searchDate){
		    linkParams.put('searchDate',params.searchDate)
		}
		if (params.searchField){
		    linkParams.put('searchField',params.searchField)
		}
		// determine column title
		def title = attrs.remove("title")
		def titleKey = attrs.remove("titleKey")
		if(titleKey) {
			if(!title) title = titleKey
			def messageSource = grailsAttributes.getApplicationContext().getBean("messageSource")
			def locale = RCU.getLocale(request)
			title = messageSource.getMessage(titleKey, null, title, locale)
		}

		writer << "<th "
		// process remaining attributes
		attrs.each { k, v ->
			writer << "${k}=\"${v.encodeAsHTML()}\" "
		}
		writer << ">${remoteLink(action:action, 'update':attrs.update , params:linkParams) { title }}</th>"
	}


    /**
      * A simple date picker modified to be used by the servicesSchedule UI.
      * The only difference is that we're dislpaying minutes as 0,15,30,45
      * eg. <g:datePicker name="myDate" value="${new Date()}" />
      */
      def datePickerServices = {attrs ->
          def xdefault = attrs['default']
          if (xdefault == null) {
              xdefault = new Date()
          } else if (xdefault.toString() != 'none') {
              if (xdefault instanceof String) {
                  xdefault = DateFormat.getInstance().parse(xdefault)
              }else if(!(xdefault instanceof Date)){
                  throwTagError("Tag [datePicker] requires the default date to be a parseable String or a Date")
              }
          } else {
              xdefault = null
          }

          def value = attrs['value']
          if (value.toString() == 'none') {
              value = null
          } else if (!value) {
              value = xdefault
          }
          def name = attrs['name']
          def id = attrs['id'] ? attrs['id'] : name

          def noSelection = attrs['noSelection']
          if (noSelection != null)
          {
              noSelection = noSelection.entrySet().iterator().next()
          }

          def years = attrs['years']

          final PRECISION_RANKINGS = ["year": 0, "month": 10, "day": 20, "hour": 30, "minute": 40]
          def precision = (attrs['precision'] ? PRECISION_RANKINGS[attrs['precision']] : PRECISION_RANKINGS["minute"])

          def day
          def month
          def year
          def hour
          def minute
          def dfs = new java.text.DateFormatSymbols(RCU.getLocale(request))

          def c = null
          if (value instanceof Calendar) {
              c = value
          }
          else if (value != null) {
              c = new GregorianCalendar();
              c.setTime(value)
          }

          if (c != null) {
              day = c.get(GregorianCalendar.DAY_OF_MONTH)
              month = c.get(GregorianCalendar.MONTH)
              year = c.get(GregorianCalendar.YEAR)
              hour = c.get(GregorianCalendar.HOUR_OF_DAY)
              minute = c.get(GregorianCalendar.MINUTE)
          }

          if (years == null) {
              def tempyear
              if (year == null) {
                  // If no year, we need to get current year to setup a default range... ugly
                  def tempc = new GregorianCalendar()
                  tempc.setTime(new Date())
                  tempyear = tempc.get(GregorianCalendar.YEAR)
              } else {
                  tempyear = year
              }
              years = (tempyear - 100)..(tempyear + 100)
          }

          out << "<input type=\"hidden\" name=\"${name}\" value=\"struct\" />"

          // create day select
          if (precision >= PRECISION_RANKINGS["day"]) {
              out.println "<select name=\"${name}_day\" id=\"${id}_day\">"

              if (noSelection) {
                  renderNoSelectionOption(noSelection.key, noSelection.value, '')
                  out.println()
              }

              for (i in 1..31) {
                  out.println "<option value=\"${i}\""
                  if (i == day) {
                      out.println " selected=\"selected\""
                  }
                  out.println ">${i}</option>"
              }
              out.println '</select>'
          }

          // create month select
          if (precision >= PRECISION_RANKINGS["month"]) {
              out.println "<select name=\"${name}_month\" id=\"${id}_month\">"

              if (noSelection) {
                  renderNoSelectionOption(noSelection.key, noSelection.value, '')
                  out.println()
              }

              dfs.months.eachWithIndex {m, i ->
                  if (m) {
                      def monthIndex = i + 1
                      out << "<option value=\"${monthIndex}\""
                      if (month == i) out << " selected=\"selected\""
                      out << '>'
                      out << m
                      out.println '</option>'
                  }
              }
              out.println '</select>'
          }

          // create year select
          if (precision >= PRECISION_RANKINGS["year"]) {
              out.println "<select name=\"${name}_year\" id=\"${id}_year\">"

              if (noSelection) {
                  renderNoSelectionOption(noSelection.key, noSelection.value, '')
                  out.println()
              }

              for (i in years) {
                  out.println "<option value=\"${i}\""
                  if (i == year) {
                      out.println " selected=\"selected\""
                  }
                  out.println ">${i}</option>"
              }
              out.println '</select>'
          }

          // do hour select
          if (precision >= PRECISION_RANKINGS["hour"]) {
              out.println "<select name=\"${name}_hour\" id=\"${id}_hour\">"

              if (noSelection) {
                  renderNoSelectionOption(noSelection.key, noSelection.value, '')
                  out.println()
              }

              for (i in 0..23) {
                  def h = '' + i
                  if (i < 10) h = '0' + h
                  out << "<option value=\"${h}\" "
                  if (hour == h.toInteger()) out << "selected=\"selected\""
                  out << '>' << h << '</option>'
                  out.println()
              }
              out.println '</select> :'

              // If we're rendering the hour, but not the minutes, then display the minutes as 00 in read-only format
              if (precision < PRECISION_RANKINGS["minute"]) {
                  out.println '00'
              }
          }

          // do minute select
          if (precision >= PRECISION_RANKINGS["minute"]) {
              out.println "<select name=\"${name}_minute\" id=\"${id}_minute\">"

              if (noSelection) {
                  renderNoSelectionOption(noSelection.key, noSelection.value, '')
                  out.println()
              }

              for (i in [0,15,30,45]) {
                  def m = '' + i
                  if (i < 10) m = '0' + m
                  out << "<option value=\"${m}\" "
                  if (minute == m.toInteger()) out << "selected=\"selected\""
                  out << '>' << m << '</option>'
                  out.println()
              }
              out.println '</select>'
          }
      }

}
