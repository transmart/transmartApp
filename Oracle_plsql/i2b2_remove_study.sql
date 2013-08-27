CREATE OR REPLACE PROCEDURE TM_CZ.I2B2_REMOVE_STUDY 
(
   study_id 			IN	VARCHAR2
  ,currentJobID		IN	NUMBER := null
)
AS
 -- not tested yet zhanh101 5/23/2013
topNode varchar2(2000);
etl_id  varchar2(2000);
etl_source_id varchar2(2000);
v_bio_experiment_id   number(18,0);

  --Audit variables
  newJobFlag INTEGER(1);
  databaseName VARCHAR(100);
  procedureName VARCHAR(100);
  jobID number(18,0);
  stepCt number(18,0);
 
BEGIN
	StudyID :=upper(study_id);
	--set audit parameters
	newJobFlag :=0;
	jobID := currentJobID;

	SELECT sys_context('USERENV', 'CURRENT_SCHEMA') INTO databaseName FROM dual;
	procedureName := $$PLSQL_UNIT;
	
	select sysdate into etlDate from dual;

	--Audit JOB Initialization
	--If Job ID does not exist, then this is a single procedure run and we need to create it
	IF(jobID IS NULL or jobID < 1)
	THEN
		newJobFlag := 1; -- True
		cz_start_audit (procedureName, databaseName, jobID);
	END IF;
    	
	stepCt := 0;

	stepCt := stepCt + 1;
	tText := 'Start i2b2_remove_study for ' || StudyID;
	cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Done');
	
	--get topNode
    select c_fullname into topNode from i2b2metadata.i2b2 where sourcesystem_CD=StudyID and c_hlevel=1;	
	
	if (topNode IS NOT NULL)
	THEN
		stepCt := stepCt + 1;
		cz_write_audit(jobId,databaseName,procedureName,'get clinical topNode for study',SQL%ROWCOUNT,stepCt,'Done');
		commit;
	
		--execute i2b2_backout_trial;
		I2B2_BACKOUT_TRIAL(StudyID, topNode,jobID);
		
	END IF;
	
	--get analysis associated
	
	 FOR r_delAnalysis in (select ETL_ID_source from bio_assay_analysis where ETL_ID=study_id) Loop

    --	deletes hidden nodes for a trial one at a time

		i2b2_remove_analysis(r_delAnalysis, jobID);
		stepCt := stepCt + 1;
		tText := 'Deleted analysis: ETL_ID_SOURCE=' || r_delAnalysis;
		
		cz_write_audit(jobId,databaseName,procedureName,tText,SQL%ROWCOUNT,stepCt,'Done');
	END LOOP;
	
	
	
   -- delete entries in bio_experiment and bio_clinical_trial
	select bio_experiment_id into v_bio_experiment_id from biomart.bio_experiment where accession=study_id;
	
	delete from bio_experiment where bio_experiment_id=v_bio_experiment_id;
	
	delete from bio_clinical_trial where bio_experiment_id=v_bio_experiment_id;
	
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'End i2b2_remove_study',0,stepCt,'Done');
	
    ---Cleanup OVERALL JOB if this proc is being run standalone
	if newJobFlag = 1
	then
		cz_end_audit (jobID, 'SUCCESS');
	end if;
	
	exception
		 --Handle errors.
		cz_error_handler (jobID, procedureName);
    --End Proc
		cz_end_audit (jobID, 'FAIL');
		rtnCode := 16;
	
end;
/
		
	
