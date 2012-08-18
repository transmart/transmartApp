  <g:form method="post" controller="igv">
   <input type="hidden" name="sessionFile" value="${sessionFile}" />
     
	<table class="subsettable" style="margin: 10px;width:530px; border: 0px none; border-collapse: collapse;">
	<tr>
				<td colspan="2">
					<span class='AnalysisHeader'>Result</span>				
				</td>			
			</tr>
			<td colspan="2">
					<hr />
				</td>
			<tr>
				<td align="center">
					 <div class="buttons">
                    <span class="button">
                    <g:actionSubmit value="Launch IGV" action="launchJNLP"/>
                    </span>
            </div>	
				</td>
			</tr>
		</table>	
		
	</g:form>