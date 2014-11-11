


package com.recomdata.util;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * helper class provides assistance for downloading domain object
 * instances to Excel from the internet
 *
 * @author jspencer
 */
public class DomainObjectExcelHelper {

    private String _fileName = "";
    private IDomainExcelWorkbook _domainObject = null;

    public DomainObjectExcelHelper(IDomainExcelWorkbook domainObject, String title) {
        _domainObject = domainObject;
        _fileName = title;
    }

    /**
     * download the object to Excel
     *
     * @param response
     */
    public void downloadDomainObject(HttpServletResponse response) throws IOException {

        // setup headers for download
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + _fileName + "\"");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setHeader("Expires", "0");

        // send workbook to response
        ServletOutputStream os = null;
        try {
            os = response.getOutputStream();
            os.write(_domainObject.createWorkbook());
        } finally {
            os.flush();
        }
    }

    public static void downloadToExcel(HttpServletResponse response, String fileName, byte[] excelWB) throws IOException {
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Pragma", "public");
        response.setHeader("Expires", "0");

        ServletOutputStream os = null;
        try {
            os = response.getOutputStream();
            os.write(excelWB);
        } finally {
            os.flush();
        }
    }
}
