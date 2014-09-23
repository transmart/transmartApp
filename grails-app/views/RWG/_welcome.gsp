<center>
<div class="welcome" style="margin: 40px; background: #F4F4F4; border: 1px solid #DDD; padding: 20px; width: 400px; text-align: center; border-top-left-radius: 20px; border-bottom-right-radius: 20px">

<p><b>Welcome tranSMART for U-BIOPRED</b></p>

<p>The <b>Browse</b> window lets you search and dive into the information contained in tranSMART,
including Programs, Studies, Assays and the associated Analyses Results, Subject Level Data and Raw Files.
This is also the location to export files stored in tranSMART. Note: to edit the Program, Study, or Assay
information, you must be logged in as an Administrator.
</p>
<p>The <b>Analyze</b> window lets you perform a number of analyses either on studies selected
in the Browse window, or from the global search box located in the top ribbon of your screen.
More information about the analyses you can perform is available in the “Help” section of the "Utilities" menu.
</p>
<br><br>
<a id="etrikspowered" target="_blank" href="http://www.etriks.org" style="text-decoration: none;">
    <div>
        <img src="${resource(dir:'images', file: 'UBIOPRED_logo.png')}" alt="U-BIOPRED" style="height:35px;vertical-align:middle;margin-bottom: 12px;">
        <span style="font-size:20px;display: inline-block;line-height: 35px; height: 35px;">&nbsp;+&nbsp;</span>
        <img src="${resource(dir:'images', file: 'eTRIKS_logo.png')}" alt="eTRIKS" style="height:35px;vertical-align:middle;margin-bottom: 12px;">
    </div>
</a>
</div>


<sec:ifAnyGranted roles="ROLE_ADMIN">
	<div style="padding: 0px 16px 16px 16px; border-radius: 8px; border: 1px solid #DDD; width: 20%">
		<h4>Admin Tools</h4>
		<span class="greybutton buttonicon addprogram">Add new program</span>
	</div>
</sec:ifAnyGranted>

<br/><br/>
</center>