package com.recomdata.genepattern

import org.json.JSONObject;

class WorkflowStatus {
    def jobStatusList = new ArrayList();
    def currentStatus = "Starting"; /* Starting, Running, Cancelled, Completed*/
    JSONObject result = null;
    int currentStatusIndex = 1;

    // repeat count to help manage dup javascript firings
    int rpCount = 0;

    /**
     * update object if it's in set, add if not exists
     */
    def addJobStatus(status) {
        int sindex = jobStatusList.indexOf(status);
        if (sindex > -1) {
            def s = jobStatusList.get(sindex);
            s.status = status.status;
            s.message = status.message;
            s.gpJobId = status.gpJobId;
            s.totalRecord = status.totalRecord;
        } else {
            jobStatusList.add(status);
        }
    }

    def addNewJob(String sname) {
        jobStatusList.add(new JobStatus(name: sname, status: "Q"));
    }


    def setCurrentJobStatus(status) {
        // set previous job to be completed..
        int sindex = jobStatusList.indexOf(status);
        if (sindex > -1) {
            for (int i = 0; i < sindex; i++) {
                jobStatusList[i].setComplete();
            }
        }
        addJobStatus(status);
        // find running index
        int si = 0;
        for (s in jobStatusList) {
            si++;
            if (s.isRunning()) {
                currentStatusIndex = si;
                break;
            }
        }
        currentStatus = "Running";

    }

    def setCancelled() {
        this.currentStatus = "Cancelled";
    }

    def isCancelled() {
        return currentStatus == "Cancelled";
    }

    def isCompleted() {
        return currentStatus == "Completed";
    }

    def setCompleted() {
        currentStatus = "Completed";
    }

}
