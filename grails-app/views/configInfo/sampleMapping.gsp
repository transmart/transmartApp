<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <meta name="layout" content="admin"/>
    <title>Config Info - Sample Explorer Field Mapping</title>
  </head>

  <body>
    <script type="text/javascript" src="${resource(dir:'js', file:'toggle.js')}"></script>
    <!-- override main.css -->
    <style type="text/css">
      .detail td a {
      padding-left: 10px;
      vertical-align: top;
      }

      .detail td a:hover {
      white-space: normal;
      }
    </style>

    <div class="body">

      <h2>Sample explorer field mapping &nbsp;&nbsp;
	<a target="_blank" href="${grailsApplication.config.com.recomdata.adminHelpURL ?: "JavaScript:D2H_ShowHelp('1259','${grailsApplication.config.com.recomdata.adminHelpURL}','wndExternal',CTXT_DISPLAY_FULLHELP )"}">
          <img src="${resource(dir:'images',file:'help/helpicon_white.jpg')}"
	       alt="Help" border=0 width=18pt style="vertical-align:middle;margin-left:5pt;"/>
	</a>
      </h2>

      <table id="configSampleMapping"  class="detail" style="width: 100%">
	<g:tableHeaderToggle
          label="mapping (${mapping.size()})"
	  divPrefix="config_sample_mapping" status="open" colSpan="${5}"/>

	<tbody id="config_sample_mapping_detail" style="display: block;">
          <tr>
	    <th>header</th>
	    <th>dataIndex</th>
	    <th>menuItem</th>
	    <th>showInGrid</th>
	    <th>width</th>
	    <th width="100%">Description</th>
	  </tr>

          <g:each in="${mapping}" var="column">
            <tr>
	      <td>${column.header}</td>
	      <td>${column.dataIndex}</td>
	      <td>${column.menuItem}</td>
	      <td>${column.showInGrid}</td>
	      <td>${column.width}</td>
	      <td>${column.desc}</td>
	    </tr>
	  </g:each>

	</tbody>
      </table>

      <p>
	These mappings define the appearance and use of solr sample fields in the Sample Explorer tab.
      </p>
      <p>
	These depend on the content of field1 through field10, defined in the solR sample mapping file transmart-data/solr/solr/sample/conf/data-config.xml
	and transmart-data/solr/schemas/schema_sample.xml.
      </p>
    </div>

  </body>
</html>
