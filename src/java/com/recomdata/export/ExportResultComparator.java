


package com.recomdata.export;

import java.util.Comparator;

public class ExportResultComparator implements Comparator {
    public int compare(Object obj1, Object obj2) {
        Integer i1 = Integer.parseInt(((ExportResult) obj1).getSubject());
        Integer i2 = Integer.parseInt(((ExportResult) obj2).getSubject());
        if (i1 > i2) {
            return 1;
        } else if (i1 < i2) {
            return -1;
        } else {
            return 0;
        }
    }
}