


package com.recomdata.export;

public class ParsedIds {
    protected String ids;
    protected Integer length;

    public ParsedIds(String i, Integer l) {
        this.ids = i;
        this.length = l;
    }

    public String getIds() {
        return ids;
    }

    public Integer getLength() {
        return length;
    }

}