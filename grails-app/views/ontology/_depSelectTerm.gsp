<g:if test="${tagtype != 'ALL'}">
Terms:<br>
<div style="overflow:auto; width:200px;">
	<g:select class="searchform" name="tagterm" id="tagterm" from="${tags}" multiple="multiple"  size="5" style="min-width:200px"></g:select>
</div>
</g:if>



