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
  

// global members
var _d2hSecondaryWindowsByTopics = null;
function d2hInitSecondaryWindows()
{
    if (_d2hSecondaryWindowsByTopics == null)
    {
        _d2hSecondaryWindowsByTopics = new Array();
        
    }
}

function newDocContext(strTitle, strData)
{
    var elem = "<html><head>"
    if (strTitle != "")
        elem += "<title>" + strTitle + "</title>";
    elem += "</head><body>";
    elem += strData;
    elem += "</body></html>";
    document.clear();
    document.write(elem);
}


function jSearch(doc, strQuery)
{
    display(doc, jExecQuery(doc, strQuery));
}

function Clause(str){
    var exactPhrase = (str.indexOf(d2hQuotePrefix) == 0 && quotes[str] != null) || (str.indexOf(searchNotInSpaces + d2hQuotePrefix) == 0 && quotes[str.substring(searchNotInSpaces.length, str.length)] != null);
    var parts;
    var foundNot = false;
    if (!exactPhrase)
    {        
        parts = str.split(new RegExp(searchOrInSpaces.replace(/\|/gi,"\\|"),"gi"));
        if (parts.length == 1)
        {
            parts = str.split(new RegExp(searchAndInSpaces.replace(/\|/gi,"\\|"),"gi"));            
            this.type = 2;
        }
        else
            this.type = 1;
        if (parts.length == 1)
        {
            foundNot = str.toUpperCase().indexOf(searchNotInSpaces) == 0;
            if (foundNot)
                str = str.substring(searchNotInSpaces.length, str.length);
            parts = str.split(" ");
            this.type = 2;
        }
        parts = removeRepeatingTerms(parts);
    }
    else
    {    
        foundNot = str.toUpperCase().indexOf(searchNotInSpaces) == 0;
        if (foundNot)
            str = str.substring(searchNotInSpaces.length, str.length);    
        this.type = 0;
        parts = quotes[str].split(" ");
        str = quotes[str];
    }
    if (parts.length > 1 || exactPhrase)
    {
        this.children = new Array(parts.length);
        for (var i = 0; i < parts.length; i++)
        {
            this.children[i] = new Clause(parts[i]);
            if (exactPhrase)
                this.children[i].type = 0;
        }
    }
    if (foundNot)
    {
        if (parts.length == 1 || exactPhrase)
            this.not = true;
        else
            this.children[0].not = true;
    }
    this.value = str.toLowerCase();
}

Clause.prototype={
    value:"",
    children:null,
    type:0, //0 - exact phrase, 1 - OR, 2 - AND
    not:false,
    docs:null,
    execute:function(){
        if (this.children == null)
        {
            if (this.type != 0)
            {
                var aliases = getAliases(this.value);
                for (var i = 0; i < aliases.length; i++)
                    this.docs = mergeDocs(this.docs, searchInIndex(aliases[i], true));
            }
            else
                this.docs = searchInIndex(this.value, this.type != 0);
        }
        else
        {
            for (var i = 0; i < this.children.length; i++)
            {
                if (this.children[i].execute())
                {
                    if (i == 0)
                        this.docs = this.children[i].docs;
                    else
                    {
                        if (this.type == 0 || this.type == 2)
                            this.docs = intersect(this.docs, this.children[i].docs, this.type == 0);
                        else
                            this.docs = mergeDocs(this.docs, this.children[i].docs);
                    }
                }
                else if (this.type != 1 || i == 0)
                    this.docs = null;
                if (this.docs == null)
                    break;                
            }
        }
        if (this.not && this.docs)
            this.docs = invert(this.docs);                    
        return this.docs != null;
    },
    getQueryString:function(corrected){
        if (this.children == null)
        {
            var word;
            // if this is a probably misspelled word
            if (corrected && this.type != 0 && !isWildcard(this.value) && (!this.docs || this.docs.length < 2) && !aliasesHT[this.value])
                word = getSimilarWord(this.value);
            else
                word = this.value;
            return this.not && this.type != 0 ? searchNotInSpaces + word : word;
        }
        else
        {
            var query = "";
            var typeName = this.type == 1 ? searchOrInSpaces : " ";
            for (var i = 0; i < this.children.length; i++)
            {
                var subquery = this.children[i].getQueryString(corrected);
                if (i == 0)
                    query = subquery;
                else if (this.children[i].not && this.type == 2)
                    query += searchAndInSpaces + subquery;
                else
                    query += typeName + subquery;
            }
            if (this.type == 0)
            {
                query = "\"" + query + "\"";
                if (this.not)
                    query = searchNotInSpaces + query;                
            }
            return query;
        }
    }
}

var searchAnd = "AND".toUpperCase();
var searchOr = "OR".toUpperCase();
var searchNot = "NOT".toUpperCase();
var searchAndInSpaces = " " + searchAnd + " ";
var searchOrInSpaces = " " + searchOr + " ";
var searchNotInSpaces = searchNot + " ";
var d2hQuotePrefix = "_d2hQuote";
var quotes = new Array();
var aliasesHT = new Array();

function getWords()
{
    var wordsSorted = new Array();
    wordsSorted.length = g_sWords.length;
    for(var i = 0; i < g_sWords.length; i++)
        wordsSorted[i] = g_sWords[i];
    wordsSorted.sort(sortByWordsLength);
    return wordsSorted;    
}

function sortByWordsLength(x, y)
{
    var delta = x.length - y.length;
    if (delta < 0)
        return 1;
    if (delta > 0)        
        return -1;
    return 0;
}

function getAliases(word)
{
    var indexes = aliasesHT[word];
    var words;
    if (indexes && (typeof indexes != "function"))
    {
        words = g_sAliases[indexes[0]];
        for (var i = 1; i < indexes.length; i++)
            words = mergeSimple(words, g_sAliases[indexes[i]]);
    }
    else
    {
        words = new Array(1);
        words[0] = word;
    }
    return words;
}

function getDistance(x, y, maxDelta)
{
    if (x.charAt(0) != y.charAt(0) || Math.abs(x.length-y.length) > 2)
        return maxDelta + 1;
    var N = x.length + 1, M = y.length + 1, min;
    var a = new Array(N);
    for (var i = 0; i < N; i++)
    {
        a[i] = new Array(M);
        a[i][0] = i;
    }
    for (var i = 0; i < M; i++)
        a[0][i] = i;
    for (var i = 1; i < N; i++)
    {
        min = N+M;
        for (var j = 1; j < M; j++)
        {
            a[i][j] = Math.min(a[i-1][j-1] + (x.charAt(i-1)==y.charAt(j-1) ? 0 : 1), Math.min(a[i-1][j], a[i][j-1]) + 1);
            if (a[i][j] < min)
                min = a[i][j];
        }
        if (min > maxDelta)
            return maxDelta + 1;
    }
    return a[N-1][M-1];
}

function getSimilarWord(w)
{
    if (isEasternLanguage(w))
        return w;
    var maxDelta = 3 * Math.round(0.4 + w.length / 10);
    var bestWordIndex = -1;
    for(var i = 0; i < g_sWords.length; i++)
    {
        var word = g_sWords[i];
        if (g_sWordTopics[i].length == 1 && !aliasesHT[word])
            continue;
        var d = getDistance(word, w, maxDelta);
        if (d > maxDelta || d == 0)
            continue;
        if (d < maxDelta || bestWordIndex == -1)
        {
            maxDelta = d;
            bestWordIndex = i;
        }
        else if (g_sWordTopics[i].length > g_sWordTopics[bestWordIndex].length)
            bestWordIndex = i;
    }
    return bestWordIndex != -1 ? g_sWords[bestWordIndex] : w;
}

function initAliases()
{
    for (var i = 0; i < g_sAliases.length; i++)
        for (var j = 0; j < g_sAliases[i].length; j++)
        {
            var word = g_sAliases[i][j];
            if (aliasesHT[word] == null)
                aliasesHT[word] = new Array(1);
            aliasesHT[word][aliasesHT[word].length-1] = i;
        }
}

function addAND(str)
{
    for (var i = str.length - searchNotInSpaces.length - 1; i >=0; i--)
        if (str.substring(i, i + searchNotInSpaces.length + 1).toUpperCase() == " " + searchNotInSpaces)
        {                
            var startIndex = i - searchAndInSpaces.length + 1;
            var found = startIndex >= 0 ? str.substring(startIndex, i + 1).toUpperCase() == searchAndInSpaces : false;            
            if (!found)
            {
                startIndex = i - searchOrInSpaces.length + 1;
                found = startIndex >= 0 ? str.substring(startIndex, i + 1).toUpperCase() == searchOrInSpaces : false;            
            }
            if (!found)
                str = str.substring(0, i) + " " + searchAnd + str.substring(i, str.length);
        }
   return str;
}

function jExecQuery(doc, strQuery)
{
    var regExp = /"(.)*"/gi;
    var obj = regExp.exec(strQuery);
    var i = 0;
    while (obj && obj.index != -1)
    {
        var s = d2hQuotePrefix + i;
        quotes[s] = obj[0].substring(1, obj[0].length-1);
        strQuery = strQuery.replace(obj[0], s);
        obj = regExp.exec(strQuery);
    }
    for (var i = 0; i < g_sStopWords.length; i++)
    {
        var word = g_sStopWords[i].toUpperCase();    
        if (word == searchOr || word == searchAnd || word == searchNot)
            continue;
        var r = new RegExp("\\b" + g_sStopWords[i] + "\\b", 'ig');
        strQuery = strQuery.replace(r, "");
    }    
    strQuery = strQuery.replace(/\s+|\.|,|"|;|\(|\)|\{|\}|\[|\]/g, " ").replace(/^\s+/g, "").replace(/\s+$/g, "");    
    strQuery = addAND(strQuery);
    initAliases();
    var root = new Clause(strQuery);
    root.execute();
    var original = root.getQueryString(false);
    var corrected = root.getQueryString(true);
    if (original == corrected)
        corrected = "";
    setQuery2EditBox(original);    
    if (this.docs)
        root.docs = calcHistogram(root.docs);    
	return getQueryResult(doc, corrected, root.docs);    
}

function getDocID(arr)
{
    return arr[0];
}

function invert(docs)
{
    var docsLength = docs ? docs.length : 0;
    var cnt = g_sTopics.length - docsLength;
    var newDocs = new Array(cnt);
    var j = 0, l = 0;
    var id = j < docsLength ? getDocID(docs[j++]) : g_sTopics.length;        
    for (var i = 0; i < g_sTopics.length; i++)
    {        
        if (i < id)
        {
            newDocs[l] = new Array(2);
            newDocs[l][0] = i;
            newDocs[l][1] = new Array();
            l++;
        }
        else if (i == id)
            id = j < docsLength ? getDocID(docs[j++]) : g_sTopics.length;
    }
    return newDocs;
}

function intersect(docs1, docs2, exactPhrase)
{
    var docs = new Array(docs1.length);
    var i = 0, j = 0, k = 0;
    while (i < docs1.length && j < docs2.length)
    {
        var id1 = getDocID(docs1[i]), id2 = getDocID(docs2[j]);
        if (id1 == id2)
        {
            var p1 = 0, p2 = 0, p = 0;
            var positions = new Array();
            if (exactPhrase)
            {                
                while (p1 < docs1[i][1].length && p2 < docs2[j][1].length)
                {
                    if (docs1[i][1][p1] == docs2[j][1][p2] - 1)
                    {
                        positions[p++] = docs2[j][1][p2];
                        p1++;
                        p2++;
                    }
                    while (p1 < docs1[i][1].length && docs1[i][1][p1] < docs2[j][1][p2] - 1)
                        p1++;            
                    while (p2 < docs2[j][1].length && docs2[j][1][p2] <= docs1[i][1][p1])
                        p2++;                            
                }
            }
            if (!exactPhrase || positions.length > 0)
            {
                docs[k] = new Array();
                docs[k][0] = docs1[i][0];
                docs[k][1] = positions;
                k++;
            }
            i++;
            j++;            
        }
        else if (id1 < id2)
        {
            while (i < docs1.length && getDocID(docs1[i]) < id2)
                i++;            
        }
        else
        {
            while (j < docs2.length && getDocID(docs2[j]) < id1)
                j++;
        }
    }
    if (docs.length > k)
        docs.length = k;
    return docs;
}

function mergeSimple(x, y)
{
    if (x == null)
        return y;
    if (y == null)
        return x;
    var res = new Array(x.length + y.length);
    var i = 0, j = 0, k = 0;    
    while (i < x.length && j < y.length)
    {        
        if (x[i] == y[j])
        {
            res[k++] = x[i++];
            j++;        
        }
        else
            res[k++] = x[i] < y[j] ? x[i++] : y[j++];    
    }
    while (i < x.length)
        res[k++] = x[i++];
    while (j < y.length)
        res[k++] = y[j++];
    if (res.length > k)
        res.length = k;
    return res;
}

function mergeDocs(x, y)
{
    if (x == null)
        return y;
    if (y == null)
        return x;
    var res = new Array(x.length + y.length);
    var i = 0, j = 0, k = 0;    
    while (i < x.length && j < y.length)
    {
        var id1 = getDocID(x[i]), id2 = getDocID(y[j]);
        if (id1 == id2)
        {
            res[k] = new Array(2);
            res[k][0] = id1;
            res[k++][1] = mergeSimple(x[i++][1], y[j++][1]);
        }
        else
            res[k++] = id1 < id2 ? x[i++] : y[j++];
    }
    while (i < x.length)
        res[k++] = x[i++];
    while (j < y.length)
        res[k++] = y[j++];
    if (res.length > k)
        res.length = k;
    return res;
}

function setQuery2EditBox(query)
{
    var doc = d2hGetSearchFrameDocument();
    if (doc != null)
    {
        var elmSearch = getElemById(doc, "query");
        if (elmSearch != null)
            elmSearch.value = query.replace(/\s+$/g, "");
    }
}

function getWordIndex(word)
{
    var l = 0, r = g_sWords.length - 1;    
    while (r > l)
    {
        var m = Math.round((l + r) / 2);
        if (g_sWords[m] < word)
            l = m + 1;
        else if (g_sWords[m] > word)
            r = m - 1;
        else
            return m;
    }
    return l == r && g_sWords[l] == word ? l : -1;
}

function searchInIndex(term, allowWildcards)
{
    var wildcard = allowWildcards && isWildcard(term);
    if (wildcard)
    {
        var re = new RegExp(getWildcardRegexp(term), "gi");
        var indx;
        var res = null;
        for(var i = 0; i < g_sWords.length; i++)
        {
            indx = g_sWords[i].search(re);
            if (indx > -1)
            {
                if (res)
                    res = mergeDocs(res, g_sWordTopics[i]);
                else
                    res = g_sWordTopics[i];
            }
        }
        return res;
    }
    else
    {
        var index = getWordIndex(term);
        return index != -1 ? g_sWordTopics[index] : null;
    }
}

function getWordsFromIndex(termWithWildcards)
{
    var words = new Array();
    var re = new RegExp(getWildcardRegexp(termWithWildcards), "gi");
    for(var i = 0; i < g_sWords.length; i++)
        if (g_sWords[i].search(re) > -1)
        {
            words.length = words.length + 1;
            words[words.length - 1] = g_sWords[i];
        }
    return words;
}

function isWildcard(term)
{
    return term.indexOf("?") > -1 || term.indexOf("*") > -1;
}

function removeRepeatingTerms(terms)
{
    var htbl = new Array();
    var res = new Array();
    for (var i = 0; i < terms.length; i++)
        if (!htbl[terms[i]])
        {
            res[res.length] = terms[i];
            htbl[terms[i]] = true;
        }
    return res;
}

function calcHistogram(arr)
{
    var tbl = new Array();
    var id;
    for (var i = 0; i < arr.length; i++)
    {
        id = "x" + arr[i][0];
        if (tbl[id])
        {
            tbl[id][1] = new Array(tbl[id][1].length + arr[i][1].length);
            arr[i] = null;
        }
        else
            tbl[id] = arr[i];
    }
    arr.sort(sortByCounterNumber);
    return arr;
}

function sortByCounterNumber(x, y)
{
    if (x == null)
        return 1;
    if (y == null)
        return -1;
    var delta = x[1].length - y[1].length;
    if (delta == 0)
    {
        var xTopic = g_sTopics[x[0]][1];
        var yTopic = g_sTopics[y[0]][1];
        if (xTopic > yTopic)
            return 1;
        else if (xTopic < yTopic)
            return -1;            
        return 0;
    }
    if (delta < 0)
        return 1;
    return -1;
}

function getQueryResult(doc, newQuery, arr)
{
    var res = "";
    if (arr != null)
        for (var i = 0; i < arr.length; i++)
            if (arr[i])
                res += getResultItem(i, arr[i]);
    if (res.length == 0)
        res = "No topics found.";
    if (newQuery.length > 0)
        res = "<div class=\"clsSearchDidYouMean\">Did you mean: <a class=\"clsMouseOver\" href=\"" + doc.location.href.replace(doc.location.search, "?query=" + encodeURI(newQuery)) + "\">" + newQuery + "</a></div><br>" + res;
    return res;
}

function getResultItem(id, item)
{
    var res = "";
    try
    {
        var td = g_sTopics[item[0]];
        if (td)
        {
            res = "<div nowrap class=\"clsSearchResultItem\"><a id=\"ri-" + id + "\" href=\"";
            res += td[0];

            res += "\" target=\"right\"";

            res += "onclick=\"return d2hSearchItemSelect(\'" + td[0] + "\', event)\" onmouseover=\"d2hItemOver(event)\" onmouseout=\"d2hItemOut(event)\">";
            res += td[1] + "</a></div>";
        }
    }
    catch (e)
    {
        res = "";
    }
    return res;
}

function display(doc, content)
{
    var elem = doc.forms[1];
    if (elem.parentNode)
        elem.parentNode.setAttribute("nowrap", true);
    waitCursor(doc, true);
    elem.innerHTML = content;
    waitCursor(doc, false);
}

function waitCursor(doc, isWait)
{
}
