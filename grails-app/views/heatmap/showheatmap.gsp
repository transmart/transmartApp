<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
-->

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Trial Analysis Heatmap</title>
	<g:if test="${createLink(action:'test', absolute:true).startsWith('https')}">
		<script type="text/javascript" src="https://www.google.com/jsapi"></script>
	</g:if>
	<g:else>
		<script type="text/javascript" src="http://www.google.com/jsapi"></script>
	</g:else>
	<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
	<link rel="stylesheet" href="${resource(dir:'js',file:'ext/resources/css/ext-all.css')}" />
	<link rel="stylesheet" href="${resource(dir:'js',file:'ext/resources/css/xtheme-gray.css')}" />

	<script type="text/javascript" src="${resource(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'ext/ext-all.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'toggle.js')}"></script>	
	<script type="text/javascript" src="${resource(dir:'js', file:'application.js')}"></script>
	
	<script type="text/javascript">
		 google.load("visualization", "1", {packages:["table"]});
	</script>
	
	<script type="text/javascript" src="${resource(dir:'js', file:'bioheatmap.js')}"></script>
	<script type="text/javascript" src="${resource(dir:'js', file:'searchcombobox.js')}"></script>
	<link rel="stylesheet" href="${resource(dir:'js',file:'ext/resources/css/combos.css')}" />
	
	<style type="text/css">
	p {
		width: 430px;
	}
	
	.ext-ie .x-form-text {
		position: static !important;
	}
	</style>
	
	<script type="text/javascript">
			google.setOnLoadCallback(drawMultipleHeatMap);
		    var gheatmap = null;
		    var gcomtable = null;
		    var gcortable = null;
		    var grbmtable = null;
		    var grhotable = null;
		  //  var gdata = null;
		    var gcomdata = null;
		    var gcordata =null;
		    var grbmdata = null;
	        var grhodata = null;
	
		    function drawMultipleHeatMap(){
		    	var com = '${comtable}';
			if(com!=null && com!=''){
			  var comevdata = com.evalJSON();
			   gcomdata = new google.visualization.DataTable();
				drawHeatMap(gcomdata, comevdata, 'comHeatmapContainer');
			}
			var cor = '${cortable}';
				if(cor!=null && cor!=''){
				var corevdata = cor.evalJSON();
				gcordata = new google.visualization.DataTable();
				 drawHeatMap(gcordata, corevdata, 'corHeatmapContainer');
				}
			var rbm = '${rbmtable}';
			if(rbm!=null && rbm!=''){
				var rbmevdata = rbm.evalJSON();
				grbmdata = new google.visualization.DataTable();
				 drawHeatMap(grbmdata, rbmevdata, 'rbmHeatmapContainer');
			}
	
			var rho = '${rhotable}';
			if(rho!=null && rho!=''){
				var rhoevdata = rho.evalJSON();
				grhodata = new google.visualization.DataTable();
				 drawHeatMap(grhodata, rhoevdata, 'rhoHeatmapContainer');
				}
			}
	
			function drawHeatMap(gdata, evdata, container) {
	
				//gdata = new google.visualization.DataTable();
		        var colsize = evdata.table.cols.size();
		        gdata.addColumn('string', 'Gene Name');
		        for(i=1;i<colsize;i++){
					gdata.addColumn('number', evdata.table.cols[i].label);
		        }
		        var rowcount =evdata.table.rows.size();
		        gdata.addRows(rowcount);
		        var jrow=null;
		        for (j=0;j<rowcount;j++) {
		            jrow= evdata.table.rows[j];
		        	gdata.setCell(j, 0, jrow[0].v);
		        	var cellcc=null;
		          	for(cc=1;cc<colsize;cc++) {
		               	cellcc= jrow[cc];
						gdata.setCell(j, cc, cellcc.v,cellcc.f);
		           	}
		      	}
				gheatmap = new org.systemsbiology.visualization.BioHeatMap(document.getElementById(container));
				google.visualization.events.addListener(gheatmap, 'select', selectHandler);
	
				gheatmap.draw(gdata, {
										emptyDataColor:{r:230,g:230,b:230,a:1},
										numberOfColors: 256,
										passThroughBlack: true
									});
	
			}
	
			function selectHandler() {
	   			var row = gheatmap.getSelection()[0].row;
	  			popup(gdata.getValue(row, 0));
	  		}

			function backToTop() {
				window.location.hash="pageTop";
			}
			
			function showHeatMapView(){
				document.getElementById('heatmapViewContainer').style.display="block";
				document.getElementById('tableContainer').style.display="none";
				window.location.hash="heatmapViewContainer";
			}
	
			function showTableView(){
	
				if(gcomtable==null && gcomdata!=null){
					gcomtable = new google.visualization.Table(document.getElementById("comTableView"));
					drawTable(gcomtable, gcomdata);
				}
	
				if(gcortable==null && gcordata!=null){
					gcortable = new google.visualization.Table(document.getElementById("corTableView"));
					drawTable(gcortable, gcordata);
				}
				
				if(grbmtable==null && grbmdata!=null){
					grbmtable = new google.visualization.Table(document.getElementById("rbmTableView"));
					drawTable(grbmtable, grbmdata);
				}
				
				if(grhotable==null && grhodata!=null){
					grhotable = new google.visualization.Table(document.getElementById("rhoTableView"));
					drawTable(grhotable, grhodata);
				}
				
				document.getElementById('heatmapViewContainer').style.display="none";
				document.getElementById('tableContainer').style.display="block";
				window.location.hash="tableContainer";
			}
	
			function drawTable(gtable, gdata){
				if(gdata!=null){
					//gtable = new google.visualization.Table(document.getElementById("comTableView"));
				 	var formatter = new google.visualization.TableColorFormat();
				  	formatter.addRange(-100, 0, 'green', '#ffffff');
				  	formatter.addRange(0, 100, 'red', '#ffffff');
				  	var colsize = gdata.getNumberOfColumns();
				  	for(ci=1;ci<colsize;ci++){
						formatter.format(gdata, ci);
				  	}
				  	gtable.draw(gdata,{allowHtml: true});
				}
			}
	
			function popup(mylink)
	  		{
	  			var baseurl='${createLink(controller:'geneCard',action:'showmoreinfo')}';
	  			var newurl=baseurl+'?id='+mylink;
	  			var w=window.open(newurl, 'geneCardWindow', 'width=900,height=800');
	  			w.focus();
	  			return false;
	  		}
	
			function updateType(id, newvalue) {
	
			}
	
			function submitTopGene() {
				document.getElementById("heatmapfiltertype").value="topgene";
				document.trialhmform.submit();
		    }
	
			Ext.onReady( function() {

				Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

				// set ajax to 90*1000 milliseconds
				Ext.Ajax.timeout = 180000;

				// this overrides the above
				Ext.Updater.defaults.timeout = 90000;
				
				// qtip on
				Ext.QuickTips.init();

				var combo = new Ext.app.SearchComboBox({
					id: "search-combobox",
					value: "${session?.searchFilter?.heatmapFilter?.searchTerm?.keyword}",
					renderTo: "search-text",
					searchUrl:"${createLink([action:'loadHeatMapFilterAJAX',controller:'search'])}",
					submitUrl:"${createLink([action:'filterheatmap',controller:'heatmap'])}",
				   onSelect: function(record){ // override default onSelect to do redirect
				    	//  Ext.MessageBox.show({
					     //      msg: 'Generating Heatmaps, please wait...',
					     //      progressText: 'Processing..',
					     //      width:300,
					     //      wait:true,
					     //      waitConfig: {interval:200},
					     //      animEl: 'topgeneradio'
					     //  });
					     //   setTimeout(function(){
					            //This simulates a long-running operation like a database save or XHR call.
					            //In real code, this would be in a callback function.
					      //      Ext.MessageBox.hide();
					           // Ext.example.msg('Done', 'Your fake data was saved!');
					      //  }, 35000);
					        this.collapse();
							if (this.submitUrl != "") {
								window.location = String.format(this.submitUrl + "?{0}", Ext.urlEncode({id: record.data.id}));
							}
				    }
	
				});
	
	
				Ext.get('topgeneradio').on('click', function(){
	
			      //  Ext.MessageBox.show({
			        //   msg: 'Generating Heatmaps, please wait...',
			        //   progressText: 'Processing..',
			        //   width:300,
			        //   wait:true,
			        //   waitConfig: {interval:200},
			        //   animEl: 'topgeneradio'
			      // });
			       // setTimeout(function(){
			            //This simulates a long-running operation like a database save or XHR call.
			            //In real code, this would be in a callback function.
			         //   Ext.MessageBox.hide();
			           // Ext.example.msg('Done', 'Your fake data was saved!');
			        //}, 35000);
			    	submitTopGene();
	
			    });
	
			});
	
			// Show a dialog window animated from the specifed id paramter with
			// title and content contained in value parameter.
			function showDialog(id, value) {
				// Attempt to get exitsing window with id.
				var win = Ext.getCmp(id + '-win');
	
				if(win==null){
			    win = new Ext.Window({
			    	id: id + '-win',
			        animateTarget: id,
			        autoScroll: true,
			        width: 550,
			        height: 350,
			        closeAction: 'hide',
			        bodyBorder:'false',
			        plain:'true',
			        title: value.title,
			        contentEl: value.element
				});
				}
	
			    win.show();
			    win.toFront();
			    var atitle = id+'_anchor';
			   // alert(atitle);
			  //  var anchor = document.getElementById(value.title+'_anchor');
			    win.alignTo(atitle,'bl-tl?');
			}
	
	</script>
	<!-- ************************************** -->
	<!-- This implements the Help functionality -->
	<script type="text/javascript" src="${resource(dir:'js', file:'help/D2H_ctxt.js')}"></script>
	<script language="javascript">
		helpURL = "/transmart/help/userHelp/default.htm";
	</script>
	<sec:ifAnyGranted roles="ROLE_ADMIN">
		<script language="javascript">
			helpURL = "/transmart/help/adminHelp/default.htm";
		</script>
	</sec:ifAnyGranted>
	<!-- ************************************** --> 
</head>

<body>
<div id="pageTop" style="padding: 15px 10px 15px 10px">
	<div>
		<h3 style="background: #dfe8f6; padding-bottom: 5px; padding-top: 5px;">
			Clinical Trials Analysis View
			<%topicID="1026" %>
			<a HREF='JavaScript:D2H_ShowHelp(<%=topicID%>,helpURL,"wndExternal",CTXT_DISPLAY_FULLHELP )'>
				<img src="${resource(dir:'images',file:'help/helpbutton.jpg')}" alt="Help" border=0 width=18pt style="margin-top:1pt;margin-bottom:1pt;margin-right:18pt;float:right"/>
			</a>
		</h3>
		<br>
	</div>
<br>
<g:form controller="heatmap" name="trialhmform" action="filterheatmap">
	<g:hiddenField id="heatmapfiltertype" name="heatmapfiltertype" value="" />
	<div style="padding-bottom: 5px">
	<table style="border: 1px; padding-top: 5px;">
		<tr>
			<td style="width: 100px; vertical-align: middle;">
				<span style="font-weight: bold;">Filter By:</span>
			</td>
			<td style="width: 160px;">
				<input type="radio" name="radioheatmapfilter" value="pathway" onClick="javascript:updateType('heatmapfiltertype','pathway')"
				${session?.searchFilter?.heatmapFilter?.heatmapfiltertype==null? "checked":""} />
				<span style="font-weight: bold; color: #538d4e;">Gene/Pathway</span>
			</td>
			<td>
				<div style="width: 660px;" id="search-text"></div>
			</td>
		</tr>
		<tr>
			<td>&nbsp;</td>
			<td colspan="2">
				<input type="radio" name="radioheatmapfilter" id="topgeneradio" value="topgene"
					${session?.searchFilter?.heatmapFilter?.heatmapfiltertype=="topgene"?"checked":""} />
				<span style="font-weight: bold; color: #538d4e;">Top 50 Genes</span>
			</td>
	</table>
	</div>
</g:form>
<hr>

<div id="linksMenu" style="padding-top: 5px">
	<a href="javascript:showHeatMapView();">Heatmap View</a>&nbsp;&nbsp;|&nbsp;&nbsp;
	<a href="javascript:showTableView();">Grid View</a>&nbsp;&nbsp;|&nbsp;&nbsp;
	<g:link action="downloadheatmapexcel"><img alt="download xls" src="${resource(dir:'images',file:'Excel-16.gif')}" />Export</g:link>
</div>
<br />

<!--  legend table -->
<div class="gtbl" style="overflow: auto;">
<table class="detail">
	<g:tableHeaderToggle label="Legend" divPrefix="legend" colSpan="3" />

	<tbody id="legend_detail" style="display: none;">
	<g:each in="${contentlist}" status="i" var="content">
		<tr class="prop">
			<td width="300px" class="name" align=left
				style="text-align: left; white-space: normal">
					<a style="text-decoration: underline" onclick="showDialog('AnalysisDet_${content.id}', { title: '${content.shortDescription}', element: 'detaildiv_${content.id}' });">${content.shortDescription}</a></td>
			<td class="value" align=left style="text-align: left">${content.longDescription}</td>
			<td>
				<div style="display: none">
					<div id="detaildiv_${content.id}" style="background-color: #ffffff;"><g:render template="/trial/analysisdetail" model="[analysis:content]" /></div>
				</div>
			</td>
		</tr>
	</g:each>
	</tbody>
</table>
</div>

<div id="heatmapViewContainer">
<p style="padding-bottom: 5px; padding-top: 5px;"><a href="javascript:backToTop();">Back to Top</a></p>
<table class="" border="0">
	<tr>
		<g:if test="${comtable!=null}">
			<td width='${(comtable==null||comtable=="")?1:(hmapwidth)}%'
				align="center"
				style="text-align: center; padding: 0 0 0 0; white-space: no-wrap;">
			Gene Expression Comparison<br>
			<div id="comHeatmapContainer"><img src="${resource(dir:'images',file:'loader-mid.gif')}" alt="loading" />
			</div>
			</td>
		</g:if>
		<g:if test="${cortable!=null}">
			<td width='${(cortable==null||cortable=="")?1:(hmapwidth)}%'
				align="center"
				style="text-align: center; padding: 0 0 0 0; white-space: no-wrap;">Gene
			Expression Correlation<br>
			<div id="corHeatmapContainer"><img src="${resource(dir:'images',file:'loader-mid.gif')}" alt="loading" /></div>
			</td>
		</g:if>
		<g:if test="${rbmtable!=null}">
			<td width='${(rbmtable==null||rbmtable=="")?1:(hmapwidth)}%'
				style="text-align: center; padding: 0 0 0 0; white-space: no-wrap;">
			RBM Comparison<br>
			<div id="rbmHeatmapContainer"></div>
			</td>
		</g:if>
		<g:if test="${rhotable!=null}">
			<td width='${(rhotable==null||rhotable=="")?1:(hmapwidth)}%'
				style="text-align: center; padding: 0 0 0 0; white-space: no-wrap;">
			RBM Spearman Correlation<br>
			<div id="rhoHeatmapContainer"></div>
			</td>
		</g:if>
	</tr>
</table>
</div>

<div id="tableContainer" style="display: none">
<p style="padding-bottom: 5px; padding-top: 5px;"><a href="javascript:backToTop();">Back to Top</a></p>
<table>
	<g:if test="${comtable!=null}">
		<tr>
			<td>Gene Expression Comparison<br>
			<div id="comTableView"></div>
			</td>
		</tr>
		<tr>
			<td>
			<hr>
			<br>
			</td>
		</tr>
	</g:if>
	
	<g:if test="${cortable!=null}">
		<tr>
			<td>Gene Expression Correlation<br>
			<div id="corTableView"></div>
			</td>
		</tr>
		<tr>
			<td>
			<hr>
			<br>
			</td>
		</tr>
	</g:if>
	
	<g:if test="${rbmtable!=null}">
		<tr>
			<td>RBM Comparison<br>
			<div id="rbmTableView"></div>
			</td>
		</tr>
		<tr>
			<td>
			<hr>
			<br>
			</td>
		</tr>
	</g:if>
	
	<g:if test="${rhotable!=null}">
		<tr>
			<td>RBM Spearman Correlation<br>
			<div id="rhoTableView"></div>
			</td>
		</tr>
	</g:if>
</table>
</div>

</div>
</body>
</html>
