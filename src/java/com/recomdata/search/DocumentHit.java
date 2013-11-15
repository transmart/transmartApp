/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

/**
* $Id: DocumentHit.java 9178 2011-08-24 13:50:06Z mmcduffie $
**/
package com.recomdata.search;

import java.io.*;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.*;
import org.apache.lucene.search.*;
import org.apache.lucene.search.highlight.*;

/**
 * This object holds information about a document found during a search of a Lucene index.
 * 
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 **/
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