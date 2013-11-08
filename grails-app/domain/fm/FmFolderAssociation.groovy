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

package fm

class FmFolderAssociation implements Serializable {

    def grailsApplication

    static transients = ['bioObject']

    String objectUid
    String objectType
    FmFolder fmFolder

    static mapping = {
        table 'fm_folder_association'
        version false
        cache true
        sort "objectUid"
        id composite: ["objectUid", "fmFolder"]
        fmFolder column: 'folder_id'
    }

    static constraints = {

//		objectUid(unique: 'fmFolder')

    }

    static FmFolderAssociation get(String objectUid, long fmFolderId) {
        find 'from FmFolderAssociation where objectUid=:objectUid and fmFolder.id=:fmFolderId',
                [objectUid: objectUid, fmFolderId: fmFolderId]
    }

    static boolean remove(String objectUid, FmFolder fmFolder, boolean flush = false) {
        FmFolderAssociation instance = FmFolderAssociation.findByObjectUidAndFmFolder(objectUid, fmFolder)
        instance ? instance.delete(flush: flush) : false
    }

    public getBioObject() {
        log.info "ObjectUID=" + this.objectUid
        def bioData = bio.BioData.findByUniqueId(this.objectUid)
        def clazz = lookupDomainClass()
        if (!clazz || !bioData) {
            return null
        } else {
//			return clazz.getObjectUid(this.objectUid)
            return clazz.get(bioData.id)
        }

    }

    protected Class lookupDomainClass() {
        //		def conf = SpringSecurityUtils.securityConfig
        // This probably should come from the config file

        String domainClassName = this.objectType //conf.rememberMe.persistentToken.domainClassName ?: ''
        def clazz = grailsApplication.getClassForName(domainClassName)
        if (!clazz) {
            log.error "Persistent token class not found: '${domainClassName}'"
        }

        return clazz
    }

    /**
     * override display
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("objectUid: ").append(this.objectUid).append(", objectType: ").append(this.objectType);
        sb.append(", Folder: ").append(this.fmFolder.id);
        return sb.toString();
    }


}
