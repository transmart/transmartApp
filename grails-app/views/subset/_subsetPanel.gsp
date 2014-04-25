<div style="width:100%;height:100%;background-color:white">

	<br />
	<!--
	The advanced functionality was shelved for later use.  
	<table class="subsettable" border="0px"  style="margin:10px">
		<tr>
			<td align="center">
				<span style="font-size:1.25em;font-weight:bold;">Event Linking : </span>
					
				<select id="selEventTiming" onchange="toggleQueryTimingButtons(this.value);">
					<option value="SAMEEVENT">Treat all input criteria as being from the same event</option>
					<option value="MIXEDEVENT">Specify criteria from varying events</option>
				</select>
			
			</td>
		</tr>
	</table>
	-->
	
	<table class="subsettable" border="0px"  style="margin:10px">
		
		<tr>
			<td align="center">
				<span style="text-decoration: underline;font-size:1.25em;font-weight:bold;">Subset 1</span>
			</td>
			<td id="subsetdivider" rowspan="21" valign="center" align="center"  height="100%">
				<div style="margin:15px;border:1px solid black;background:black;width:1px; height:150px"></div>
			</td>
			<td align="center">
				<span style="text-decoration: underline;font-size:1.25em;font-weight:bold;">Subset 2</span>
			</td>
		</tr>

		<g:each var="currentGroup" in="${(1..20).toList()}">
			<tr id="qcr${currentGroup}">
				<td align="right">
					<g:if test="${currentGroup > 1}">
					    AND&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
					</g:if>
					<button id="btnPanelTimingGroup1_${currentGroup}" style="font:9pt tahoma;display:none;" onclick="timingGroup(this, '1','${currentGroup}')">Independent</button>
					<button id="btnExcludeGroup1_${currentGroup}" style="font:9pt tahoma;" onclick="excludeGroup(this, '1','${currentGroup}')">Exclude</button>
					<button id="clearGroup1_${currentGroup}" style="font:9pt tahoma;" onclick="clearGroup('1','${currentGroup}')">X</button>
					<br>
					<div id='queryCriteriaDiv1_${currentGroup}' class="queryGroupInclude queryGroupSAMEEVENT"></div>
				</td>
				<td align="right">
					<g:if test="${currentGroup > 1}">
					    AND&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp
					</g:if>				
					<button id="btnPanelTimingGroup2_${currentGroup}" style="font:9pt tahoma;display:none;" onclick="timingGroup(this, '2','${currentGroup}')">Independent</button>
					<button id="btnExcludeGroup2_${currentGroup}" style="font:9pt tahoma;" onclick="excludeGroup(this, '2','${currentGroup}')">Exclude</button>
					<button id="clearGroup2_${currentGroup}" style="font:9pt tahoma;" onclick="clearGroup('2','${currentGroup}')">X</button>
					<br>
					<div id='queryCriteriaDiv2_${currentGroup}' class="queryGroupInclude queryGroupSAMEEVENT"></div>
				</td>
			</tr>  	
		</g:each>
	</table>
</div>

<div id="hiddenDragDiv" style="display:none;background-color:white"></div>

<script type="text/javascript">
	setupDragAndDrop();
	hideCriteriaGroups();

	//When the user changes the dropdown box we show/hide the timing toggle button and reset the fields to their default.
	function toggleQueryTimingButtons(dropdownValue)
	{
		for(var i=1;i<=20;i++)
		{
			var currentButton1 = "#btnPanelTimingGroup1_" + i
			var currentButton2 = "#btnPanelTimingGroup2_" + i

			var currentGroupBox1 = "#queryCriteriaDiv1_" + i
			var currentGroupBox2 = "#queryCriteriaDiv2_" + i

			//Same event means we hide all the buttons and set the css flag to include the Same Event flag.
			if(dropdownValue == "SAMEEVENT")
			{
				jQuery(currentButton1).hide()
				jQuery(currentButton2).hide()
				
				jQuery(currentButton1).text("Independent")
				jQuery(currentButton2).text("Independent")
				
				jQuery(currentGroupBox1).addClass("queryGroupSAMEEVENT");
				jQuery(currentGroupBox2).addClass("queryGroupSAMEEVENT");
			}
			else if(dropdownValue == "MIXEDEVENT")
			{
				var currentButton1 = "#btnPanelTimingGroup1_" + i
				var currentButton2 = "#btnPanelTimingGroup2_" + i

				var currentGroupBox1 = "#queryCriteriaDiv1_" + i
				var currentGroupBox2 = "#queryCriteriaDiv2_" + i

				jQuery(currentButton1).show()
				jQuery(currentButton2).show()	
			}
		}

	}
</script>
