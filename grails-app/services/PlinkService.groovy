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
  



import org.jfree.util.Log;

import com.recomdata.export.PlinkFiles;
import groovy.sql.*;

import i2b2.SubjectSnpDataset;
import i2b2.SnpProbeSortedDef;


class PlinkService {
	

	def dataSource;

	/**
	 *  extract the selected corhort through patient_num
	 *  
	 * @param subjectIds
	 * @return
	 */
	
	def String [] getStudyInfoBySubject(String subjectIds){
	
		def sql = new Sql(dataSource);
		
		String query = "select platform_name, trial_name from DE_SUBJECT_SNP_DATASET "						  
		query       += " where rownum=1 and platform_name is not null and patient_num in (" + subjectIds + ")";
		
		def row = sql.firstRow(query)
		
		return [row.platform_name, row.trial_name]
	}

	/**
	 * Retrieve the Platform_Name and Trial_Name based on the Result_Instance_Id
	 * 
	 * @param resultInstanceId
	 * @return
	 */
	def String [] getStudyInfoByResultInstanceId(String resultInstanceId){
		
		def sql = new Sql(dataSource);
		
		def query = """
						select a.platform_name, a.trial_name 
						from DE_SUBJECT_SNP_DATASET a
						INNER JOIN de_subject_sample_mapping c on c.omic_patient_id=a.patient_num
						INNER JOIN (SELECT DISTINCT patient_num FROM qt_patient_set_collection WHERE result_instance_id = CAST(? AS numeric) AND patient_num IN
						            (SELECT patient_num FROM patient_dimension WHERE sourcesystem_cd NOT LIKE '%:S:%')) b on c.patient_id=b.patient_num
						where rownum=1 
						and a.platform_name is not null
					"""
		def row = sql.firstRow(query, [resultInstanceId])
		
		return [row.platform_name, row.trial_name]
	}
	
	/**
	 *  Create a *.map file for PLINK
	 *  
	 * @param subjectIds
	 * @param chr
	 * @param plinkMapFile
	 */
	
	def void getMapDataByChromosome(String subjectIds, String chr, File plinkMapFile){
		
		def sql = new Sql(dataSource);
		 
		// 0 -- Platform Name   1 -- Trial Name
		def platform = getStudyInfoBySubject(subjectIds)[0]; 
		
		String chroms;
		if(chr.contains(",")) {
			chroms = chr.replace(",", "','");
		} else {
			chroms = chr;
		}
		
		
		String query = """ SELECT probe_def FROM de_snp_probe_sorted_def 
		                   WHERE chrom in (?) and platform_name=?""";

		sql.eachRow(query, [chroms, platform]) { it ->
			if(it.probe_def != null){
				java.sql.Clob clob = (java.sql.Clob) it.probe_def;
				//plinkMapFile.append clob.getAsciiStream().getText();
				// change probe_def format from "SNP  chr  position" to "chr  SNP position"
				clob.getAsciiStream().getText().eachLine { 
					def items = it.split()
					plinkMapFile.append(items[1] + "\t" + items[0] + "\t" + items[2] + "\n")
				}				
			}			
		};
	}


	/**
	 *   Create a *.ped file for PLINK
	 *   
	 * @param subjectIds
	 * @param chr
	 * @param plinkMapFile
	 */
	
	def void getSnpDataBySujectChromosome(String subjectIds, String chr, File plinkPedFile){
		
		def query;
		def sql = new Sql(dataSource);
		
		// 0 -- Platform Name   1 -- Trial Name
		def trialName = getStudyInfoBySubject(subjectIds)[1]; 
		
		String chroms;
		if(chr.contains(",")) {
			chroms = chr.replace(",", "','");
		} else {
			chroms = chr;
		}
		
		query = """SELECT t1.PATIENT_NUM, 
		                  case t2.PATIENT_GENDER 
		                      when 'M' then 1
		                      when 'F' then 2
		                      else 0
		                  end as PATIENT_GENDER,
		                  t1.PED_BY_PATIENT_CHR 
		           FROM DE_SNP_DATA_BY_PATIENT t1,  
		                (select distinct PATIENT_NUM, TRIAL_NAME, PATIENT_GENDER, SUBJECT_SNP_DATASET_ID 
		                 from DE_SUBJECT_SNP_DATASET) t2
		           WHERE t1.PATIENT_NUM=t2.PATIENT_NUM and t1.TRIAL_NAME=t2.TRIAL_NAME and 
		           		 t1.PED_BY_PATIENT_CHR is not null and 
		           		 t2.SUBJECT_SNP_DATASET_ID=t1.SNP_DATASET_ID and 
		                 t1.chrom in (?) and t1.trial_name=?""";

		
		sql.eachRow(query, [chroms, trialName]) { it ->
			if(it.PED_BY_PATIENT_CHR != null){
				java.sql.Clob clob = (java.sql.Clob) it.PED_BY_PATIENT_CHR;
				plinkPedFile.append("${it.PATIENT_NUM} ${it.PATIENT_NUM} 0 0 ${it.PATIENT_GENDER} 0  ${clob.getAsciiStream().getText()}\n");
			}			
		};
	}

	/**
	*   Create a *.ped file for PLINK
	*
	* @param subjectIds
	* @param chr
	* @param plinkMapFile
	*/
   
   def void getSnpDataBySujectChromosome(String subjectIds, String chr, File plinkPedFile, 
	      List<String> conceptCodeList, String isAffected){
	   
	   def query;
	   def sql = new Sql(dataSource);
	   
	   // 0 -- Platform Name   1 -- Trial Name
	   def trialName = getStudyInfoBySubject(subjectIds)[1];
	   
	   String chroms;
	   if(chr.contains(",")) {
		   chroms = chr.replace(",", "','");
	   } else {
		   chroms = chr;
	   }
	   
	   String conceptCd = ""
	   if(conceptCodeList.size() > 0){
		   for(item in 0..conceptCodeList.size()-2) {
			   conceptCd += "'" + conceptCodeList[item] + "',"
		   }
		   conceptCd += "'" + conceptCodeList[conceptCodeList.size()-1] + "'"
	   }
	   
	   query = """SELECT t1.PATIENT_NUM,
						 case t2.PATIENT_GENDER
							 when 'M' then 1
							 when 'F' then 2
							 else 0
						 end as PATIENT_GENDER,
						 t1.PED_BY_PATIENT_CHR
				  FROM DE_SNP_DATA_BY_PATIENT t1,
					   (select distinct PATIENT_NUM, TRIAL_NAME, PATIENT_GENDER, SUBJECT_SNP_DATASET_ID
						from DE_SUBJECT_SNP_DATASET
						where concept_cd in (""" + conceptCd + """) and patient_num in (""" + subjectIds + """)) t2
				  WHERE t1.PATIENT_NUM=t2.PATIENT_NUM and t1.TRIAL_NAME=t2.TRIAL_NAME and
						   t1.PED_BY_PATIENT_CHR is not null and
						   t2.SUBJECT_SNP_DATASET_ID=t1.SNP_DATASET_ID and
						t1.chrom in (?) and t1.trial_name=?""";

	   sql.eachRow(query, [chroms, trialName]) { it ->
		   if(it.PED_BY_PATIENT_CHR != null){
			   java.sql.Clob clob = (java.sql.Clob) it.PED_BY_PATIENT_CHR;
			   plinkPedFile.append("${it.PATIENT_NUM} ${it.PATIENT_NUM} 0 0 ${it.PATIENT_GENDER} ${isAffected} ${clob.getAsciiStream().getText()}\n");
		   }
	   };
   }

	def getPhenotypicDataByPatient(String subjectIds){
	}

	def getPhenotypicDataByChromosome(String chromsomes){
	}


	def getPhenotypicDataByGene(String genes){
	}
}
