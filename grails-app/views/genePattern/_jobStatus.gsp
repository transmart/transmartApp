<h3>Step ${wfstatus.currentStatusIndex} of ${wfstatus.jobStatusList.size()}</h3>
<table class="jobstatus">
    <g:each in="${wfstatus.jobStatusList}">
        <tr><td>${it.name}<g:if test="${it.isCompleted()}">&nbsp;&nbsp;
            <img src="${resource(dir: 'images', file: 'green_check.png')}"/>
        </g:if>
            <g:elseif test="${it.isRunning()}">
                <img src="${resource(dir: 'images', file: 'loading-balls.gif')}"/>
            </g:elseif>
        </td></tr></g:each></table>