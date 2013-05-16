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



import au.com.bytecode.opencsv.CSVReader
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class TsvFileReaderController {

    def config = ConfigurationHolder.config;
    String temporaryImageFolder = config.RModules.temporaryImageFolder
    def DEFAULT_FIELDS = ['chromosome', 'start', 'end', 'pvalue', 'fdr']
    char DEFAULT_SEPARATOR = '\t'
    int TO_LAST_ROW = -1

    def index = {
        response.contentType = 'text/json'
        if(!(params?.jobName ==~ /(?i)[-a-z0-9]+/)) {
            render new JSON([error: 'jobName parameter is required. It should contains just alphanumeric characters and dashes.'])
            return
        }
        def file = new File("${temporaryImageFolder}", "${params.jobName}/survival-test.txt")
        if(file && file.exists()) {
            def from = params.from ? params.int('from') : 1
            def to = params.max ? from + params.int('max') - 1 : -1
            def fields = (params.fields?.split('\\s*,\\s*') ?: DEFAULT_FIELDS) as Set<String>

            def obj = parseTsv(file, from, to, fields)

            def json = new JSON(obj)
            json.prettyPrint = false
            render json
        } else {
            response.status = 404
            render '[]'
        }
    }

    def parseTsv(file, from, to, fields) {
        def resultRows = []
        def csvReader = new CSVReader(new BufferedReader(new InputStreamReader(new FileInputStream(file), 'UTF-8')), DEFAULT_SEPARATOR)
        int rowNumber = 0
        try {
            String[] headerRow
            if((headerRow = csvReader.readNext()) != null) {
                def useFields = []
                def usePositions = []
                headerRow.eachWithIndex{ String entry, int i ->
                    if(fields.contains(entry)) {
                        useFields << entry
                        usePositions << i
                    }
                }
                String[] row
                while ((row = csvReader.readNext()) != null) {
                    rowNumber += 1
                    if(rowNumber >= from && (to == TO_LAST_ROW || rowNumber <= to)) {
                        def rowMap = [:]
                        def useValues = row[usePositions]
                        useFields.eachWithIndex{ String entry, int i ->
                            rowMap[entry] = useValues[i]
                        }
                        resultRows.add(rowMap)
                    }
                }
            }
        } finally {
            csvReader.close()
        }
        [totalCount: rowNumber, result: resultRows]
    }
}
