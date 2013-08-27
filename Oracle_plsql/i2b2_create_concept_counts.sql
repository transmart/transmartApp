CREATE OR REPLACE PROCEDURE TM_CZ."I2B2_CREATE_CONCEPT_COUNTS" 
(
  path VARCHAR2
 ,currentJobID NUMBER := null
)
AS
  -------------------------------------------------------------
  -- Insert records into the Concept Counts table for new nodes
  -- KCR@20090404 - First Rev
  -- KCR@20090709 - NEXT Rev
  -- JEA@20090817 - Changed processing to eliminate need for cursor
  -- JEA@20091118 - Added auditing
  -- JEA@20100507 - Changed to account for Biomarker mRNA nodes that may have different patient counts from
  --				the Samples & Timepoints concept
  -- JEA220100702 - Remove separate pass for Biomarker mRNA nodes, they now have unique concept codes
  -- JEA@20111025	Exclude samples from being counted as subjects
  -- JEA@20120113	Allow for third character in c_visualattributes
  
  --1. BUILD A TEMP TABLE OF ALL CONCEPT CODES WITH THEIR PATIENTS.
  -- NEED TO INCLUDE ROLLUPS OF INDIRECT RELATIONSHIPS (FOLDERS TO THEIR CHILDREN)
  --Build a cursor of Paths by level
  --iterate through the paths in reverse, so determine max level and go backwards, 
  --this way each folder will have the data needed when you get to it already rolled up

  -------------------------------------------------------------
    
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
  
  delete 
    from concept_counts
  where 
    concept_path like path || '%';
  stepCt := stepCt + 1;
  cz_write_audit(jobId,databaseName,procedureName,'Delete counts for trial from I2B2DEMODATA concept_counts',SQL%ROWCOUNT,stepCt,'Done');
	
  commit;

/*	Removed because mRNA nodes have unique concept_cds (20100702)
  
	execute immediate('truncate table tmp_concept_counts');
	
	--	insert data for leaf nodes, do Biomarker mRNA nodes first so that the correct patients are joined to de_subject_sample_mapping
	--	this is done because mRNA nodes can share concept_cds with Samples & Timepoints nodes
	
	insert into tmp_concept_counts
	(leaf_path
	,patient_num
	)
	select distinct la.c_fullname
		  ,tpm.patient_num
	from i2b2 la
		,observation_fact tpm
		,deapp.de_subject_sample_mapping sm
		,reference.gpl_info gi
	where la.c_fullname like path || '%'
	  and la.c_visualattributes like 'L%'
	  and la.c_basecode = tpm.concept_cd(+)
	  and tpm.patient_num = sm.patient_id
	  and tpm.modifier_cd = sm.trial_name
	  and sm.platform = 'MRNA_AFFYMETRIX'
	  and sm.gpl_id = gi.platform
	  and la.c_fullname like path || '%' || gi.title || '%';
	
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Insert mRNA leaf counts for trial into I2B2DEMODATA concept_counts',SQL%ROWCOUNT,stepCt,'Done');
	commit;	
	
	--	insert data for remaining leaf nodes and exclude Biomarker mRNA leaf nodes
	
	insert into tmp_concept_counts
	(leaf_path
	,patient_num
	)
	select distinct la.c_fullname
		  ,tpm.patient_num
	from i2b2 la
		,observation_fact tpm
	where la.c_fullname like path || '%'
	  and la.c_visualattributes like 'L%'
	  and la.c_basecode = tpm.concept_cd(+)
	  and not exists
	     (select 1 from tmp_concept_counts cx
		  where la.c_fullname = cx.leaf_path);
	
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Insert all remaining leaf counts for trial into I2B2DEMODATA concept_counts',SQL%ROWCOUNT,stepCt,'Done');
	commit;	
*/
	
	--	Join each node (folder or leaf) in the path to it's leaf in the work table to count patient numbers

	insert into concept_counts
	(concept_path
	,parent_concept_path
	,patient_count
	)
	select /*+ INDEX(I2B2 I2B2_IDX1)*/ fa.c_fullname
		  ,ltrim(SUBSTR(fa.c_fullname, 1,instr(fa.c_fullname, '\',-1,2)))
		  ,count(distinct tpm.patient_num)
	from i2b2 fa
	    ,i2b2 la
		,observation_fact tpm
		,patient_dimension p
	where fa.c_fullname like path || '%'
	  and substr(fa.c_visualattributes,2,1) != 'H'
	  and la.c_fullname like fa.c_fullname || '%'
	  and la.c_visualattributes like 'L%'
	  and tpm.patient_num = p.patient_num
	  and p.sourcesystem_cd not like '%:S:%'
	  and la.c_basecode = tpm.concept_cd(+)
	group by fa.c_fullname
			,ltrim(SUBSTR(fa.c_fullname, 1,instr(fa.c_fullname, '\',-1,2)));
			
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Insert counts for trial into I2B2DEMODATA concept_counts',SQL%ROWCOUNT,stepCt,'Done');
		
	commit;

	--execute immediate('truncate table tmp_concept_counts');
	
	--SET ANY NODE WITH MISSING OR ZERO COUNTS TO HIDDEN
    execute immediate('create index cc_path_count_idx on CONCEPT_COUNTS(CONCEPT_PATH, PATIENT_COUNT) tablespace "INDX"');
    execute immediate('create index cc_path_idx on CONCEPT_COUNTS(CONCEPT_PATH) tablespace "INDX"');

	updat i2b2
	set c_visualattributes = substr(c_visualattributes,1,1) || 'H' || substr(c_visualattributes,3,1)
	where c_fullname like path || '%'
	  and (not exists
			 (select 1 from concept_counts nc
				  where c_fullname = nc.concept_path)
				 or
			 exists
				 (select 1 from concept_counts zc
				  where c_fullname = zc.concept_path
					and zc.patient_count = 0)
			  )
		and c_name != 'SECURITY';
		
	stepCt := stepCt + 1;
	cz_write_audit(jobId,databaseName,procedureName,'Nodes hidden with missing/zero counts for trial into I2B2DEMODATA concept_counts',SQL%ROWCOUNT,stepCt,'Done');
		
        execute immediate('drop index cc_path_count_idx');
        execute immediate('drop index cc_path_idx');
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

/*	needed for i2b2 1.4

update i2b2 i
set c_totalnum=(select count(distinct tpm.patient_num)
				 from i2b2 la
				     ,observation_fact tpm
				 where la.c_fullname like i.c_fullname || '%'
                   and la.c_visualattributes like 'L%'
                   and la.c_basecode = tpm.concept_cd(+)
				)
where exists
     (select 1 from i2b2 lax
				   ,observation_fact tpmx
	  where lax.c_fullname like i.c_fullname || '%'
        and lax.c_visualattributes like 'L%'
        and lax.c_basecode = tpmx.concept_cd(+)
       )
and i.c_visualattributes not like '%H%'
and i.c_fullname like '%BEERLUNG%'

*/

/* old CODE

--	Cursor

  maxLevel NUMBER := 0;
  currentLevel number := 0;

  CURSOR cPath is
  select a.concept_cd, a.concept_path
    FROM concept_dimension a
    join i2b2 b
      on a.concept_path = b.c_fullname
    where b.c_hlevel = currentLevel
      and b.c_visualattributes not like '%H%' --do not consider Hidden values
      and b.c_fullname like path || '%';

  insert 
    into concept_counts(
      patient_count, 
      concept_path)  
  select 
    count(distinct c.patient_num) patient_count, 
    e.c_fullname as concept_path 
  from 
    i2b2 e
  join
    concept_dimension d
  on 
    d.concept_path like e.c_fullname ||'%'
  left outer join 
    observation_fact c
  on 
    d.concept_cd = c.concept_cd
  where
    e.c_fullname like path || '%'  
  group by (e.c_fullname);  
  commit;

  --determine the parent_path
  update concept_counts
  set parent_concept_path = ltrim(SUBSTR(concept_path, 1,instr(Concept_Path, '\',-1,2))) 
  where concept_path like path || '%';
  commit;
  
    update i2b2
    set c_visualattributes = 'FH'
  where c_fullname like path || '%'
    and c_visualattributes like 'F%'
    and c_fullname in (select concept_path from concept_counts where patient_count = 0 and concept_path like path || '%')
    and c_name != 'SECURITY';
  commit;
  
  
    update i2b2
    set c_visualattributes = 'LH'
  where c_fullname like path || '%'
    and c_visualattributes like 'L%'
    and c_fullname in (select concept_path from concept_counts where patient_count = 0 and concept_path like path || '%')
    and c_name != 'SECURITY';
  
*/

/*	The following code was never implemented in production

  --Truncate temp table
  EXECUTE IMMEDIATE('TRUNCATE TABLE I2B2_PATIENT_ROLLUP');

  --REMOVE RECORDS FROM CONCEPT COUNTS FOR THIS PATH

  --get max level
  SELECT max(c_hlevel) into maxLevel 
    FROM i2b2
      WHERE c_visualattributes not like '%H%' --do not consider Hidden values
      and c_fullname like path || '%';

  --iterate through all paths by level in reverse
  FOR Lpath IN REVERSE 0..maxLevel
  LOOP
    --inner loop through cursor for the particular level
    currentLevel := Lpath;
    FOR r_cPath in cPath Loop
      insert into i2b2_patient_rollup
        SELECT distinct r_cPath.concept_cd, r_cPath.concept_path, b.patient_num, currentLevel
          from concept_dimension a
          join observation_fact b
            on a.concept_cd = b.concept_cd
            and a.concept_cd = r_cPath.concept_cd
        union
        select distinct r_cPath.concept_cd, r_cPath.concept_path, a.patient_num, currentLevel
          from i2b2_patient_rollup a
            where a.concept_path like r_cPath.concept_path || '%'
              and a.c_hlevel = (currentLevel + 1);
    COMMIT;
    END LOOP;  
  END LOOP;
  
  --aggregate the temp table and load into concept_counts  
  INSERT
  INTO CONCEPT_COUNTS
  (
    CONCEPT_PATH,
    PATIENT_COUNT
  )
  SELECT CONCEPT_PATH, COUNT(DISTINCT PATIENT_NUM)
  FROM i2b2_patient_rollup
  GROUP BY CONCEPT_PATH;
  COMMIT;
*/
/

