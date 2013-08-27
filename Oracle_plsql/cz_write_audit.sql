CREATE OR REPLACE PROCEDURE TM_CZ."CZ_WRITE_AUDIT" 
(
	jobId IN NUMBER,
	databaseName IN VARCHAR2 , 
	procedureName IN VARCHAR2 , 
	stepDesc IN VARCHAR2 , 
	recordsManipulated IN NUMBER,
	stepNumber IN NUMBER,
	stepStatus IN VARCHAR2
)
AS
  lastTime timestamp;
BEGIN
  select max(job_date)
    into lastTime
    from cz_job_audit
    where job_id = jobID;

	insert 	into cz_job_audit(
		job_id, 
		database_name,
 		procedure_name, 
 		step_desc, 
		records_manipulated,
		step_number,
		step_status,
    job_date,
    time_elapsed_secs
	)
	select
 		jobId,
		databaseName,
		procedureName,
		stepDesc,
		recordsManipulated,
		stepNumber,
		stepStatus,
    SYSTIMESTAMP,
      COALESCE(
      EXTRACT (DAY    FROM (SYSTIMESTAMP - lastTime))*24*60*60 + 
      EXTRACT (HOUR   FROM (SYSTIMESTAMP - lastTime))*60*60 + 
      EXTRACT (MINUTE FROM (SYSTIMESTAMP - lastTime))*60 + 
      EXTRACT (SECOND FROM (SYSTIMESTAMP - lastTime))
      ,0)
  from dual;
  
  COMMIT;

END;
/

