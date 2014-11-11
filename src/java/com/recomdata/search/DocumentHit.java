


/**
 * $Id: DocumentHit.java 9178 2011-08-24 13:50:06Z mmcduffie $
 **/
package com.recomdata.search;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;

import java.io.IOException;
import java.io.StringReader;

/**
 * This object holds information about a document found during a search of a Lucene index.
 *
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class DocumentHit {
    private Document doc = null;
    private int id = -1;
    private double score = -1.0d;
    private String filePath = null;
    private String title = null;
    private String subject = null;
    private String creator = null;
    private String repository = null;
    private String fullText = null;
    private String highlightedText = null;

    /**
     * Default constructor.
     */
    public DocumentHit() {
    }

    /**
     * Constructs this object using properties and fields form a LiusHit object.
     *
     * @param document
     */
    public DocumentHit(Document doc, int id, double score, Query query, Analyzer analyzer) {

        this.doc = doc;
        this.id = id;
        this.score = score;
        filePath = doc.get("path");
        title = doc.get("title");
        repository = doc.get("repository");

        Highlighter highlighter = new Highlighter(new SimpleHTMLFormatter("<span class=\"search-term\">", "</span>"), new QueryScorer(query, "contents"));
        highlighter.setTextFragmenter(new SimpleFragmenter(50));
        String summary = doc.get("contents");
        TokenStream tokenStream = analyzer.tokenStream("contents", new StringReader(summary));
        try {
            this.highlightedText = highlighter.getBestFragments(tokenStream, summary, 5, "...");
            this.fullText = this.highlightedText;
        } catch (IOException e) {
            this.highlightedText = "";
            this.fullText = "";
        }

    }

    public int getDocId() {

        return this.id;

    }

    public double getScore() {

        return this.score;

    }

    public void setFilePath(String filePath) {

        this.filePath = filePath;

    }

    public String getFilePath() {

        String filePath = this.filePath;

        filePath = filePath.replace("<span class=\"search-term\">", "");
        filePath = filePath.replace("</span>", "");

        return filePath;

    }

    public String getFileName() {

        String filePath = getFilePath();
        int start = filePath.lastIndexOf("/");

        if (start > 0) {
            filePath = filePath.substring(start + 1, filePath.length());
        }

        return filePath;

    }

    public String getRepository() {

        return this.repository;

    }

    public String getTitle() {

        return this.title;

    }

    public String getSubject() {

        return this.subject;

    }

    public String getCreator() {

        return this.creator;

    }

    public void setFullText(String fullText) {

        this.fullText = fullText;

    }

    public String getFullText() {

        return this.fullText;

    }

    public String getHighlightedText() {

        return this.highlightedText;

    }

}