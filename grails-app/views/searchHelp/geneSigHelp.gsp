<g:if test="${gs.foldChgMetricConceptCode?.bioConceptCode != 'NOT_USED'}">
    <td style="width:25%">${createKeywordSearchLink(popup: true, jsfunction: "refreshParent", keyword: glMap.getAt(gs.id))}</td>
</g:if>
<g:else><td style="width:25%;">NA</td></g:else>

<td>${gs.description}</td>
</tr>
</g:each>
</tbody>
</table>

</div>
</body>
</html>
