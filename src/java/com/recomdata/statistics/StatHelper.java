


package com.recomdata.statistics;

public class StatHelper {

    //===================================================== max
    public static double max(double[] t) {
        if (t.length == 0) {
            return 0;
        }
        double maximum = t[0];   // start with the first value
        for (int i = 1; i < t.length; i++) {
            if (t[i] > maximum) {
                maximum = t[i];   // new maximum
            }
        }
        return maximum;
    }//end method max

    public static double min(double[] t) {
        if (t.length == 0) {
            return 0;
        }
        double minimum = t[0];   // start with the first value
        for (int i = 1; i < t.length; i++) {
            if (t[i] < minimum) {
                minimum = t[i];   // new maximum
            }
        }
        return minimum;
    }//end method max
}
