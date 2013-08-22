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

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Access Log
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton"><g:link class="list" controller="accessLog" action="list">View Access Log</g:link></span>
            </li>
        </ul>
    </div>
</div>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Users
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton"><g:link class="list" controller="authUser" action="list">User List</g:link></span>
            </li>
            <li>
                <span class="adminMenuButton"><g:link class="create" controller="authUser" action="create">Create User</g:link></span>
            </li>
        </ul>
    </div>
</div>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Groups
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton"><g:link class="list" controller="userGroup" action="list">Group List</g:link></span>
            </li>
            <li>
                <span class="adminMenuButton"><g:link class="create" controller="userGroup" action="create">Create Group</g:link></span>
            </li>

            <li>
                <span class="adminMenuButton"><g:link class="create" controller="userGroup" action="membership">Group Membership</g:link></span>
            </li>
        </ul>
    </div>
</div>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Study
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton"><g:link class="list" controller="secureObject" action="list">Study List</g:link></span>
            </li>
            <li>
                <span class="adminMenuButton"><g:link class="create" controller="secureObject" action="create">Add Study</g:link></span>
            </li>
        </ul>
    </div>
</div>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Concept Path Links
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton"><g:link class="list" controller="secureObjectPath" action="list">Path Link List</g:link></span>
            </li>
            <li>
                <span class="adminMenuButton"><g:link class="create" controller="secureObjectPath" action="create">Add Path Link</g:link></span>
            </li>
        </ul>
    </div>
</div>

<div class='navbarBox'>
    <div class="navcontainer1">
        <h1 class="panelHeader">
            Access Control
        </h1>
        <ul class="navlist">
            <li>
                <span class="adminMenuButton"><g:link class="create" controller="secureObjectAccess" action="manageAccess">Access Control by User/Group</g:link></span>
            </li>
            <li>
                <span class="adminMenuButton"><g:link class="create" controller="secureObjectAccess" action="manageAccessBySecObj">Access Control by Study</g:link></span>
            </li>
        </ul>
    </div>
</div>

<div class='navbarBox'>
<div class="navcontainer1">
<h1 class="panelHeader">
Roles
</h1>
<ul class="navlist">
<li>
<span class="adminMenuButton"><g:link class="list" controller="role" action="list">Role List</g:link></span>
</li>
<li>
<span class="adminMenuButton"><g:link class="create" controller="role" action="create">Create Role</g:link></span>
</li>
</ul>
</div>
</div>

<div class='navbarBox'>
<div class="navcontainer1">
<h1 class="panelHeader">
RequestMap Setup
</h1>
<ul class="navlist">
<li>
<span class="adminMenuButton"><g:link class="list" controller="requestmap" action="list">Requestmap List</g:link></span>
</li>
<li>
<span class="adminMenuButton"><g:link class="create" controller="requestmap" action="create">Requestmap Create</g:link></span>
</li>
</ul>
</div>
</div>


