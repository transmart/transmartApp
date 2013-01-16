			
			<g:if test="${hide}"><g:set var="csshide">display: none</g:set></g:if>
			
			<div id="box-search" style="${csshide}">
		        <div id="title-search-div" class="ui-widget-header boxtitle">
			         <h2 style="float:left" class="title">Active Filters</h2>
					 <h2 style="float:right; padding-right:5px;" class="title">
					 	<a href="#" onclick="clearSearch(); return false;">Clear</a>
					 </h2> 
					 <div id="filterbutton" class="greybutton filterbrowser">
						<img src="${resource(dir:'images', file:'filter.png')}"/> Filter
					 </div>
				</div>
				<div id="active-search-div" class="boxcontent">
					&nbsp;
				</div>
			</div>