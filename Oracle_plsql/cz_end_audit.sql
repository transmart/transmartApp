CREATE OR REPLACE PROCEDURE TM_CZ."CZ_END_AUDIT" 
(
  jobID NUMBER, 
  jobStatus VARCHAR2
)
AS

  endDate timestamp;

BEGIN
  
  endDate := systimestamp;
  
	update cz_job_master
		set 
			active='N',
			end_date = endDate,
      time_elapsed_secs = 
      EXTRACT (DAY    FROM (endDate - START_DATE))*24*60*60 + 
      EXTRACT (HOUR   FROM (endDate - START_DATE))*60*60 + 
      EXTRACT (MINUTE FROM (endDate - START_DATE))*60 + 
      EXTRACT (SECOND FROM (endDate - START_DATE)),
			job_status = jobStatus		
		where active='Y' 
		and job_id=jobID;

END;
/

