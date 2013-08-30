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
  


import grails.converters.*
import org.json.*;
import org.transmart.searchapp.AuthUser;

import edu.mit.wi.haploview.*;
class OntologyController {

    def index = { }
    def i2b2HelperService
    def springSecurityService
    
    def showOntTagFilter= {
    		def tagtypesc=[]
    		tagtypesc.add("ALL")
    		def tagtypes=i2b2.OntNodeTag.executeQuery("SELECT DISTINCT o.tagtype FROM i2b2.OntNodeTag as o order by o.tagtype")
    		tagtypesc.addAll(tagtypes)
    		def tags=i2b2.OntNodeTag.executeQuery("SELECT DISTINCT o.tag FROM i2b2.OntNodeTag o order by o.tag")  /*WHERE o.tagtype='"+tagtypesc[0]+"'*/
    		log.trace(tags as JSON)
    		render(template:'filter',model:[tagtypes:tagtypesc, tags:tags])
    }
    
    def ajaxGetOntTagFilterTerms = {
    		def tagtype = params.tagtype
    		log.trace("calling search for tagtype:"+tagtype)
       		def tags=i2b2.OntNodeTag.executeQuery("SELECT DISTINCT o.tag FROM i2b2.OntNodeTag o WHERE o.tagtype='"+tagtype+"' order by o.tag")
       		log.trace (tags as JSON)
       		render(template:'depSelectTerm', model:[tagtype:tagtype, tags:tags])
    }
    
    def ajaxOntTagFilter =
    {
    		def concepts=[];
    		log.trace("called ajaxOntTagFilter")
    		log.trace("tagterm:"+params.tagterm)
    		def searchtags=params.tagterm;
    		def searchterm=params.ontsearchterm;
    		def tagsearchtype=params.tagtype;
    		def myNodes;
			searchterm = searchterm.trim();
			if(searchterm.length()==0)
				searchterm = null;
    		log.trace("searching for:"+searchtags+" of type"+tagsearchtype+"with searchterm:"+searchterm)
			def myCount  =0;
			def allSystemCds = []
			def searchtermWild = '%'+searchterm.toLowerCase()+'%';
			def visualAttrHiddenWild = '%H%';
			
			if(searchterm==null){// if there is no search term just do exact match
    		def c = i2b2.OntNode.createCriteria()
    		 myCount = c.get{
    		    projections{
    				countDistinct("id")
    				and
    			{
    				if(searchtags!=null)
    				{
    					tags {
    						and {
    							//like('tag', '%'+tagsearchterm+'%') 
    							eq('tagtype', tagsearchtype)
    							'in'("tag", searchtags)	
    							}
    						}
    				}
					not {ilike('visualattributes', '%H%')} //h for hidden
    			}
    		    }
    		}
    		log.trace("SEARCH COUNT:"+myCount);
			
			def d=i2b2.OntNode.createCriteria();
			myNodes = d.list {
				and
				{
					if(searchtags!=null)
					{
						tags {
							and {
								//like('tag', '%'+tagsearchterm+'%')
								eq('tagtype', tagsearchtype)
								'in'("tag", searchtags)
								}
							}
					}
					if(searchterm!=null)
					{
						ilike('name', '%'+searchterm+'%')
					}
					not {ilike('visualattributes', '%H%')} //h for hidden
				}
				maxResults(100)
			}
			}else {
			// if there is a serch term then use tag type to find system cd
			// this is not a generic solution - 
			// if tag type is all then do a name like search
			if(tagsearchtype=='ALL'){
				myCount = i2b2.OntNode.executeQuery("SELECT COUNT(DISTINCT o.id) from i2b2.OntNode o WHERE lower(o.name) like '"+searchtermWild+"' AND o.visualattributes NOT like '"+visualAttrHiddenWild+"'")[0]
				
				myNodes = i2b2.OntNode.executeQuery("SELECT o from i2b2.OntNode o WHERE lower(o.name) like '"+searchtermWild+"' AND o.visualattributes NOT like '"+visualAttrHiddenWild+"'", [max:100])
  
			}else{
			 allSystemCds = i2b2.OntNode.executeQuery("SELECT DISTINCT o.sourcesystemcd FROM i2b2.OntNode o JOIN o.tags t WHERE t.tag IN (:tagArg) AND t.tagtype =:tagTypeArg",[tagArg:searchtags, tagTypeArg:tagsearchtype], [max:800])
			 	
			  myCount = i2b2.OntNode.executeQuery("SELECT COUNT(DISTINCT o.id) from i2b2.OntNode o WHERE o.sourcesystemcd IN (:scdArg) AND lower(o.name) like '"+searchtermWild+"' AND o.visualattributes NOT like '"+visualAttrHiddenWild+"'", [scdArg:allSystemCds])[0]
			  
			  myNodes = i2b2.OntNode.executeQuery("SELECT o from i2b2.OntNode o WHERE o.sourcesystemcd IN (:scdArg) AND lower(o.name) like '"+searchtermWild+"' AND o.visualattributes NOT like '"+visualAttrHiddenWild+"'", [scdArg:allSystemCds], [max:100])
			}
			 }
			
    	
    		//check the security
    		def keys=[:]
    		myNodes.each{node ->
    		//keys.add("\\"+node.id.substring(0,node.id.indexOf("\\",2))+node.id)
    		keys.put(node.id, node.securitytoken)
    		log.trace(node.id+" security token:"+node.securitytoken)
    		}
    		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
    		def access=i2b2HelperService.getAccess(keys, user);
    		log.trace(access as JSON)
    		
    		//build the JSON for the client
    		myNodes.each{node -> 
    		log.trace(node.id)
    		/*node.tags.each{
    				tag -> 
    				log.debug(tag.id)
    				log.debug("FOUND A TAG************")
    		}*/
    		 def level=node.hlevel
    		 def key="\\"+node.id.substring(0,node.id.indexOf("\\",2))+node.id
    		 def name=node.name
    		 def synonym_cd=node.synonymcd
    		 def visualattributes=node.visualattributes
    		 def totalnum=node.totalnum
    		 def facttablecolumn=node.facttablecolumn
    		 def tablename=node.tablename
    		 def columnname=node.columnname
    		 def columndatatype=node.columndatatype
    		 def operator=node.operator
    		 def dimcode=node.dimcode
    		 def comment=node.comment
    		 def tooltip=node.tooltip
    		 def metadataxml=i2b2HelperService.metadataxmlToJSON(node.metadataxml)
    		 concepts.add([level:level, key:key,  name:name, synonym_cd:synonym_cd, visualattributes:visualattributes, totalnum:totalnum, facttablecolumn:facttablecolumn, tablename:tablename, columnname:columnname, columndatatype:columndatatype, operator:operator, dimcode:dimcode, comment:comment, tooltip:tooltip, metadataxml:metadataxml, access:access[node.id]] )
    					
    		}
            def resulttext;
            if(myCount<100){resulttext="Found "+myCount+" results."}
            else
            {resulttext ="Returned first 100 of "+myCount+" results."}
            
    		def result=[concepts:concepts, resulttext:resulttext]
    		log.trace(result as JSON)
    		render result as JSON		
    }
    
    
    def getInitialSecurity=
    {
    		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
    		def result=i2b2HelperService.getAccess(i2b2HelperService.getRootPathsWithTokens(), user);
    		render result as JSON
    }
    def sectest=
    {
    		log.trace("KEYS:"+params.keys)
    		def keys=params.keys.toString().split(",");
    		def paths=[];
    		def access;
    		if(params.keys!="")
    		{
    		keys.each{ key ->
    			log.debug("in LOOP")
    			paths.add(i2b2HelperService.keyToPath(key))	
    		}	
    		def user = AuthUser.findByUsername(springSecurityService.getPrincipal().username)
    	
    		
    		 access=i2b2HelperService.getConceptPathAccessCascadeForUser(paths, user)
    		}
    		log.trace(access as JSON)
    }
	
	def showConceptDefinition =
	{
		def conceptPath=i2b2HelperService.keyToPath(params.conceptKey);
		def node=i2b2.OntNode.get(conceptPath);
		//def testtag=new i2b2.OntNodeTag(tag:'test', tagtype:'testtype');
		//node.addToTags(testtag);
		//node.save();
        if (node == null)
        {
            render(template:'showDefinition', model:[tags:[]])
            return
        }

		def result=node.tags.findAll{ it != null }
		def trial=result.find { it.tagtype == "Trial" }
		if(trial!=null)
		{
			def trialid=trial.tag;
			chain(controller:'trial', action:'trialDetailByTrialNumber', id:trialid)
		}

		render(template:'showDefinition', model:[tags:result])
	}
	
}
