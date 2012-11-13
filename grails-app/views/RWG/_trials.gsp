<div class="search-result-info">
	<table><tr><td>
    Search results:&nbsp;&nbsp;${trials.size()} 
    
    <g:if test="${trials.size() == 1}">study</g:if>
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
    <g:each in="${trials.entrySet()}" status="ti" var="trialresult">        
        <div class="${ (ti % 2) == 0 ? 'result-trial-odd' : 'result-trial-even'}" id="TrialDet_${trialresult.key.id}_anchor">   
        	<g:set var="safeTitle">${trialresult.key.title.replace("'", "\\'")}</g:set>                         
            <a href="#" onclick="javascript:showDetailDialog('${createLink(controller:'trial',action:'expDetail',id:trialresult.key.id)}', '${trialresult.key.trialNumber}: ${safeTitle}', 600);">
               <span style="display:block; float:left;">
                   <img alt="" src="${resource(dir:'images',file:'view_detailed.png')}" />
               </span>
               <span class="result-trial-name"> ${trialresult.key.trialNumber}</span></a>: ${trialresult.key.title}
               <span class="result-analysis-label">
               <g:set var="ts" value="${Calendar.instance.time.time}" />       
               <a id="toggleDetail_${experimentresult.key.id}" href="#" onclick="javascript:toggleDetailDiv('${trialresult.key.trialNumber}', '${createLink(controller:'RWG',action:'getTrialAnalysis',params:[id:trialresult.key.id,trialNumber:trialresult.key.trialNumber,unqKey:ts])}');">
                <img alt="expand/collapse" id="imgExpand_${trialresult.key.trialNumber}" src="${resource(dir:'images',file:'down_arrow_small2.png')}" style="display: inline;"/>                  
                      <%--${trialresult.value}
                      <g:if test="${trialresult.value > 1}">analyses found</g:if>
    				  <g:else>analysis found</g:else>--%>
    				  Analyses
               </a>                                                                          
               </span>
               <div id="${trialresult.key.trialNumber}_detail" name="${trialresult.key.trialNumber}" class="detailexpand"></div>
        </div> 
    </g:each>
</div>