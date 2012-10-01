<html>
    <head>
        <title>D3 Test</title>
        <link rel="stylesheet" type="text/css" href="${resource(dir:'css/jquery/cupertino', file:'jquery-ui-1.8.18.custom.css')}">
        <script type="text/javascript" src="${resource(dir:'js/jquery', file:'jquery-1.7.1.min.js')}"></script>
	    <script>jQuery.noConflict();</script> 
		<script type="text/javascript" src="${resource(dir:'js/jquery', file:'jquery-ui-1.8.17.custom.min.js')}"></script>	
        <script type="text/javascript" src="${resource(dir:'js/d3', file:'d3.v2.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'manipulateGeneSig.js')}"></script>
    </head>
    <body>
    	<div id="svg">
	       	<script type="text/javascript">
	       		visualize();
	        </script>
        </div>
        <br>
        <div style="width:480; height:240">
   	        <div style="text-align: right; float:left;">
	       		<g:submitButton class="edit" onClick="exportSVGImage();" value="Export" name="Export"/>
	        </div>
	        <div style="text-align: right; float:right;">
	        	<g:submitButton class="edit" onClick="resetResults();" value="Reset" name="Reset"/>
	        </div>
	        <br>
	        <g:textArea id="results" name="results" value="TTI1 INPPP1 TMX1 SCN11A DCT CRYGN MET H2AFJ ILF3" style="height: 180; width:480; border:1px double black"/>
	        <br>
	        <g:textField name="outputList" value="Output List Name" onClick="resetOutputList()"/>
	        <g:submitButton class="edit" onClick="saveOutputList();" value="Save" name="Save"/>
        </div>
    </body>
</html>