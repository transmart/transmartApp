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
  

function getDateTime()
{
 return "2008-01-21T18:24:59.406-05:00";
}  
/*****************************************************************************
PM REQUESTS
******************************************************************************/
function getPMRequestHeader(){
return "<ns2:request xmlns:ns4='http://www.i2b2.org/xsd/hive/msg/version/' \
 xmlns:ns3='http://www.i2b2.org/xsd/cell/pm/1.1/' xmlns:ns2='http://www.i2b2.org/xsd/hive/msg/1.1/'>\
    <message_header>\
        <i2b2_version_compatible>1.0</i2b2_version_compatible>\
        <hl7_version_compatible>2.4</hl7_version_compatible>\
        <sending_application>\
            <application_name>i2b2 Project Management</application_name>\
            <application_version>1.0</application_version>\
        </sending_application>\
        <sending_facility>\
            <facility_name>i2b2 Hive</facility_name>\
        </sending_facility>\
        <receiving_application>\
            <application_name>Project Management Cell</application_name>\
            <application_version>1.0</application_version>\
        </receiving_application>\
        <receiving_facility>\
            <facility_name>i2b2 Hive</facility_name>\
        </receiving_facility>\
        <datetime_of_message>"+getDateTime()+"</datetime_of_message>\
        <security>\
            <domain>"+GLOBAL.Domain+"</domain>\
            <username>"+GLOBAL.Username+"</username>\
            <password>"+GLOBAL.Password+"</password>\
        </security>\
        <message_control_id>\
            <message_num>1</message_num>\
            <instance_num>0</instance_num>\
        </message_control_id>\
        <processing_id>\
            <processing_id>P</processing_id>\
            <processing_mode>I</processing_mode>\
        </processing_id>\
        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\
        <application_acknowledgement_type>AL</application_acknowledgement_type>\
        <country_code>US</country_code>\
        <project_id xmlns:xsi='http://www.w3.org/2001/XMLSchema-instance' xsi:nil='true' />\
    </message_header>\
    <request_header>\
        <result_waittime_ms>120000</result_waittime_ms>\
    </request_header>\
    <message_body>";
    }

function getPMRequestFooter(){
    return "  </message_body>\
</ns2:request>";
}

function getServices()
{   
	loginComplete();
}





/*****************************************************************************
ONTOLOGY REQUESTS
******************************************************************************/
 function getONTRequestHeader()
 {
 return "<ns3:request xmlns:ns3='http://www.i2b2.org/xsd/hive/msg/1.1/' xmlns:ns4='http://www.i2b2.org/xsd/cell/ont/1.1/' xmlns:ns2='http://www.i2b2.org/xsd/hive/plugin/'>\
    <message_header>\
        <i2b2_version_compatible>1.1</i2b2_version_compatible>\
        <hl7_version_compatible>2.4</hl7_version_compatible>\
        <sending_application>\
            <application_name>i2b2 Ontology</application_name>\
            <application_version>1.2</application_version>\
        </sending_application>\
        <sending_facility>\
            <facility_name>i2b2 Hive</facility_name>\
        </sending_facility>\
        <receiving_application>\
            <application_name>Ontology Cell</application_name>\
            <application_version>1.0</application_version>\
        </receiving_application>\
        <receiving_facility>\
            <facility_name>i2b2 Hive</facility_name>\
        </receiving_facility>\
        <datetime_of_message>"+getDateTime()+"</datetime_of_message>\
        <security>\
            <domain>"+GLOBAL.Domain+"</domain>\
            <username>"+GLOBAL.Username+"</username>\
            <password>"+GLOBAL.Password+"</password>\
        </security>\
        <message_control_id>\
            <message_num>1</message_num>\
            <instance_num>0</instance_num>\
        </message_control_id>\
        <processing_id>\
            <processing_id>P</processing_id>\
            <processing_mode>I</processing_mode>\
        </processing_id>\
        <accept_acknowledgement_type>AL</accept_acknowledgement_type>\
        <application_acknowledgement_type>AL</application_acknowledgement_type>\
        <country_code>US</country_code>\
        <project_id>"+GLOBAL.ProjectID+"</project_id>\
    </message_header>\
    <request_header>\
        <result_waittime_ms>120000</result_waittime_ms>\
    </request_header>\
    <message_body>";
 }   
function getONTRequestFooter(){ return "</message_body>\
                   </ns3:request>";
                   }

function getCategories()
{
    jQuery.ajax(pageInfo.basePath + '/concepts/getCategories', {
            dataType : 'json'
        })
        .always(getCategoriesComplete)
}

 function getONTgetNameInfoRequest(matchstrategy, matchterm, matchontology)
 {
  var query=getONTRequestHeader();
  query=query+'<ns4:get_name_info blob="true" type="core" category="'+matchontology+'">\
            <match_str strategy="'+matchstrategy+'">'+matchterm+'</match_str>\
        </ns4:get_name_info>'
  query=query+getONTRequestFooter();
  return query;
  }
/*****************************************************************************
DATA REPOSITORY REQUESTS
******************************************************************************/


function getCRCRequestHeader(){
return '<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/ont/1.1/" \
xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/" \
xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/" \
xmlns:ns8="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/"> \
    <message_header>\
        <sending_application>\
            <application_name>i2b2_QueryTool</application_name>\
            <application_version>0.2</application_version>\
        </sending_application>\
        <sending_facility>\
            <facility_name>PHS</facility_name>\
        </sending_facility>\
        <receiving_application>\
            <application_name>i2b2_DataRepositoryCell</application_name>\
            <application_version>0.2</application_version>\
        </receiving_application>\
        <receiving_facility>\
            <facility_name>PHS</facility_name>\
        </receiving_facility>\
        <security>\
            <domain>'+GLOBAL.Domain+'</domain>\
            <username>'+GLOBAL.Username+'</username>\
            <password>'+GLOBAL.Password+'</password>\
        </security>\
        <message_type>\
            <message_code>Q04</message_code>\
            <event_type>EQQ</event_type>\
        </message_type>\
        <message_control_id>\
            <message_num>ibcZyQQTZQ6zAmKawJtE</message_num>\
            <instance_num>0</instance_num>\
        </message_control_id>\
        <processing_id>\
            <processing_id>P</processing_id>\
            <processing_mode>I</processing_mode>\
        </processing_id>\
        <accept_acknowledgement_type>messageId</accept_acknowledgement_type>\
         <project_id>'+GLOBAL.ProjectID+'</project_id>\
    </message_header>\
    <request_header>\
        <result_waittime_ms>180000</result_waittime_ms>\
    </request_header>\
    <message_body>\
        <ns4:psmheader>';
 }
        
   
            
function getCRCRequestFooter(){
return '<result_output_list>\
			<result_output priority_index="10" name="PATIENTSET"/>\
 		</result_output_list>\
 		</ns4:request>\
 		</message_body>\
 		</ns6:request>';
}


function getCRCQueryRequest(subset, queryname)
{
    if (queryname == "" || queryname == undefined) {
        var d = new Date();
        queryname = GLOBAL.Username+"'s Query at "+ d.toString();
    }

    var query =
        '<ns4:query_definition xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/">\
          <query_name>'+queryname+'</query_name>\
          <specificity_scale>0</specificity_scale>';

    for (var i = 1; i <= GLOBAL.NumOfQueryCriteriaGroups; i++) {
        var qcd = Ext.get("queryCriteriaDiv" + subset + '_' + i.toString());
        if(qcd.dom.childNodes.length>0) {
            query = query + getCRCRequestPanel(qcd.dom, i);
        }
    }
    query = query + getSecurityPanel() + "</ns4:query_definition>";

    return query;
}

//takes actual dom element
function getCRCRequestPanel(qd, number) 
{
//set the invert
var invert=0;
if(qd.className=="queryGroupExclude")
	invert=1;
//set the occurs (later)
var occurences=1;
var panel="<panel>\
               <panel_number>"+number+"</panel_number>\
                    <invert>"+invert+"</invert>\
                    <total_item_occurrences>"+occurences+"</total_item_occurrences>";

for(var i=0;i<qd.childNodes.length;i++)
{
var itemel=qd.childNodes[i];
panel=panel+getCRCRequestItem(itemel);
}
panel=panel+"</panel>";
return panel;
}
                  

function getCRCRequestItem(el){
	
	//Construct part of the XML document being sent to i2b2 services. Escape when there are < or > in the concept path.
 	var item=		'<item>\
                        <hlevel>'+el.getAttribute("conceptlevel")+'</hlevel>\
                        <item_name>'+el.getAttribute("conceptname").replace("<", "&lt;").replace(">", "&gt;")+'</item_name>\
                        <item_key>'+el.getAttribute("conceptid").replace("<", "&lt;").replace(">", "&gt;")+'</item_key>\
                        <tooltip>'+el.getAttribute("concepttooltip").replace("<", "&lt;").replace(">", "&gt;")+'</tooltip>\
                        <class>ENC</class>';
                    
      if(el.getAttribute("setvaluemode")=='numeric')
      	{
            item=item+'<constrain_by_value>\
                            <value_operator>'+el.getAttribute("setvalueoperator")+'</value_operator>';
         
         if(el.getAttribute("setvalueoperator")=="BETWEEN")  
         			 {  
                      item=item+'<value_constraint>'+el.getAttribute("setvaluelowvalue")+' and '+el.getAttribute("setvaluehighvalue")+'</value_constraint>'
                      }
                      else item=item+'<value_constraint>'+el.getAttribute("setvaluelowvalue")+'</value_constraint>'
                            
                	item=item+'<value_unit_of_measure>'+el.getAttribute("setvalueunits")+'</value_unit_of_measure>\
                            <value_type>NUMBER</value_type>\
                        </constrain_by_value>';
        }
        else if(el.getAttribute("setvaluemode")=='highlow')
      	{
            item=item+'<constrain_by_value><value_operator>EQ</value_operator>';
            item=item+'<value_constraint>'+el.getAttribute("setvaluehighlowselect").substring(0,1).toUpperCase()+'</value_constraint><value_type>FLAG</value_type></constrain_by_value>';
        	item=item+'<value_unit_of_measure>'+el.getAttribute("setvalueunits")+'</value_unit_of_measure>'               
        }
       // else if (el.getAttribute("oktousevalues")=="Y" && el.getAttribute("setvaluemode")=="novalue")
       // {
       //item=item+'<constrain_by_value></constrain_by_value>'   
       //}
                   item=item+'</item>';
                    
                    return item;
}

function getCRCpdoRequestHeader(){
return '<ns6:request xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/" xmlns:ns7="http://www.i2b2.org/xsd/cell/ont/1.1/" \
xmlns:ns3="http://www.i2b2.org/xsd/cell/crc/pdo/1.1/" xmlns:ns5="http://www.i2b2.org/xsd/hive/plugin/"\
 xmlns:ns2="http://www.i2b2.org/xsd/hive/pdo/1.1/" xmlns:ns6="http://www.i2b2.org/xsd/hive/msg/1.1/" \
 xmlns:ns8="http://www.i2b2.org/xsd/cell/crc/psm/querydefinition/1.1/">\
    <message_header>\
        <sending_application>\
            <application_name>i2b2_QueryTool</application_name>\
            <application_version>0.2</application_version>\
        </sending_application>\
        <sending_facility>\
            <facility_name>PHS</facility_name>\
        </sending_facility>\
        <receiving_application>\
            <application_name>i2b2_DataRepositoryCell</application_name>\
            <application_version>0.2</application_version>\
        </receiving_application>\
        <receiving_facility>\
            <facility_name>PHS</facility_name>\
        </receiving_facility>\
        <security>\
            <domain>'+GLOBAL.Domain+'</domain>\
            <username>'+GLOBAL.Username+'</username>\
            <password>'+GLOBAL.Password+'</password>\
        </security>\
        <message_type>\
            <message_code>Q04</message_code>\
            <event_type>EQQ</event_type>\
        </message_type>\
        <message_control_id>\
            <message_num>IcNAUIKPAgdXl3GGSXwD</message_num>\
            <instance_num>0</instance_num>\
        </message_control_id>\
        <processing_id>\
            <processing_id>P</processing_id>\
            <processing_mode>I</processing_mode>\
        </processing_id>\
        <accept_acknowledgement_type>messageId</accept_acknowledgement_type>\
        <project_id>'+GLOBAL.ProjectID+'</project_id>\
    </message_header>\
    <request_header>\
        <result_waittime_ms>180000</result_waittime_ms>\
    </request_header>\
    <message_body>\
        <ns3:pdoheader>\
            <patient_set_limit>0</patient_set_limit>\
            <estimated_time>180000</estimated_time>\
            <request_type>getPDO_fromInputList</request_type>\
        </ns3:pdoheader>\
        <ns3:request xsi:type="ns3:GetPDOFromInputList_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">';
        }
          
            
function getCRCpdoRequestFooter(){  
     return   '</ns3:request>\
    </message_body>\
</ns6:request>';
}


function getCRCpdoRequest(patientlistid, minpatient, maxpatient, subset)
{
var request=getCRCpdoRequestHeader()+
		 '<input_list>\
                <patient_list max="'+maxpatient+'" min="'+minpatient+'">\
                    <patient_set_coll_id>'+patientlistid+'</patient_set_coll_id>\
                </patient_list>\
            </input_list>\
            <filter_list>';
 for(var d=1;d<=GLOBAL.NumOfQueryCriteriaGroups;d++)
{
 var queryDiv=Ext.get("queryCriteriaDiv"+subset+'_'+d);
  for(var c=0;c<queryDiv.dom.childNodes.length;c++)
		{
		var invert=0;
		var concept=queryDiv.dom.childNodes[c];
		/*if(queryDiv.dom.className=="queryGroupExclude")
			{
			invert=1;
			}*/
		request=request+'<panel name="'+concept.getAttribute("conceptid")+'">\
                    <invert>'+invert+'</invert>\
                    <panel_accuracy_scale>0</panel_accuracy_scale>\
                    <total_item_occurrences>0</total_item_occurrences>\
                    <item>\
                        <item_key>'+concept.getAttribute("conceptid")+'</item_key>\
                        <dim_tablename>'+concept.getAttribute("concepttablename")+'</dim_tablename>\
                        <dim_dimcode>'+concept.getAttribute("conceptdimcode")+'</dim_dimcode>\
                    </item>\
                </panel>';
           }
     }  
     request=request+'</filter_list>\
            <output_option>\
                <observation_set blob="false" onlykeys="false"/>\
                <patient_set select="using_input_list" onlykeys="false"/>\
            </output_option>';
     request=request+getCRCpdoRequestFooter();
     return request;
  }
         
 function getPreviousQueries()
{  
     var getPreviousQueriesRequest=getCRCRequestHeader()+'<user login="'+GLOBAL.Username+'">'+GLOBAL.Username+'</user>\
            <patient_set_limit>0</patient_set_limit>\
            <estimated_time>0</estimated_time>\
            <request_type>CRC_QRY_getQueryMasterList_fromUserId</request_type>\
        </ns4:psmheader>\
        <ns4:request xsi:type="ns4:user_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\
            <user_id>'+GLOBAL.Username+'</user_id>\
            <group_id>'+GLOBAL.ProjectID+'</group_id>\
            <fetch_size>20</fetch_size>'+getCRCRequestFooter(); 
 	
 	Ext.Ajax.request(
    	    {
    	        url: pageInfo.basePath+"/proxy?url="+GLOBAL.CRCUrl+"request",
    	        method: 'POST',
    	        xmlData: getPreviousQueriesRequest,                                        
    	        success: function(result, request){getPreviousQueriesComplete(result);},
    	        failure: function(result, request){getPreviousQueriesComplete(result);}
    	    }); 
}

function getPreviousQueryFromID(subset, queryMasterID) {
    queryPanel.el.mask('Rebuilding query...', 'x-mask-loading');
    Ext.Ajax.request(
        {
            url: pageInfo.basePath + "/queryTool/getQueryDefinitionFromResultId",
            params: {
                result_id: queryMasterID
            },
            success: function (result, request) {
                getPreviousQueryFromIDComplete(subset, result);
            },
            failure: function (result, request) {
                getPreviousQueryFromIDComplete(subset, result);
            },
            timeout: '300000'
        });
}

function getQueryInstanceList(queryName, queryMasterId)
{  
     var getQueryInstanceListRequest=getCRCRequestHeader()+'<user login="'+GLOBAL.Username+'">'+GLOBAL.Username+'</user>\
            <patient_set_limit>0</patient_set_limit>\
            <estimated_time>0</estimated_time>\
            <request_type>CRC_QRY_getQueryInstanceList_fromQueryMasterId</request_type>\
        </ns4:psmheader>\
        <ns4:request xsi:type="ns4:master_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\
             <query_master_id>'+queryMasterId+'</query_master_id>'+getCRCRequestFooter(); 
 	
 	Ext.Ajax.request(
    	    {
    	        url: pageInfo.basePath+"/proxy?url="+GLOBAL.CRCUrl+"request",
    	        method: 'POST',
    	        scope: this,
    	        xmlData: getQueryInstanceListRequest,                                        
    	        success: function(result, request){getQueryInstanceListComplete(result, queryName);},
    	        failure: function(result, request){getQueryIntanceListComplete(result);}
    	    }); 
}

function getQueryInstanceListComplete(response, queryName)
{
var queryInstances=response.responseXML.selectNodes('//query_instance');
//get the first instance of the query
var firstQueryInstanceId=queryInstances[0].selectSingleNode('query_master_id').firstChild.nodeValue;
//alert(firstQueryInstanceId);
//get the results of the first instance
getQueryResultInstanceList(firstQueryInstanceId, queryName);
}

function getQueryResultInstanceList(queryInstanceId, queryName)
{  
     var getQueryResultInstanceListRequest=getCRCRequestHeader()+'<user login="'+GLOBAL.Username+'">'+GLOBAL.Username+'</user>\
            <patient_set_limit>0</patient_set_limit>\
            <estimated_time>0</estimated_time>\
            <request_type>CRC_QRY_getQueryResultInstanceList_fromQueryInstanceId</request_type>\
        </ns4:psmheader>\
        <ns4:request xsi:type="ns4:instance_requestType" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">\
             <query_instance_id>'+queryInstanceId+'</query_instance_id>'+getCRCRequestFooter(); 
 	
 	Ext.Ajax.request(
    	    {
    	        url: pageInfo.basePath+"/proxy?url="+GLOBAL.CRCUrl+"request",
    	        method: 'POST',
    	        scope: this,
    	        headers:{'Content-Type':'text/xml'},
    	        xmlData: getQueryResultInstanceListRequest,                                        
    	        success: function(result, request){getQueryResultInstanceListComplete(result, queryName);},
    	        failure: function(result, request){getQueryResultInstanceListComplete(result);}
    	    }); 
}

function getQueryResultInstanceListComplete(response, queryName)
{
var queryResultInstances=response.responseXML.selectNodes('//query_result_instance');
//get the first instance of the query
var firstResultInstanceId=queryResultInstances[0].selectSingleNode('result_instance_id').firstChild.nodeValue;
//alert(firstResultInstanceId);
createExportItem(queryName, firstResultInstanceId);
}


function getSecurityPanel() {
//      Commenting this out while investigating for the right parameters
//		if(!GLOBAL.IsAdmin)
		if(false)
		{
		 return"<panel><panel_number>21</panel_number> \
               <invert>0</invert><total_item_occurrences>1</total_item_occurrences>\
   			   <item>\
					<item_key>\\\\Public Studies\\Public Studies\\SECURITY\\</item_key>\
					<class>ENC</class>\
					<constrain_by_value>\
					<value_operator>IN</value_operator>\
					<value_constraint>("+GLOBAL.Tokens+")</value_constraint>\
					<value_unit_of_measure>unit</value_unit_of_measure>\
					<value_type>TEXT</value_type>\
					</constrain_by_value>\
					</item>\
  				</panel>";
  		}
  		else return ""; //no security panel
  		}
