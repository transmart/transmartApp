<!DOCTYPE HTML>
<html>
  <head>
    <meta content="main" name="layout">
    <title><g:if env="development">Grails Runtime Exception</g:if><g:else>Error</g:else></title>
<!--  <g:if env="development">
      <link rel="stylesheet" href="${resource(dir: 'css', file: 'errors.css')}" type="text/css">
      </g:if> -->
  </head>

  <body>
    <g:logMsg>transmartApp An error has occurred</g:logMsg>
    <g:if env="development">
      <g:renderException exception="${exception}"/>
    </g:if>
    <g:else>
      <h1>Grails Runtime Exception</h1>
      <ul class="errors">
        <li>An error has occurred in transmartApp</li>
      </ul>
      <h2>Error Details</h2>
      <div class="message">
	<strong>Error ${request.'javax.servlet.error.status_code'}:</strong>
        ${request.'javax.servlet.error.message'.encodeAsHTML()}<br/>
        <strong>Servlet:</strong> ${request.'javax.servlet.error.servlet_name'}<br/>
        <strong>URI:</strong> ${request.'javax.servlet.error.request_uri'}<br/>
        <g:if test="${exception}">
	  <strong>Exception Message:</strong> ${exception.message?.encodeAsHTML()} <br/>

	  <strong>Caused by:</strong> ${exception.cause?.message?.encodeAsHTML()} <br/>
	  <strong>Class:</strong> ${exception.className} <br/>
	  <strong>At Line:</strong> [${exception.lineNumber}] <br/>
	  <g:if test="${exception.lineNumber > 0}">
	    <strong>Code Snippet:</strong><br/>
	    <div class="snippet">
	      <g:each var="cs" in="${exception.codeSnippet}">
		${cs?.encodeAsHTML()}
	      </g:each>
	    </div>
	  </g:if>
	</g:if>
      </div>
      <g:if test="${exception}">
	<h2>Stack Trace</h2>
	<div class="stack">
	  <pre>
	    <g:each in="${exception.stackTraceLines}">
	      ${it.encodeAsHTML()}
	    </g:each>
	  </pre>
	</div>
      </g:if>
    </g:else>
    <g:if test="${exception}">
      <h2> Exception report</h2>
      <g:renderException exception="${exception}"/>
    </g:if>
  </body>
</html>
