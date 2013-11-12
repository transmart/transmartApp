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


package org.transmart.biomart

class BioAssayAnalysisPlatform {
    Long id
    String platformName
    String platformVersion
    String platformDescription
    static mapping = {
        table 'BIO_ASY_ANALYSIS_PLTFM'
        version false
        cache usage: 'read-only'
        id generator: 'sequence', params: [sequence: 'SEQ_BIO_DATA_ID']
        columns {
            id column: 'BIO_ASY_ANALYSIS_PLTFM_ID'
            platformName column: 'PLATFORM_NAME'
            platformVersion column: 'PLATFORM_VERSION'
            platformDescription column: 'PLATFORM_DESCRIPTION'
        }
    }
    static constraints = {
        platformName(nullable: true, maxSize: 400)
        platformVersion(nullable: true, maxSize: 400)
        platformDescription(nullable: true, maxSize: 2000)
    }
}
