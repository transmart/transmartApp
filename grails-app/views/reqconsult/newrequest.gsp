<html>
    <head>
        <title>Centocor research subject data repository (Prototype) - Request Consult</title>
		<meta name="layout" content="main" />
    </head>
    <body>
<g:render template="/layouts/commonheader" model="['app':'consult']" />
           <div class="body" style="float:left;">
<br>
            <h2>Please specify what data to be retrieved from the repository</h2>

                <div class="dialog">
                    <table  style="border:0px">
                        <tbody>
<g:form controller="reqconsult">
                        <tr class="prop">

                       <td><g:textArea name="consulttext" value="" style="width:400px; height:200px"/></td>
                    </tr>
<tr><td>
<g:actionSubmit class="search" value="Submit" action="Saverequest" />
</td></tr>
</g:form>
                        </tbody>
                    </table>
                </div>

           </div>
    </body>
</html>