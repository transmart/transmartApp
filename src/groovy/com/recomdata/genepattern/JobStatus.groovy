package com.recomdata.genepattern

class JobStatus {
    def name = "";
    def status = ""; // C -completed, R- Running, I - initializing, Q -queued, T - terminated
    def message = "";
    def gpJobId;
    def totalRecord = 0;

    def isRunning() {
        return status == "R";
    }

    def setComplete() {
        status = "C"
    }

    def isCompleted() {
        return status == "C";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        //if (this == obj)
        //	return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        JobStatus other = (JobStatus) obj;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }

}
