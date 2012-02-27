<%@ page language="java" import="java.util.*" pageEncoding="ISO-8859-1"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title></title>
    
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
  	<g:javascript library="prototype" />
	<script type="text/javascript" src="${createLinkTo(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
	<script type="text/javascript" src="${createLinkTo(dir:'js', file:'ext/ext-all.js')}"></script>
	<g:if test="${createLink(action:'test', absolute:true).startsWith('https')}">
		<script type="text/javascript" src="https://www.google.com/jsapi"></script>
	</g:if>
	<g:else>
		<script type="text/javascript" src="https://www.google.com/jsapi"></script>
	</g:else>
	<script type="text/javascript" src="${createLinkTo(dir:'js', file:'datasetExplorer/i2b2common.js')}"></script>
	<!-- Include Ext stylesheets here: -->
	<link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'js/ext/resources/css', file:'ext-all.css')}">
	<link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'js/ext/resources/css', file:'xtheme-gray.css')}">
	<link rel="stylesheet" type="text/css" href="${resource(dir:'css', file:'datasetExplorer.css')}">
	<link rel="stylesheet" type="text/css" href="${createLinkTo(dir:'css', file:'chartservlet.css')}">
	<script type="text/javascript">
		var pageInfo={ 
					basePath:"${request.getContextPath()}"
					}
</script>
	<script type="text/javascript">
      google.load("visualization", "1", {});  	
   
   Ext.EventManager.onDocumentReady(function() {
		CompareSubsets();
	}); 
		
    function CompareSubsets(){	
    	var setname1= "<%= request.getParameter("setname1") %>";
    	var setname2= "<%= request.getParameter("setname2") %>";
		
		Ext.Ajax.request(
    	    {
    	        url: pageInfo.basePath+"/analysis/heatmap",
    	        method: 'POST',                                       
    	        success: function(result, request){compareSubsetsComplete(result, setname1, setname2);},
    	        failure: function(result, request){compareSubsetsComplete(result, setname1, setname2);},
    	        timeout: '600000',
    	        params: { idList:  '<%= request.getParameter("result_instance_id1") %>', 
    	        		  result_instance_id2:  '<%= request.getParameter("result_instance_id2") %>', 
    	        		  pathway_name:  '<%= request.getParameter("pathway") %>',
    	        		  datatype:  '<%= request.getParameter("datatype") %>',
    					  analysis:  '<%= request.getParameter("analysis") %>',
    					  resulttype: '<%= request.getParameter("resulttype") %>',
    					  nclusters: '<%= request.getParameter("nclusters") %>'
    	        		}
    	    });  	    
    }
    
    function compareSubsetsComplete(result, setname1, setname2)
	{
		var response=eval("(" + result.responseText + ")");
		var img = document.getElementById("heatmap");		
		// alert("imageFile" + response.imageFile);
		img.src = response.imageFile;
		
		var error = response.error;
		if (error != undefined) {
			alert("Our apologies as we are unable to process the heatmap");
			mylegend = "Error encountered: " + error;			
		} else	{
			if ("<%= request.getParameter("datatype") %>" == "RBM") {
				var pathway = response.pathway;
		    	if (pathway == null) {		  		  	
	      			mylegend="PLATFORM:<%= request.getParameter("datatype") %> "+"<br><table><tr><td>"+response.subset1+"</td><td>"+response.subset2+"</tr></table>";
	      		} else	{
	      			mylegend="PLATFORM:<%= request.getParameter("datatype") %> "+response.pathway+"<br><table><tr><td>"+response.subset1+"</td><td>"+response.subset2+"</tr></table>";
	      		}
			} else {
	      		mylegend="PLATFORM:<%= request.getParameter("datatype") %> "+response.pathway+"<br><table><tr><td>"+response.subset1+"</td><td>"+response.subset2+"</tr></table>";
			}
		}
		Ext.get("heatmapLegend").update(mylegend);	
	}
    </script> 
  
  </head>
  
  <body>
  	<div id='all'>
	<div style='position:relative;left:0px;'><a  href="javascript:showInfoInner('help/heatmap.html', 300, 300);"><img src="${createLinkTo(dir:'images',file:'information.png')}"></a></div>	
    <div id="heatmapDescription"></div><br>
    <div id="heatmapContainer"></div><br>
    <div class="analysis" id="heatmapLegend"></div>
    <br>
    <br>
    <div class="analysis" id="heatmapImage">
    <img src="${createLinkTo(dir:'images',file:'loader-large.gif')}" alt="loading" id="heatmap"/>
    </div>
    </div>
</body>
</html>
