


package com.recomdata.export;

/**
 * @author Jeremy Isikoff
 * @version 1.0
 *          <p/>
 *          Copyright 2008 Recombinant Data Corp.
 */
public class ExportResult {
    private String subject;
    private String concept;
    private String value;
    private String description;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getConcept() {
        return concept;
    }

    public void setConcept(String concept) {
        this.concept = concept;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
