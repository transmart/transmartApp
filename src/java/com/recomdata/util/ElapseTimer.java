


/**
 * $Id: ElapseTimer.java 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
package com.recomdata.util;

import org.apache.log4j.Logger;

public class ElapseTimer {

    static Logger log = Logger.getLogger(ElapseTimer.class);

    private Long startTime;

    public ElapseTimer() {
        startTime = System.currentTimeMillis();
    }

    public void reset() {
        startTime = System.currentTimeMillis();
    }

    public long elapsed(boolean reset) {
        Long e = System.currentTimeMillis() - startTime;
        if (reset)
            reset();
        return e;
    }

    public long elapsed() {
        return elapsed(true);
    }

    public void logElapsed(String tag, boolean reset) {
        log.info(tag + ":" + (System.currentTimeMillis() - startTime) + " ms");
        if (reset)
            reset();
    }

    public String elapsedTime() {
        long time = elapsed(true) / 1000;
        int seconds = (int) (time % 60);
        int minutes = (int) ((time % 3600) / 60);
        int hours = (int) (time / 3600);
        StringBuilder s = new StringBuilder();
        if (hours > 0) {
            s.append(hours).append(" hour(s) ");
        }
        if (minutes > 0) {
            s.append(minutes).append(" minute(s) ");

        }
        if (seconds > 0) {
            s.append(seconds).append(" second(s)");
        }
        return s.toString();
    }
}
