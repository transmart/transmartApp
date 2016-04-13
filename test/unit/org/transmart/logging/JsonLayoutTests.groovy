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
