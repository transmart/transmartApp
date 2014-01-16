<div class="search-result-info">
    <h1>Search Results</h1>${trials.size()} 
    
    <g:if test="${trials.size() == 1}">study </g:if>
    <g:else>studies </g:else>with ${analysisCount}
    <g:if test="${analysisCount > 1}">analyses</g:if>
    <g:else>analysis</g:else> in ${duration} 
    
</div>
<div class="search-results-table">
    <g:each in="${trials.entrySet()}" status="ti" var="trialresult">            
        <div class="${ (ti % 2) == 0 ? 'result-trial-odd' : 'result-trial-even'}" id="TrialDet_${trialresult.key}_anchor">                            
            <a href="#" onclick="javascript:showDetailDialog('${createLink(controller:'trial',action:'trialDetailByTrialNumber',id:trialresult.value.studyId)}', '${trialresult.value.studyId} Details', 400);">
               <span style="display:block; float:left;">
                   <img alt="" src="${resource(dir:'images',file:'view_detailed.png')}" />
               </span>
               <span class="result-trial-name"> ${trialresult.value.studyId}</span></a>: ${trialresult.value.title}
               <span class="result-analysis-label">
               <g:set 
                   var="ts" value="${Calendar.instance.time.time}"                 
                />       
               <a href="#" onclick="javascript:toggleDetailDiv('${trialresult.value.studyId}', '${createLink(controller:'RWG',action:'getTrialAnalysis',params:[id:trialresult.key,trialNumber:trialresult.value.studyId,unqKey:ts])}', '${trialresult.key}');">
                <img alt="expand/collapse" id="imgExpand_${trialresult.value.studyId}" src="${resource(dir:'images',file:'down_arrow_small2.png')}" style="display: inline;"/>                  
                      ${trialresult.value.analysisCount}
                      <g:if test="${trialresult.value.analysisCount > 1}">analyses found</g:if>
    				  <g:else>analysis found</g:else>
               </a>                                                                          
               </span>
               <div id="${trialresult.value.studyId}_detail"></div>
        </div> 
    </g:each>
</div>