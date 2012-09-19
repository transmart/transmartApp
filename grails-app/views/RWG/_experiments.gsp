<div class="search-result-info">
    Search results:&nbsp;&nbsp;${experiments.size()} 
    
    <g:if test="${experiments.size() == 1}">study</g:if>
    <g:else>studies</g:else>
    
    with ${analysisCount}&nbsp;
     
    <g:if test="${analysisCount > 1}">analyses</g:if>
    <g:else>analysis</g:else>
    
    &nbsp;in ${duration} 
    
</div>
<div class="search-results-table">
    <g:each in="${experiments.entrySet()}" status="ti" var="experimentresult">        
        <div class="${ (ti % 2) == 0 ? 'result-trial-odd' : 'result-trial-even'}" id="TrialDet_${experimentresult.key.id}_anchor">
            <a href="#" onclick="javascript:showDetailDialog('${createLink(controller:'experiment',action:'expDetail',id:experimentresult.key.id)}', '${experimentresult.key.id} Details', 650);">
               <span style="display:block; float:left;">
                   <img alt="" src="${resource(dir:'images',file:'view_detailed.png')}" />
               </span>
               <span class="result-trial-name"> ${experimentresult.key.id}</span></a>: ${experimentresult.key.title}
               <span class="result-analysis-label">
               <g:set 
                   var="ts" value="${Calendar.instance.time.time}"                 
                />
               <a href="#" onclick="javascript:toggleDetailDiv('${experimentresult.key.id}', '${createLink(controller:'RWG',action:'getTrialAnalysis',params:[id:experimentresult.key.id,trialNumber:experimentresult.key.id,unqKey:ts])}');">
                <img alt="expand/collapse" id="imgExpand_${experimentresult.key.id}" src="${resource(dir:'images',file:'down_arrow_small2.png')}" style="display: inline;"/>                  
                      ${experimentresult.value}
                      <g:if test="${experimentresult.value > 1}">analyses found</g:if>
    				  <g:else>analysis found</g:else>
               </a>                                                                          
               </span>
               <div id="${experimentresult.key.id}_detail"></div>
        </div> 
    </g:each>
</div>