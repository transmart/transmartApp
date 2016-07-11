


package com.recomdata.genesignature;

import java.util.Collection;
import java.util.Iterator;

/**
 * Special exception class for invalid gene signature upload files
 * $Id: FileSchemaException.java 9178 2011-08-24 13:50:06Z mmcduffie $
 *
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class FileSchemaException extends RuntimeException {

    /**
     * required for serialization
     */
    private static final long serialVersionUID = 7745871701505432136L;

    public FileSchemaException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileSchemaException(String message) {
        super(message);
    }

    /**
     * helper function for constructing an error messages for invalid gene symbols that could not be
     * mapped to the warehouse schema
     *
     * @param invalidGenes
     * @return
     */
    public static void ThrowInvalidGenesFileSchemaException(Collection<String> invalidGenes) throws FileSchemaException {
        StringBuffer msg = new StringBuffer("The following gene symbols could not be mapped; " +
                "it is possible that they are not known in the selected platform (meta-data); " +
                "please fix or remove:<br><br>");
        msg.append("<ul style='list-style-type: disc; list-style-position: inside;'>");
        Iterator<String> it = invalidGenes.iterator();
        while (it.hasNext()) {
            msg.append("<li>" + it.next().toString() + "</li>");
        }
        msg.append("<ul>");
        throw new FileSchemaException(msg.toString());
    }

}
