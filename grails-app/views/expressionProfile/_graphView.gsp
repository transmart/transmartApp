<!-- render graph when successful -->
<g:if test="${epr.graphURL!=null && epr.graphURL != 'empty'}">
	
<table>
	<tr><td><img src="${epr.graphURL}"/></td></tr>
	<tr><td>&nbsp;</td></tr>
	<tr><td><center>
		<g:set var="printURL" value="${createLink(action:'printChart') + '?' + epr.graphURL.substring(epr.graphURL.indexOf('filename='))}" />
		<a href="#" onclick="window.open('${printURL}','_boxplot','width=850,height=600,resizable=yes,scrollbars=yes,location=no,menubar=yes');">
			<img src="${resource(dir:'images',file:'print.png')}" />
			Print Chart
		</a>
	</center></td></tr>
</table>
<br>


<!-- override main.css -->
<style type="text/css">

	.list td a:hover {
	    font-size: 12px;
	    white-space: normal;
	 }

</style>

<div id="dataset_div" class="body">
	<h1 style="font-weight: bold; border-bottom-style: inset;">Information on individual datasets</h1>
	<br>
	<table class="list" width="98%">
	<thead>
		<tr style="background-color: black;">
			<th>Dataset</th>
			<th style="white-space: nowrap;">No. Samples</th>
			<th>Experiment</th>
		</tr>
	</thead>
	<tbody>
	
	<g:each in="${epr.datasetItems}" status="i" var="ds">
		<tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
			<td style="vertical-align: top;">${ds.dataset.name}</td>
			<td style="text-align: center; vertical-align: top;">${ds.sampleCount}</td>

			<td style="vertical-align: top;">
				<a onclick="showDialog('ExpDetail_${ds.experiment.id}', { title: '${ds.experiment.accession}', url: '${createLink(controller:'experimentAnalysis', action:'expDetail', id:ds.experiment.id)}' });">
				   <img alt="Experiment Detail" src="${resource(dir:'images',file:'view_detailed.png')}"/>&nbsp;<span style="font-weight: bold; color: #339933; white-space: nowrap;">${ds.experiment.accession}:</span>&nbsp;&nbsp;${ds.experiment.title}</a></td>
		</tr>
	</g:each>	
	
	</tbody>
	</table>
</div>			
																		
</g:if>
<g:else>
	<!-- problem creating graph -->
	<g:if test="${epr.graphURL==null}">
		<br><br>
	 	<span style="text-align: center; font-weight: bold;">&nbsp;Graph service is not available</span>
	</g:if>
	<g:else>	
		<br><br>
	 	<span style="text-align: center; font-weight: bold;">&nbsp;No data available for your selections</span>
	</g:else>
</g:else>
