<div class="search-result-info">
	<table><tr><td>
    Search results:&nbsp;&nbsp;${experiments.size()} 
    
    <g:if test="${experiments.size() == 1}">study</g:if>
    <g:else>studies</g:else>
    
    with ${analysisCount}&nbsp;
     
    <g:if test="${analysisCount > 1}">analyses</g:if>
    <g:else>analysis</g:else>
    
    &nbsp;in ${duration} 
    </td>
    <td style="text-align: right"><div id="selectedAnalyses">&nbsp;</div></td>
	</tr>
    </table>
</div>
<div class="search-results-table">
    <g:each in="${experiments.entrySet()}" status="ti" var="experimentresult">        
        <div class="${ (ti % 2) == 0 ? 'result-trial-odd' : 'result-trial-even'}" id="TrialDet_${experimentresult.key.id}_anchor">
        	<g:set var="safeTitle">${experimentresult.key.title.replace("'", "\\'")}</g:set>
            <a href="#" onclick="javascript:showDetailDialog('${createLink(controller:'experimentAnalysis',action:'expDetail',id:experimentresult.key.id)}', '${experimentresult.key.accession}: ${safeTitle}', 600);">
               <span style="display:block; float:left;">
                   <img alt="" src="${resource(dir:'images',file:'view_detailed.png')}" />
               </span>
               <span class="result-trial-name"> ${experimentresult.key.accession}</span></a>: ${experimentresult.key.title}
               <span class="result-analysis-label">
               <g:set var="ts" value="${Calendar.instance.time.time}" />
               <a id="toggleDetail_${experimentresult.key.id}" href="#" onclick="javascript:toggleDetailDiv('${experimentresult.key.id}', '${createLink(controller:'RWG',action:'getTrialAnalysis',params:[id:experimentresult.key.id,trialNumber:experimentresult.key.id,unqKey:ts])}');">
                <img alt="expand/collapse" id="imgExpand_${experimentresult.key.id}" src="${resource(dir:'images',file:'down_arrow_small2.png')}" style="display: inline;"/>                  
                      <%--${experimentresult.value}
                      <g:if test="${experimentresult.value > 1}">analyses found</g:if>
    				  <g:else>analysis found</g:else>--%>
    				  Analyses
               </a>
               </span>
               <div id="${experimentresult.key.id}_detail" name="${experimentresult.key.id}" class="detailexpand"></div>
        </div> 
    </g:each>
</div>