package org.transmart.dataexport

class FileContentTestUtils {

    static def parseSepValTable(String content, String sep = '\t') {
        content.split('\n')*.split(sep, -1)
                .collect { row -> row.collect { it.replaceAll('^"|"$', '') }}
    }

    static def parseSepValTable(File file, String sep = '\t') {
        parseSepValTable(file.text, sep)
    }

}
