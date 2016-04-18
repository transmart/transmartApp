/*
 * Copyright © 2013-2016 The Hyve B.V.
 *
 * This file is part of Transmart.
 *
 * Transmart is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Transmart.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmart.logging

import groovy.json.JsonSlurper
import org.apache.log4j.Category
import org.apache.log4j.Level
import org.apache.log4j.spi.LoggingEvent
import org.gmock.WithGMock
import org.junit.Test
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

class JsonLayoutTests {

    static LoggingEvent makeEvent(msg) {
        new LoggingEvent("", new Category('debug'), Level.DEBUG, msg, null)
    }

    @Test
    void testSingleLine() {
        def j = new JsonLayout(singleLine: true, conversionPattern: "%m%n")
        assertThat j.format(makeEvent([1,2,3])), is('[1,2,3]\n')
        assertThat j.format(makeEvent([foo: 'bar', baz: 42, qux: null])), is ('{"foo":"bar","baz":42,"qux":null}\n')
    }

    @Test
    void testMultiLine() {
        def j = new JsonLayout(conversionPattern: '%m')
        def obj = [1,2,3]
        assertThat new JsonSlurper().parseText(j.format(makeEvent(obj))), is(obj)
        def obj2 = [foo: 'bar', baz: 42, qux: null]
        assertThat new JsonSlurper().parseText(j.format(makeEvent(obj2))), is(obj2)
    }

    @Test
    void testDate() {
        def d = new Date(1454412462729)
        def j = new JsonLayout(conversionPattern: '%m')
        TimeZone defaultzone = TimeZone.default

        try {
            TimeZone.setDefault(TimeZone.getTimeZone("GMT+1"))
            assertThat j.format(makeEvent(d)), is('"2016-02-02 12:27:42.729+01"')
        } finally {
            TimeZone.setDefault(defaultzone)
        }
    }
}
