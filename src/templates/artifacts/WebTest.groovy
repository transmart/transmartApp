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
class @webtest.name.caps@Test extends grails.util.WebTest {

    // Unlike unit tests, functional tests are often sequence dependent.
    // Specify that sequence here.
    void suite() {
        test@webtest.name.caps@ListNewDelete()
        // add tests for more operations here
    }

    def test@webtest.name.caps@ListNewDelete() {
        webtest('@webtest.name.caps@ basic operations: view list, create new entry, view, edit, delete, view') {
            invoke(url:'@webtest.name.lower@')
            verifyText(text:'Home')

            verifyListPage(0)

            clickLink(label:'New @webtest.name.caps@')
            verifyText(text:'Create @webtest.name.caps@')
            clickButton(label:'Create')
            verifyText(text:'Show @webtest.name.caps@', description:'Detail page')
            clickLink(label:'List', description:'Back to list view')

            verifyListPage(1)

            group(description:'edit the one element') {
                clickLink(label:'Show', description:'go to detail view')
                clickButton(label:'Edit')
                verifyText(text:'Edit @webtest.name.caps@')
                clickButton(label:'Update')
                verifyText(text:'Show @webtest.name.caps@')
                clickLink(label:'List', description:'Back to list view')
            }

            verifyListPage(1)

            group(description:'delete the only element') {
                clickLink(label:'Show', description:'go to detail view')
                clickButton(label:'Delete')
                verifyXPath(xpath:"//div[@class='message']", text:/@webtest.name.caps@.*deleted./, regex:true)
            }

            verifyListPage(0)
        }
    }

    String ROW_COUNT_XPATH = "count(//td[@class='actionButtons']/..)"

    def verifyListPage(int count) {
        ant.group(description:"verify @webtest.name.caps@ list view with $count row(s)") {
            verifyText(text:'@webtest.name.caps@ List')
            verifyXPath(xpath:ROW_COUNT_XPATH, text:count, description:"$count row(s) of data expected")
        }
    }
}