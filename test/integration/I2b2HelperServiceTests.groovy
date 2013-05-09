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


/**
* $Id: I2b2HelperServiceTests.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
*/
import grails.test.*
import groovy.sql.Sql

class I2b2HelperServiceTests extends GrailsUnitTestCase {
	def i2b2HelperService
    def dataSource

    private static dataInserts = [
            '''insert into qt_query_master (query_master_id, name, user_id, group_id, create_date, request_xml)
                values(-1, '', '', '',  '1980-01-01', '<foobar><panel><panel_number>20</panel_number><invert>1</invert></panel></foobar>');''',
            '''insert into qt_query_instance (query_instance_id, query_master_id, user_id, group_id, start_date)
                values(-2, -1, '', '', '1980-01-01');''',
            '''insert into qt_query_result_instance (result_instance_id, query_instance_id, result_type_id, status_type_id, start_date)
                values(-50, -2, 3, 3, '1980-01-01');''',
    ]

    protected void setUp() {
        super.setUp()
        dataInserts.each {
            new Sql(dataSource).executeInsert(it)
        }
    }

    void testRenderQueryDefinition() {
		StringWriter sw = new StringWriter()
		String rID = "-50"
		String title = "Subset1"
		i2b2HelperService.renderQueryDefinition(rID, title, sw)
        assert sw.toString() == '''<table class='analysis'><tr><th>Subset1</th></tr><tr><td><br><b>NOT</b><br><b>(</b><b>)</b></td></tr></table>'''
    }
}
