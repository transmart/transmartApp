<g:set var="analysisId" value="${analysis.id}" />
<g:set var="longDescription" value="${analysis.longDescription}" />
<g:set var="shortDescription" value="${analysis.shortDescription}" />
<g:set var="isTimeCourse" value="${analysis.isTimeCourse}" />


<div id="TrialDetail_${analysisId}_anchor" class="result-analysis" >
    <div class="analysis-name">
        <table class="analysis-table">
            <tr>
                <td style="width:20px;">
		          <g:form controller="RWG" name="AnalysisDetail_${analysisId}" id="AnalysisDetail_${analysisId}" action="doComparison">
		            <!--   <input type="checkbox" name="Analysis:" + ${counter} onchange="updateAnalysisCount(this.checked);"/>   -->
		              <input type="hidden" id="analysisDiv_${analysisId}_state" value="0" />
			          <a href="#" onclick="showDetailDialog('${createLink(controller:'trial', action:'showAnalysis', id:analysisId)}', '${shortDescription}');">
	                   <img alt="Analysis" src="${resource(dir:'images',file:'analysis.png')}" style="vertical-align: top;margin-top: -2px;" /></a>                          
	              </g:form>
                </td>
                <td onclick="showVisualization('${analysisId}', false);" class="td-link">${longDescription}</td>                
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
	          <li><a href="#heatmap_${analysisId}">Heatmap</a></li>
		      <li><a href="#boxplot_${analysisId}">Boxplot</a></li>
		      <g:if test="${isTimeCourse}">
		         <li><a href="#lineplot_${analysisId}">Lineplot</a></li>
		      </g:if>
	       </ul>
           <div id="lineplot_${analysisId}" style="display:none;">
		      <div class='vis-toolBar' >
			     <div id="btnLineplotExport_${analysisId}" class='vis-toolbar-item' onclick="analysisMenuEvent(this.id);"><img alt="" src="${resource(dir:'images',file:'tiny_down_arrow.png')}" /> Export</div>	          	
			     <div id="lineplotExportOpts_${analysisId}" class='menuOptList' style="display:none;">
		             <ul>
		             <li onclick="exportLinePlotData('${analysisId}','data');">Export data (.csv)</li>
		             <li onclick="exportLinePlotData('${analysisId}','image');">Export image (.png)</li>
		             </ul>
		         </div>
		         
                     <div id="btnLineplotControls_${analysisId}" class='vis-toolbar-item' onclick="analysisMenuEvent(this.id);"><img alt="" src="${resource(dir:'images',file:'tiny_down_arrow.png')}" /> Lineplot Options</div>                

                    <div id="lineplotControls_${analysisId}" class='boxplotControls' style="display:none;">
                       <table>
                        <tr>
                            <td><p>Probeset:</p></td>
                            <td><select id="probeSelectionLineplot_${analysisId}" class="probeSelectionDD" onchange="loadLinePlotData('${analysisId}');" /></td>
                        </tr>
                        <tr>
                        	<td style='vertical-align: top;'>Range:</td>
                        	<td> <input type="radio" name="lineplotRangeRadio_${analysisId}"  onchange="changeRangeRadioBtn('lineplot','${analysisId}')" id="lineplotRangeRadio_Auto_${analysisId}" value="Auto" checked /> Auto<br />
								 <input type="radio" name="lineplotRangeRadio_${analysisId}"  onchange="changeRangeRadioBtn('lineplot','${analysisId}')" id="lineplotRangeRadio_Manual_${analysisId}" value="Manual" /> Manual
							</td>
                        </tr><tr>
                        	<td></td>
                        	<td>Min: <input type="text" id="lineplotRangeMin_${analysisId}" disabled=true class='numericInput' onkeyup="updateLineplot('${analysisId}')" />
                        	    Max: <input type="text" id="lineplotRangeMax_${analysisId}" disabled=true class='numericInput' onkeyup="updateLineplot('${analysisId}')" /></td>
                        </tr>
                       </table>
                     </div>
              </div>	
          
              <table style="clear:both"><tr><td>
                 <div id="lineplotLegend_${analysisId}" class='legend' style="display:none;"></div>
              </td></tr></table>        
              <div id="lineplotAnalysis_${analysisId}" class="lineplot_analysis" style="display:none; padding-top:10px;"></div>
           </div>	
	       <div id="boxplot_${analysisId}">	
		      <div class='vis-toolBar' >
			     <div id="btnBoxplotExport_${analysisId}" class='vis-toolbar-item' onclick="analysisMenuEvent(this.id);"><img alt="" src="${resource(dir:'images',file:'tiny_down_arrow.png')}" /> Export</div>	          	
			     <div id="boxplotExportOpts_${analysisId}" class='menuOptList' style="display:none;">
		             <ul>
		             <li onclick="exportBoxPlotData('${analysisId}','data');">Export data (.csv)</li>
		             <li onclick="exportBoxPlotData('${analysisId}','image');">Export image (.png)</li>
		             </ul>
		         </div>
	             <div class='boxplotControls_holder'>
                     <div id="btnBoxplotControls_${analysisId}" class='vis-toolbar-item' onclick="analysisMenuEvent(this.id);"><img alt="" src="${resource(dir:'images',file:'tiny_down_arrow.png')}" /> Boxplot Options</div>                
                     <div id="boxplotControls_${analysisId}" class='boxplotControls' style="display:none;">
                       <table>
                        <tr>
                            <td><p>Probeset:</p></td>
                            <td><select id="probeSelection_${analysisId}" class="probeSelectionDD" onchange="loadBoxPlotData('${analysisId}');"></select></td>
                        </tr>
                        <tr>
                        	<td style='vertical-align: top;'>Range:</td>
                        	<td> <input type="radio" name="boxplotRangeRadio_${analysisId}"  onchange="changeRangeRadioBtn('boxplot','${analysisId}')" id="boxplotRangeRadio_Auto_${analysisId}" value="Auto" checked /> Auto<br />
								 <input type="radio" name="boxplotRangeRadio_${analysisId}"  onchange="changeRangeRadioBtn('boxplot','${analysisId}')" id="boxplotRangeRadio_Manual_${analysisId}" value="Manual" /> Manual
							</td>
                        </tr><tr>
                        	<td></td>
                        	<td>Min: <input type="text" id="boxplotRangeMin_${analysisId}" disabled=true class='numericInput' onkeyup="updateBoxPlot('${analysisId}')" />
                        	    Max: <input type="text" id="boxplotRangeMax_${analysisId}" disabled=true class='numericInput' onkeyup="updateBoxPlot('${analysisId}')" /></td>
                        </tr>
                       </table>
                     </div>
                 </div>
              </div>	  
	          <table style="clear:both"><tr><td>
                 <div id="boxplotLegend_${analysisId}" class='legend' style="display:none;"></div>
              </td></tr></table>	
	          <div id="boxplotAnalysis_${analysisId}" class="boxplot_analysis" style="display:none;"></div>
	          
	          <div id ="output_image_${analysisId}"></div>
	          
	          <div id="boxplotEmpty_${analysisId}" class="messageBox" style="display:none;"> 
	   		     <p>The box plot visualization displays the gene expression data for a single probe. To use, first select a probe from the heatmap by clicking on the probe ID.</p>
	   		  </div>
	       </div>
	       <div id="heatmap_${analysisId}">
	   	      <div class='vis-toolBar' >
			     <div id="btnHeatmapExport_${analysisId}" class='vis-toolbar-item' onclick="analysisMenuEvent(this.id);">
			     	<img alt="" src="${resource(dir:'images',file:'tiny_down_arrow.png')}" /> Export</div>			
			     <div id="heatmapExportOpts_${analysisId}" class='menuOptList' style="display:none;">
		             <ul>
		             <li onclick="exportHeatmapData('${analysisId}','currentPage');">Export current page (.csv)</li>
		             <li onclick="exportHeatmapData('${analysisId}','allProbes');">Export all analysis data (.csv)</li>
		             <li onclick="exportHeatmapData('${analysisId}','image');">Export image (.png)</li>
		             </ul>
		         </div>

    			 <div id="btnHeatmapControls_${analysisId}" onclick="analysisMenuEvent(this.id);" class='vis-toolbar-item'>
    			 	<img alt="" src="${resource(dir:'images',file:'tiny_down_arrow.png')}" /> Heatmap Options
    			 </div>				
		   	     <div id="heatmapControls_${analysisId}" class='heatmapControls' style="display:none;">
				   <table>
				       <tr>
					      <td><p>Cell size:</p></td>
					      <td><div id="heatmapSlider_${analysisId}"></div></td>
					   </tr><tr>
					      <td><p>Color range:</p></td>
					      <td><div id="heatmapColorSlider_${analysisId}"></div></td>
					   </tr><tr>
	                         <td><p>Probes/Page:</p></td>
		                     <td><select id="probesPerPage_${analysisId}" onchange="showVisualization('${analysisId}', true);">
					          <option value="10">10</option>
					          <option value="20">20</option>
					          <option value="50">50</option>
					          <option value="100">100</option>
					          <option value="150">150</option>
					          <option value="200">200</option>
					          <option value="250">250</option>
					      </select></td>
					   </tr>
			       </table>
			     </div>
			    </div>
		      <table style="clear:both"><tr>
		          <td><div id="heatmapLegend_${analysisId}" class='legend' style="display:none;"></div></td>
    	      </tr></table>
	          <div id="analysisDiv_${analysisId}" class="heatmap_analysis" style="display: none; padding-top:10px;"></div>	    
              <div id="pagination_${analysisId}" class="pagination"></div>
              
	       </div>
        </div>
    </div>
</div>


<script type="text/javascript">

	jQuery('#boxplotRangeMin_' +${analysisId}).numeric({ negative: false }, function() { alert("No negative values"); this.value = ""; this.focus(); });
	jQuery('#boxplotRangeMax_' +${analysisId}).numeric({ negative: false }, function() { alert("No negative values"); this.value = ""; this.focus(); });

</script>
