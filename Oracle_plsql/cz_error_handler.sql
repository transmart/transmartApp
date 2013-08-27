CREATE OR REPLACE PROCEDURE TM_CZ."CZ_ERROR_HANDLER" 
(
  jobID NUMBER,
  procedureName NVARCHAR2
)
AS
  databaseName NVARCHAR2(100);
	errorNumber NUMBER(18,0);
	errorMessage NVARCHAR2(1000);
  errorStack NVARCHAR2(4000);
  errorBackTrace NVARCHAR2(4000);
	stepNo NUMBER(18,0);

BEGIN
  --Get DB Name
	select database_name INTO databaseName
		from cz_job_master 
		where job_id=jobID;
  --Get Latest Step
	select max(step_number) into stepNo from cz_job_audit where job_id = jobID;
  
  --Get all error info
  errorNumber := SQLCODE;
  errorMessage := SQLERRM;
  errorStack := dbms_utility.format_error_stack;
  errorBackTrace := dbms_utility.format_error_backtrace;

  --Update the audit step for the error
  cz_write_audit(jobID, databaseName,procedureName, 'Job Failed: See error log for details',SQL%ROWCOUNT, stepNo, 'FAIL');

  
  --write out the error info
  cz_write_error(jobID, errorNumber, errorMessage, errorStack, errorBackTrace);

END;
/

