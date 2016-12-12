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

import org.apache.log4j.Level
import org.apache.log4j.spi.LoggingEvent
import org.apache.log4j.Category
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.apache.commons.io.FileUtils.readFileToString
import static org.apache.commons.io.FileUtils.writeStringToFile
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.*

import static ChildProcessAppender.ChildFailedException

class ChildProcessAppenderTests {

    @Rule
    public TemporaryFolder temp = new TemporaryFolder()

    static String TESTSTRING = "hello world! testing org.transmart.logging.ChildProcessAppender\n"

    static sh(cmd) { return ['sh', '-c', cmd] }
    // escape shell strings, based on http://stackoverflow.com/a/1250279/264177
    static path(File file) { "'${file.path.replaceAll("'", "'\"'\"'").replaceAll("\\\\","\\\\")}'" }

    static void waitForChild(ChildProcessAppender a) {
        a.input.close()
        a.process.waitFor()
    }

    @Test
    void testLoggingEvent() {
        File output = temp.newFile('output')
        def p = new ChildProcessAppender(command: sh("cat >"+path(output)))
        LoggingEvent e = new LoggingEvent("", new Category('debug'), Level.DEBUG, [foo: 'bar', baz: 'quux'], null)
        p.doAppend(e)
        p.close()
        waitForChild(p)
        assertThat readFileToString(output), is(String.format('{"foo":"bar","baz":"quux"}%n'))
    }

    @Test
    void testOutput() {
        File output = temp.newFile('output')
        def p = new ChildProcessAppender(command: sh("cat > "+path(output)))
        p.write(TESTSTRING)
        waitForChild(p)
        assertThat readFileToString(output), is(TESTSTRING)
    }

    @Test(expected=ChildFailedException.class)
    void testFail() {
        def p = new ChildProcessAppender(command: ["false"], restartLimit: 3, throwOnFailure: true)
        p.write(TESTSTRING)
        waitForChild(p)
        p.write(TESTSTRING)
        waitForChild(p)
        p.write(TESTSTRING)
        waitForChild(p)
        p.write(TESTSTRING)
        waitForChild(p)
        // unreachable
        assert false
    }

    @Test
    void testRestart() {
        do_testRestart(3, 15)
    }

    @Test(expected=ChildFailedException.class)
    void testRestartLimit() {
        do_testRestart(5, 3)
    }

    void do_testRestart(int restarts, int limit) {
        File runcount = temp.newFile('count')
        File output = temp.newFile('output')
        writeStringToFile(runcount, '0\n')
        String command = """
            countfile=${path(runcount)}
            count=`cat "\$countfile"`
            if [ "\$count" -le ${restarts} ]
            then
                echo `expr "\$count" + 1` > "\$countfile"
                exit
            else
                cat > ${path(output)}
            fi"""
        def p = new ChildProcessAppender(command: sh(command), restartLimit: limit, throwOnFailure: true)
        int count = -1
        while (count <= restarts) {
            p.write(TESTSTRING)
            def countstr = readFileToString(runcount).trim()
            if (countstr) count = Integer.parseInt(countstr)
        }
        p.write(TESTSTRING)
        p.close()
        waitForChild(p)

        // restarting a child process may lose some messages, so we can not be sure of how many copies of TESTSTRING
        // there are
        assertThat readFileToString(output), containsString(TESTSTRING)
    }
}
