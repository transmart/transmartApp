<table class="biobankResults">
	<tr>
		<th>
			Sample Tube ID
		</th>
		<th>
			Client Sample Tube ID
		</th>
		<th>
			Container ID
		</th>		
		<th>
			Source Type
		</th>		
		<th>
			Accession Number
		</th>
		<th>
			Import Date
		</th>						
	</tr>

	<g:each var="sample" in="${samples}" status="iterator">
		<g:if test="${sample}">
			<tr>
				<td class="${(iterator % 2) == 0 ? '' : 'altRow'}">
					${sample.id}
				</td>
				<td class="${(iterator % 2) == 0 ? '' : 'altRow'}">
					${sample.client_sample_tube_id}
				</td>
				<td class="${(iterator % 2) == 0 ? '' : 'altRow'}">
					${sample.container_id}
				</td>
				<td class="${(iterator % 2) == 0 ? '' : 'altRow'}">
					${sample.source_type}
				</td>
				<td class="${(iterator % 2) == 0 ? '' : 'altRow'}">
					${sample.accession_number}
				</td>
				<td class="${(iterator % 2) == 0 ? '' : 'altRow'}">
					${sample.import_date}
				</td>
			</tr>															
		</g:if>
	</g:each>
</table>