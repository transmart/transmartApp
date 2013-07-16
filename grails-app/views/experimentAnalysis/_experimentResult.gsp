<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see http://www.gnu.org/licenses/>.
  
 
-->

<g:setProvider library="prototype"/>
<g:if test="${searchresult?.result?.groupByExp}" ><div id='ptfilterresult'></g:if>
<g:else><div id='ptfilterresult_tea'></g:else>

<g:if test="${searchresult?.result==null || searchresult?.result?.analysisCount==0 }"><g:render template="/search/noResult"/></g:if>
<g:else>
	<g:set var="ear" value="${searchresult?.result?.expAnalysisResults[0]}"/>
	<g:set var="teaDisplay" value="${(ear!=null && ear.bioMarkerCt>1) ? true : false}"/>	
	
	<!-- results header -->
	<p style="font-weight:bold; font-size:11px;padding-left:5px;padding-bottom:5px; padding-top:5px;">
		<g:if test="${searchresult?.result?.groupByExp}" > Study result:&nbsp;&nbsp;${searchresult?.result?.analysisCount}
					<g:if test="${searchresult?.result?.analysisCount>1}">analyses</g:if>
					<g:else>analysis</g:else>
					&nbsp;from ${searchresult?.result?.expCount} experiment(s)	
		</g:if>
		<g:else> 		
		Analysis result:&nbsp;&nbsp;${searchresult?.result?.analysisCount}
					<g:if test="${searchresult?.result?.analysisCount>1}">analyses</g:if>
					<g:else>analysis</g:else>
					&nbsp;from ${searchresult?.result?.expCount} experiment(s)
					<g:if test="${teaDisplay}">&nbsp;[${ear.analysisCount-ear.inSignificantAnalCount} Significant TEA / ${ear.inSignificantAnalCount} Insignificant TEA]		
						<br><span style="color:red;">Note, only significant TEA Analyses are displayed!</span>
					</g:if>					
		</g:else>
	</p>
	
	<g:if test="${searchresult?.result?.groupByExp}" >
	
	<!--  paging tabs -->
	<div id="expListDiv">
  	<div class="paginateButtons">
   		<g:remotePaginate update="ptfilterresult" total="${searchresult?.experimentCount}" 
   			controller="experimentAnalysis" action="datasourceResult"  maxsteps="${grailsApplication.config.com.recomdata.search.paginate.maxsteps}" max="${grailsApplication.config.com.recomdata.search.paginate.max}"/>
    </div>
    
	<table width="100%" class="trborderbottom">
		<g:each in="${searchresult.result.expAnalysisResults}" status="ti" var="expAnalysisResult">
		<tr>
			<td width="100%" class="bottom">
				<table width="100%">
					<tr style="padding-bottom: 5px">
						<td style="padding: 5px 0px 10px 5px; margin-top: 5px;"><div id="TrialDet_${expAnalysisResult.experiment.id}_anchor">
						  	<a onclick="javascript:if(divIsEmpty('${expAnalysisResult.experiment.id}_detail')){var ldiv='${expAnalysisResult.experiment.id}_detail_loading';${remoteFunction(action:'getAnalysis',controller:'experimentAnalysis', id:expAnalysisResult.experiment.id, before:'toggleVisible(ldiv)',onComplete:'toggleVisible(ldiv)', update:expAnalysisResult.experiment.id+'_detail')}};toggleDetail('${expAnalysisResult.experiment.id}')">
								<div id="${expAnalysisResult.experiment.id}_fclose" style="visibility: hidden; display: none; width: 16px;">
									<img alt="" src="${resource(dir:'images',file:'folder-minus.gif')}" style="vertical-align: middle;" />
								</div>
								<div id="${expAnalysisResult.experiment.id}_fopen" style="display: inline; width: 16px;">
									<img alt="" src="${resource(dir:'images',file:'folder-plus.gif')}" style="vertical-align: middle;" />
								</div>
						 	</a>
							<a onclick="showDialog('TrialDet_${expAnalysisResult.experiment.id}', { title: '${expAnalysisResult.experiment.accession}', url: '${createLink(action:'expDetail', id:expAnalysisResult.experiment.id)}'});"
								onmouseover="delayedTask.delay(2000, showDialog, this,['TrialDet_${expAnalysisResult.experiment.id}', { title: '${expAnalysisResult.experiment.id}', url: '${createLink(action:'expDetail', id:expAnalysisResult.experiment.id)}' }]);"
								onmouseout="delayedTask.cancel();">
								<img alt="" src="${resource(dir:'images',file:'view_detailed.png')}" style="vertical-align: top;" />
								<b><span style="color: #339933;">${expAnalysisResult.experiment.accession}: </span>&nbsp;&nbsp;${expAnalysisResult.experiment.title}</b>
							</a>
							<br>
							&nbsp;&nbsp;&nbsp; - ${expAnalysisResult.analysisCount}
							<g:if test="${expAnalysisResult.analysisCount>1}">analyses found</g:if>
							<g:else>analysis found</g:else>
							&nbsp;&nbsp;&nbsp;
							<g:if test="${!expAnalysisResult.experiment.files.isEmpty() }">
								<g:set var="fcount" value="${0}" />
							 	<g:each in="${expAnalysisResult.experiment.files}" var="plan">
							 		<g:if test="${plan.content.type !='Experiment Web Link'}">
										<g:set var="fcount" value="${fcount++}" />
										<g:if test="${fcount > 1}">, </g:if>
								 	  	<g:createFileLink content="${plan.content}" displayLabel="${plan.type}" />
							 	  	</g:if>
							 	</g:each>
							</g:if>
						</td>
					</tr>
					<tr>
						<td style="padding-left: 20px;">
							<g:waitIndicator divId="${expAnalysisResult.experiment.id}_detail_loading" />
							<div id="${expAnalysisResult.experiment.id}_detail" class="gtb1" style="display: none;"></div>
						</td>
					</tr>
				</table>
			</td>
		</tr>
		</g:each>
	</table>
	</div>
	
	</g:if>
	<g:else>
		<!--  paging tabs -->	
		<div id="analysisListDiv">	
	  	<div class="paginateButtons">
	   		<g:remotePaginate update="ptfilterresult_tea" controller="experimentAnalysis" action="pageTEAAnalysisView" 
	   				total="${ear.analysisCount-ear.inSignificantAnalCount}" max="${grailsApplication.config.com.recomdata.search.paginate.max}"/>
	    </div>	
	
		<!-- display Analyses with or without TEA score -->
		<table style="background-color: #fffff;" width="100%">			
			<tbody>
			<g:set var="counter" value="${1}" />
			<g:each in="${ear.pagedAnalysisList}" status="i" var="analysisResult">
				<g:if test="${!teaDisplay || analysisResult.bSignificantTEA}">
					<g:set var="counter" value="${counter+1}" />
					<g:render template="/trial/teaAnalysisSummary" model="[ analysisResult: analysisResult, counter: counter, showTrial: true ]" />
				</g:if>
			</g:each>
			</tbody>
		</table>	
		</div>
	</g:else>
		
</g:else>
</div>
