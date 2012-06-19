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
  



<%=packageName ? "package ${packageName}\n\n" : ''%>class ${className}Controller {
    
    def index = { redirect(action:list,params:params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete:'POST', save:'POST', update:'POST']

    def list = {
        params.max = Math.min( params.max ? params.max.toInteger() : 10,  100)
        [ ${propertyName}List: ${className}.list( params ), ${propertyName}Total: ${className}.count() ]
    }

    def show = {
        def ${propertyName} = ${className}.get( params.id )

        if(!${propertyName}) {
            flash.message = "${className} not found with id \${params.id}"
            redirect(action:list)
        }
        else { return [ ${propertyName} : ${propertyName} ] }
    }

    def delete = {
        def ${propertyName} = ${className}.get( params.id )
        if(${propertyName}) {
            try {
                ${propertyName}.delete()
                flash.message = "${className} \${params.id} deleted"
                redirect(action:list)
            }
            catch(org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${className} \${params.id} could not be deleted"
                redirect(action:show,id:params.id)
            }
        }
        else {
            flash.message = "${className} not found with id \${params.id}"
            redirect(action:list)
        }
    }

    def edit = {
        def ${propertyName} = ${className}.get( params.id )

        if(!${propertyName}) {
            flash.message = "${className} not found with id \${params.id}"
            redirect(action:list)
        }
        else {
            return [ ${propertyName} : ${propertyName} ]
        }
    }

    def update = {
        def ${propertyName} = ${className}.get( params.id )
        if(${propertyName}) {
            if(params.version) {
                def version = params.version.toLong()
                if(${propertyName}.version > version) {
                    <%def lowerCaseName = grails.util.GrailsNameUtils.getPropertyName(className)%>
                    ${propertyName}.errors.rejectValue("version", "${lowerCaseName}.optimistic.locking.failure", "Another user has updated this ${className} while you were editing.")
                    render(view:'edit',model:[${propertyName}:${propertyName}])
                    return
                }
            }
            ${propertyName}.properties = params
            if(!${propertyName}.hasErrors() && ${propertyName}.save()) {
                flash.message = "${className} \${params.id} updated"
                redirect(action:show,id:${propertyName}.id)
            }
            else {
                render(view:'edit',model:[${propertyName}:${propertyName}])
            }
        }
        else {
            flash.message = "${className} not found with id \${params.id}"
            redirect(action:edit,id:params.id)
        }
    }

    def create = {
        def ${propertyName} = new ${className}()
        ${propertyName}.properties = params
        return ['${propertyName}':${propertyName}]
    }

    def save = {
        def ${propertyName} = new ${className}(params)
        if(!${propertyName}.hasErrors() && ${propertyName}.save()) {
            flash.message = "${className} \${${propertyName}.id} created"
            redirect(action:show,id:${propertyName}.id)
        }
        else {
            render(view:'create',model:[${propertyName}:${propertyName}])
        }
    }
}
