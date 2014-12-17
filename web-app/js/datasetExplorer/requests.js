
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