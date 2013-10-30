import org.codehaus.groovy.grails.commons.ConfigurationHolder;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter

import com.recomdata.upload.DataUploadResult
import org.transmart.searchapp.RequiredUploadField;

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

public class DataUploadService{

	def verifyFields(providedFields, uploadType) {
		
		def requiredFields = RequiredUploadField.findAllByType(uploadType)*.field
		def missingFields = []
		for (field in requiredFields) {
			def found = false
			for (providedField in providedFields) {
				if (providedField.trim().toLowerCase().equals(field.trim().toLowerCase())) {
					found = true
					break
				}
				//Special case for p-value - if we have log p-value, count this as present as well
				if(providedField.trim().toLowerCase().equals("log_p_value") &&
					field.trim().toLowerCase().equals("p_value")) {
					found = true
					break
				}
			}
			if (!found) {
				missingFields.add(field)
			}
		}
		def success = (missingFields.size() == 0)
		def result = new DataUploadResult(success: success, requiredFields: requiredFields, providedFields: providedFields, missingFields: missingFields, error: "Required fields were missing from the uploaded file.")
		return (result);
	}
	
	def writeFile(location, file, upload) throws Exception {
		CSVReader csvRead = new CSVReader(new InputStreamReader(file.getInputStream()), '\t'.charAt(0), CSVWriter.NO_QUOTE_CHARACTER);
		String[] header = csvRead.readNext();
		
		//Verify fields and return immediately if we don't have a required one
		def result = verifyFields(header, upload.dataType)
		if (!result.success) {
			return result;
		}
		
		def headerList = header.toList();
		
		def pValueIndex = -1;
		def logpValueIndex = -1;
		def numberOfColumns = headerList.size();
		for (int i = 0; i < headerList.size(); i++) {
			def column = headerList[i];
			if (column.trim().toLowerCase().equals("p_value")) {
				pValueIndex = i;
			}
			else if (column.trim().toLowerCase().equals("log_p_value")) {
				logpValueIndex = i;
			}
		}
		
		//If we don't have p-value or log p-value, add this column at the end
		if (pValueIndex < 0) {
			pValueIndex = headerList.size();
			headerList[headerList.size()] = "p_value";
		}
		else if (logpValueIndex < 0) {
			logpValueIndex = headerList.size();
			headerList[headerList.size()] = "log_p_value";
		}
		
		/* Columns are sorted - now start writing the file */
		
		CSVWriter csv = null;
		try {
			csv = new CSVWriter(new FileWriter(new File(location)), '\t'.charAt(0), CSVWriter.NO_QUOTE_CHARACTER) //How to specify character in Grails...?!
			csv.writeNext(headerList as String[])
			
			//For each line, check the value and p-value - if we have one but not the other, calculate and fill it
			String[] nextLine
			while ((nextLine = csvRead.readNext()) != null) {
				def columns = nextLine.toList()
				def currentpValue = columns[pValueIndex]
				def currentlogpValue = columns[logpValueIndex]
				
				if (!currentpValue && !currentlogpValue) {
					throw new Exception("No p_value or log_p_value was provided for a row.")
				}
				if (!currentpValue) {
					columns[pValueIndex] = 0 - Math.power(10.0, Double.parseDouble(currentlogpValue))
				}
				else if (!currentlogpValue) {
					columns[logpValueIndex] = 0 - Math.log10(Double.parseDouble(currentpValue))
				}
				
				//This row is now complete - write it!
				csv.writeNext(columns as String[])
			}
			
			return result;
		}
		catch (Exception e) {
			throw e;
		}
//			upload.status = "ERROR"
//			upload.save(flush: true)
//			render(view: "complete", model: [result: new DataUploadResult(success:false, error: "Could not write file: " + e.getMessage()), uploadDataInstance: upload]);
//			return;
//		}
		finally {
			if (csvRead != null) {
				csvRead.close();
			}
			if (csv != null) {
				csv.flush()
				csv.close()
			}
		}
	}
	
	def runStaging(etlId) throws Exception {
		
			def etlPath = ConfigurationHolder.config.com.recomdata.dataUpload.etl.dir
			def stageScript = ConfigurationHolder.config.com.recomdata.dataUpload.stageScript
			ProcessBuilder pb = new ProcessBuilder(etlPath + stageScript, String.valueOf(etlId));
			pb.directory(new File(new File(etlPath).getCanonicalPath()))
			
			pb.start()
		
	}
}

