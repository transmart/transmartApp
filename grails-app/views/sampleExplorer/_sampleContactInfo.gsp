<g:set var="sectionCounter" value="0"/>

<g:each var="contact" in="${allSamplesByContact}">

    <div style="font-size:1.5em;text-align:center;padding:10px;">
        <g:if test="${contact.key == 'NO_CONTACT'}">
            Contact Information unavailable for these samples.
        </g:if>
        <g:else>
            Contact E-Mail : <a
                href="mailto:${contact.key}?Subject=Sample Information&body=Sample%20IDs%20${contactSampleIdMap[contact.key].join(',')}"
                target="_top">${contact.key}</a>
        </g:else>
    </div>

    <br/>

    &nbsp;&nbsp;&nbsp;<a href="#"
                         onclick="toggleContactSampleSection(document.getElementById('sampleSection${sectionCounter}'))">Toggle Sample Information</a>

    &nbsp;&nbsp;&nbsp;Sample ID List - <input type="text" value="${contactSampleIdMap[contact.key].join(',')}"/>

    <div id="divSample${sectionCounter}" class="detailedSampleInformation">
        <div id="sampleSection${sectionCounter}" style="display:none;">
            <g:each var="sample" in="${contact.value}">
                <table class="biobankResults">
                    <g:each var="sampleAttribute" in="${sample}">
                        <g:if test="${sampleAttribute.key != 'count'}">
                            <tr>
                                <th>
                                    ${columnPrettyNameMapping[sampleAttribute.key]}
                                </th>
                                <td style='vertical-align:middle;border-color:black;border-width:1px;border-style:solid;padding: 3px;'>
                                    ${sampleAttribute.value}
                                </td>
                            </tr>
                        </g:if>
                    </g:each>
                </table>

                <br/>
                <br/>

            </g:each>
        </div>
    </div>

    <g:set var="sectionCounter" value="${sectionCounter + 1}"/>

</g:each>