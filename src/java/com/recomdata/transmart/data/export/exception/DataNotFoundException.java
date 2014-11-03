


package com.recomdata.transmart.data.export.exception;


public class DataNotFoundException extends Exception {

    private static final long serialVersionUID = 1268973588723740635L;

    public DataNotFoundException() {
    }

    public DataNotFoundException(String specificErrorMessage) {
        //this.errorMessage = specificErrorMessage;
        super(specificErrorMessage);
    }
}
