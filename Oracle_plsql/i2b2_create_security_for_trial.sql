CREATE OR REPLACE PROCEDURE TM_CZ."I2B2_CREATE_SECURITY_FOR_TRIAL" 
(
  trial_id VARCHAR2
 ,secured_study varchar2 := 'N'
 ,currentJobID NUMBER := null
)
AS
/*************************************************************************
* Copyright 2008-2012 Janssen Research and Development, LLC.
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

	TrialID 			varchar2(100);
	securedStudy 		varchar2(5);
	pExists				int;
	v_bio_experiment_id	number(18,0);
  
	--Audit variables
	newJobFlag INTEGER(1);
	databaseName VARCHAR(100);
	procedureName VARCHAR(100);
	jobID number(18,0);
	stepCt number(18,0);

BEGIN
	TrialID := trial_id;
	securedStudy := secured_study;
  
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
  
	delete from i2b2demodata.observation_fact
	where case when modifier_cd = '@'
			   then sourcesystem_cd
			   else modifier_cd end = TrialId
	  and concept_cd = 'SECURITY';
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete security records for trial from I2B2DEMODATA observation_fact',SQL%ROWCOUNT,stepCt,'Done');
	
	commit;

	insert into i2b2demodata.observation_fact
    (patient_num
	,concept_cd
	,provider_id
	,modifier_cd
	,valtype_cd
	,tval_char
	,valueflag_cd
	,location_cd
	,update_date
	,download_date
	,import_date
	,sourcesystem_cd
	,instance_num
	)
	select distinct patient_num
		  ,'SECURITY'
		  ,'@'
		  ,'@'
		  ,'T'
		  ,decode(securedStudy,'N','EXP:PUBLIC','EXP:' || trialID)
		  ,'@'
		  ,'@'
		  ,sysdate
		  ,sysdate
		  ,sysdate
		  ,TrialId
		  ,1
	from patient_dimension
	where sourcesystem_cd like TrialID || ':%';
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Insert security records for trial from I2B2DEMODATA observation_fact',SQL%ROWCOUNT,stepCt,'Done');
	
	commit;
	
	--	insert patients to patient_trial table
	
	delete from patient_trial
	where trial  = TrialID;
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Delete data for trial from I2B2DEMODATA patient_trial',SQL%ROWCOUNT,stepCt,'Done');
	
	commit;
  
	insert into i2b2demodata.patient_trial
	(patient_num
	,trial
	,secure_obj_token
	)
	select patient_num, 
		   TrialID,
		   decode(securedStudy,'Y','EXP:' || TrialID,'EXP:PUBLIC')
	from patient_dimension
	where sourcesystem_cd like TrialID || ':%';
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Insert data for trial into I2B2DEMODATA patient_trial',SQL%ROWCOUNT,stepCt,'Done');
	commit;
	
	--	if secure study, then create bio_experiment record if needed and insert to search_secured_object
	
	select count(*) into pExists
	from searchapp.search_secure_object sso
	where bio_data_unique_id = 'EXP:' || TrialId;
	
	if pExists = 0 then
		--	if securedStudy = Y, add trial to searchapp.search_secured_object
		if securedStudy = 'Y' then
			select count(*) into pExists
			from biomart.bio_experiment
			where accession = TrialId;
			
			if pExists = 0 then
				insert into biomart.bio_experiment
				(title, accession, etl_id)
				select 'Metadata not available'
					  ,TrialId
					  ,'METADATA:' || TrialId
				from dual;
				stepCt := stepCt + 1;
				cz_write_audit(jobId,databaseName,procedureName,'Insert trial/study into biomart.bio_experiment',SQL%ROWCOUNT,stepCt,'Done');
				commit;
			end if;
			
			select bio_experiment_id into v_bio_experiment_id
			from biomart.bio_experiment
			where accession = TrialId;
			
			insert into searchapp.search_secure_object
			(bio_data_id
			,display_name
			,data_type
			,bio_data_unique_id
			)
			select v_bio_experiment_id
				  ,parse_nth_value(md.c_fullname,2,'\') || ' - ' || md.c_name as display_name
				  ,'BIO_CLINICAL_TRIAL' as data_type
				  ,'EXP:' || TrialId as bio_data_unique_id
			from i2b2metadata.i2b2 md
			where md.sourcesystem_cd = TrialId
			  and md.c_hlevel = 
				 (select min(x.c_hlevel) from i2b2metadata.i2b2 x
				  where x.sourcesystem_cd = TrialId)
			  and not exists
				 (select 1 from searchapp.search_secure_object so
				  where v_bio_experiment_id = so.bio_data_id);
			stepCt := stepCt + 1;
			cz_write_audit(jobId,databaseName,procedureName,'Inserted trial/study into SEARCHAPP search_secure_object',SQL%ROWCOUNT,stepCt,'Done');
			commit;
		end if;
	else
		--	if securedStudy = N, delete entry from searchapp.search_secure_object
		if securedStudy = 'N' then
			delete from searchapp.search_secure_object
			where bio_data_unique_id = 'EXP:' || TrialId;
			stepCt := stepCt + 1;
			cz_write_audit(jobId,databaseName,procedureName,'Deleted trial/study from SEARCHAPP search_secure_object',SQL%ROWCOUNT,stepCt,'Done');
			commit;
		end if;		
	end if;
     
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
/

