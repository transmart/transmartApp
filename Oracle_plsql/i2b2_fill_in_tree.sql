CREATE OR REPLACE PROCEDURE TM_CZ."I2B2_FILL_IN_TREE" 
(
  trial_id VARCHAR2
 ,path VARCHAR2
 ,currentJobID NUMBER := null
)
AS
  TrialID varchar2(100);
  
    --Audit variables
  newJobFlag INTEGER(1);
  databaseName VARCHAR(100);
  procedureName VARCHAR(100);
  jobID number(18,0);
  stepCt number(18,0);
  
  auditText varchar2(4000);
  
  ----------------------------------------------
  --Goal: To fill out an I2B2 Tree node
  --Steps. Walk backwards through an i2b2 tree and fill in all missing nodes.
  --\1\2\3\4\5\6\
  --Will check that \1\, \1\2\, etc..all exist.
  ----------------------------------------------
  
  -- JEA@20100107 - Added auditing
  
  --Get the nodes
  CURSOR cNodes is
    --Trimming off the last node as it would never need to be added.
    select distinct substr(c_fullname, 1,instr(c_fullname,'\',-2,1)) as c_fullname
    --select c_fullname
    from i2b2 
    where c_fullname like path || '%';
--      and c_hlevel > = 2;
  
  root_node varchar2(1000);
  node_name varchar(1000);
  v_count NUMBER;
  
BEGIN
  TrialID := upper(trial_id);
  
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
  
  --start node with the first slash
 
  --Iterate through each node
  FOR r_cNodes in cNodes Loop
    root_node := '\';
    --Determine how many nodes there are
    --Iterate through, Start with 2 as one will be null from the parser
    
    for loop_counter in 2 .. (length(r_cNodes.c_fullname) - nvl(length(replace(r_cNodes.c_fullname, '\')),0)) / length('\')
    LOOP
      --Determine Node:
      node_name := parse_nth_value(r_cNodes.c_fullname, loop_counter, '\');
      root_node :=  root_node || node_name || '\';
    
      --Dont run for first 2 nodes
    --  if loop_counter > 3 then 
        --Check if node exists. If it does not, add it.
        select count(*)
          into v_count 
        from i2b2
        where c_fullname = root_node;

        --If it doesn't exist, add it
        if v_count = 0 then
			auditText := 'Inserting ' || root_node;
			stepCt := stepCt + 1;
			cz_write_audit(jobId,databaseName,procedureName,auditText,0,stepCt,'Done');
            i2b2_add_node(trial_id, root_node, node_name, jobId);
        end if;
    --  end if;
      
    END LOOP;

    --RESET VARIABLES
    root_node := '';
    node_name := '';
  END LOOP;
  
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

