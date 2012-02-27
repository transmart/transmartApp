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