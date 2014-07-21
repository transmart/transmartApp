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


package transmartapp

import grails.converters.JSON
import org.transmart.biomart.BioAssayPlatform

class BioAssayPlatformController {

//	measurements  = org.transmart.biomart.BioAssayPlatform.executeQuery("SELECT DISTINCT platformType FROM BioAssayPlatform as p ORDER BY p.platformType")
//	vendors = org.transmart.biomart.BioAssayPlatform.executeQuery("SELECT DISTINCT vendor FROM BioAssayPlatform as p ORDER BY p.vendor")
//	technologies = org.transmart.biomart.BioAssayPlatform.executeQuery("SELECT DISTINCT platformTechnology FROM BioAssayPlatform as p ORDER BY p.platformTechnology")
//	platforms = org.transmart.biomart.BioAssayPlatform.executeQuery("SELECT DISTINCT name FROM BioAssayPlatform as p ORDER BY p.name")


    def platformsForVendor = {

        def platforms;
        if (params.type) {
            platforms = BioAssayPlatform.executeQuery("SELECT bd.unique_id, p.name FROM BioAssayPlatform p, BioData bd WHERE p.id=bd.id and bd.bioDataType='BIO_ASSAY_PLATFORM' and p.vendor = :term AND p.platformType = :type", [term: params.vendor, type: params.type]);
        } else {
            platforms = BioAssayPlatform.executeQuery("SELECT id, name, accession FROM BioAssayPlatform p WHERE p.vendor = :term", [term: params.vendor]);
        }

        def itemlist = [];
        for (platform in platforms) {
            itemlist.add([id: platform[0], title: platform[1], accession: platform[2]])
        }

        def result = [rows: itemlist]
        render result as JSON;

    }

    def getSelections = {

        Map<String, Object> paramMap = new HashMap<Long, Object>();

        // construct query
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT bd.unique_id, p." + param.name + " FROM BioAssayPlatform p, BioData bd  WHERE p.id=bd.id and bd.bioDataType='BIO_ASSAY_PLATFORM' ");

        if (params.vendor != null && params.vendor != "") {
            sb.append(" and p.vendor = :vendor");
            paramMap.put("vendor", params.vendor);
        }

        if (params.platformTechnology != null && params.platformTechnology != "") {
            sb.append(" and p.platformTechnology = :platformTechnology");
            paramMap.put("platformTechnology", params.platformTechnology);
        }

        if (params.platformType != null && params.platformType != "") {
            sb.append(" and p.platformType = :platformType");
            paramMap.put("platformType", params.platformType);
        }

        if (params.platformName != null && params.platformName != "") {
            sb.append(" and p.name = :platformName");
            paramMap.put("platformName", params.platformName);
        }

        // sort
        sb.append(" order by ").append(param.sort);

        List<BioAssayPlatform> platforms = BioAssayPlatform.executeQuery(sb.toString(), paramMap);
    }

    def getPlatforms = {

        Map<String, Object> paramMap = new HashMap<Long, Object>();

        // construct query
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT bd.unique_id, p.name FROM BioAssayPlatform p, BioData bd  WHERE p.id=bd.id and bd.bioDataType='BIO_ASSAY_PLATFORM' ");

        if (params.vendor != null && params.vendor != "") {
            sb.append(" and p.vendor = :vendor");
            paramMap.put("vendor", params.vendor);
        }

        if (params.platformTechnology != null && params.platformTechnology != "") {
            sb.append(" and p.platformTechnology = :platformTechnology");
            paramMap.put("platformTechnology", params.platformTechnology);
        }

        if (params.platformType != null && params.platformType != "") {
            sb.append(" and p.platformType = :platformType");
            paramMap.put("platformType", params.platformType);
        }

        if (params.platformName != null && params.platformName != "") {
            sb.append(" and p.name = :platformName");
            paramMap.put("platformName", params.platformName);
        }

        // sort
        sb.append(" order by name");

        List<BioAssayPlatform> platforms = BioAssayPlatform.executeQuery(sb.toString(), paramMap);

    }
}
