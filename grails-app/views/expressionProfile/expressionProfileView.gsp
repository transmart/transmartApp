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
<g:setProvider library="prototype"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<meta name="layout" content="main" />
	<title>Expression Profile View</title>
	<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}" />
</head>

<body>

<!--  wait image for gene selections -->
<g:waitIndicator divId="expr_profile_main_loading_div" />
<!--  if no expression profile results, render noResult -->
<g:if test="${epr==null || epr.profCount==0 }">
    <g:render template="/search/noResult" />
</g:if>
<g:else>
<div id="expr_profile_main_div" style="display: block;" class="body">
<table>

<g:if test="${epr.genes.size>=grailsApplication.config.com.recomdata.search.gene.max}">
	<tr><td style="color: red; font-weight: bold;">More than ${grailsApplication.config.com.recomdata.search.gene.max} genes detected, please refine search to see extra detail!</td></tr>
</g:if>

	<tr>
		<td>
		<table class="jubfilter">
			<tr><th>Filter (Note: search found ${epr.profCount} studies)</th></tr>
			<tr>
				<td>
				<table class="jubfiltersection">
					<tr>
						<td>Gene:</td>
						<td>
							<g:select from="${epr.genes}" name="bioMarkerId"
								optionKey="id" optionValue="name"
								value="${session.searchFilter.exprProfileFilter.bioMarkerId}"
								onChange="${remoteFunction(action:'selectGene',
								                           before:'toggleVisible(\'expr_profile_main_loading_div\'); toggleVisible(\'expr_profile_main_div\');',
								                           onComplete:'toggleVisible(\'expr_profile_main_loading_div\'); toggleVisible(\'expr_profile_main_div\');',
								                           update:'expr_profile_main_div', params:'\'bioMarkerId=\'+this.value')}" />
						</td>
					</tr>
					<tr>
						<td>Disease:</td>
						<td>
							<g:select from="${epr.diseases}" name="bioDiseaseId"
								optionKey="id" optionValue="preferredName"
								value="${session.searchFilter.exprProfileFilter.bioDiseaseId}"
								onChange="${remoteFunction(action:'selectDisease',
								                           before:'toggleVisible(\'expr_profile_main_loading_div\'); toggleVisible(\'expr_profile_main_div\');',
								                           onComplete:'toggleVisible(\'expr_profile_main_loading_div\'); toggleVisible(\'expr_profile_main_div\');',
														   update:'expr_profile_main_div', params:'\'bioDiseaseId=\'+this.value')}" />
						</td>
					</tr>
					<tr>
						<td>Probe Set:</td>
						<td>
							<g:select from="${epr.probeSets}" name="probeSet"
								value="${session.searchFilter.exprProfileFilter.probeSet}"
								onChange="${remoteFunction(action:'selectProbeset',
								                           before:'toggleVisible(\'expr_profile_graph_div_loading\'); toggleVisible(\'expr_profile_graph_div\');',
								                           onComplete:'toggleVisible(\'expr_profile_graph_div_loading\'); toggleVisible(\'expr_profile_graph_div\');',
								                           update:'expr_profile_graph_div', params:'\'probeSet=\'+this.value')}" />
						</td>
					</tr>
				</table>
				</td>
			</tr>
		</table>
		</td>
		</tr>

		<tr>
		<td>
			<br>
			<!--  wait image for probeset and disease selections -->
			<g:waitIndicator divId="expr_profile_graph_div_loading" />
			<div id="expr_profile_graph_div" style="display: block;"><g:render template="graphView" model="graphURL:graphURL" /></div>
		</td>
	</tr>

	<tr>
		<td><br/>
		Data courtesy of <a style="font-weight: bold; line-height: 13px;"
							onclick="window.open('http://compbio.dfci.harvard.edu/tgi/cgi-bin/tucan/tucan.pl');">Dana-Farber Cancer Institute GeneChip Oncology Database</a>.</td>
	</tr>
</table>
</div>
</g:else>
</body>
</html>
