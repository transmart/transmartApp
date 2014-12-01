<%@ page import="org.apache.commons.lang.ArrayUtils; org.jfree.data.statistics.Statistics" %>
<table width="80%">
    <tbody>
    <tr>
        <td width="30%">
            <img src="${graphs.ageHisto}" width="245" height="180" border="0">
        </td>
        <g:each in="${subsets}" var="s">
            <g:set var="p" value="${s?.value}"></g:set>
            <g:if test="${p?.exists}">
                <td align="center">
                    <div class="smalltitle">
                        <b>
                            Subset ${s.key}
                        </b>
                    </div>
                    <table class="analysis">
                        <tbody>
                        <tr><td><b>Mean: </b>${p.ageStats.mean.round(2)}</td></tr>
                        <tr><td><b>Median: </b>${p.ageStats.median.round(2)}</td></tr>
                        <tr><td><b>IQR: </b>${(p.ageStats.q3 - p.ageStats.q1).round(2)}</td></tr>
                        <tr><td><b>SD: </b>${Statistics.getStdDev(ArrayUtils.toObject(p.ageData)).round(2)}</td></tr>
                        <tr><td><b>Data Points: </b>${p.ageData.size()}</td></tr>
                        </tbody>
                    </table>
                </td>
            </g:if>
        </g:each>
        <td width="30%">
            <img src="${graphs.agePlot}" width="200" height="300" border="0">
        </td>
    </tr>
    </tbody>
</table>