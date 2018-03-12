<head>
    <meta name='layout' content='main'/>
    <title>${grailsApplication.config.com.recomdata.appTitle}</title>

    <style type='text/css' media='screen'>
    #login {
        margin: 15px 0px;
        padding: 0px;
        text-align: center;
    }

    #login .inner {
        width: 260px;
        margin: 0px auto;
        text-align: left;
        padding: 10px;
        border-top: 1px dashed #499ede;
        border-bottom: 1px dashed #499ede;
        background-color: #EEF;
    }

    #login .inner .fheader {
        padding: 4px;
        margin: 3px 0px 3px 0;
        color: #2e3741;
        font-size: 14px;
        font-weight: bold;
    }

    #login .inner .cssform p {
        clear: left;
        margin: 0;
        padding: 5px 0 8px 0;
        padding-left: 105px;
        border-top: 1px dashed gray;
        margin-bottom: 10px;
        height: 1%;
    }

    #login .inner .cssform input[type='text'] {
        width: 120px;
    }

    #login .inner .cssform label {
        font-weight: bold;
        float: left;
        margin-left: -105px;
        width: 100px;
    }

    #login .inner .login_message {
        color: red;
    }

    #login .inner .text_ {
        width: 120px;
    }

    #login .inner .chk {
        height: 12px;
    }
    </style>
</head>

<body>

<div align="center" style="clear:both; margin-left:auto; margin-right:auto; text-align:center">
    <table style="width:auto; border:0px; text-align:center; margin:auto;" align="center">
        <tr>
            <td style="text-align:center;vertical-align:middle;margin-left:-40px;padding-top:20px;">
                <g:link controller="RWG" action="index"><img
                        src="${resource(dir: 'images', file: grailsApplication.config.com.recomdata.largeLogo)}"
                        alt="Transmart"/></g:link>
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <tr>
            <td colspan=2 valign="middle" style="text-align:center;vertical-align:middle;border:1px;font-size:11px"
                nowrap="nowrap">
                <div style="margin-right:auto;margin-left:auto;width:435px;">
                    <div class="x-box-tl">
                        <div class="x-box-tr">
                            <div class="x-box-tc">
                            </div>
                        </div>
                    </div>

                    <div class="x-box-ml">
                        <div class="x-box-mr">
                            <div class="x-box-mc" style="text-align:left">
                                <h3 style="margin-bottom:20px; text-align:left; font-size:11px; color: #006dba;">
                                    ${grailsApplication.config.com.recomdata.appTitle}
                                </h3>
                                <g:if test='${flash.message}'>
                                    <div class='login_message' style="color:red; font-size:12px;">${flash.message}</div>
                                </g:if>
                                <form action='${postUrl}' method='POST' id='loginForm' class='cssform'>
                                    <table style="border:0px; text-align:center; width:100%">
                                        <tr>
                                            <td style="width: 100px">
                                                <label for='j_username' style="font-weight:bold">Username :</label>
                                            </td>
                                            <td style="white-space:nowrap;" NOWRAP>
                                                <input type='text' class='text_' name='j_username' id='j_username'
                                                       style="width:100%" autofocus/>
                                                <script>
                                                    if (!("autofocus" in document.createElement("input"))) {
                                                        document.getElementById("j_username").focus();
                                                    }
                                                </script>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <label for='j_password' style="font-weight:bold">Password :</label>
                                            </td>
                                            <td>
                                                <input type='password' class='text_' name='j_password' id='j_password'
                                                       style="width:100%"/>
                                            </td>
                                        </tr>
                                        <tr>
                                            <td>
                                                <label for='remember_me' style="font-weight:bold">Remember me :</label>
                                            </td>
                                            <td>
                                                <input type='checkbox' class='chk' name='_spring_security_remember_me' id='remember_me' />
                                            </td>
                                        </tr>
                                        <tr>
                                            <td colspan=2 style="text-align:center">
                                                <br>
                                                <input type='submit' id='loginButton' value='Login' style="width:100%"/>
                                                <br>
                                                <br>
                                            </td>
                                        </tr>
                                        <g:if test='${grailsApplication.config.com.recomdata.adminEmail}'>
                                            <tr>
                                                <td colspan="2" style="font-size:10px;">
                                                    Not a user ? Contact <a
                                                        href="mailto:${grailsApplication.config.com.recomdata.adminEmail}"
                                                        target="_blank"
                                                        style="text-decoration:underline;color:#0000FF">administrator</a>
							to request an account.
                                                </td>
                                            </tr>
                                        </g:if>
                                        <g:if test='${grailsApplication.config.org.transmart.security.samlEnabled}'>
                                            <tr>
                                                <td colspan="2" style="font-size:10px;">
                                                    <a href="${createLink([action: 'login', controller: 'saml'])}">&gt; Federated Login</a>
                                                </td>
                                            </tr>
                                        </g:if>
                                    </table>
                                </form>
                            </div>
                        </div>
                    </div>

                    <div class="x-box-bl">
                        <div class="x-box-br">
                            <div class="x-box-bc">
                            </div>
                        </div>
                    </div>
                </div>
            </td>
        </tr>
        <tr><td>&nbsp;</td></tr>
        <g:if test='${grailsApplication.config.ui.loginScreen.disclaimer}'>
            <tr>
                <td style="font-weight: bold;text-align:center;color:#CC0000;vertical-align:middle;margin-left:-40px; padding-top: 10px;">
                    <div style="margin-right:auto;margin-left:auto;width:435px">
                        ${grailsApplication.config.ui.loginScreen.disclaimer}
                     </div>
                </td>
            </tr>
        </g:if>
        <tr><td>&nbsp;</td></tr>
        <tr>
            <td style="text-align:center;vertical-align:middle;margin-left:-40px; padding-top: 10px;">
                <g:set var="projectName" value="${grailsApplication.config?.com?.recomdata?.projectName}"/>
                <g:set var="projectLogo" value="${grailsApplication.config?.com?.recomdata?.projectLogo}"/>
                <g:set var="providerName" value="${grailsApplication.config?.com?.recomdata?.providerName}"/>
                <g:set var="providerLogo" value="${grailsApplication.config?.com?.recomdata?.providerLogo}"/>
                <g:if test="${projectName}">
		    <span style="font-size:10px;display: inline-block;line-height: 35px; height: 35px;">Provided by&nbsp;</span>
		    <g:if test="{$projectLogo}">
                        <img src="${projectLogo}" alt="${projectName}"
                             style="height:35px;vertical-align:middle;margin-bottom: 12px;">
                    </g:if>
                    <g:else>
		        <span style="font-size:10px;display: inline-block;line-height: 35px; height: 35px;">${projectName}</span>
                    </g:else>
                </g:if>
                <g:if test="${projectName && providerName}">
		    <span style="font-size:10px;display: inline-block;line-height: 35px; height: 35px;">&nbsp;and&nbsp;</span>
                </g:if>
                <g:if test="${providerName}">
                    <a id="providerpowered" target="_blank"
                       href="${grailsApplication.config?.com?.recomdata?.providerURL}" style="text-decoration: none;">
                        <div>
                            <span style="font-size:10px;display: inline-block;line-height: 35px; height: 35px;">Powered by&nbsp;</span>
			    <g:if test="{$providerLogo}">
                                <img src="${providerLogo}" alt="${providerName}"
                                     style="height:35px;vertical-align:middle;margin-bottom: 12px;">
                            </g:if>
                            <g:else>
			        <span style="font-size:10px;display: inline-block;line-height: 35px; height: 35px;">${providerName}</span>
                            </g:else>
                        </div>
                    </a>
                </g:if>
            </td>
        </tr>
    </table>
</div>
</body>
