<head>
    <meta name='layout' content='main'/>
    <title>${grailsApplication.config.com.recomdata.appTitle}</title>
</head>

<body>

<div style="width: 600px; margin: 50px auto 50px auto;">
    <h3>Training Guide</h3>
    &nbsp;
    <br>
    <sec:ifAnyGranted roles="ROLE_PUBLIC_USER">
        <table class="detail">
            <tr>
            <td><a style="color:blue" href="${resource(dir: 'help', file: 'PublicTrainingTutorials-Basic.pdf')}">Basic Training Guide</a>
	</td></tr>
        <td><a style="color:blue"
               href="${resource(dir: 'help', file: 'PublicTrainingTutorials-Advanced.pdf')}">Advanced Training Guide</a>
        </td></tr>
	</table>
    </sec:ifAnyGranted>

    <sec:ifNotGranted roles="ROLE_PUBLIC_USER">
        <table class="detail">
        <tr>
        <td><a style="color:blue" href="${resource(dir: 'help', file: 'TransmartTrainingTutorials-Basic.pdf')}">Transmart Basic Training Guide</a>
	</td></tr>
        <td><a style="color:blue"
               href="${resource(dir: 'help', file: 'TransmartTrainingTutorials-Advanced.pdf')}">Transmart Advanced Training Guide</a>
        </td></tr>
	</table>
    </sec:ifNotGranted>
</div>

</body>
