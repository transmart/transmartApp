<%-- Quick, self-contained replica of the Ext utilities menu, for use on pages without the Ext library (faceted search) --%>
<g:javascript>
    function toggleMenu() {
        jQuery('#utilitiesMenu').fadeToggle();
    }

    jQuery(document).ready(function () {
        jQuery('#main').click(function () {
            jQuery('#utilitiesMenu').hide();
        });
    });
</g:javascript>
<style type="text/css">
#utilitiesMenu {
    font: normal 11px tahoma, arial, sans-serif;
    border: 1px solid #718bb7;
    z-index: 15000;
    zoom: 1;
    background: #f0f0f0 repeat-y;
    padding: 4px;
    position: absolute;
    right: 0;
    top: 24px;
    display: none;
}

#utilitiesMenu a {
    text-decoration: none !important;
    font-weight: normal !important;
}

#utilitiesMenuList {
    background: transparent;
    border: 0 none;
    list-style: none;
    margin: 10px 0 10px 25px;
}

#utilitiesMenu li {
    line-height: 100%;
    padding: 4px;
    border: 1px solid #f0f0f0;
    cursor: pointer;
}

#utilitiesMenu li:hover {
    background-color: #DDDDFF;
    border: 1px solid #718bb7
}

#utilitiesMenu li a {
    color: #000
}

#utilitiesMenuButton {
    cursor: pointer;
    font-weight: bold;
    color: #EEE;
}

li.utilMenuSeparator {
    padding: 0px;
    font-size: 1px;
    line-height: 1px;
}

span.utilMenuSeparator {
    display: block;
    font-size: 1px;
    line-height: 1px;
    margin: 2px 3px;
    background-color: #E0E0E0;
    border-bottom: 1px solid white;
    overflow: hidden;
}
</style>
<th class="menuLink" style="width: 100px; text-align: right">
    <a href="#" onclick="toggleMenu();
    return false;" id="utilitiesMenuButton">Utilities</a>
</th>

<g:set var="buildNumber"><g:meta name="environment.BUILD_NUMBER"/></g:set>
<g:set var="buildId"><g:meta name="environment.BUILD_ID"/></g:set>
<div id="utilitiesMenu">
    <ul id="utilitiesMenuList">
        <li><a href="#" onclick="jQuery('#utilitiesMenu').hide();popupWindow('${grailsApplication.config.com.recomdata.adminHelpURL}', '_help')">Help</a></li>
        <g:if test="${grailsApplication.config.com.recomdata.containsKey("bugreportURL")}">
            <li><a href="#" onclick="jQuery('#utilitiesMenu').hide();window.open('${grailsApplication.config.com.recomdata.bugreportURL}')">Report a Bug</a></li>
        </g:if>
        <li><a onclick="jQuery('#utilitiesMenu').hide();" href="mailto:${grailsApplication.config.com.recomdata.contactUs}">Contact Us</a></li>
        <li><a href="#" onclick="jQuery('#utilitiesMenu').hide();alert('${grailsApplication.config.com.recomdata.appTitle}', 'Build Version: ${buildNumber} - ${buildId}')">About</a></li>
        <li class="utilMenuSeparator"><span class="utilMenuSeparator">&nbsp;</span></li>
        <li><a href="${createLink(controller: 'changeMyPassword', action: 'show')}">Change My Password</a></li>
        <li><a href="${createLink(controller: 'login', action: 'forceAuth')}">Log Out</a></li>
    </ul>
</div>