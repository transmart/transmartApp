<%--<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/ui.multiselect.js')}"></script>--%>

<script type="text/javascript">
	jQuery(function(){
	  // choose either the full version
	  jQuery(".multiselect").multiselect();
	  // or disable some features
	  //jQuery(".multiselect").multiselect({sortable: false, searchable: false});
	});
</script>

<select id="multiselectbox" class="multiselect" multiple="multiple" name="analyses[]">
	<g:each in="${analyses}" var="analysis">
		<option value="${analysis[0]}">${analysis[1]}</option>
	</g:each>
</select>


