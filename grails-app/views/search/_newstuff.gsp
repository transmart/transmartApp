        <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery-1.7.1.min.js')}"></script>
        <script type="text/javascript" src="${resource(dir:'js', file:'jQuery/jquery.dataTables.js')}"></script>   
	    <script>jQuery.noConflict();</script> 		    
		    
		    
		    <link rel="stylesheet" href="${resource(dir:'css', file:'testingcss.css')}"></link>
			<script src="http://www.java.com/js/deployJava.js"></script>
		    <script>
		    	var jq = jQuery.noConflict();
		    
		        // using JavaScript to get location of JNLP
		        // file relative to HTML page
		        var dir = location.href.substring(0,location.href.lastIndexOf('/')+1);
		        var url = dir + "TestWS.jnlp";

				url = "http://localhost:8080/transmartApp/search/testWS"
				
		        deployJava.createWebStartLaunchButton(url, '1.6.0');
	
				
				
				//$(document).ready(function() {
				//    $('#table_id').dataTable();
				//} );

				//jq(document).ready(function() {
				//    jq('#table_id').dataTable( {
				//        "bProcessing": true,
				//        "bDestroy": true,
				//        "sAjaxSource": 'http://localhost:8080/transmartApp/search/getGwasResults'
				//    } );
				//} );				
				
				jq.ajax( {
				    "url": 'http://localhost:8080/transmartApp/search/getGwasResults',
				    bDestroy: true,
				    bServerSide: true,
				    "success": function ( json ) { jq('#table_id').dataTable( json );},
				    "dataType": "json"
				} );				
				
		    </script>
		    
		    <br /><br /><br />
		    
			<table id="table_id" cellpadding="0" cellspacing="0" border="0" class="display">
				<thead>
				</thead>
				<tbody>
				</tbody>
				<tfoot>	
				</tfoot>
			</table>	