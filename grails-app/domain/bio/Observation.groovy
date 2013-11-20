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


package bio

class Observation {
    Long id
    String name
    String code
    String description
    Long etlId
    String type
    String codeSource
    //	static hasMany=[experiments:Experiment,literatures:Literature,analyses:BioAssayAnalysis]

    String uniqueId
    static transients = ['uniqueId']


    static mapping = {
        table 'BIO_OBSERVATION'
        version false
        cache usage: 'read-only'
        id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID']
        columns {
            id column: 'BIO_OBSERVATION_ID'
            name column: 'OBS_NAME'
            code column: 'OBS_CODE'
            description column: 'OBS_DESCR'
            etlId column: 'ETL_ID'
            type column: 'OBS_TYPE'
            codeSource column: 'OBS_CODE_SOURCE'

            //	experiments joinTable:[name:'BIO_DATA_UID', key:'BIO_OBSERVATION_ID']
            //	literatures joinTable:[name:'BIO_DATA_UID', key:'BIO_OBSERVATION_ID']
            //	analyses joinTable:[name:'BIO_DATA_UID', key:'BIO_OBSERVATION_ID']
        }
    }

    /**
     * Use transient property to support unique ID for tagValue.
     * @return tagValue's uniqueId
     */
    String getUniqueId() {
        if (uniqueId == null) {
            if (id) {
                BioData data = BioData.get(id);
                if (data != null) {
                    uniqueId = data.uniqueId
                    return data.uniqueId;
                }
                return null;
            } else {
                return null;
            }
        } else {
            return uniqueId;
        }
    }

    /**
     * Find concept code by its uniqueId
     * @param uniqueId
     * @return concept code with matching uniqueId or null, if match not found.
     */
    static Observation findByUniqueId(String uniqueId) {
        Observation cc;
        BioData bd = BioData.findByUniqueId(uniqueId);
        if (bd != null) {
            cc = Observation.get(bd.id);
        }
        return cc;
    }


}