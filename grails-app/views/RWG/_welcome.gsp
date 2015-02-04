<div style="text-align: center;">
    <div class="welcome"
         style="margin: 40px auto; background: #F4F4F4; border: 1px solid #DDD; padding: 20px; width: 400px; text-align: center; border-top-left-radius: 20px; border-bottom-right-radius: 20px">
        <g:set var="projectName" value="${grailsApplication.config?.com?.recomdata?.projectName}"/>
        <g:set var="providerName" value="${grailsApplication.config?.com?.recomdata?.providerName}"/>
        <p><b>Welcome to tranSMART <g:if test="${projectName}">for ${projectName}</g:if></b></p>

        <p>The <b>Browse</b> window lets you search and dive into the information contained in tranSMART,
        including Programs, Studies, Assays and the associated Analyses Results, Subject Level Data and Raw Files.
        This is also the location to export files stored in tranSMART. Note: to edit the Program, Study, or Assay
        information, you must be logged in as an Administrator.
        </p>

        <p>The <b>Analyze</b> window lets you perform a number of analyses either on studies selected
        in the Browse window, or from the global search box located in the top ribbon of your screen.
        More information about the analyses you can perform is available in the “Help ? section of the "Utilities" menu.
        </p>
        <br><br>

        <div>
            <g:if test="${projectName}">
                <img src="${resource(dir: 'images', file: 'project_logo.png')}" alt="${projectName}"
                     style="height:35px;vertical-align:middle;margin-bottom: 12px;">
            </g:if>
            <g:if test="${projectName && providerName}">
                <span style="font-size:20px;display: inline-block;line-height: 35px; height: 35px;">&nbsp;+&nbsp;</span>
            </g:if>
            <g:if test="${providerName}">
                <a id="providerpowered" target="_blank" href="${grailsApplication.config?.com?.recomdata?.providerURL}"
                   style="text-decoration: none;">
                    <img src="${resource(dir: 'images', file: 'provider_logo.png')}" alt="${providerName}"
                         style="height:35px;vertical-align:middle;margin-bottom: 12px;">
                </a>
            </g:if>
        </div>
    </div>


    <sec:ifAnyGranted roles="ROLE_ADMIN">
        <div style="margin: auto; padding: 0px 16px 16px 16px; border-radius: 8px; border: 1px solid #DDD; width: 20%">
            <h4>Admin Tools</h4>
            <span class="greybutton buttonicon addprogram">Add new program</span>
        </div>
    </sec:ifAnyGranted>

    <br/><br/>
</div>