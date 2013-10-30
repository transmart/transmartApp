<g:set var="analysisId" value="${analysis.id}" />
<g:set var="longDescription" value="${analysis.longDescription}" />
<g:set var="shortDescription" value="${analysis.shortDescription}" />
<g:set var="isTimeCourse" value="${analysis.isTimeCourse}" />


<div id="TrialDetail_${analysisId}_anchor" class="result-analysis" >
    <div class="analysis-name">
        <table class="analysis-table">
            <tr>
            	<td style="width:20px;">
					<input type="checkbox" name="${analysisId}" class="analysischeckbox" id="analysis_checkbox_${analysisId}" onclick="updateSelectedAnalyses();"/>
            	</td>
                <td style="width:20px;">
		          <g:form controller="RWG" name="AnalysisDetail_${analysisId}" id="AnalysisDetail_${analysisId}" action="doComparison">
		              <input type="hidden" id="analysis_results_${analysisId}_state" value="0" />
						
			          <a href="#" onclick="showDetailDialog('${createLink(controller:'trial', action:'showAnalysis', id:analysisId)}', '${analysis.name.replace("'", "\\'")}');">
	                   <img alt="Analysis" src="${resource(dir:'images',file:'analysis.png')}" style="vertical-align: top;margin-top: -2px;" /></a>                          
	              </g:form>
                </td>
                <td onclick="showVisualization('${analysisId}', false);" class="td-link">${analysis.name}</td>                
                <td onclick="showVisualization('${analysisId}', false);" style="text-align:right; vertical-align:middle"  class="td-link">
	                  <img alt="expand/collapse" id="imgExpand_${analysisId}" src="${resource(dir:'images',file:'down_arrow_small2.png')}" style="vertical-align: middle; padding-left:10px; padding-right:10px;"/>      
                </td>
            </tr>
        </table>
	</div>     
</div>
    
<div class="analysis_spacer">
    <div id="analysis_holder_${analysisId}" style="display: none;" class="analysis_holder">
	   <div id="visTabs_${analysisId}" class="analysis-tabs">
	       <ul>
	          <li><a href="#results_${analysisId}">Analysis Results</a></li>
	          <g:unless test="${analysis.assayDataType.equals('Gene Expression')}">
	          	<li><a href="#qqplot_${analysisId}" onclick="loadQQPlot('${analysisId}');">QQ Plot</a></li>
	          </g:unless>
	       </ul>
	       
			<div id="results_${analysisId}">
				<div class='vis-toolBar' >
					<div id="btnResultsExport_${analysisId}" class='vis-toolbar-item'><a href="${createLink([controller:'search', action:'getAnalysisResults', params:[export: true, analysisId: analysisId]])}"><img alt="" src="${resource(dir:'images',file:'internal-link.gif')}" /> Export as CSV</a></div>
					<div id="resultsExportOpts_${analysisId}" class='menuOptList' style="display:none;">
						<ul>
							<li onclick="exportResultsData('${analysisId}','data');">Export data (.csv)</li>
							<li onclick="exportResultsData('${analysisId}','image');">Export image (.png)</li>
						</ul>
					</div>
					
					<div id ="analysis_results_${analysisId}" class="heatmap_analysis" style="width:80%;margin: 0px auto;">
						<div id="analysis_results_table_${analysisId}_wrapper" class="dataTables_wrapper" role="grid">&nbsp;
						</div>
					</div>
					             
				</div>
			</div>	
           <g:unless test="${analysis.assayDataType.equals('Gene Expression')}">
		       <div id="qqplot_${analysisId}">
			      <div class='vis-toolBar' >
				     <div id="btnqqplotExport_${analysisId}" class='vis-toolbar-item' onclick="">
				     	<a href="" target="_blank" id="qqplot_export_${analysisId}">
				     		<img alt="" src="${resource(dir:'images',file:'internal-link.gif')}" /> Export as PNG
				     	</a>
				     </div>
			         
			         <div id ="qqplot_results_${analysisId}" class="heatmap_analysis"></div>
			         
	              </div>              
	           </div>
           </g:unless>	
        </div>
    </div>
</div>

