


package com.recomdata.util;

import java.util.Enumeration;
import java.util.Properties;

public class PropManager {

    public static String displayProps() {

        Properties props = System.getProperties();
        Enumeration keys = props.propertyNames();
        StringBuffer buf = new StringBuffer();
        buf.append("<table>");

        String key = "";
        while (keys.hasMoreElements()) {
            key = (String) keys.nextElement();
            buf.append("<tr><td>" + key + ":</td><td>" + props.getProperty(key) + "</td></tr>");
        }

        buf.append("</table>");
        return buf.toString();
    }
}
