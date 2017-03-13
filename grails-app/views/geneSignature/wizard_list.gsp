<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
 
-->

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="genesigmain" />
	<g:if test="${wizard.wizardType==1}">
		<title>Gene List Edit</title>
	</g:if>
	<g:else>
		<title>Gene List Create</title>
	</g:else>

    <r:script disposition="head">$j = jQuery.noConflict();</r:script>

    <r:script disposition="head">

        jQuery(document).ready(function() {
            var pasteContent;

            jQuery('#biomarkerList').on('change', '.biomarkerEntry', function(event) {
                var name = jQuery(this).attr('name');
                var index = parseInt(name.substring(10));
                checkGene(index);
            });

            jQuery('#biomarkerList').on('keypress', '.biomarkerEntry', function(event) {
                if (event.which == 13) {
                    event.preventDefault();

                    var name = jQuery(this).attr('name');
                    var startingIndex = parseInt(name.substring(10));
                    createNewInputIfRequired(startingIndex);
                    $j('#biomarker_' + (startingIndex+1)).focus();
                }
            });

            jQuery('#biomarkerList').on('paste', '.biomarkerEntry', function(event) {
                pasteContent = null;
                var startingInput = jQuery(this);
                startingInput.val('');
                if (window.clipboardData) {
                    pasteContent = window.clipboardData.getData('Text');
                }
                //Paste is ABOUT TO happen, so set timeout
                setTimeout(function() {
                    if (pasteContent == null) {
                        pasteContent = startingInput.val();
                    }
                    var pasteList = pasteContent.split(",");
                    if (pasteList.length < 2) {
                        pasteList = pasteContent.split("\n");
                    }
                    var name = startingInput.attr('name');
                    var startingIndex = parseInt(name.substring(10));
                    for (var i = 0; i < pasteList.length; i++) {
                        var currentIndex = startingIndex + i;
                        $j('#biomarker_' + currentIndex).val(pasteList[i].trim());
                        checkGene(currentIndex);
                    }
                    jQuery('#pasteSource').val('');
                }, 1);
            });

            jQuery('#biomarkerList').on('click', '.biomarkerDelete', function(event) {
                var name = jQuery(this).attr('name');
                var index = parseInt(name.substring(10));
                jQuery('#biomarker_' + index).val('');
                jQuery('#geneCheckIcon' + index).removeClass('loading').removeClass('success').removeClass('failure').text('');
            });

            <%-- Add checks for all fields if this is an edit --%>
            <g:if test="${wizard.wizardType == 1}">
                <g:set var="n" value="${0}"/>
                <g:while test="${n < gs?.geneSigItems?.size()}">
                    checkGene(${n});
                    <g:set var="n" value="${n+1}"/>
                </g:while>
            </g:if>
        });

        function checkBiomarkerValues() {
            var foundEntry = false;
            var biomarkerFields = jQuery(".biomarkerEntry");

            for (var n = 0; n < biomarkerFields.size(); n++) {
                var textContent = jQuery(biomarkerFields[n]).val();
                if (textContent != null && textContent.trim() != "") {
                    foundEntry = true;
                    break;
                }
            }
            return foundEntry;
        }

		function validate() {

            var errorMsg = "";
			// list name required
			if(document.geneSignatureFrm.name.value=="") {
				errorMsg = "You must specify a list name";
            }

            if(document.geneSignatureFrm.uploadFile.value=="" && !checkBiomarkerValues())
                 errorMsg = errorMsg + "\n- Please select a file, or manually enter a gene list";

            if(document.geneSignatureFrm.uploadFile.value!="" && checkBiomarkerValues())
                errorMsg = errorMsg + "\n- You have both specified a file and manually entered a list";
            // if no errors, continue submission
            if(errorMsg=="") return true;

            alert("Please correct the following errors:\n" + errorMsg);
            return false;
		}

        function createNewInputIfRequired(index) {
            var removeImage = "${resource(dir:'images',file:'remove.png')}"
            var newIndex = index+1;
            var checkInputField = jQuery('#biomarker_' + newIndex);
            if (checkInputField.length == 0) {
                var newtr = jQuery('<tr/>').attr('id', 'new_item_' + newIndex);
                var newtdTextArea = jQuery('<td/>').append(jQuery('<textArea/>').attr('name', 'biomarker_' + newIndex).attr('id', 'biomarker_' + newIndex).addClass('biomarkerEntry'));
                var newtdIcon = jQuery('<td/>').append(jQuery('<div/>').attr('id', 'geneCheckIcon' + newIndex).addClass('geneCheckIcon'));
                var newtdImage = jQuery('<td/>').attr('style', 'text-align: center;').append(jQuery('<img/>').attr('name', 'biomarker_' + newIndex).attr('src', removeImage).addClass('biomarkerDelete'));

                newtr.append(newtdTextArea).append(newtdIcon).append(newtdImage);
                jQuery('#biomarkerList').append(newtr);
            }
        }

        function checkGene(index) {
            createNewInputIfRequired(index);
            var geneName = jQuery('#biomarker_' + index).val();
            jQuery('#geneCheckIcon' + index).removeClass('loading').removeClass('success').removeClass('failure').text('');
            if (geneName == null || geneName == "") {
                return;
            }
            else {
                jQuery('#geneCheckIcon' + index).addClass('loading')
                jQuery.ajax({
                    "url": '${createLink(controller: 'geneSignature', action: 'checkGene')}',
                    data: {geneName : geneName},
                    "success": function (jqXHR) {
                        if (jqXHR.found != 'none') {
                            jQuery('#geneCheckIcon' + index).removeClass('loading').addClass('success').text(jqXHR.found);
                        }
                        else {
                            jQuery('#geneCheckIcon' + index).removeClass('loading').addClass('failure').text('');
                        }
                    },
                    "error": function (jqXHR, error, e) {
                        jQuery('#geneCheckIcon' + index).removeClass('loading')
                    },
                    "dataType": "json"
                });
            }

        }
	</r:script>
</head>

<body>

<div class="body">
	<!-- initialize -->
    <g:set var="gs" value="${wizard.geneSigInst.properties}" />

<!--  show message -->
    <g:if test="${flash.message}">
        <div class="warning">${flash.message}</div>
        <g:hasErrors bean="${wizard.geneSigInst}"><div class="errors"><g:renderErrors bean="${wizard.geneSigInst}" as="list" /></div></g:hasErrors>
        <br>
    </g:if>

    <g:if test="${wizard.wizardType==1}">
        <h1>Gene List Edit: ${gs?.name}</h1>
    </g:if>
    <g:else>
        <h1>Gene/RSID List Create</h1>
    </g:else>
    <g:form name="geneSignatureFrm" enctype="multipart/form-data" method="post">

     <table class="detail" style="width: 100%">
        <tbody id="ListNameDetail">
        <tr class="prop">
            <td class="name">Signature/List Name<g:requiredIndicator/></td>
            <td class="value"><g:textField name="name" value="${gs.name}" size="100%" maxlength="100" /></td>
        </tr>
        </tbody>
     </table>
     <br>
     <table class="detail">
         <tbody id="FileInfoDetail">
        <tr class="prop">
            <td class="name">Upload File
                <br/>
                <span class="infotext">Upload a tab-delimited text file.</span>
            </td>
            <td class="value"><input type="file" name="uploadFile" <g:if test="${wizard.wizardType==0}">value="${gs.uploadFile}"</g:if><g:else>value=""</g:else> size="100" /></td>
        </tr>

        <tr>
            <td class="name">Enter List Manually
                <br/>
                <span class="infotext">Type or copy and paste a list of genes and/or SNPs here. The form will expand as needed. Pasted lists should be comma or new line separated.</span>
            </td>
            <td>
        <table class="detail" width="300" id="biomarkerList">
        <tbody id="_new_items_detail" style="display: block;">
        <tr id="new_header">
            <%--<th style="text-align: center;">#</th>--%>
            <th style="text-align: center;">Gene Symbol or rsID</th>
            <th style="text-align: center;">&nbsp;</th>
            <th style="text-align: center;">Remove</th>
        </tr>

        <g:set var="n" value="${0}"/>
        <g:set var="geneSigItems" value="${gs?.geneSigItems}"/>
        <g:set var="geneSigItemsIterator" value="${gs?.geneSigItems.iterator()}"/>
        <g:while test="${n < 5 || geneSigItemsIterator?.hasNext()}">

            <tr id="new_item_${n}">
                <%--<td style="color: gray;">${n}</td>--%>

            <!-- check if coming from an error -->
                <g:set var="bioMarkerValue" value=""/>
                <g:if test="${geneSigItems}">
                    <g:set var="nextItem" value="${geneSigItemsIterator.hasNext() ? geneSigItemsIterator.next(): null}"/>
                    <g:if test="${nextItem?.bioMarker}">
                        <g:set var="bioMarkerValue" value="${nextItem.bioMarker.name}"/>
                    </g:if>
                    <g:elseif test="${nextItem?.bioDataUniqueId}">
                        <g:set var="bioMarkerValue" value="${nextItem.bioDataUniqueId?.substring(4)}"/> <%-- Substring to cut off SNP: --%>
                    </g:elseif>
                    <g:else>
                        <g:set var="bioMarkerValue" value=""/>
                    </g:else>
                </g:if>
                <td><g:textArea name="biomarker_${n}" class="biomarkerEntry" value="${bioMarkerValue}"/></td>
                <td><div class="geneCheckIcon" id="geneCheckIcon${n}">&nbsp;</div></td>
                <td style="text-align: center;"><img class="biomarkerDelete" name="biomarker_${n}" alt="remove item" src="${resource(dir:'images',file:'remove.png')}" /></td>

            </tr>
            <%n++%>
        </g:while>
        </tbody>
    </table>
            </td>
        </tr>
         <tr class="prop">
             <td class="name">Flanking Region
                 <br/>
                 <span class="infotext">When searching on this gene list, include the gene regions +/- this number of chromosomal positions</span>
             </td>
             <td class="value"><input type="text" id="flankingRegion" name="flankingRegion" value="${gs?.flankingRegion ?: 0}"/></td>
         </tr>
         <tr class="prop">
             <td class="name">Make List Public
                 <br/>
                 <span class="infotext">Allow others to view this gene list</span>
             </td>
             <td class="value"><g:checkBox name="publicFlag" value="${gs.publicFlag}" />
         </tr>
</tbody>
      </table>

        <div class="buttons">
        <g:hiddenField name="isEdit" value="${wizard.wizardType==1}"/>
        <g:actionSubmit class="save" action="saveList" value="Save" onclick="return validate();" />
		<g:actionSubmit class="cancel" action="refreshSummary" onclick="return confirm('Are you sure you want to exit without saving this list?')" value="Cancel" />
	</div>			

	<br>
	</g:form>
</div>
</body>
</html>
