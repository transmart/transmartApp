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
	<title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
	<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
	<script type="text/javascript">

	function refreshParent(newurl){
     parent.window.close();
	 if(parent!=null && parent.window.opener!=null && !parent.window.opener.closed){
		parent.window.opener.location =newurl;
		}
	}

	</script>
</head>
<body>
<div id="summary">
	<p class="Title">
		<span class="Title">
		</span>
	</p>
	<div id="SummaryHeader">
		<span class="SummaryHeader">Available Compounds</span>
	</div>

	<table class="trborderbottom" width="100%">
		<g:each in="${compounds}" status ="i" var="rec">
			<g:set var="k" value="${rec[0]}" />
			<g:set var="c" value="${rec[1]}" />
			<tr style="border-bottom:1px solid #CCCCCC;padding-botton:2px;">
				<td style="width:150px;">${createKeywordSearchLink(popup:true, jsfunction:"refreshParent", keyword:k)}</td>
				<td>
					${c.genericName}
					<g:if test="${c.cntoNumber?.length() > 0}">
						, ${c.cntoNumber}
					</g:if>
					<g:if test="${c.brandName?.length() > 0}">
						, ${c.brandName}
					</g:if>
					<g:if test="${c.number?.length() > 0 && c.number != k.keyword}">
						, ${c.number}
					</g:if>
					<g:if test="${c.mechanism?.length() > 0}">
						- ${c.mechanism}
					</g:if>
				</td>
			</tr>
		</g:each>
	</table>
	<br />
</div>
</body>
</html>