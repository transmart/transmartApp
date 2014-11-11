package org.transmart

/**
 * $Id: DocumentFilter.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class DocumentFilter {
    LinkedHashMap repositories = new LinkedHashMap()
    String path = ""
    boolean type_excel = true
    boolean type_html = true
    boolean type_pdf = true
    boolean type_powerpoint = true
    boolean type_text = true
    boolean type_word = true
    boolean type_other = true

    DocumentFilter() {

        repositories.put("Biomarker", true)
        repositories.put("Conferences", true)
        repositories.put("DIP", true)
        repositories.put("Jubilant Oncology", true)

    }

    LinkedHashMap<String, ArrayList<String>> getFilters() {

        LinkedHashMap<String, ArrayList<String>> filters = new LinkedHashMap<String, ArrayList<String>>()

        if (!type_excel || !type_html || !type_pdf || !type_powerpoint || !type_text || !type_word || !type_other) {
            ArrayList<String> types = new ArrayList<String>()
            // Other checked and one or more types checked - use NOTEXTENSION filter
            if (type_other) {
                if (!type_excel) {
                    types.add("xls")
                    types.add("xlsx")
                }
                if (!type_html) {
                    types.add("htm")
                    types.add("html")
                }
                if (!type_pdf) {
                    types.add("pdf")
                }
                if (!type_powerpoint) {
                    types.add("ppt")
                    types.add("pptx")
                }
                if (!type_text) {
                    types.add("txt")
                }
                if (!type_word) {
                    types.add("doc")
                    types.add("docx")
                }
                filters.put("NOTEXTENSION", types)
            } else {
                if (type_excel) {
                    types.add("xls")
                    types.add("xlsx")
                }
                if (type_html) {
                    types.add("htm")
                    types.add("html")
                }
                if (type_pdf) {
                    types.add("pdf")
                }
                if (type_powerpoint) {
                    types.add("ppt")
                    types.add("pptx")
                }
                if (type_text) {
                    types.add("txt")
                }
                if (type_word) {
                    types.add("doc")
                    types.add("docx")
                }
                if (types.size() > 0) {
                    filters.put("EXTENSION", types)
                }
            }
        }
        ArrayList<String> repos = new ArrayList<String>()
        for (key in repositories.keySet()) {
            if (repositories.get(key) == true) {
                repos.add(key)
            }
        }
        if (repos.size() > 0 && repos.size() != repositories.size()) {
            filters.put("REPOSITORY", repos)
        }
        ArrayList<String> paths = new ArrayList<String>()
        if (path.length() > 0) {
            paths.add(path)
            filters.put("PATH", paths)
        }

        return filters

    }
}
