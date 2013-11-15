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


package annotation

import fm.FmFolder

class AmTagItemService {

    boolean transactional = true

    def serviceMethod() {

    }

    def getDisplayItems(Long key) {

        log.info "Searching amTagItems for tag template " + key

        def amTagItems

        if (key) {
            Map<String, Object> paramMap = new HashMap<Long, Object>();

            StringBuffer sb = new StringBuffer();
            sb.append("from AmTagItem ati where viewInGrid=1 ");
            sb.append(" and ati.amTagTemplate.id = :amTagTemplateId order by displayOrder");
            paramMap.put("amTagTemplateId", key);

            amTagItems = AmTagItem.findAll(sb.toString(), paramMap);

            log.info "amTagItems = " + amTagItems + " for key = " + key
        } else {
            log.error "Unable to retrieve an amTagItems with a null key value"
        }


        return amTagItems
    }

    def getChildDisplayItems(Long key) {
        log.info "Searching child amTagItems for tag template " + key

        def amTagItems

        if (key) {
            Map<String, Object> paramMap = new HashMap<Long, Object>();

            StringBuffer sb = new StringBuffer();
            sb.append("from AmTagItem ati where viewInChildGrid=1 ");
            sb.append(" and ati.amTagTemplate.id = :amTagTemplateId order by displayOrder");
            paramMap.put("amTagTemplateId", key);

            amTagItems = AmTagItem.findAll(sb.toString(), paramMap);

            log.info "amTagItems = " + amTagItems + " for key = " + key
        } else {
            log.error "Unable to retrieve an child amTagItems with a null key value"
        }

        return amTagItems

    }

    def getEditableItems(Long key) {
        log.info "Searching amTagItems for tag template " + key

        def amTagItems

        if (key) {
            Map<String, Object> paramMap = new HashMap<Long, Object>();

            StringBuffer sb = new StringBuffer();
            sb.append("from AmTagItem ati where editable= 1 ");
            sb.append(" and ati.amTagTemplate.id = :amTagTemplateId order by displayOrder");
            paramMap.put("amTagTemplateId", key);

            amTagItems = AmTagItem.findAll(sb.toString(), paramMap);

            log.info "amTagItems = " + amTagItems + " for key = " + key
        } else {
            log.error "Unable to retrieve an amTagItems with a null key value"
        }


        return amTagItems
    }

    def getRequiredItems(Long key) {

        log.info "Searching amTagItems for tag template " + key

        def amTagItems

        if (key) {
            Map<String, Object> paramMap = new HashMap<Long, Object>();

            StringBuffer sb = new StringBuffer();
            sb.append("from AmTagItem ati where required=1 ");
            sb.append(" and ati.amTagTemplate.id = :amTagTemplateId order by displayOrder");
            paramMap.put("amTagTemplateId", key);

            amTagItems = AmTagItem.findAll(sb.toString(), paramMap);

            log.info "amTagItems = " + amTagItems + " for key = " + key
        } else {
            log.error "Unable to retrieve an amTagItems with a null key value"
        }


        return amTagItems
    }

    def beforeValidate(FmFolder folder, params) {
        if (folder.folderName) {
            def amTagTemplate = AmTagTemplate.findByTagTemplateType(folder.folderName)
            def metaDataTagItems = amTagItemService.getRequiredItems(amTagTemplate.id)
            metaDataTagItems.each
                    {
                        if (it.tagItemType != 'FIXED') {
                            if (null != params."amTagItem_${it.id}" && "" != params."amTagItem_${it.id}") {
                                folder.errors.addError(it.displayName, it.displayName + " is required")
                            }
                        }
                    }
        } else {
            folder.errors.addError("folderName", "Folder name must have a value")
        }

    }

}
