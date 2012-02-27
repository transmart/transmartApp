<g:set var="ar" value="${analysisResult}" />
<g:set var="analysis" value="${ar.analysis}" />
<g:set var="anadivid" value="al_sig_${ar.analysis.id}" />
<g:set var="bmDivId" value="${ar.analysis.id+'_'+ar.experimentId}" />

<tr class="${(counter % 2) == 0 ? 'oddlightblue' : 'even'}">
	<td>
	<table width="100%" class="rnoborder">
		<tr>
			<td colspan="2">
			<table class="rnoborder" width="100%">
				<tr>
					<td width="70%">
					<g:set var="content" value="${analysis.longDescription}" />
					<div id="TrialDetail_${anadivid}_anchor">

					<!-- display TEA score if calculated -->
					<g:if test="${ar.teaScore!=null}">
					<span style="font-weight:bold;">
						[ <img alt="TEA" src="${resource(dir:'images',file:'tea_pot.ico')}" />
						<!--  regulation status -->
						<g:if test="${ar.bTeaScoreCoRegulated}">
							<span style="font-style: italic; color: red">
								<g:if test="${session.searchFilter.globalFilter.hasPathway()}">up-regulated</g:if><g:else>co-regulated</g:else>
							</span>
						</g:if>
						<g:else>
							<span style="font-style: italic; color: green">
								<g:if test="${session.searchFilter.globalFilter.hasPathway()}">down-regulated</g:if><g:else>anti-regulated</g:else>
							</span>
						</g:else>
						&nbsp;<g:formatNumber number="${ar.calcDisplayTEAScore()}" format="#0.000" />&nbsp;]
					</span>
					<br>
					</g:if>

					<!--  show trial -->
					<g:if test="${showTrial!=null && showTrial}">
					<a onclick="javascript:showDialog('TrialDetail_${anadivid}', { title: '${ar.experimentAccession}', url: '${createLink(action:'expDetail',id:ar.experimentId)}' });"
						onmouseover="delayedTask.delay(2000, showDialog, this,['TrialDetail_${anadivid}', { title: '${ar.experimentAccession}', url: '${createLink(action:'expDetail',id:ar.experimentId)}'}]);"
						onmouseout="delayedTask.cancel();">
						<img alt="Study Dtl" src="${resource(dir:'images',file:'view_detailed.png')}" style="vertical-align: top;" />&nbsp;<span style="font-weight: bold; color: #339933;">${ar.experimentAccession}</span></a>
					 -
					</g:if>

					<!-- analysis display -->
					<a onclick="showDialog('AnalysisDetail_${anadivid}', { title: '${analysis.shortDescription}', url: '${createLink(controller:'trial', action:'showAnalysis', id:ar.analysis.id)}'})"
						id="AnalysisDetail_${anadivid}_anchor">
    					<img alt="Analysis" src="${resource(dir:'images',file:'analysis.png')}" style="vertical-align: top;" /> ${content} </a>
					</div>
					</td>

					<!--  content links -->
					<td width="10%" align="right">&nbsp; <g:link class="normal" action="downloadanalysisexcel" id="${analysis.id}">
																<img alt="download analysis" src="${resource(dir:'images',file:'Excel-16.gif')}" />Excel</g:link>
					</td>

					<td width="10%" align="right">&nbsp;
					<!--  
					<sec:ifNotGranted roles="ROLE_PUBLIC_USER">
					<g:link class="normal" action="downloadanalysisgpe" id="${analysis.id}">
						<img alt="download analysis" src="${resource(dir:'images',file:'impex.png')}" />Pathway Studio</g:link>
					</sec:ifNotGranted>
					-->
					</td>
					<td width="10%" align="right">&nbsp;
						<g:if test="${analysis.files!=null && !analysis.files.isEmpty()}">
							<g:createFileLink content="${analysis.files.iterator().next().content}" displayLabel="Analysis File" />
						</g:if>
					</td>
					<!--  end of content -->
				</tr>

			  	<g:if test="${!ar.assayAnalysisValueList.isEmpty()}">
				<tr>
					<td colspan="3" width="100%">
						<a onclick="toggleDetail('${bmDivId}')">
							<div id="${bmDivId}_fclose" style="visibility: hidden; display: none; width: 16px;">
								<img alt="-" src="${resource(dir:'images',file:'folder-minus.gif')}" style="vertical-align: middle;" />
							</div>
							<div id="${bmDivId}_fopen" style="display: inline; width: 16px;">
								<img alt="+" src="${resource(dir:'images',file:'folder-plus.gif')}" style="vertical-align: middle;" />
							</div>
							<b>BioMarkers</b>
							<g:if test="${ar.teaScore!=null}">
								<g:if test="${ar.showTop()}">(top 5 signature/pathway genes matched):</g:if>
								<g:else>(${ar.size()} signature/pathway genes matched):</g:else>
							</g:if>
							<g:else>
								<g:if test="${ar.showTop()}">(top 5 of ${ar.getBioMarkerCount()}):</g:if>
								<g:else>(${ar.size()} of ${ar.analysis.dataCount}):</g:else>
							</g:else>
						</a>

						<!-- don't display in TEA view -->
						<g:if test="${ar.teaScore==null}">
							<br>
							<g:each in="${ar.getAnalysisValueSubList()}" status="ai" var="analysisvalue">
								<g:if test="${ai>0}">, </g:if>
								<g:createFilterDetailsLink id="${analysisvalue.bioMarker?.id}" label="${analysisvalue.bioMarker?.name}" type="gene" />
								<g:if test="${analysisvalue.analysisData?.foldChangeRatio!=null}"> (Fold Change:${analysisvalue.analysisData?.foldChangeRatio})
									<g:if test="${analysisvalue.analysisData?.foldChangeRatio>0}">
										<img alt="signature up" src="${resource(dir:'images',file:'up_arrow.PNG')}" />
									</g:if>
									<g:else>
										<img alt="signature down" src="${resource(dir:'images',file:'down_arrow.PNG')}" />
									</g:else>
								</g:if>
								<g:if test="${analysisvalue.analysisData.rValue!=null}"> (R Value:${analysisvalue.analysisData.rValue})</g:if>
								<g:if test="${analysisvalue.analysisData.rhoValue!=null}"> (Rho Value:${analysisvalue.analysisData.rhoValue})</g:if>
								<g:if test="${analysisvalue.analysisData.resultsValue!=null}"> (Result:${analysisvalue.analysisData.resultsValue})</g:if>
								<g:if test="${analysisvalue.analysisData.numericValue!=null}">&nbsp;&nbsp;&nbsp;<B>${analysisvalue.analysisData.numericValueCode}:</B>&nbsp;${analysisvalue.analysisData.numericValue}</g:if>
							</g:each>
						</g:if>

						<g:set var="genes" value="${ar.getGeneNames()}"/>						
					</td>
				</tr>
				</g:if>
			</table>
			</td>
		</tr>
		<tr>
			<td colspan="3">
				<div id="${bmDivId}_detail" class="gtb1" style="display: none;"><g:render template="/trial/analysisBiomarkerDetail" model="[analysisresult:ar]" /></div>
			</td>
		</tr>
	</table>
	</td>
</tr>

