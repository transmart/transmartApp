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
  

 
import i2b2.SnpDataset;
import i2b2.SnpProbeSortedDef;
import i2b2.StringLineReader

import java.util.List;
import java.util.Map;

import com.recomdata.export.SnpViewerFiles;

/**
 * This class contains methods that will help us ran our analysis components like the modules in the advanced workflow menu.
 * @author MMcDuffie
 *
 */
class AnalysisService 
{
	
	def dataSource;
	
	/**
	 * Get a list of genes from the database for a given list of Sample Ids. We access the haploview_data table to get this information.
	 * @return
	 */
	def  getGenesForHaploviewFromSampleId(result) 
	{
		//This will be a list of all the distinct genes for the selected patients.
		def genes=[];
		
		//Use this datasource to get the genes.
		groovy.sql.Sql sql = new groovy.sql.Sql(dataSource);
		
		//Query the Haploview table directly to get the genes. The Sample ID should be the "i2b2_id" or patient_num
		String sqlt = """	SELECT	DISTINCT gene
							FROM	haploview_data HD
							WHERE	HD.I2B2_ID IN (?) order by gene asc"""
		
		//We get a distinct list that covers all the subsets.
		result.each
		{
			currentSampleList ->
			
			String[] currentStringArray = (String[]) currentSampleList.value
			
			sql.eachRow(sqlt, [quoteCSV(currentStringArray.join(","))], 
				{
					row ->
				if(!genes.get(row.gene)) genes.add(row.gene);
				})
		}
		return genes;
	}

	def String quoteCSV(String val) {
		String[] inArray;
		StringBuilder s = new StringBuilder();
		
		if (val != null && val.length() > 0) {
			inArray= val.split(",");
			s.append("'" +inArray[0] + "'");
			for (int i=1; i < inArray.length; i++) {
				s.append(",'" +inArray[i] + "'");
			}
		}
		return s.toString();
	}
		
}
