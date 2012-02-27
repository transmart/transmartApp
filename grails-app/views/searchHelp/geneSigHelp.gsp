<html>
<head>
	<title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
	<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
	<script type="text/javascript" src="${resource(dir:'js', file:'maintabpanel.js')}"></script>	
	<script type="text/javascript">		
		function refreshParent(newurl) {
    		parent.window.close();
	 		if(parent!=null && parent.window.opener!=null && !parent.window.opener.closed) {
				parent.window.opener.location = newurl;
	 		}
		}
	</script>
</head>

<body>
<div id="summary">

	<!--  gene signatures -->
	<div id="SummaryHeader"><span class="SummaryHeader">Available Gene Signatures</span></div>

	<table class="trborderbottom" width="100%">
		<thead>
		<tr>
			<th style="white-space: nowrap;">Gene Signature</th>
			<th style="white-space: nowrap;">Gene List (up-regulated version)</th>
			<th>Description</th>			
		</tr>
		</thead>
	
		<tbody>
		<g:each in="${signatures}" var="gs" status="i">
		<g:set var="dtlLink" value="${createLink(controller:'geneSignature', action:'show', id:gs.id)}" />
		<tr style="border-bottom:1px solid #CCCCCC;padding-botton:2px;">  		
			<td width="25%">${createKeywordSearchLink(popup:true, jsfunction:"refreshParent", keyword:gsMap.getAt(gs.id))}</td>
			
			<!--  offer gene list version if applicable -->				
			<g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode!='NOT_USED'}">
				<td style="width:25%">${createKeywordSearchLink(popup:true, jsfunction:"refreshParent", keyword:glMap.getAt(gs.id))}</td>
			</g:if>
			<g:else><td style="width:25%;">NA</td></g:else>
			
	  		<td>${gs.description}</td>			
		</tr>
		</g:each>		
		</tbody>		
	</table>
	
</div>
</body>
</html>