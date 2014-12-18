<%@ page import="org.apache.commons.lang.ArrayUtils; org.jfree.data.statistics.Statistics" %>
<table width="80%" style="text-align: center">
    <tbody>
    <tr>
        %{-- This is hardcoded badness. Multiple (>2) cohort selection should work on that --}%
        <td width="50%">
            ${subsets[1]?.conceptBar ?: ''}
            <g:render template="/chart/detailedStats" model="${[subset: subsets.entrySet().find {it.key == 1}]}"/>
        </td>
        <td width="50%">
            <g:if test="${subsets[2].exists}">
                ${subsets[2]?.conceptBar ?: ''}
                <g:render template="/chart/detailedStats" model="${[subset: subsets.entrySet().find {it.key == 2}]}"/>
            </g:if>
        </td>
    </tr>
    </tbody>
</table>