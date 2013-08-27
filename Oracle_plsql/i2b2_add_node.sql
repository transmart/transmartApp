CREATE OR REPLACE PROCEDURE TM_CZ."I2B2_ADD_NODE" 
(
  TrialID VARCHAR2,
  path VARCHAR2,
  path_name VARCHAR2
 ,currentJobID NUMBER := null
)
AS
  
  root_node		varchar2(2000);
  root_level	int;
  
  
  --Audit variables
  newJobFlag INTEGER(1);
  databaseName VARCHAR(100);
  procedureName VARCHAR(100);
  jobID number(18,0);
  stepCt number(18,0);
  
BEGIN
  
  -------------------------------------------------------------
  -- Add a tree node in I2b2
  -- KCR@20090519 - First Rev
  -- JEA@20100107 - Added auditing
  -- JEA@20111212	Added i2b2_id sequence
  -- JEA@20120529	Updated for i2b2 1.6
  
  -------------------------------------------------------------
    
  stepCt := 0;
	
  --Set Audit Parameters
  newJobFlag := 0; -- False (Default)
  jobID := currentJobID;

  SELECT sys_context('USERENV', 'CURRENT_SCHEMA') INTO databaseName FROM dual;
  procedureName := $$PLSQL_UNIT;
  
	select parse_nth_value(path, 2, '\') into root_node from dual;
	
	select c_hlevel into root_level
	from table_access
	where c_name = root_node;

  --Audit JOB Initialization
  --If Job ID does not exist, then this is a single procedure run and we need to create it
  IF(jobID IS NULL or jobID < 1)
  THEN
    newJobFlag := 1; -- True
    cz_start_audit (procedureName, databaseName, jobID);
  END IF;
  
  if path != ''  or path != '%' or path_name != ''
  then 
    --Delete existing node.
    --I2B2
    DELETE 
      FROM OBSERVATION_FACT 
    WHERE 
      concept_cd IN (SELECT C_BASECODE FROM I2B2 WHERE C_FULLNAME = PATH);
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Deleted any concepts for path from I2B2DEMODATA observation_fact',SQL%ROWCOUNT,stepCt,'Done');
 --   COMMIT;

      --CONCEPT DIMENSION
    DELETE 
      FROM CONCEPT_DIMENSION
    WHERE 
      CONCEPT_PATH = path;
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Deleted any concepts for path from I2B2DEMODATA concept_dimension',SQL%ROWCOUNT,stepCt,'Done');
  --  COMMIT;
    
      --I2B2
      DELETE
        FROM i2b2
      WHERE 
        C_FULLNAME = PATH;
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Deleted path from I2B2METADATA i2b2',SQL%ROWCOUNT,stepCt,'Done');
   -- COMMIT;
    
      --CONCEPT DIMENSION
    INSERT INTO CONCEPT_DIMENSION
      (CONCEPT_CD, CONCEPT_PATH, NAME_CHAR,  UPDATE_DATE,  DOWNLOAD_DATE, IMPORT_DATE, SOURCESYSTEM_CD, TABLE_NAME)
    VALUES
      (concept_id.nextval,
      path,
      to_char(path_name),
      sysdate,
      sysdate,
      sysdate,
      TrialID,
      'CONCEPT_DIMENSION');
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Inserted concept for path into I2B2DEMODATA concept_dimension',SQL%ROWCOUNT,stepCt,'Done');
    --COMMIT;
    
    --I2B2
    INSERT
     INTO I2B2
      (c_hlevel, C_FULLNAME, C_NAME, C_VISUALATTRIBUTES, c_synonym_cd, C_FACTTABLECOLUMN, C_TABLENAME, C_COLUMNNAME,
      C_DIMCODE, C_TOOLTIP, UPDATE_DATE, DOWNLOAD_DATE, IMPORT_DATE, SOURCESYSTEM_CD, c_basecode, C_OPERATOR, c_columndatatype, c_comment,
	  i2b2_id, m_applied_path)
    SELECT 
      (length(concept_path) - nvl(length(replace(concept_path, '\')),0)) / length('\') - 2 + root_level,
      CONCEPT_PATH,
      NAME_CHAR,
      'FA',
      'N',
      'CONCEPT_CD',
      'CONCEPT_DIMENSION',
      'CONCEPT_PATH',
      CONCEPT_PATH,
      CONCEPT_PATH,
      sysdate,
      sysdate,
      sysdate,
      SOURCESYSTEM_CD,
      CONCEPT_CD,
      'LIKE',
      'T',
      decode(TrialID,null,null,'trial:' || TrialID),
	  i2b2_id_seq.nextval,
	  '@'
    FROM
      CONCEPT_DIMENSION
    WHERE 
      CONCEPT_PATH = path;
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Inserted path into I2B2METADATA i2b2',SQL%ROWCOUNT,stepCt,'Done');
    COMMIT;
	  END IF;
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

