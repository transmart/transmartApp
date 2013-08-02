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
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />


<script type="text/javascript">

function init(){

setTimeout("showheatmap();",500)
}

function showheatmap(){
	document.showheatmapform1.submit();


}

</script>
</head>
<body onLoad="javascript:init()">
<div style="margin-top:100px;text-align: center;">

<img src="${resource(dir:'images',file:'loader-large.gif')}" alt="loading" />
</div>

<div style="margin-top:20px; text-align: center;"><b>Generating Heatmap...</b>
</div>

<div style="display:none">
<g:form name="showheatmapform1" controller="heatmap" action="showheatmap" >
</g:form>
</div>
</body>
</html>
