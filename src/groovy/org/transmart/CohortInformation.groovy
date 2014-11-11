package org.transmart

/**
 *
 * @author mkapoor
 *
 */
class CohortInformation {

    public static final int PLATFORMS_TYPE = 1;
    public static final int TRIALS_TYPE = 2;
    public static final int TIMEPOINTS_TYPE = 3;
    public static final int SAMPLES_TYPE = 4;
    public static final int GPL_TYPE = 5;
    public static final int TISSUE_TYPE = 6;
    public static final int RBM_PANEL_TYPE = 7;

    def platforms = new ArrayList();
    def trials = new ArrayList();
    def timepoints = new ArrayList();
    def samples = new ArrayList();
    def gpls = new ArrayList();
    def tissues = new ArrayList();
    def rbmpanels = new ArrayList();

    def getAllTrials =
            {
                StringBuilder strng = new StringBuilder();
                Iterator itr = trials.iterator();
                if (itr.hasNext()) {
                    strng.append(itr.next());
                }
                while (itr.hasNext()) {
                    strng.append(",").append(itr.next());
                }

                return strng.toString();
            }
}
