<head>
    <meta name='layout' content='main'/>
    <title>${grailsApplication.config.com.recomdata.appTitle}</title>
</head>

<body>
<center>
    <div style="width: 400px; margin: 50px auto 50px auto;">
        <img style="display: block; margin: 12px auto;"
             src="${resource(dir: 'images', file: grailsApplication.config.com.recomdata.largeLogo)}"
             alt="Transmart"/>
        <center><h3>ATTENTION: Users of ${grailsApplication.config.com.recomdata.appTitle}</h3></center>

        <div style="text-align: justify; margin: 12px;">
            ${grailsApplication.config.com.recomdata.disclaimer}
        </div>
        <center>
            <g:form name="disclaimer" method="post" id="disclaimerForm">
                <g:actionSubmit value="I agree" action="agree"/>
                <g:actionSubmit value="I disagree" action="disagree"/>
            </g:form>
        </center>
    </div>
</center>
</body>
