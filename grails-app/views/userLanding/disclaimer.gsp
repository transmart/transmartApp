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

<head>
	<meta name='layout' content='main' />
	<title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
</head>
<body>
	<center>
	<div style="width: 400px; margin: 50px auto 50px auto;">
		<img style="display: block; margin: 12px auto;" src="${resource(dir:'images',file:grailsApplication.config.com.recomdata.searchtool.largeLogo)}" alt="Transmart" />
		<center><h3>ATTENTION: Users of ${grailsApplication.config.com.recomdata.searchtool.appTitle}</h3></center>
		<div style="text-align: justify; margin: 12px;">
			${grailsApplication.config.com.recomdata.disclaimer}
		</div>
		<center>		
			<g:form name="disclaimer" method="post" id="disclaimerForm">
				<g:actionSubmit value="I agree" action="agree" />
				<g:actionSubmit value="I disagree" action="disagree" />
			</g:form>	
		</center>
	</div>
	</center>
</body>