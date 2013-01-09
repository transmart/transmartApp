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
<div class="body" style="clear:both; width:99%;margin-left:5px;margin-top:20px">
<table class="searchbox"  style="border: 0px; align: left; margin: 5px 5px;">
    <tr>
        <td colspan="2">&nbsp;</td>
        <td colspan="4" style="white-space: nowrap;">
            <div id="search-categories"></div>
        </td>
    </tr>
    <tr>
        <td style="vertical-align: middle;">
            <g:link controller="search">
                <img src="${resource(dir:'images',file:grailsApplication.config.com.recomdata.searchtool.smallLogo)}" alt="tranSMART" />
            </g:link>
        </td>
        <td style="vertical-align: middle;">
            <img src="${resource(dir:'images',file:'c-med.gif')}" alt=">" />
        </td>
        <td style="vertical-align: middle;">
            <div id="search-text"></div>
        </td>
        <td style="vertical-align: middle;">
            <g:form name="form" controller="search" action="search">
                <g:hiddenField id="id-field" name="id" value="" />
                <input type="hidden" name="sourcepage" value="search" />
                <button id="search-button" style="vertical-align: middle;" type="button" onclick="searchOnClick();">Search</button>
            </g:form>
        </td>
        <td style="vertical-align: middle; white-space: nowrap;">
            <div id="linkbuttons-div">
                <a id="browse-link" class="tiny" style="text-decoration:underline;color:blue;font-size:11px;" href=""
                    onclick="popupWindow('${createLink(controller:'searchHelp', action:'list')}', 'searchhelpwindow');return false;">
                    browse
                </a>
                <br />
                <a id="savedfilters-link" class="tiny" style="text-decoration:underline;color:blue;font-size:11px;"
                    href="${createLink(controller:'customFilter', action:'list')}">
                    saved filters
                </a>
            </div>
        </td>
        <td style="width: 100%;"></td>
    </tr>
</table>
</div>