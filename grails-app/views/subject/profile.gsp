<div id="subjectProfile">
	<div style="font-family:arial; font-size:14px; float:left; width:100%; length:100%;overflow:auto;">
	 <table style="width:100%;border-bottom:solid #C0C0C0;">
	 	<tr>
	 		<td style="font-family:arial; font-size:14px;" align="left"> 
	 			<a href="#" onclick="spNavigation(${subjectId},'${patientId}','${trial}',false)"><img src="../images/back_enabled.png" alt="Previous" align="absmiddle" />Previous</a>		
	 		</td>
	 		<td style="font-family:arial; font-size:14px;" align="Right" > 
	 			<a href="#" onclick="spNavigation(${subjectId},'${patientId}','${trial}',true)">Next<img src="../images/forward_enabled.png" alt="Next" align="absmiddle"/></a>
	 		</td>
	 	</tr>	 		 	
	 </table>	
	</div>
	<br/>
    <div style="font-family:arial; font-size:14px; float:left; width:45%; length:90%; overflow:auto;">

    	
    	<span>
    	    <p><span style="font-weight:bold;"> Study: </span><span>${study}</span></p>
    		<p><span style="font-weight:bold;"> Patient ID: </span><span>${patientId}</span></p>
	    	<br/>
			<g:render template="subjectDetails" model="[details:demographics, title:'Demographics']"/>    	
    	</span>
    		
    	<br/>

   		<p style="font-weight:bold;"> Other Observations</p>
   		<g:select name="selectedObservations"
         		from="${observations}"
         		multiple="multiple"
         		style="width:25%;" />
        <g:submitButton name="submitObservations" value="Submit Query" onclick="getObservations(${subjectId});"/>
    	
    	<br/><br/>
    	
    	<div style="height:300px; overflow:auto;">
    		<table id="observationDetails"  style="border-collapse:collapse; width:100%"></table>
    	</div>
    	
    </div>
    <div style="font-family:arial; font-size:14px; float:right; min-height:500px; border-left: solid #C0C0C0; width:50%; length:90%;">
        	<span>
    	    	<p><span style="font-weight:bold;"> Subject Data (with timepoints) </span></p>
    	    </span>
    	    <%--
    		<g:select name="selectedTimepointData"
         		from="${observations.entrySet()}"
         		optionKey="key"
         		optionValue="value"
         		multiple="multiple" />
        	
        	<g:submitButton name="submitObservations" value="Submit Query" onclick="getObservations(${subjectId});"/>
        	--%>
        	<div id="placeholder" style="width:300px;height:300px"></div>
    </div>
</div>