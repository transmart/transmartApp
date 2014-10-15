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


class SampleService {

    def dataSource
    def i2b2HelperService
    def grailsApplication
    def solrService

    boolean transactional = true

    //Populate the QT_PATIENT_SAMPLE_COLLECTION table based on a result_instance_id.
    public void generateSampleCollection(String result_instance_id)
    {
        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
        sql.execute("INSERT INTO QT_PATIENT_SAMPLE_COLLECTION (SAMPLE_ID, PATIENT_ID, RESULT_INSTANCE_ID) SELECT DISTINCT DSSM.SAMPLE_ID, DSSM.patient_id, ? FROM QT_PATIENT_SET_COLLECTION QT INNER JOIN DE_SUBJECT_SAMPLE_MAPPING DSSM ON DSSM.PATIENT_ID = QT.PATIENT_NUM WHERE RESULT_INSTANCE_ID = ?", [result_instance_id.toInteger(), result_instance_id.toInteger()])
    }

    public loadSampleStatisticsObject(String result_instance_id)
    {
        //This is the value object we store the count values in.
        def sampleSummary = [:]

        groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)

        StringWriter writer1 	= new StringWriter()
        PrintWriter pw1			= new PrintWriter(writer1)

        i2b2HelperService.renderQueryDefinition(result_instance_id, "Query Definition", pw1)

        sampleSummary["queryDefinition"] = writer1.toString()

        grailsApplication.config.edu.harvard.transmart.sampleBreakdownMap.each{
            currentCountVariable ->

                sampleSummary[currentCountVariable.value] = solrService.getFacetCountForField(currentCountVariable.key, result_instance_id, 'sampleExplorer')

                log.debug("Finished count for field ${currentCountVariable.value} - ${currentCountVariable.key}")
                log.debug(sampleSummary[currentCountVariable.value])

        }

        return sampleSummary
    }

}