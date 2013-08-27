CREATE OR REPLACE PROCEDURE TM_CZ."I2B2_LOAD_STUDY_METADATA" 
(
  currentJobID NUMBER := null
)
AS
/*************************************************************************
* Copyright 2008-2012 Janssen Research n, LLC.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************/

  
	--Audit variables
	newJobFlag INTEGER(1);
	databaseName VARCHAR(100);
	procedureName VARCHAR(100);
	jobID number(18,0);
	stepCt number(18,0);
	
	dcount 				int;
	lcount 				int;
	upload_date			timestamp;
	tmp_compound		varchar2(200);
	tmp_disease			varchar2(200);
	tmp_organism		varchar2(200);
	tmp_pubmed			varchar2(200);
	pubmed_id			varchar2(200);
	pubmed_title		varchar2(200);
	
	Type study_compound_rec is record
	(study_id	varchar2(200)
	,compound	varchar2(500)
	);
  
	Type study_compound_tab is table of study_compound_rec;
  
	study_compound_array study_compound_tab;
  
	Type study_disease_rec is record
	(study_id	varchar2(200)
	,disease	varchar2(500)
	);
  
	Type study_disease_tab is table of study_disease_rec;
  
	study_disease_array study_disease_tab;
  
	Type study_taxonomy_rec is record
	(study_id	varchar2(200)
	,organism	varchar2(500)
	);
  
	Type study_taxonomy_tab is table of study_taxonomy_rec;
  
	study_taxonomy_array study_taxonomy_tab;
    
	Type study_pubmed_rec is record
	(study_id	varchar2(200)
	,pubmed	varchar2(500)
	);
  
	Type study_pubmed_tab is table of study_pubmed_rec;
  
	study_pubmed_array study_pubmed_tab;

BEGIN
    
	--Set Audit Parameters
	newJobFlag := 0; -- False (Default)
	jobID := currentJobID;

	SELECT sys_context('USERENV', 'CURRENT_SCHEMA') INTO databaseName FROM dual;
	procedureName := $$PLSQL_UNIT;

	--Audit JOB Initialization
	--If Job ID does not exist, then this is a single procedure run and we need to create it
	IF(jobID IS NULL or jobID < 1)
	THEN
		newJobFlag := 1; -- True
		cz_start_audit (procedureName, databaseName, jobID);
	END IF;
  
	stepCt := 0;
	select sysdate into upload_date from dual;
	
	--	delete existing metadata from lz_src_study_metadata
	
	delete from lz_src_study_metadata
	where study_id in (select distinct study_id from lt_src_study_metadata);
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing metadata in lz_src_study_metadata',SQL%ROWCOUNT,stepCt,'Done');
	commit;

	--	insert metadata into lz_src_study_metadata
	
	insert into lz_src_study_metadata
	select x.*, upload_date
	from lt_src_study_metadata x;
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing metadata in lz_src_study_metadata',SQL%ROWCOUNT,stepCt,'Done');
	commit;
	
	--	delete existing metadata from lz_src_study_metadata_ad_hoc
	
	delete from lz_src_study_metadata_ad_hoc
	where study_id in (select distinct study_id from lt_src_study_metadata);
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing metadata in lz_src_study_metadata_ad_hoc',SQL%ROWCOUNT,stepCt,'Done');
	commit;

	--	insert metadata into lz_src_study_metadata_ad_hoc
	
	insert into lz_src_study_metadata_ad_hoc
	select x.*, upload_date
	from lt_src_study_metadata_ad_hoc x;
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing metadata in lz_src_study_metadata_ad_hoc',SQL%ROWCOUNT,stepCt,'Done');
	commit;
  
	--	Update existing bio_experiment data
	
	update biomart.bio_experiment b
	set (title
	    ,description
		,design
		,start_date
		,completion_date
		,primary_investigator
		,overall_design
		,institution
		,country) =
	    (select m.title
		       ,m.description
			   ,m.design
			   ,decode(is_date(m.start_date,'YYYYMMDD'),1,null,to_date(m.start_date,'YYYYMMDD'))
			   ,decode(is_date(m.completion_date,'YYYYMMDD'),1,null,to_date(m.completion_date,'YYYYMMDD'))
			   ,coalesce(m.primary_investigator,m.study_owner)
			   ,coalesce(substr(decode(m.primary_end_points,null,null,'N/A',null,m.primary_end_points) ||
					    decode(m.inclusion_criteria,null,null,'N/A',null,' Inclusion Criteria: ' || m.inclusion_criteria) ||
						decode(m.exclusion_criteria,null,null,'N/A',null,' Exclusion Criteria: ' || m.exclusion_criteria),1,2000)
						,m.overall_design)
			   ,m.institution
			   ,m.country
		 from lt_src_study_metadata m
		 where m.study_id is not null
		   and b.accession = m.study_id)
	where exists
		(select 1 from lt_src_study_metadata x
		 where b.accession = x.study_id
		   and b.etl_id = 'METADATA:' || x.study_id
		   and x.study_id is not null)
	;
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Updated trial data in BIOMART bio_experiment',SQL%ROWCOUNT,stepCt,'Done');
	commit;
	
	--	Update existing bio_clinical_trial data only for true Clinical Trials or JnJ Experimental Medicine Studies

	update biomart.bio_clinical_trial b
	set (study_owner
	    ,study_phase
		,blinding_procedure
		,studytype
		,duration_of_study_weeks
		,number_of_patients
		,number_of_sites
		,route_of_administration
		,dosing_regimen
		,group_assignment
		,type_of_control
		,completion_date
		,primary_end_points
		,secondary_end_points
		,inclusion_criteria
		,exclusion_criteria
		,subjects
		,gender_restriction_mfb
		,min_age
		,max_age
		,secondary_ids
		,development_partner
		,main_findings
		,geo_platform
		--,platform_name
		,search_area
        ) =
		(select m.study_owner
			   ,m.study_phase
			   ,m.blinding_procedure
			   ,m.studytype
			   ,decode(is_number(m.duration_of_study_weeks),1,null,to_number(m.duration_of_study_weeks))
			   ,decode(is_number(m.number_of_patients),1,null,to_number(m.number_of_patients))
			   ,decode(is_number(m.number_of_sites),1,null,to_number(m.number_of_sites))
			   ,m.route_of_administration
			   ,m.dosing_regimen
			   ,m.group_assignment
			   ,m.type_of_control
			   ,decode(is_date(m.completion_date,'YYYYMMDD'),1,null,to_date(m.completion_date,'YYYYMMDD'))
			   ,m.primary_end_points
			   ,m.secondary_end_points
			   ,m.inclusion_criteria
			   ,m.exclusion_criteria
			   ,m.subjects
			   ,m.gender_restriction_mfb
			   ,decode(is_number(m.min_age),1,null,to_number(m.min_age))
			   ,decode(is_number(m.max_age),1,null,to_number(m.max_age))
			   ,m.secondary_ids
			   ,m.development_partner
			   ,m.main_findings
			   ,m.geo_platform
			   --,m.platform_name
			   ,m.search_area
		 from lt_src_study_metadata m
		 where m.study_id is not null
		   and b.trial_number = m.study_id
		)
	where exists
	     (select 1 from lt_src_study_metadata x
		  where b.trial_number = x.study_id
		    and x.study_id is not null
		 )
	;
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Updated trial data in BIOMART bio_clinical_trial',SQL%ROWCOUNT,stepCt,'Done');
	commit;
	
	--	Add new trial data to bio_experiment
	
	insert into biomart.bio_experiment
	(bio_experiment_type
	,title
	,description
	,design
	,start_date
	,completion_date
	,primary_investigator
	,contact_field
	,etl_id
	,status
	,overall_design
	,accession
	,country
	,institution)
	select 'Experiment'
	      ,m.title
		  ,m.description
		  ,m.design
		  ,decode(is_date(m.start_date,'YYYYMMDD'),1,null,to_date(m.start_date,'YYYYMMDD'))
		  ,decode(is_date(m.completion_date,'YYYYMMDD'),1,null,to_date(m.completion_date,'YYYYMMDD'))
		  ,coalesce(m.primary_investigator,m.study_owner)
		  ,m.contact_field
		  ,'METADATA:' || m.study_id
		  ,m.study_id
		  ,coalesce(decode(m.primary_end_points,null,null,'N/A',null,replace(m.primary_end_points,'"',null)) ||
					    decode(m.inclusion_criteria,null,null,'N/A',null,' Inclusion Criteria: ' || replace(m.inclusion_criteria,'"',null)) ||
						decode(m.exclusion_criteria,null,null,'N/A',null,' Exclusion Criteria: ' || replace(m.exclusion_criteria,'"',null))
						,m.overall_design)
		  ,m.study_id
		  ,m.country
		  ,m.institution
	from lt_src_study_metadata m
	where m.study_id is not null
	  and not exists
	      (select 1 from biomart.bio_experiment x
		   where m.study_id = x.accession
		     and m.study_id is not null);
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Inserted trial data in BIOMART bio_experiment',SQL%ROWCOUNT,stepCt,'Done');
	commit;
		
	--	Add new trial data to bio_clinical_trial
	
	insert into biomart.bio_clinical_trial
	(trial_number
	,study_owner
	,study_phase
	,blinding_procedure
	,studytype
	,duration_of_study_weeks
	,number_of_patients
	,number_of_sites
	,route_of_administration
	,dosing_regimen
	,group_assignment
	,type_of_control
	,completion_date
	,primary_end_points
	,secondary_end_points
	,inclusion_criteria
	,exclusion_criteria
	,subjects
	,gender_restriction_mfb
	,min_age
	,max_age
	,secondary_ids
	,bio_experiment_id
	,development_partner
	,main_findings
	,geo_platform
	--,platform_name
	,search_area
	)
	select m.study_id
          ,m.study_owner
          ,m.study_phase
          ,m.blinding_procedure
          ,m.studytype
		  ,decode(is_number(m.duration_of_study_weeks),1,null,to_number(m.duration_of_study_weeks))
		  ,decode(is_number(m.number_of_patients),1,null,to_number(m.number_of_patients))
		  ,decode(is_number(m.number_of_sites),1,null,to_number(m.number_of_sites))
          ,m.route_of_administration
          ,m.dosing_regimen
          ,m.group_assignment
          ,m.type_of_control
          ,decode(is_date(m.completion_date,'YYYYMMDD'),1,null,to_date(m.completion_date,'YYYYMMDD'))
          ,m.primary_end_points
          ,m.secondary_end_points
          ,m.inclusion_criteria
          ,m.exclusion_criteria
          ,m.subjects
          ,m.gender_restriction_mfb
		  ,decode(is_number(m.min_age),1,null,to_number(m.min_age))
		  ,decode(is_number(m.max_age),1,null,to_number(m.max_age))
          ,m.secondary_ids
          ,b.bio_experiment_id
		  ,m.development_partner
		  ,m.main_findings
		  ,m.geo_platform
		  --,m.platform_name
		  ,m.search_area
	from lt_src_study_metadata m
	    ,biomart.bio_experiment b
	where m.study_id is not null
	  and m.study_id = b.accession
	  and not exists
	      (select 1 from biomart.bio_clinical_trial x
		   where m.study_id = x.trial_number);
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Inserted trial data in BIOMART bio_clinical_trial',SQL%ROWCOUNT,stepCt,'Done');
	commit;
	
	--	Insert new trial into bio_data_uid
	
	insert into biomart.bio_data_uid
	(bio_data_id
	,unique_id
	,bio_data_type
	)
	select distinct b.bio_experiment_id
	      ,'EXP:' || m.study_id
		  ,'EXP'
	from biomart.bio_experiment b
		,lt_src_study_metadata m
	where m.study_id is not null
	  and m.study_id = b.accession
	  and not exists
	      (select 1 from biomart.bio_data_uid x
		   where x.unique_id = 'EXP:' || m.study_id)
	;
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Inserted trial data into BIOMART bio_data_uid',SQL%ROWCOUNT,stepCt,'Done');
	commit;

	--	delete existing compound data for study, compound list may change
	
	delete bio_data_compound dc
	where dc.bio_data_id in 
		 (select x.bio_experiment_id
		  from bio_experiment x
			  ,lt_src_study_metadata y
		  where x.accession = y.study_id
		    and x.etl_id = 'METADATA:' || y.study_id);

	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing data from bio_data_compound',SQL%ROWCOUNT,stepCt,'Done');
	commit;

	select distinct study_id, compound
	bulk collect into study_compound_array
	from lt_src_study_metadata
	where compound is not null;
	
	if SQL%ROWCOUNT > 0 then 
		for i in study_compound_array.first .. study_compound_array.last
		loop
		
			select length(study_compound_array(i).compound) -
				   length(replace(study_compound_array(i).compound,';',null))+1
				into dcount
			from dual;
	 
			while dcount > 0
			Loop	
		
				select parse_nth_value(study_compound_array(i).compound,dcount,';') into tmp_compound
				from dual;
				   
				--	add new compound
				
				insert into bio_compound bc
				(generic_name)
				select tmp_compound
				from dual
				where not exists
					 (select 1 from bio_compound x
					  where upper(x.generic_name) = upper(tmp_compound))
				  and tmp_compound is not null;
				stepCt := stepCt + 1;
				cz_write_audit(jobId,databaseName,procedureName,'Added compound to bio_compound',SQL%ROWCOUNT,stepCt,'Done');
				commit;
							
				--	Insert new trial data into bio_data_compound

				insert into bio_data_compound
				(bio_data_id
				,bio_compound_id
				,etl_source
				)
				select b.bio_experiment_id
					  ,c.bio_compound_id
					  ,'METADATA:' || study_compound_array(i).study_id
				from biomart.bio_experiment b
					,biomart.bio_compound c
				where upper(tmp_compound) = upper(c.generic_name) 
				  and tmp_compound is not null
				  and b.accession = study_compound_array(i).study_id
				  and not exists
						 (select 1 from biomart.bio_data_compound x
						  where b.bio_experiment_id = x.bio_data_id
							and c.bio_compound_id = x.bio_compound_id);

				stepCt := stepCt + 1;
				cz_write_audit(jobId,databaseName,procedureName,'Inserted trial data in BIOMART bio_data_compound',SQL%ROWCOUNT,stepCt,'Done');
				commit;
				
				dcount := dcount - 1;
			end loop;
		end loop;
	end if;

	--	delete existing disease data for studies
	
	delete bio_data_disease dc
	where dc.bio_data_id in 
		 (select x.bio_experiment_id
		  from bio_experiment x
			  ,lt_src_study_metadata y
		  where x.accession = y.study_id
		    and x.etl_id = 'METADATA:' || y.study_id);

	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing data from bio_data_disease',SQL%ROWCOUNT,stepCt,'Done');
	commit;

	select distinct study_id, disease
	bulk collect into study_disease_array
	from lt_src_study_metadata
	where disease is not null;
	
	if SQL%ROWCOUNT > 0 then 
		for i in study_disease_array.first .. study_disease_array.last
		loop
		
			select length(study_disease_array(i).disease) -
				   length(replace(study_disease_array(i).disease,';',null))+1
				into dcount
			from dual;
	 
			while dcount > 0
			Loop	
		
				select parse_nth_value(study_disease_array(i).disease,dcount,';') into tmp_disease
				from dual;
				   
				--	add new disease
				
				insert into bio_disease bc
				(disease
				,prefered_name)
				select tmp_disease
					  ,tmp_disease
				from dual
				where not exists
					 (select 1 from bio_disease x
					  where upper(x.disease) = upper(tmp_disease))
				  and tmp_disease is not null;
				stepCt := stepCt + 1;
				cz_write_audit(jobId,databaseName,procedureName,'Added disease to bio_disease',SQL%ROWCOUNT,stepCt,'Done');
				commit;
							
				--	Insert new trial data into bio_data_disease

				insert into bio_data_disease
				(bio_data_id
				,bio_disease_id
				,etl_source
				)
				select b.bio_experiment_id
					  ,c.bio_disease_id
					  ,'METADATA:' || study_disease_array(i).study_id
				from biomart.bio_experiment b
					,biomart.bio_disease c
				where upper(tmp_disease) = upper(c.disease) 
				  and tmp_disease is not null
				  and b.accession = study_disease_array(i).study_id
				  and not exists
						 (select 1 from biomart.bio_data_disease x
						  where b.bio_experiment_id = x.bio_data_id
							and c.bio_disease_id = x.bio_disease_id);

				stepCt := stepCt + 1;
				cz_write_audit(jobId,databaseName,procedureName,'Inserted trial data in BIOMART bio_data_disease',SQL%ROWCOUNT,stepCt,'Done');
				commit;
				
				dcount := dcount - 1;
			end loop;
		end loop;
	end if;

	--	delete existing taxonomy data for studies
	
	delete bio_data_taxonomy dc
	where dc.bio_data_id in 
		 (select x.bio_experiment_id
		  from bio_experiment x
			  ,lt_src_study_metadata y
		  where x.accession = y.study_id
		    and x.etl_id = 'METADATA:' || y.study_id);

	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing data from bio_data_taxonomy',SQL%ROWCOUNT,stepCt,'Done');
	commit;

	select distinct study_id, organism
	bulk collect into study_taxonomy_array
	from lt_src_study_metadata
	where organism is not null;
	
	if SQL%ROWCOUNT > 0 then 
		for i in study_taxonomy_array.first .. study_taxonomy_array.last
		loop
		
			select length(study_taxonomy_array(i).organism) -
				   length(replace(study_taxonomy_array(i).organism,';',null))+1
				into dcount
			from dual;
	 
			while dcount > 0
			Loop	
		
				select parse_nth_value(study_taxonomy_array(i).organism,dcount,';') into tmp_organism
				from dual;
				   
				--	add new organism
				
				insert into bio_taxonomy bc
				(taxon_name
				,taxon_label)
				select tmp_organism
					  ,tmp_organism
				from dual
				where not exists
					 (select 1 from bio_taxonomy x
					  where upper(x.taxon_name) = upper(tmp_organism))
				  and tmp_organism is not null;
				stepCt := stepCt + 1;
				cz_write_audit(jobId,databaseName,procedureName,'Added organism to bio_taxonomy',SQL%ROWCOUNT,stepCt,'Done');
				commit;
							
				--	Insert new trial data into bio_data_taxonomy

				insert into bio_data_taxonomy
				(bio_data_id
				,bio_taxonomy_id
				,etl_source
				)
				select b.bio_experiment_id
					  ,c.bio_taxonomy_id
					  ,'METADATA:' || study_disease_array(i).study_id
				from biomart.bio_experiment b
					,biomart.bio_taxonomy c
				where upper(tmp_organism) = upper(c.taxon_name) 
				  and tmp_organism is not null
				  and b.accession = study_disease_array(i).study_id
				  and not exists
						 (select 1 from biomart.bio_data_taxonomy x
						  where b.bio_experiment_id = x.bio_data_id
							and c.bio_taxonomy_id = x.bio_taxonomy_id);

				stepCt := stepCt + 1;
				cz_write_audit(jobId,databaseName,procedureName,'Inserted trial data in BIOMART bio_data_taxonomy',SQL%ROWCOUNT,stepCt,'Done');
				commit;
				
				dcount := dcount - 1;
			end loop;
		end loop;
	end if;
	
	--	add ncbi/GEO linking
	
	--	check if ncbi exists in bio_content_repository, if not, add
	
	select count(*) into dcount
	from bio_content_repository
	where repository_type = 'NCBI'
	  and location_type = 'URL';
	
	if dcount = 0 then
		insert into bio_content_repository
		(location
		,active_y_n
		,repository_type
		,location_type) 
		values ('http://www.ncbi.nlm.nih.gov/','Y','NCBI','URL');
		
		stepCt := stepCt + 1;
		cz_write_audit(jobId,databaseName,procedureName,'Inserted NCBI URL in bio_content_repository',SQL%ROWCOUNT,stepCt,'Done');
		commit;
		
	end if;

	--	insert GSE studies into bio_content
	
	insert into bio_content
	(repository_id
	,location
	,file_type
	,etl_id_c
	)
	select bcr.bio_content_repo_id
		  ,'geo/query/acc.cgi?acc=' || m.study_id
		  ,'Experiment Web Link'
		  ,'METADATA:' || m.study_id
	from lt_src_study_metadata m
		,bio_content_repository bcr
	where m.study_id like 'GSE%'
	  and bcr.repository_type = 'NCBI'
	  and bcr.location_type = 'URL'
	  and not exists
		 (select 1 from bio_content x
		  where x.etl_id_c like '%' || m.study_id || '%'
		    and x.file_type = 'Experiment Web Link'
			and x.location = 'geo/query/acc.cgi?acc=' || m.study_id);

	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Inserted GEO study into bio_content',SQL%ROWCOUNT,stepCt,'Done');
	commit;
	
	--	insert GSE studies into bio_content_reference
	
	insert into bio_content_reference
	(bio_content_id
	,bio_data_id
	,content_reference_type
	,etl_id_c
	)
	select bc.bio_file_content_id
		  ,be.bio_experiment_id
		  ,'Experiment Web Link'
		  ,'METADATA:' || m.study_id
	from lt_src_study_metadata m
		,bio_experiment be
		,bio_content bc
	where m.study_id like 'GSE%'
	  and m.study_id = be.accession
	  and bc.file_type = 'Experiment Web Link'
	  and bc.etl_id_c = 'METADATA:' || m.study_id
	  and bc.location = 'geo/query/acc.cgi?acc=' || m.study_id
	  and not exists
		 (select 1 from bio_content_reference x
		  where bc.bio_file_content_id = x.bio_content_id
		    and be.bio_experiment_id = x.bio_data_id);

	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Inserted GEO study into bio_content_reference',SQL%ROWCOUNT,stepCt,'Done');
	commit;

	--	add PUBMED linking
	
	--	delete existing pubmed data for studies
	
	delete bio_content_reference dc
	where dc.bio_content_id in 
		 (select x.bio_file_content_id
		  from bio_content x
			  ,lt_src_study_metadata y
		  where x.file_type = 'Publication Web Link'
		    and x.etl_id_c = 'METADATA:' || y.study_id);

	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing Pubmed data from bio_content_reference',SQL%ROWCOUNT,stepCt,'Done');
	commit;			
			
	delete bio_content dc
	where dc.bio_file_content_id in 
		 (select x.bio_file_content_id
		  from bio_content x
			  ,lt_src_study_metadata y
		  where x.file_type = 'Publication Web Link'
		    and x.etl_id_c = 'METADATA:' || y.study_id);

	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing Pubmed data from bio_content',SQL%ROWCOUNT,stepCt,'Done');
	commit;
	
	select distinct study_id, pubmed_ids
	bulk collect into study_pubmed_array
	from lt_src_study_metadata
	where pubmed_ids is not null;
	
	if SQL%ROWCOUNT > 0 then
		--	check if PubMed url exists in bio_content_repository, if not, add
		select count(*) into dcount
		from bio_content_repository
		where repository_type = 'PubMed';	
	
		if dcount = 0 then
			insert into bio_content_repository
			(location
			,active_y_n
			,repository_type
			,location_type) 
			values ('http://www.ncbi.nlm.nih.gov/pubmed/','Y','PubMed','URL');
			stepCt := stepCt + 1;
			cz_write_audit(jobId,databaseName,procedureName,'Inserted GEO study into bio_content_reference',SQL%ROWCOUNT,stepCt,'Done');
			commit;
		end if;

		for i in study_pubmed_array.first .. study_pubmed_array.last
		loop
			select length(study_pubmed_array(i).pubmed)-length(replace(study_pubmed_array(i).pubmed,';',null))+1
			into dcount
			from dual;
 
			while dcount > 0
			Loop	
				-- multiple pubmed id can be separated by ;, pubmed id and title are separated by :
				
				select parse_nth_value(study_pubmed_array(i).pubmed,dcount,';') into tmp_pubmed from dual;			
				select instr(tmp_pubmed,':') into lcount from dual;
				
				if lcount = 0 then
					pubmed_id := tmp_pubmed;
					pubmed_title := null;
				else
					pubmed_id := substr(tmp_pubmed,1,instr(tmp_pubmed,':')-1);
					cz_write_audit(jobId,databaseName,procedureName,'pubmed_id: ' || pubmed_id,1,stepCt,'Done');	
					pubmed_title := substr(tmp_pubmed,instr(tmp_pubmed,':')+1);
					cz_write_audit(jobId,databaseName,procedureName,'pubmed_title: ' || pubmed_title,1,stepCt,'Done');
				end if;
	
				insert into bio_content
				(repository_id
				,location
				,title
				,file_type
				,etl_id_c
				)
				select bcr.bio_content_repo_id
					  ,pubmed_id
					  ,pubmed_title
					  ,'Publication Web Link'
					  ,'METADATA:' || study_pubmed_array(i).study_id
				from bio_content_repository bcr
				where bcr.repository_type = 'PubMed'
				  and not exists
					 (select 1 from bio_content x
					  where x.etl_id_c like '%' || study_pubmed_array(i).study_id || '%'
					    and x.file_type = 'Publication Web Link'
						and x.location = pubmed_id);

				stepCt := stepCt + 1;
				cz_write_audit(jobId,databaseName,procedureName,'Inserted pubmed for study into bio_content',SQL%ROWCOUNT,stepCt,'Done');
				commit;				
		
				insert into bio_content_reference
				(bio_content_id
				,bio_data_id
				,content_reference_type
				,etl_id_c
				)
				select bc.bio_file_content_id
					  ,be.bio_experiment_id
					  ,'Publication Web Link'
					  ,'METADATA:' || study_pubmed_array(i).study_id
				from bio_experiment be
					,bio_content bc
				where be.accession = study_pubmed_array(i).study_id
				  and bc.file_type = 'Publication Web Link'
				  and bc.etl_id_c = 'METADATA:' || study_pubmed_array(i).study_id
				  and bc.location = pubmed_id
				  and not exists
					 (select 1 from bio_content_reference x
					  where bc.bio_file_content_id = x.bio_content_id
						and be.bio_experiment_id = x.bio_data_id);	

				stepCt := stepCt + 1;
				cz_write_audit(jobId,databaseName,procedureName,'Inserted pubmed for study into bio_content_reference',SQL%ROWCOUNT,stepCt,'Done');
				commit;				
	
				dcount := dcount - 1;
			end loop;
		end loop;
	end if;
	
		--	Create i2b2_tags

	delete from i2b2_tags
	where upper(tag_type) = 'Trial';
	
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing Trial tags in i2b2_tags',SQL%ROWCOUNT,stepCt,'Done');
	commit;
	
	insert into i2b2_tags
	(path, tag, tag_type, tags_idx)
	select min(b.c_fullname) as path
		  ,be.accession as tag
		  ,'Trial' as tag_type
		  ,0 as tags_idx
	from bio_experiment be
		,i2b2 b
	where be.accession = b.sourcesystem_cd
	group by be.accession;
	
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Add Trial tags in i2b2_tags',SQL%ROWCOUNT,stepCt,'Done');
	commit;
					 
	--	Insert trial data tags - COMPOUND
	
	delete from i2b2_tags t
	where upper(t.tag_type) = 'COMPOUND';

	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing Compound tags in I2B2METADATA i2b2_tags',SQL%ROWCOUNT,stepCt,'Done');
	commit;	
	
	insert into i2b2_tags
	(path, tag, tag_type, tags_idx)
	select distinct min(o.c_fullname) as path
		  ,decode(x.rec_num,1,c.generic_name,c.brand_name) as tag
		  ,'Compound' as tag_type
		  ,1 as tags_idx
	from bio_experiment be
		,bio_data_compound bc
		,bio_compound c
		,i2b2 o
		,(select rownum as rec_num from table_access where rownum < 3) x
	where be.bio_experiment_id = bc.bio_data_id
       and bc.bio_compound_id = c.bio_compound_id
       and be.accession = o.sourcesystem_cd
       and decode(x.rec_num,1,c.generic_name,c.brand_name) is not null
	group by decode(x.rec_num,1,c.generic_name,c.brand_name);

	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Insert Compound tags in I2B2METADATA i2b2_tags',SQL%ROWCOUNT,stepCt,'Done');
	commit;	
					 
	--	Insert trial data tags - DISEASE
	
	delete from i2b2_tags t
	where upper(t.tag_type) = 'DISEASE';

	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing DISEASE tags in I2B2METADATA i2b2_tags',SQL%ROWCOUNT,stepCt,'Done');
	commit;	
		
	insert into i2b2_tags
	(path, tag, tag_type, tags_idx)
	select distinct min(o.c_fullname) as path
		   ,c.prefered_name
		   ,'Disease' as tag_type
		   ,1 as tags_idx
	from bio_experiment be
		,bio_data_disease bc
		,bio_disease c
		,i2b2 o
      --,(select rownum as rec_num from table_access where rownum < 3) x
	where be.bio_experiment_id = bc.bio_data_id
      and bc.bio_disease_id = c.bio_disease_id
      and be.accession = o.sourcesystem_cd
    --and decode(x.rec_num,1,c.generic_name,c.brand_name) is not null
	group by c.prefered_name;

	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Insert Disease tags in I2B2METADATA i2b2_tags',SQL%ROWCOUNT,stepCt,'Done');
	commit;	

	--	Load bio_ad_hoc_property
	
	delete from bio_ad_hoc_property
	where bio_data_id in
		 (select distinct x.bio_experiment_id 
		  from lt_src_study_metadata_ad_hoc t
			  ,bio_experiment x
		  where t.study_id = x.accession);
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing ad_hoc metadata from BIOMART BIO_AD_HOC_PROPERTY',SQL%ROWCOUNT,stepCt,'Done');
	commit;		 
	
	insert into bio_ad_hoc_property
	(bio_data_id
	,property_key
	,property_value)
	select b.bio_experiment_id
		  ,t.ad_hoc_property_key
		  ,t.ad_hoc_property_value
	from lt_src_study_metadata_ad_hoc t
		,bio_experiment b
	where t.study_id = b.accession;
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Insert ad_hoc metadata into BIOMART BIO_AD_HOC_PROPERTY',SQL%ROWCOUNT,stepCt,'Done');
	commit;
	
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'End i2b2_load_study_metadata',SQL%ROWCOUNT,stepCt,'Done');
	commit;
	
    ---Cleanup OVERALL JOB if this proc is being run standalone
	IF newJobFlag = 1
	THEN
		cz_end_audit (jobID, 'SUCCESS');
	END IF;

	EXCEPTION
	WHEN OTHERS THEN
		--Handle errors.
		cz_error_handler (jobID, procedureName);
		
		--End Proc
		cz_end_audit (jobID, 'FAIL');
	
END;


/*	ignore for now
	--	Add trial/study to search_secure_object
	
	insert into searchapp.search_secure_object
	(bio_data_id
	,display_name
	,data_type
	,bio_data_unique_id
	)
	select b.bio_experiment_id
	      ,parse_nth_value(md.c_fullname,2,'\') || ' - ' || b.accession as display_name
		  ,'BIO_CLINICAL_TRIAL' as data_type
		  ,'EXP:' || b.accession as bio_data_unique_id
	from i2b2metadata.i2b2 md
		,biomart.bio_experiment b
	where b.accession = md.sourcesystem_cd
	  and md.c_hlevel = 0
	  and md.c_fullname not like '\Public Studies\%'
	  and md.c_fullname not like '\Internal Studies\%'
	  and md.c_fullname not like '\Experimental Medicine Study\NORMALS\%'
	  and not exists
		 (select 1 from searchapp.search_secure_object so
		  where b.bio_experiment_id = so.bio_data_id)
	;
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Inserted new trial/study into SEARCHAPP search_secure_object',SQL%ROWCOUNT,stepCt,'Done');
	commit;
*/


/*	not used	
	--	Insert WORKFLOW tags
	
	delete from i2b2_tags
	where tag_type = 'WORKFLOW';
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete existing trial WORKFLOW in I2B2METADATA i2b2_tags',SQL%ROWCOUNT,stepCt,'Done');
	commit;	
	
	insert into i2b2_tags
	(path
	,tag_type
	,tag
	)
	select distinct b.c_fullname
		  ,'WORKFLOW'
		  ,decode(d.platform,'MRNA_AFFYMETRIX','Gene Expression','RBM','RBM','Protein','Protein',null) as tag
	from deapp.de_subject_sample_mapping d
		,i2b2 b
	where d.platform is not null
	  and d.trial_name = b.sourcesystem_cd
	  and b.c_hlevel = 0
	  and b.c_fullname not like '%Across Trials%';
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Inserted heatmap WORKFLOW in I2B2METADATA i2b2_tags',SQL%ROWCOUNT,stepCt,'Done');
	commit;	

	insert into i2b2_tags
	(path
	,tag_type
	,tag
	)
	select distinct c.c_fullname
		  ,'WORKFLOW'
		  ,'SNP'
	from deapp.de_snp_data_by_patient snp
	,i2b2 c
	where snp.trial_name = c.sourcesystem_cd
	  and c.c_hlevel = 0;
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Inserted SNP WORKFLOW in I2B2METADATA i2b2_tags',SQL%ROWCOUNT,stepCt,'Done');
	commit;		
*/
/

