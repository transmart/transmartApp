<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
-->

<table class="detail">
	<tbody>
		<g:each in="${analysisresult.getAnalysisValueSubList()}" status="adi" var="analysisvalue">
		<g:set var="data" value="${analysisvalue.analysisData}" />
	   	<g:set var="marker" value="${analysisvalue.bioMarker}" />
		<tr class="prop">
			<td valign="top" class="name" style="text-align: right; font-weight: bold;">${fieldValue(bean:analysisvalue,field:'bioMarker.name')}&nbsp;
			<!-- signature regulation status -->
			[&nbsp;
			<g:if test="${analysisvalue.valueMetric!= null && analysisvalue.valueMetric!=0}">
				<g:if test="${analysisvalue.valueMetric>0}"><img alt="signature up" src="${resource(dir:'images',file:'up_arrow.PNG')}" /></g:if>
				<g:else><img alt="signature down" src="${resource(dir:'images',file:'down_arrow.PNG')}" /></g:else>
			</g:if>

			<!--  removed NA diplay since is not application for non gene sig/list searches -->

			<!--  gene expression/regulation status -->
			<g:if test="${data.foldChangeRatio!= null}">
				<g:if test="${data.foldChangeRatio>=0}"><img alt="fc up" src="${resource(dir:'images',file:'up_arrow.PNG')}" /></g:if>
				<g:else><img alt="fc down" src="${resource(dir:'images',file:'down_arrow.PNG')}" /></g:else>
			</g:if>
			<g:else>NA</g:else>
			&nbsp;]&nbsp;</td>
			<g:if test="${data.featureGroupName!=null}"><td style="white-space: no-wrap;"><B>ProbeSet:</B>&nbsp;${fieldValue(bean:data,field:'featureGroupName')}</td></g:if>
			<td style="white-space: no-wrap;"><b>Gene:</b>&nbsp;<g:createFilterDetailsLink id="${marker?.id}" label="${marker?.name}" type="gene" /></td>
			<g:if test="${data.foldChangeRatio!=null}"><td style="white-space: no-wrap;"><B>Fold Change:</B>&nbsp;${fieldValue(bean:data,field:'foldChangeRatio')}</td></g:if>
			<g:if test="${data.rValue!=null}"><td style="white-space: no-wrap;"><B>RValue:</B>&nbsp;${fieldValue(bean:data,field:'rValue')}</td></g:if>
			<g:if test="${data.rawPvalue!=null}"><td style="white-space: no-wrap;"><B>p-Value:</B>&nbsp;${fieldValue(bean:data,field:'rawPvalue')}</td></g:if>
			<g:if test="${data.teaNormalizedPValue!=null}"><td style="white-space: no-wrap;"><B>TEA p-Value:</B>&nbsp;${fieldValue(bean:data,field:'teaNormalizedPValue')}</td></g:if>
			<g:if test="${dataadjustedPvalue!=null}"><td style="white-space: no-wrap;"><B>FDR p-Value:</B>&nbsp;${fieldValue(bean:data,field:'adjustedPvalue')}</td></g:if>
			<g:if test="${data.rhoValue!=null}"><td style="white-space: no-wrap;"><B>Rho-Value:</B>&nbsp;${fieldValue(bean:data,field:'rhoValue')}</td></g:if>
			<g:if test="${data.cutValue!=null}"><td style="white-space: no-wrap;"><B>Cut Value:</B>&nbsp;${fieldValue(bean:data,field:'cutValue')}</td></g:if>
			<g:if test="${data.resultsValue!=null}"><td style="white-space: no-wrap;"><B>Results Value:</B>&nbsp;${fieldValue(bean:data,field:'resultsValue')}</td></g:if>
			<g:if test="${data.numericValue!=null}"><td style="white-space: no-wrap;"><B>${fieldValue(bean:data,field:'numericValueCode')}:</B>&nbsp;${fieldValue(bean:data,field:'numericValue')}</td></g:if>
		</tr>
		</g:each>
	</tbody>
</table>