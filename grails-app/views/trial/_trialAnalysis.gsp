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

<!--  TEA presentation or single gene view -->
<g:if test="${trialresult.bioMarkerCt>1}"> 
	<!--  display significant analyses in open block -->
	<table style="background-color: #fffff;" width="100%">
		<g:tableHeaderToggle label="(${trialresult.analysisCount-trialresult.inSignificantAnalCount}) Significant TEA Analyses" divPrefix="significant_block" status="open" />
	
		<tbody id="significant_block_detail" style="display: block;">
		<g:set var="counter" value="${1}" />
		<g:each in="${trialresult.analysisResultList}" status="i" var="analysisResult">
			<g:if test="${analysisResult.bSignificantTEA}">
				<g:set var="counter" value="${counter+1}" />
				<g:render template="/trial/teaAnalysisSummary" model="[analysisResult: analysisResult, counter: counter, showTrial: showTrial]" />
			</g:if>
		</g:each>
		</tbody>
	</table>
	
	<!--  display insignificant analyses initially closed -->
	<g:if test="${trialresult.inSignificantAnalCount>0}">
	<table style="background-color: #fffff;" width="100%">
		<g:tableHeaderToggle label="(${trialresult.insigAnalResultList.size()}) Insignificant TEA Analyses" divPrefix="insignificant_block" />
	
		<tbody id="insignificant_block_detail" style="display: none;">	
		<g:each in="${trialresult.insigAnalResultList}" status="i" var="analysisResult">
			<g:render template="/trial/teaAnalysisSummary" model="[analysisResult: analysisResult, counter: i, showTrial: showTrial]" />
		</g:each>
		</tbody>
	</table>
	</g:if>
</g:if>
<g:else>
	<!--  display analyses without TEA -->
	<table style="background-color: #fffff;" width="100%">
		<tbody>	
		<g:each in="${trialresult.analysisResultList}" status="i" var="analysisResult">
			<g:render template="/trial/teaAnalysisSummary" model="[analysisResult: analysisResult, counter: i, showTrial: showTrial]" />
		</g:each>
		</tbody>
	</table>
</g:else>
