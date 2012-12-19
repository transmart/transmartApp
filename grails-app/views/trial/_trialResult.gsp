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
<g:setProvider library="prototype"/>
<g:if test="${searchresult?.result?.groupByExp}" ><div id='gfilterresult'></g:if>
<g:else><div id='gfilterresult_tea'></g:else>

<g:if test="${searchresult?.result==null || searchresult.result?.analysisCount==0 }">
	<g:render template="/search/noResult" />
</g:if>
<g:else>
	<p style="font-weight:bold; font-size:11px;padding-left:5px;padding-bottom:5px; padding-top:5px;">
	  	<g:if test="${searchresult?.result?.groupByExp}" > Study result:&nbsp;&nbsp;${searchresult?.result?.expCount} clinical trial(s) with ${searchresult?.result?.analysisCount}&nbsp;
				<g:if test="${searchresult?.result?.analysisCount>1}">analyses</g:if>
				<g:else>analysis</g:else>
	  	</g:if>
		<g:else> Analysis result:&nbsp;&nbsp;${searchresult?.result?.analysisCount}&nbsp;
				<g:if test="${searchresult?.result?.analysisCount>1}">analyses</g:if>
				<g:else>analysis</g:else>
				from ${searchresult?.result?.expCount} clinical trial(s)
		</g:else>
	</p>
	<g:if test="${searchresult?.result?.groupByExp}" >
	<div id="expListDivtrial">
  	<div class="paginateButtons">
    	<g:remotePaginate update="gfilterresult" total="${searchresult?.trialCount}" controller="trial" action="datasourceTrial" maxsteps="${grailsApplication.config.com.recomdata.search.paginate.maxsteps}" max="${grailsApplication.config.com.recomdata.search.paginate.max}"/>
    </div>
	
	<table width="100%" class="trborderbottom">
	<g:each in="${searchresult.result.expAnalysisResults}" status="ti" var="trialresult">
		<tr>
			<td width="100%" class="bottom">
				<table width="100%" >
					<tr>
						<td style="padding: 5px 0px 5px 5px; margin-top: 5px;"><div id="TrialDet_${trialresult.trial.id}_anchor">
						  	<a onclick="javascript:if(divIsEmpty('${trialresult.trial.trialNumber}_detail')){ var ldiv='${trialresult.trial.trialNumber}_detail_loading'; ${remoteFunction(action:'getTrialAnalysis',controller:'trial', id:trialresult.trial.id, before:'toggleVisible(ldiv);', onComplete:'toggleVisible(ldiv);', update:trialresult.trial.trialNumber+'_detail')};};toggleDetail('${trialresult.trial.trialNumber}')">
								<div id="${trialresult.trial.trialNumber}_fclose" style="visibility: hidden; display: none; width: 16px;">
									<img alt="" src="${resource(dir:'images',file:'folder-minus.gif')}" style="vertical-align: middle;" />
								</div>
								<div id="${trialresult.trial.trialNumber}_fopen" style="display: inline; width: 16px;">
									<img alt="" src="${resource(dir:'images',file:'folder-plus.gif')}" style="vertical-align: middle;" />
								</div>
						 	</a>
							<a onclick="javascript:showDialog('TrialDet_${trialresult.trial.id}', { title: '${trialresult.trial.trialNumber}', url: '${createLink(action:'expDetail',id:trialresult.trial.id)}' });"
								onmouseover="delayedTask.delay(2000, showDialog, this,['TrialDet_${trialresult.trial.id}', { title: '${trialresult.trial.trialNumber}', url: '${createLink(action:'expDetail',id:trialresult.trial.id)}'}]);"
								onmouseout="delayedTask.cancel();">
								<img alt="" src="${resource(dir:'images',file:'view_detailed.png')}" style="vertical-align: top;" />
								<b><span style="color: #339933;"> ${trialresult.trial.trialNumber}</span>:&nbsp;&nbsp;${trialresult.trial.title}</b>
							</a>
							<br>
							&nbsp;&nbsp;&nbsp;- ${trialresult.analysisCount}
							<g:if test="${trialresult.analysisCount>1}">analyses found</g:if>
							<g:else>analysis found</g:else>
							<br>
							&nbsp;&nbsp;&nbsp;
							<g:set var="pt" value="${trialresult.getProtocol()}"/>
							<g:if test="${pt != null}">
							  	<g:createFileLink content="${pt.content}" displayLabel="Protocol" />
							</g:if>
							<g:else>
								<b>Protocol</b>: not available
							</g:else>
							&nbsp;&nbsp;&nbsp;|
							<g:set var="hasFiles" value="${false}" />
						 	<g:each in="${trialresult.getFiles()}" status="fileCount" var="file">
						 	  	<g:if test="${fileCount > 0}">, </g:if><g:else><g:set var="hasFiles" value="${true}" /></g:else>
						 	 	<g:createFileLink content="${file.content}" displayLabel="${file.type}"/>
						 	</g:each>
							<g:if test="${!hasFiles}">
								<b>Analysis plan</b>: not available
							</g:if>
						</td>
					</tr>
					<tr>
						<td style="padding-left: 20px;">
							<div id="${trialresult.trial.trialNumber}_detail_loading" style="display:none">
			                   <img  src="${resource(dir: 'images', file: 'loader-small.gif')}" alt=""/>Loading...
							</div>
							<div id="${trialresult.trial.trialNumber}_detail" class="gtb1" style="display: none;"></div>
						</td>
					</tr>
				</table>
			</td>
		</tr>

	</g:each>
	</table>
	</g:if>
	<g:else>
		<g:render template="/trial/trialAnalysis" model="[trialresult:searchresult?.result?.expAnalysisResults[0],showTrial:true]" />
	</g:else>
</g:else>

</div>
