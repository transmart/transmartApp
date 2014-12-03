
function getDateTime()
{
 return "1337-01-42T00:00:00.000-00:00";
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

function getCRCQueryRequest(subset, queryname)
{
    if (queryname == "" || queryname == undefined) {
        var d = new Date();
        queryname = GLOBAL.Username+"'s Query at "+ d.toUTCString();
    }

    var number = 0
    var query =
        '<ns4:query_definition xmlns:ns4="http://www.i2b2.org/xsd/cell/crc/psm/1.1/">\
          <query_name>'+queryname+'</query_name>\
          <specificity_scale>0</specificity_scale>';



    jQuery("#queryTable .panelModel").filter(function () {
        return jQuery(this).attr('subset') == subset
    }).each(function () {
        query += getCRCRequestPanel(Ext.get(jQuery(this).find(".panelBoxList").attr('id')).dom, number++)
    })

    query = query + "</ns4:query_definition>";

    return query;
}

//takes actual dom element
function getCRCRequestPanel(qd, number) 
{
    if (qd.childNodes.length == 0)
        return ''
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
