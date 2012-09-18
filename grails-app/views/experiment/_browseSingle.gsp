<script type="text/javascript" src="${resource(dir:'js', file:'jQuery/ui.multiselect.js')}"></script>

<select id="experiments" name="experiments[]" size="${experiments.size()}">
	<g:each in="${experiments}" var="experiment">
		<option value="${experiment.id}">${experiment.accession}</option>
	</g:each>
</select>


