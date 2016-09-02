<%@ page import="org.jfree.data.statistics.Statistics" %>
<g:set var="prefix" value="${prefix ?: 'concept'}"/>
<table width="80%">
    <tbody>
    <tr>
        <td width="30%" style="text-align: right">
            ${subsets?.commons?."${prefix}Histo" ?: ''}
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
                        <g:set var="stats" value="${p?."${prefix}Stats"}"/>
                        <g:if test="${stats}">
                            <tr><td><b>Mean: </b>${(stats.mean?.round(2) =~ /NaN/).replaceAll("-")}</td></tr>
                            <tr><td><b>Median: </b>${(stats.median?.round(2) =~ /NaN/).replaceAll("-")}</td></tr>
                            <tr><td><b>IQR: </b>${((stats.q3 - stats.q1).round(2) =~ /NaN/).replaceAll("-")}</td></tr>
                        </g:if>
                        <g:if test="${p?."${prefix}Data".size()}">
                            <tr><td><b>SD: </b>${Statistics.getStdDev((Number [])p?."${prefix}Data".toArray()).round(2)}</td></tr>
                        </g:if>
                        <g:else>
                            <tr><td><b>SD: </b>-</td></tr>
                        </g:else>
                        <tr><td><b>Data Points: </b>${p?."${prefix}Data"?.size()}</td></tr>
                        </tbody>
                    </table>
                </td>
            </g:if>
        </g:each>
        <td width="30%">
            ${subsets?.commons?."${prefix}Plot" ?: ''}
        </td>
    </tr>
    </tbody>
</table>
