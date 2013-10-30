import com.recomdata.export.ExportColumn
import com.recomdata.export.ExportRowNew
import com.recomdata.export.ExportTableNew;

/**
 * 
 *
 * @author $Author: mmcduffie $
 * @version $Revision:  $
 */
class GridViewService {
	
	boolean transactional = false;
	
	def i2b2HelperService
	def dataSource
	
	/**
	 * Fills the main demographic data in an export table for the grid
	 */
	def ExportTableNew addAllPatientDemographicDataForSubsetToTable(ExportTableNew tablein, String result_instance_id, String subset) {
		log.trace("Adding patient demographic data to grid with result instance id:" +result_instance_id+" and subset: "+subset)
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
		String sqlt = """SELECT * FROM patient_dimension p INNER JOIN patient_trial t ON p.patient_num=t.patient_num
		    WHERE p.PATIENT_NUM IN (select distinct patient_num from qt_patient_set_collection where result_instance_id = ?)
		    ORDER BY p.PATIENT_NUM""";
		
		//if i have an empty table structure so far
		if(tablein.getColumns().size()==0)
		{
			tablein.putColumn("subject", new ExportColumn("subject", "Subject", "", "String", 100));
			tablein.putColumn("patient", new ExportColumn("patient", "Patient", "", "String", 100));
			tablein.putColumn("subset", new ExportColumn("subset", "Subset", "", "String", 100));
			tablein.putColumn("TRIAL", new ExportColumn("TRIAL", "Trial", "", "String", 100));
			tablein.putColumn("SEX_CD", new ExportColumn("SEX_CD", "Sex", "", "String", 100));
			tablein.putColumn("AGE_IN_YEARS_NUM", new ExportColumn("AGE_IN_YEARS_NUM", "Age", "", "Number", 100));
			tablein.putColumn("RACE_CD", new ExportColumn("RACE_CD", "Race", "", "String", 100));
			tablein.putColumn("ENCOUNTER_NUM", new ExportColumn("ENCOUNTER_NUM", "Event Number", "", "Number", 100));
		}
		//def founddata=false;
		sql.eachRow(sqlt, [result_instance_id], {row ->
			/*If I already have this subject mark it in the subset column as belonging to both subsets*/
			//founddata=true;
			String subject=row.PATIENT_NUM;
			if(tablein.containsRow(subject))
			{
				String s=tablein.getRow(subject).get("subset");
				s=s+","+subset;
				tablein.getRow(subject).put("subset", s);
			}
			else /*fill the row*/ {
				ExportRowNew newrow=new ExportRowNew();
				
				def arr = row.SOURCESYSTEM_CD?.split(":")
				
				newrow.put("subject", subject);
				newrow.put("patient", arr?.length == 2 ? arr[1] : "");
				newrow.put("subset", subset);
				newrow.put("TRIAL", row.TRIAL)
				newrow.put("SEX_CD", row.SEX_CD)
				newrow.put("AGE_IN_YEARS_NUM", row.AGE_IN_YEARS_NUM.toString())
				newrow.put("RACE_CD", row.RACE_CD)
				newrow.put("ENCOUNTER_NUM", "-")

				tablein.putRow(subject, newrow);
			}
		})

		return tablein;
	}
	
	/**
	 * Adds a column of data to the grid export table
	 */
	def ExportTableNew addConceptDataToTable(ExportTableNew tablein,String concept_key,String result_instance_id) {
		if(i2b2HelperService.isLeafConceptKey(concept_key)) 
		{
			String valueColumnName = ""
			String columnid=i2b2HelperService.getShortNameFromKey(concept_key).replace(" ", "_").replace("...", "");
			String columnname=i2b2HelperService.getColumnNameFromKey(concept_key).replace(" ", "_");

			/*add the subject column to the table if its not there*/
			if(tablein.getColumn("subject")==null)
			{
				tablein.putColumn("subject", new ExportColumn("subject", "Subject", "", "String", 50));
			}
			
			
			ExportColumn thisColumn = tablein.getColumn(columnid);
			
			//Add other columns if not there
			if(tablein.getColumn(columnid)==null) {
				thisColumn = new ExportColumn(columnid, columnname, "", "");
				tablein.putColumn(columnid, thisColumn);
			}
			
			if(i2b2HelperService.isValueConceptKey(concept_key)) 
			{	
				thisColumn.setType("Number");
				valueColumnName = "NVAL_NUM";
			}
			else
			{
				thisColumn.setType("String");
				valueColumnName = "TVAL_CHAR";
			}
				
			/*get the data*/
			String concept_cd=i2b2HelperService.getConceptCodeFromLongPath(concept_key);
			groovy.sql.Sql sql = new groovy.sql.Sql(dataSource)
			String sqlt = """SELECT f.PATIENT_NUM, f.""" + valueColumnName + """ VALUE_COLUMN, f.START_DATE, f.ENCOUNTER_NUM, f.VALTYPE_CD, DEL.LINK_TYPE
					FROM OBSERVATION_FACT f
					INNER JOIN CONCEPT_DIMENSION CD ON f.CONCEPT_CD = CD.CONCEPT_CD
					INNER JOIN DE_ENCOUNTER_LEVEL DEL ON CD.CONCEPT_CD = DEL.CONCEPT_CD
					WHERE f.CONCEPT_CD = ? AND
			        f.PATIENT_NUM IN (select  patient_num
					from qt_patient_set_collection
					where result_instance_id = ?)""";
			
			log.debug("Retrieving Data for addConcepDataToTable: " + sqlt);	
					
			sql.eachRow(sqlt, [
				concept_cd,
				result_instance_id
			], {row ->
			
				/*If I already have this subject mark it in the subset column as belonging to both subsets*/
				String subject		= row.PATIENT_NUM
				String encounterNum	= row.ENCOUNTER_NUM
				String value		= row.VALUE_COLUMN
				String timingLevel	= row.LINK_TYPE
				String valType     	= row.VALTYPE_CD
				String startDate	= row.START_DATE
				String rowIndex		= subject
				
				if(valType=='D')
				{
                    if(startDate && startDate.count("-") == 2)
                    {
                        String[] valueArray = startDate.split("-")
                        def localDate = startDate.toString()

                        String dateString = valueArray[1]+" "+valueArray[2].substring(0,2) +" "+valueArray[0]

                        value = dateString
                    }
                    else
                    {
                        value = ""
                    }
					
					thisColumn.setType("Date");
					// Date is stored in both NVAL_NUM & START_DATE
					//value = startDate
				}
				
				if(timingLevel == "E")
				{
					rowIndex = subject + "|" + encounterNum
				}
				
				if(!i2b2HelperService.isValueConceptKey(concept_key) && value==null){value="Y";}
				
				if(tablein.containsRow(rowIndex)) /*should contain all subjects already if I ran the demographics first*/ {
					tablein.getRow(rowIndex).put(columnid, value.toString());
				}
				else /*fill the row*/ {
					//Create a new row with data from the other columns.
					ExportRowNew newrow = createTableRowWithCopy(subject,encounterNum,tablein)
					
					//Add the new column to our new row.
					newrow.put(columnid, value.toString());
											
					//Add the row to the table.
					tablein.putRow(rowIndex, newrow);
				}
				
				//If this isn't timingLevel "E" that means it's subject level and we need to fill all the related subject rows with this data.
				if(timingLevel != "E")
				{
					propagateSubjectLevelRecords(tablein, subject, columnid, value, false)
				}
				
			})
			
			//pad all the empty values for this column
			for(ExportRowNew row: tablein.getRows())
			{
				if(!row.containsColumn(columnid)) {
					row.put(columnid, "");
				}
			}
		}
		else {
			log.trace("must be a folder dont add to grid");
		}
		return tablein;
	}
	
	/**
	 * This method will populate related records for the given subject with new values.
	 * @param tableIn
	 * @param subjectId
	 * @param columnId
	 * @param columnValue
	 * @return
	 */
	def propagateSubjectLevelRecords(ExportTableNew tableIn, String subjectId, String columnId, String columnValue, boolean concatColumnValues = false)
	{
		//Grab all the keys in our rows. We'll investigate each and see if it's for the current subject.
		tableIn.getRowMap().keySet().each()
		{
			currentKey ->
			
			//Check the key to see if it contains the current subject id.
			if(currentKey.startsWith(subjectId + "|"))
			{
				
				if(concatColumnValues)
				{
					//Get the current row in an object.
					def tRow = 	tableIn.getRow(currentKey)
					
					//Get the value in the current row, current column.
					def tColVal = tRow.get(columnId);
					
					//Determine the value that goes in the column.
					if (tColVal)
					{
						List<String> values = tColVal.split(",").toList();
						
						if (!values.contains(columnValue))
						{
							tColVal += tColVal ? ", " + columnValue :  columnValue;
						}
					}
					else
					{
						tColVal =  columnValue;
					}
					
					tableIn.getRow(currentKey).put(columnId, tColVal.toString());
				}
				else
				{
					//For this row, fill in the new column value.
					tableIn.getRow(currentKey).put(columnId, columnValue);
				}
			}
		}
	}
	
	def createTableRowWithCopy(String subjectId, String encounterNum, ExportTableNew tableToAddTo)
	{
		//Create a new row.
		ExportRowNew newrow=new ExportRowNew();
		
		//Add the id columns to the new row.
		newrow.put("subject", subjectId);
		
		//We now have to add all the columns that already exist in the table to this new row.
		//Verify the existance of the inital Subject ID node.
		if(tableToAddTo.containsRow(subjectId))
		{
			//Find the initial row for this subject id.
			def referenceRow = tableToAddTo.getRow(subjectId)
			
			//Get the list of all the columns that exist for this record.
			def referenceKeys = referenceRow.getKeys()
			
			//Loop through the list of values in the rows hashmap.
			referenceKeys.each()
			{
				currentColumn ->
				
				newrow.put(currentColumn, referenceRow.get(currentColumn));
			}
			
		}
		
		//The Encounter Number might have gotten overwritten by the above loop, reset it here.
		newrow.put("ENCOUNTER_NUM", encounterNum);
		
		return(newrow);

	}
	
	/**
	 * Adds a column of data to the grid export table
	 */
	def ExportTableNew addFolderDataToTable(ExportTableNew tablein, String folder_key, String concept_key,String result_instance_id) {

		String columnid=i2b2HelperService.getShortNameFromKey(folder_key).replace(" ", "_").replace("...", "");
		String columnname=i2b2HelperService.getColumnNameFromKey(folder_key).replace(" ", "_");
		
		ExportColumn thisColumn = tablein.getColumn(columnid);

		if(!thisColumn)
		{
			thisColumn = new ExportColumn(columnid, columnname, "", "");
			tablein.putColumn(columnid, thisColumn);
		}

		def prefix = concept_key.substring(0, concept_key.indexOf("\\",2));
		
		if(i2b2HelperService.isValueConceptCode(concept_key))
		{
			thisColumn.setType("Number");
		}
		else
		{
			thisColumn.setType("String");
		}
		
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		def cpSQL = concept_key + "%";

		String sqlt = """SELECT DISTINCT Q.PATIENT_NUM, f.ENCOUNTER_NUM, DEL.LINK_TYPE
						FROM I2B2DEMODATA.CONCEPT_DIMENSION C 
						INNER JOIN DE_ENCOUNTER_LEVEL DEL ON C.CONCEPT_CD = DEL.CONCEPT_CD
						INNER JOIN I2B2DEMODATA.OBSERVATION_FACT f ON (f.concept_cd = c.concept_cd AND c.concept_path LIKE ?) 
						INNER JOIN I2B2DEMODATA.QT_PATIENT_SET_COLLECTION Q ON ( Q.PATIENT_NUM = f.PATIENT_NUM AND RESULT_INSTANCE_ID = ?)"""

		def resultRows = sql.rows(sqlt, [cpSQL, result_instance_id]);
	
		resultRows.each { row ->
			
			try {
				String subject		= row.PATIENT_NUM;
				String encounterNum = row.ENCOUNTER_NUM;
				String timingLevel	= row.LINK_TYPE
				String rowIndex		= subject
				
				if(timingLevel == "E")
				{
					rowIndex = subject + "|" + encounterNum
				}
				
				//Get the short name of the column.
				String shortName	= i2b2HelperService.getColumnNameFromKey(concept_key).replace(" ", "_").replace("...", "");
								
				if(tablein.containsRow(rowIndex)) {
					
					//Get the current row in an object.
					def tRow = 	tablein.getRow(rowIndex);
					
					//Get the value in the current row, current column.
					def tColVal = tRow.get(columnid);
					
					//Determine the value that goes in the column.
					if (tColVal)
					{
						List<String> values = tColVal.split(",").toList();
						
						if (!values.contains(shortName))
						{
							tColVal += tColVal ? ", " + shortName :  shortName;
						}
					}
					else
					{
						tColVal =  shortName;
					}
					
					tablein.getRow(rowIndex).put(columnid, tColVal.toString());
				}
				else /*fill the row*/ {
					//Create a new row with data from the other columns.
					ExportRowNew newrow = createTableRowWithCopy(subject,encounterNum,tablein)
					
					//Add the new column to our new row.
					newrow.put(columnid, shortName.toString());
											
					//Add the row to the table.
					tablein.putRow(subject + "|" + encounterNum, newrow);
				}
				
				
				//If this isn't timingLevel "E" that means it's subject level and we need to fill all the related subject rows with this data.
				if(timingLevel != "E")
				{
					propagateSubjectLevelRecords(tablein, subject, columnid, shortName.toString(), true)
					
				}
				
			}
			catch (Exception exc)
			{
				log.trace(exc);
			
			}
		};
		
		for(ExportRowNew row: tablein.getRows())
		{
			if(!row.containsColumn(columnid)) {
				row.put(columnid, "");
			}
		}

		return tablein;
	}
	
	
}