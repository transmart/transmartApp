CREATE OR REPLACE PROCEDURE TM_CZ."I2B2_ADD_ROOT_NODE" 
(root_node		varchar2
,currentJobID	NUMBER := null
)
AS
	--	JEA@20120510	New
	--	JEA@20120529	Updated for i2b2 1.6
	
	--Audit variables
	newJobFlag 	INTEGER(1);
	databaseName 	VARCHAR(100);
	procedureName VARCHAR(100);
	jobID 		number(18,0);
	stepCt 		number(18,0);

	rootNode	varchar2(200);
	rootPath	varchar2(200);
	
Begin
	rootNode := root_node;
	rootPath := '\' || rootNode || '\';

    stepCt := 0;
	
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
	
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Start ' || procedureName,0,stepCt,'Done');
	
	insert into table_access
	select rootNode as c_table_cd
		  ,'i2b2' as c_table_name
		  ,'N' as protected_access
		  ,0 as c_hlevel
		  ,rootPath as c_fullname
		  ,rootNode as c_name
		  ,'N' as c_synonym_cd
		  ,'CA' as c_visualattributes
		  ,null as c_totalnum
		  ,null as c_basecode
		  ,null as c_metadataxml
		  ,'concept_cd' as c_facttablecolumn
		  ,'concept_dimension' as c_dimtablename
		  ,'concept_path' as c_columnname
		  ,'T' as c_columndatatype
		  ,'LIKE' as c_operator
		  ,rootPath as c_dimcode
		  ,null as c_comment
		  ,rootPath as c_tooltip
		  ,sysdate as c_entry_date
		  ,null as c_change_date
		  ,null as c_status_cd
		  ,null as valuetype_cd
	from dual
	where not exists
		(select 1 from table_access x
		 where x.c_table_cd = rootNode);
	
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Insert to table_access',SQL%ROWCOUNT,stepCt,'Done');
    COMMIT;	

	--	insert root_node into i2b2
	
	insert into i2b2
	(c_hlevel
	,c_fullname
	,c_name
	,c_synonym_cd
	,c_visualattributes
	,c_totalnum
	,c_basecode
	,c_metadataxml
	,c_facttablecolumn
	,c_tablename
	,c_columnname
	,c_columndatatype
	,c_operator
	,c_dimcode
	,c_comment
	,c_tooltip
	,update_date
	,download_date
	,import_date
	,sourcesystem_cd
	,valuetype_cd
	,i2b2_id
	)
	select 0 as c_hlevel
		  ,rootPath as c_fullname
		  ,rootNode as c_name
		  ,'N' as c_synonym_cd
		  ,'CA' as c_visualattributes
		  ,null as c_totalnum
		  ,null as c_basecode
		  ,null as c_metadataxml
		  ,'concept_cd' as c_facttablecolumn
		  ,'concept_dimension' as c_tablename
		  ,'concept_path' as c_columnname
		  ,'T' as c_columndatatype
		  ,'LIKE' as c_operator
		  ,rootPath as c_dimcode
		  ,null as c_comment
		  ,rootPath as c_tooltip
		  ,sysdate as update_date
		  ,null as download_date
		  ,sysdate as import_date
		  ,null as sourcesystem_cd
		  ,null as valuetype_cd
		  ,I2B2_ID_SEQ.nextval as i2b2_id
	from dual
	where not exists
		 (select 1 from i2b2 x
		  where x.c_name = rootNode);
		  
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Insert root_node ' || rootNode || ' to i2b2',SQL%ROWCOUNT,stepCt,'Done');
    COMMIT;	
			
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'End ' || procedureName,0,stepCt,'Done');
	
    COMMIT;
	--Cleanup OVERALL JOB if this proc is being run standalone
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

