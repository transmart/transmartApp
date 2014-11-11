


package com.recomdata.export;

import java.util.Comparator;

/* A simple class to compare subject id strings for sorting */

public class subjectComparator implements Comparator {

    public int compare(Object obj1, Object obj2) {

        Integer g1 = Integer.parseInt(((String) obj1).substring(1, 2));
        Integer g2 = Integer.parseInt(((String) obj2).substring(1, 2));

        Integer id1 = Integer.parseInt(((String) obj1).substring(3));
        Integer id2 = Integer.parseInt(((String) obj2).substring(3));

        if (g1 > g2) {
            return 1;
        } else if (g1 < g2) {
            return -1;
        } else if (id1 > id2) {
            return 1;
        } else if (id1 < id2) {
            return -1;
        } else {
            return 0;
        }
    }
}