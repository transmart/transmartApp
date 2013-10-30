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

<%-- Quick, self-contained replica of the Ext utilities menu, for use on pages without the Ext library (faceted search) --%>
			<g:javascript>
				function toggleMenu() {
					jQuery('#utilitiesMenu').fadeToggle();
				}
				
				jQuery(document).ready(function() {
					jQuery('#main').click(function() {
						jQuery('#utilitiesMenu').hide();
					});
				});
			</g:javascript>
			<style type="text/css">
				#utilitiesMenu{font:normal 11px tahoma,arial,sans-serif; border:1px solid #718bb7;z-index:15000;zoom:1;background:#f0f0f0 repeat-y; padding: 4px; position: absolute; right: 0; top: 24px; display: none;}
				#utilitiesMenu a{text-decoration:none!important; font-weight:normal!important;}
				#utilitiesMenuList {background:transparent;border:0 none; list-style: none;}
				#utilitiesMenu li{line-height:100%; padding: 4px; border:1px solid #f0f0f0; cursor: pointer;}
				#utilitiesMenu li:hover {background-color: #DDDDFF; border:1px solid #718bb7}
				#utilitiesMenuButton {background-color: #DDD; border-left: 1px solid black; cursor: pointer; padding: 4px;}
				li.utilMenuSeparator { padding: 0px; font-size: 1px; line-height: 1px;}
				span.utilMenuSeparator {display: block;
					font-size: 1px;
					line-height: 1px;
					margin: 2px 3px;
					background-color: #E0E0E0;
					border-bottom: 1px solid white;
					overflow: hidden;
				}
			</style>
			<th>
				<div style="text-align: right"><div onclick="toggleMenu(); return false;" id="utilitiesMenuButton">Utilities</div></div>
			</th>
			
			<g:set var="buildNumber"><g:meta name="environment.BUILD_NUMBER"/></g:set>
			<g:set var="buildId"><g:meta name="environment.BUILD_ID"/></g:set>
			<div id="utilitiesMenu">
				<ul id="utilitiesMenuList">
					<li><a href="#" onclick="jQuery('#utilitiesMenu').hide(); popupWindow('${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}', '_help')">Help</a></li>
					<li><a onclick="jQuery('#utilitiesMenu').hide();" href="mailto:${grailsApplication.config.com.recomdata.searchtool.contactUs}">Contact Us</a></li>
					<li><a href="#" onclick="jQuery('#utilitiesMenu').hide(); alert('${grailsApplication.config.com.recomdata.searchtool.appTitle}', 'Build Version: ${buildNumber} - ${buildId}')">About</a></li>
					<li class="utilMenuSeparator"><span class="utilMenuSeparator">&nbsp;</span></li>
					<li><a onclick="jQuery('#utilitiesMenu').hide();" href="${createLink(controller: 'login', action: 'forceAuth')}">Login</a></li>
					<li class="utilMenuSeparator"><span class="utilMenuSeparator">&nbsp;</span></li>
					<li><a onclick="jQuery('#utilitiesMenu').hide();" href="${createLink(controller: 'logout')}">Log Out</a></li>
				</ul>
			</div>