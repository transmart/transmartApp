<%--<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/ui.multiselect.js')}"></script>--%>

<script type="text/javascript">
	jQuery(function(){
	  // choose either the full version
	  jQuery(".multiselect").multiselect();
	  // or disable some features
	  //jQuery(".multiselect").multiselect({sortable: false, searchable: false});
	});
</script>

<select id="multiselectbox" class="multiselect" multiple="multiple" name="experiments[]">
	<g:each in="${experiments}" var="experiment">
		<option value="${experiment.id}">${experiment.title}</option>
	</g:each>
</select>


