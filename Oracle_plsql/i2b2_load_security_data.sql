CREATE OR REPLACE PROCEDURE TM_CZ."I2B2_LOAD_SECURITY_DATA" 
(
  currentJobID NUMBER := null
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

  --Audit variables
  newJobFlag INTEGER(1);
  databaseName VARCHAR(100);
  procedureName VARCHAR(100);
  jobID number(18,0);
  stepCt number(18,0);

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

  Execute immediate ('truncate table I2B2METADATA.i2b2_secure');

  stepCt := stepCt + 1;
  cz_write_audit(jobId,databaseName,procedureName,'Truncate I2B2METADATA i2b2_secure',0,stepCt,'Done');

  insert into I2B2METADATA.i2b2_secure(
    C_HLEVEL,
    C_FULLNAME,
    C_NAME,
    C_SYNONYM_CD,
    C_VISUALATTRIBUTES,
    C_TOTALNUM,
    C_BASECODE,
    C_METADATAXML,
    C_FACTTABLECOLUMN,
    C_TABLENAME,
    C_COLUMNNAME,
    C_COLUMNDATATYPE,
    C_OPERATOR,
    C_DIMCODE,
    C_COMMENT,
    C_TOOLTIP,
    UPDATE_DATE,
    DOWNLOAD_DATE,
    IMPORT_DATE,
    SOURCESYSTEM_CD,
    VALUETYPE_CD,
	secure_obj_token)
  select
    b.C_HLEVEL,
    b.C_FULLNAME,
    b.C_NAME,
    b.C_SYNONYM_CD,
    b.C_VISUALATTRIBUTES,
    b.C_TOTALNUM,
    b.C_BASECODE,
    b.C_METADATAXML,
    b.C_FACTTABLECOLUMN,
    b.C_TABLENAME,
    b.C_COLUMNNAME,
    b.C_COLUMNDATATYPE,
    b.C_OPERATOR,
    b.C_DIMCODE,
    b.C_COMMENT,
    b.C_TOOLTIP,
    b.UPDATE_DATE,
    b.DOWNLOAD_DATE,
    b.IMPORT_DATE,
    b.SOURCESYSTEM_CD,
    b.VALUETYPE_CD,
	coalesce(f.tval_char,'EXP:PUBLIC')
    from I2B2METADATA.I2B2 b
		,(select distinct case when sourcesystem_cd like '%:%' then substr(sourcesystem_cd,1,instr(sourcesystem_cd,':')-1)
							   else sourcesystem_cd end as sourcesystem_cd
				,tval_char from observation_fact where concept_cd = 'SECURITY') f
	where b.sourcesystem_cd = f.sourcesystem_cd(+);
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Insert security data into I2B2METADATA i2b2_secure',SQL%ROWCOUNT,stepCt,'Done');

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

end;
/

