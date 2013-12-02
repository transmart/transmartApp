<!--
  tranSMART - translational medicine data mart
  
  Copyright 2008-2012 Janssen Research & Development, LLC.
  
  This product includes software developed at Janssen Research & Development, LLC.
  
  This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
  as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
  1.    You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
  2.    You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
  
  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
  
  You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
  
-->

<html lang="en">
<head>
  <meta charset="utf-8">
  <title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
  <link rel="stylesheet" type="text/css" href="${resource(dir:'css/jQueryUI/smoothness', file:'jquery-ui-1.8.17.custom.css')}">  
  <script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-1.7.1.min.js')}"></script>
  <script>jQuery.noConflict();</script>
  <script type="text/javascript" src="${resource(dir:'js/jQuery', file:'jquery-ui-1.8.17.custom.min.js')}"></script>
  <script>
  jQuery(document).ready(function() {
    jQuery("#tabs").tabs({
        cache:true,
        load: function (e, ui) {
               jQuery(ui.panel).find(".tab-loading").remove();
        },
        select: function (e, ui) {
               var $panel = jQuery(ui.panel);

               if ($panel.is(":empty")) {
                   $panel.append("<div class='tab-loading'>Loading...</div>")
               }
        }
    });
  });
  </script>
</head>
<body style="margin:0px; font: 12px verdana, arial, helvetica, sans-serif;">
    <div id="tabs">
      <ul>
        <li><a href="${createLink(controller:'searchHelp',action:'listAllTrials')}">Clinical Trials</a></li>
        <li><a href="${createLink(controller:'searchHelp',action:'listAllCompounds')}">Compounds</a></li>
        <li><a href="${createLink(controller:'searchHelp',action:'listAllDiseases')}">Diseases</a></li>
        <li><a href="${createLink(controller:'searchHelp',action:'listAllPathways')}">Pathways</a></li>
        <li><a href="${createLink(controller:'searchHelp',action:'listAllGeneSignatures')}">Gene Signatures/Lists</a></li>
      </ul>
    </div> 
</body>
</html>