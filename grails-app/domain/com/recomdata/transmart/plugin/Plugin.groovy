/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/


package com.recomdata.transmart.plugin

class Plugin {

    long id
    String name
    String pluginName
    Boolean hasModules
    Boolean active
    Boolean hasForm
    String defaultLink
    String formLink
    String formPage

    static hasMany = [modules: PluginModule]

    static mapping = {
        table 'SEARCHAPP.PLUGIN'
        cache true
        version false
        id column: 'PLUGIN_SEQ',
                generator: 'sequence',
                params: [sequence: 'SEARCHAPP.PLUGIN_SEQ']
        active type: 'yes_no'
        hasModules type: 'yes_no'
        hasForm type: 'yes_no'
        modules lazy: true, cascade: "all-delete-orphan", sort: 'name', order: 'asc', cache: true
    }

    static constraints = {
        name(nullable: false)
        pluginName(nullable: false)
        hasModules(nullable: false)
        hasForm(nullable: false)
        defaultLink(nullable: false)
        formLink(nullable: true)
        formPage(nullable: true)
    }
}
