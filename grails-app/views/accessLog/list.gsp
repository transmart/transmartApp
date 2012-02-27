<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/> 
        <meta name="layout" content="admin" />
        <title>AccessLog List</title>
    </head>
    <body>
    <div class="body">
        <g:form name="form">
        <table style="width:500px"><tr><td>
        Start Date<input id="startdate" name="startdate" type="text" value="${startdate}"></input></td>
		<td>End Date<input id="enddate" name="enddate" type="text" value="${enddate}"></input></td>
		<td><br><g:actionSubmit class="filter" value="Filter" action="list" />&nbsp&nbsp&nbsp&nbsp<g:actionSubmit class="filter" value="Export to Excel" action="export" />
        </td></tr></table>
        </g:form>
            <h1>AccessLog List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
                        <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <th><g:message code="accessLogInstance.accesstime" default="Access Time"/></th>
                   	             	   
                        
                   	        <th><g:message code="accessLogInstance.username" default="User"/></th>
                   	   
                        
                   	        <th><g:message code="accessLogInstance.event" default="Event"/></th>
                   	   
                        
                   	        <th><g:message code="accessLogInstance.eventmessage" default="Event Message"/></th>
                   	             
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${accessLogInstanceList}" status="i" var="accessLogInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                        	<td>${fieldValue(bean:accessLogInstance, field:'accesstime')}</td>
                            <td>${fieldValue(bean:accessLogInstance, field:'username')}</td>
                        
                            <td>${fieldValue(bean:accessLogInstance, field:'event')}</td>
                        
                            <td>${fieldValue(bean:accessLogInstance, field:'eventmessage')}</td>                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>                                  
            </div>
            <div class="paginateButtons">
                <g:paginate 
                	total="${totalcount}"
                	maxsteps="${grailsApplication.config.com.recomdata.search.paginate.maxsteps}"
                	max="${grailsApplication.config.com.recomdata.search.paginate.max}" />
            </div>      
        </div> 
            <script>
            Ext.onReady(function(){
            var startdate = new Ext.form.DateField({
  name: 'dateField',
  allowBlank: false,
  format: 'm/d/Y',
  applyTo: 'startdate'
});
               var enddate = new Ext.form.DateField({
  name: 'dateField',
  allowBlank: false,
  format: 'm/d/Y',
  applyTo: 'enddate'
});   
    });
            </script>     
    </body>
</html>
