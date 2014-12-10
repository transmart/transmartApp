<%@ page import="org.apache.commons.lang.ArrayUtils; org.jfree.data.statistics.Statistics" %>
<g:set var="prefix" value="${prefix ?: 'concept'}"/>
<table width="80%" style="min-height: ${subsets?.commons?.minimalHeight ?: 0}px">
    <tbody>
    <tr>
        <td width="30%" style="text-align: right">
            <img src="${subsets?.commons?."${prefix}Histo"}" border="0" />
        </td>
        <g:each in="${subsets}" var="s">
            <g:set var="p" value="${s?.value}"/>
            <g:if test="${p?.exists}">
                <td align="center">
                    <div class="smalltitle">
                        <b>
                            Subset ${s.key}
                        </b>
                    </div>
                    <table class="analysis">
                        <tbody>
                        <tr><td><b>Mean: </b>${p?."${prefix}Stats"?.mean?.round(2)}</td></tr>
                        <tr><td><b>Median: </b>${p?."${prefix}Stats"?.median?.round(2)}</td></tr>
                        <tr><td><b>IQR: </b>${(p?."${prefix}Stats"?.q3 - p?."${prefix}Stats"?.q1).round(2)}</td></tr>
                        <tr><td><b>SD: </b>${Statistics.getStdDev(ArrayUtils.toObject(p?."${prefix}Data")).round(2)}</td></tr>
                        <tr><td><b>Data Points: </b>${p?."${prefix}Data"?.size()}</td></tr>
                        </tbody>
                    </table>
                </td>
            </g:if>
        </g:each>
        <td width="30%">
            <img src="${subsets?.commons?."${prefix}Plot"}" border="0" />
        </td>
    </tr>
    </tbody>
</table>