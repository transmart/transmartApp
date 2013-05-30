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
  

package com.recomdata.search;

import java.io.*;
import java.util.*;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.standard.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryParser.*;
import org.apache.lucene.search.*;

public class DocumentQuery {

	public final static int MAX_HITS = 51200;
	public final static int MAX_CLAUSE_COUNT = 8192;
	private String[] fields = { "contents", "summary", "path", "title", "repository" };
	private File index = null;

	public DocumentQuery(String index) {

		this.index = new File(index);
		BooleanQuery.setMaxClauseCount(MAX_CLAUSE_COUNT);

	}

	public int searchCount(LinkedHashMap<String, ArrayList<String> > searchTerms, LinkedHashMap<String, ArrayList<String> > filterTerms) {

		Query query = buildQuery(searchTerms);
		Filter filter = buildFilter(filterTerms);
		IndexReader reader = null;
		Searcher searcher = null;
		
		try {
		    reader = IndexReader.open(index);
		    searcher = new IndexSearcher(reader);
		    TopDocCollector collector = new TopDocCollector(MAX_HITS);
		    if (filter != null) {
		    	searcher.search(query, filter, collector);
		    } else {
		    	searcher.search(query, collector);
		    }
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
			return hits.length;
		} catch (Exception e) {
			System.out.println("exception: " + e.getMessage());
			return 0;
		} finally {
			try {
				if (reader != null) {
					reader.close();
				}
				if (searcher != null) {
					searcher.close();
				}
			} catch (Exception e2) {
				System.out.println("exception: " + e2.getMessage());
				return 0;
			}
		}

	}

	public DocumentHit[] search(LinkedHashMap<String, ArrayList<String> > searchTerms, LinkedHashMap<String, ArrayList<String> > filterTerms, int max, int offset) {

		Query query = buildQuery(searchTerms);
		Filter filter = buildFilter(filterTerms);

		DocumentHit[] documents = null;
		try {
		    IndexReader reader = IndexReader.open(index);
		    Searcher searcher = new IndexSearcher(reader);
		    Analyzer analyzer = new StandardAnalyzer();
		    TopDocCollector collector = new TopDocCollector(offset + max);
		    if (filter != null) {
		    	searcher.search(query, filter, collector);
		    } else {
		    	searcher.search(query, collector);
		    }
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    int size = hits.length - offset < max ? hits.length - offset : max;
			documents = new DocumentHit[size];
			for (int i = offset; i < offset + max && i < hits.length; i++) {
				query.rewrite(reader);
				documents[i - offset] = new DocumentHit(searcher.doc(hits[i].doc), hits[i].doc, hits[i].score, query, analyzer);
			}
		} catch (Exception e) {
			System.out.println("exception: " + e.getMessage());
		}

		return documents;

	}

	private Query buildQuery(LinkedHashMap<String, ArrayList<String> > searchTerms) {

		BooleanQuery andQuery = new BooleanQuery();

		for (String key : searchTerms.keySet()) {
			ArrayList<String> list = searchTerms.get(key);
			ArrayList<Query> queries = new ArrayList<Query>();
			for (String value : list) {
				if (value.indexOf(" ") == -1) {
					Term term = new Term("contents", value.toLowerCase());
					TermQuery termQuery = new TermQuery(term);
					queries.add(termQuery);
				} else {
					String[] values = value.split(" ");
					PhraseQuery phraseQuery = new PhraseQuery();
					for (String v : values) {
						Term term = new Term("contents", v.toLowerCase());
						phraseQuery.add(term);
					}
					queries.add(phraseQuery);
				}
			}
			addQueries(andQuery, queries);
		}
		
		return andQuery;

	}

	private Filter buildFilter(LinkedHashMap<String, ArrayList<String> > filterTerms) {

		BooleanQuery andQuery = new BooleanQuery();
		
		if (filterTerms.containsKey("REPOSITORY")) {
			// The repository field is stored as non-analyzed, so matches need to be exact.
			ArrayList<String> list = filterTerms.get("REPOSITORY");
			ArrayList<Query> queries = new ArrayList<Query>();
			for (String value : list) {
					Term term = new Term("repository", value);
					TermQuery termQuery = new TermQuery(term);
					queries.add(termQuery);
			}
			addQueries(andQuery, queries);
		}

		if (filterTerms.containsKey("PATH")) {
			// The path field is stored as analyzed, so the search terms also need to be analyzed in order to get a match.
			try {
				ArrayList<String> list = filterTerms.get("PATH");
				if (list.size() > 0) {
					StringReader reader = new StringReader(list.get(0));
					StandardAnalyzer analyzer = new StandardAnalyzer();
					TokenStream tokenizer = analyzer.tokenStream("path", reader);
					PhraseQuery phraseQuery = new PhraseQuery();
					Token token = new Token();
					for (token = tokenizer.next(token); token != null; token = tokenizer.next(token)) {
						Term term = new Term("path", token.term());
						phraseQuery.add(term);
					}
					andQuery.add(phraseQuery, BooleanClause.Occur.MUST);
				}
			} catch (IOException ex) {
				// do nothing
			}
		}
		
		if (filterTerms.containsKey("EXTENSION")) {
			ArrayList<String> list = filterTerms.get("EXTENSION");
			ArrayList<Query> queries = new ArrayList<Query>();
			for (String value : list) {
				Term term = new Term("extension", value.toLowerCase());
				TermQuery termQuery = new TermQuery(term);
				queries.add(termQuery);
			}
			addQueries(andQuery, queries);
		}

		if (filterTerms.containsKey("NOTEXTENSION")) {
			ArrayList<String> list = filterTerms.get("NOTEXTENSION");
			for (String value : list) {
				Term term = new Term("extension", value.toLowerCase());
				TermQuery termQuery = new TermQuery(term);
				andQuery.add(termQuery, BooleanClause.Occur.MUST_NOT);
			}
		}

		if (andQuery.clauses().size() > 0) {
			return new QueryWrapperFilter(andQuery);
		}
		return null;

	}
	
	private void addQueries(BooleanQuery andQuery, ArrayList<Query> queries) {
		
		if (queries.size() == 1) {
			andQuery.add(queries.get(0), BooleanClause.Occur.MUST);
		} else if (queries.size() > 1) {
			BooleanQuery orQuery = new BooleanQuery();
			for (Query query : queries) {
				orQuery.add(query, BooleanClause.Occur.SHOULD);
			}
			andQuery.add(orQuery, BooleanClause.Occur.MUST);
		}
		
	}

}
