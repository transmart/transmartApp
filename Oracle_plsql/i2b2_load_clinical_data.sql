CREATE OR REPLACE PROCEDURE TM_CZ."I2B2_LOAD_CLINICAL_DATA_HZ" 
(
  trial_id             IN    VARCHAR2
 ,top_node            in  varchar2
 ,secure_study        in varchar2 := 'N'
 ,highlight_study    in    varchar2 := 'N'
 ,currentJobID        IN    NUMBER := null
)
AS
/*************************************************************************
* Copyright 2008-2012 Janssen Research m, LLC.
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
  
  topNode        VARCHAR2(2000);
  topLevel        number(10,0);
  root_node        varchar2(2000);
  root_level    int;
  study_name    varchar2(2000);
  TrialID        varchar2(100);
  secureStudy    varchar2(200);
  etlDate        date;
  tPath            varchar2(2000);
  pCount        int;
  pExists        int;
  rtnCode        int;
  tText            varchar2(2000);
  v_bio_experiment_id    number(18,0);
  
    --Audit variables
  newJobFlag INTEGER(1);
  databaseName VARCHAR(100);
  procedureName VARCHAR(100);
  jobID number(18,0);
  stepCt number(18,0);
  
  duplicate_values    exception;
  invalid_topNode    exception;
  multiple_visit_names    exception;
  
  CURSOR addNodes is
  select DISTINCT 
         leaf_node,
             node_name
  from  wt_trial_nodes a
  ;
   
    --    cursor to define the path for delete_one_node  this will delete any nodes that are hidden after i2b2_create_concept_counts

    CURSOR delNodes is
    select distinct c_fullname 
    from  i2b2
    where c_fullname like topNode || '%'
      and substr(c_visualattributes,2,1) = 'H';
      
    --    cursor to determine if any leaf nodes exist in i2b2 that are not used in this reload (node changes from text to numeric or numeric to text)
      
    cursor delUnusedLeaf is
    select l.c_fullname
    from i2b2 l
    where l.c_visualattributes like 'L%'
      and l.c_fullname like topNode || '%'
      and l.c_fullname not in
         (select t.leaf_node 
          from wt_trial_nodes t
          union
          select m.c_fullname
          from de_subject_sample_mapping sm
              ,i2b2 m
          where sm.trial_name = TrialId
            and sm.concept_code = m.c_basecode
            and m.c_visualattributes like 'L%');
BEGIN
  
    TrialID := upper(trial_id);
    secureStudy := upper(secure_study);
    
    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
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
    tText := 'Start i2b2_load_clinical_data for ' || TrialId;
    cz_write_audit(jobId,databaseName,procedureName,tText,0,stepCt,'Done');
    
    if (secureStudy not in ('Y','N') ) then
        secureStudy := 'Y';
    end if;
    
    topNode := REGEXP_REPLACE('\' || top_node || '\','(\\){2,}', '\');
    
    --    figure out how many nodes (folders) are at study name and above
    --    \Public Studies\Clinical Studies\Pancreatic_Cancer_Smith_GSE22780\: topLevel = 4, so there are 3 nodes
    --    \Public Studies\GSE12345\: topLevel = 3, so there are 2 nodes
    
    select length(topNode)-length(replace(topNode,'\','')) into topLevel from dual;
    
    if topLevel < 3 then
        raise invalid_topNode;
    end if;    

    --    delete any existing data from lz_src_clinical_data and load new data
    
    delete from lz_src_clinical_data
    where study_id = TrialId;
    
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing data from lz_src_clinical_data',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    
    insert into lz_src_clinical_data
    (study_id
    ,site_id
    ,subject_id
    ,visit_name
    ,data_label
    ,data_value
    ,category_cd
    ,etl_job_id
    ,etl_date
    ,ctrl_vocab_code)
    select study_id
          ,site_id
          ,subject_id
          ,visit_name
          ,data_label
          ,data_value
          ,category_cd
          ,jobId
          ,etlDate
          ,ctrl_vocab_code
    from lt_src_clinical_data;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Insert data into lz_src_clinical_data',SQL%ROWCOUNT,stepCt,'Done');
    commit;
        
    --    truncate wrk_clinical_data and load data from external file
    
    execute immediate('truncate table tm_wz.wrk_clinical_data');
    
    --    insert data from lt_src_clinical_data to wrk_clinical_data
    
    insert into wrk_clinical_data
    (study_id
    ,site_id
    ,subject_id
    ,visit_name
    ,data_label
    ,data_value
    ,category_cd
    ,ctrl_vocab_code
    )
    select study_id
          ,site_id
          ,subject_id
          ,visit_name
          ,data_label
          ,data_value
          ,category_cd
          ,ctrl_vocab_code
    from lt_src_clinical_data;
    
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Load lt_src_clinical_data to work table',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;      

    -- Get study name from topNode
  
    select parse_nth_value(topNode, topLevel, '\') into study_name from dual;    
    
    --    Replace all underscores with spaces in topNode except those in study name

    topNode := replace(replace(topNode,'\'||study_name||'\',null),'_',' ') || '\' || study_name || '\';
    
    -- Get root_node from topNode
  
    select parse_nth_value(topNode, 2, '\') into root_node from dual;
    
    select count(*) into pExists
    from table_access
    where c_name = root_node;
    
    select count(*) into pCount
    from i2b2
    where c_name = root_node;
    
    if pExists = 0 or pCount = 0 then
        i2b2_add_root_node(root_node, jobId);
    end if;
    
    select c_hlevel into root_level
    from table_access
    where c_name = root_node;
    
    --    Add any upper level nodes as needed
    
    tPath := REGEXP_REPLACE(replace(top_node,study_name,null),'(\\){2,}', '\');
    select length(tPath) - length(replace(tPath,'\',null)) into pCount from dual;

    if pCount > 2 then
        stepCt := stepCt + 1;
        cz_write_audit(jobId,databaseName,procedureName,'Adding upper-level nodes',0,stepCt,'Done');
        i2b2_fill_in_tree(null, tPath, jobId);
    end if;
    
/*    Don't delete existing data, concept_cds will be reused
    --    delete any existing data
    
    i2b2_delete_all_nodes(topNode, jobId);
*/

    select count(*) into pExists
    from i2b2
    where c_fullname = topNode;
    
    --    add top node for study
    
    if pExists = 0 then
        i2b2_add_node(TrialId, topNode, study_name, jobId);
    end if;
  
    --    Set data_type, category_path, and usubjid 
  
    update wrk_clinical_data
    set data_type = 'T'
       ,category_path = replace(replace(category_cd,'_',' '),'+','\')
      -- ,usubjid = TrialID || ':' || site_id || ':' || subject_id;
       ,usubjid = REGEXP_REPLACE(TrialID || ':' || site_id || ':' || subject_id,
                   '(::){1,}', ':'); 
     
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Set columns in wrk_clinical_data',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;
  
    --    Delete rows where data_value is null
  
    delete from wrk_clinical_data
    where data_value is null;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete null data_values in wrk_clinical_data',SQL%ROWCOUNT,stepCt,'Done');
    
    --Remove Invalid pipes in the data values.
    --RULE: If Pipe is last or first, delete it
    --If it is in the middle replace with a dash

    update wrk_clinical_data
    set data_value = replace(trim('|' from data_value), '|', '-')
    where data_value like '%|%';
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Remove pipes in data_value',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;  
  
    --Remove invalid Parens in the data
    --They have appeared as empty pairs or only single ones.
  
    update wrk_clinical_data
    set data_value = replace(data_value,'(', '')
    where data_value like '%()%'
       or data_value like '%( )%'
       or (data_value like '%(%' and data_value NOT like '%)%');
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Remove empty parentheses 1',SQL%ROWCOUNT,stepCt,'Done');
    
    update wrk_clinical_data
    set data_value = replace(data_value,')', '')
    where data_value like '%()%'
       or data_value like '%( )%'
       or (data_value like '%)%' and data_value NOT like '%(%');
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Remove empty parentheses 2',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;

    --Replace the Pipes with Commas in the data_label column
    update wrk_clinical_data
    set data_label = replace (data_label, '|', ',')
    where data_label like '%|%';
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Replace pipes with comma in data_label',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;

    --    set visit_name to null when there's only a single visit_name for the catgory
    
    update wrk_clinical_data tpm
    set visit_name=null
    where (tpm.category_cd) in
          (select x.category_cd
           from wrk_clinical_data x
           group by x.category_cd
           having count(distinct upper(x.visit_name)) = 1);

    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Set single visit_name to null',SQL%ROWCOUNT,stepCt,'Done');
        
    commit;
    
    --    set data_label to null when it duplicates the last part of the category_path
    --    Remove data_label from last part of category_path when they are the same
    
    update wrk_clinical_data tpm
    --set data_label = null
    set category_path=substr(tpm.category_path,1,instr(tpm.category_path,'\',-2)-1)
       ,category_cd=substr(tpm.category_cd,1,instr(tpm.category_cd,'+',-2)-1)
    where (tpm.category_cd, tpm.data_label) in
          (select distinct t.category_cd
                 ,t.data_label
           from wrk_clinical_data t
           where upper(substr(t.category_path,instr(t.category_path,'\',-1)+1,length(t.category_path)-instr(t.category_path,'\',-1))) 
                 = upper(t.data_label)
             and t.data_label is not null)
      and tpm.data_label is not null;

    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Set data_label to null when found in category_path',SQL%ROWCOUNT,stepCt,'Done');
        
    commit;

    --    set visit_name to null if same as data_label
    
    update wrk_clinical_data t
    set visit_name=null
    where (t.category_cd, t.visit_name, t.data_label) in
          (select distinct tpm.category_cd
                 ,tpm.visit_name
                 ,tpm.data_label
          from wrk_clinical_data tpm
          where tpm.visit_name = tpm.data_label);

    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Set visit_name to null when found in data_label',SQL%ROWCOUNT,stepCt,'Done');
        
    commit;
    
    --    set visit_name to null if same as data_value
    
    update wrk_clinical_data t
    set visit_name=null
    where (t.category_cd, t.visit_name, t.data_value) in
          (select distinct tpm.category_cd
                 ,tpm.visit_name
                 ,tpm.data_value
          from wrk_clinical_data tpm
          where tpm.visit_name = tpm.data_value);

    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Set visit_name to null when found in data_value',SQL%ROWCOUNT,stepCt,'Done');
        
    commit;
    
    --    set visit_name to null if only DATALABEL in category_cd
    
    update wrk_clinical_data t
    set visit_name=null
    where t.category_cd like '%DATALABEL%'
      and t.category_cd not like '%VISITNAME%';

    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Set visit_name to null when only DATALABE in category_cd',SQL%ROWCOUNT,stepCt,'Done');
        
    commit;

/*    --    Remove sample_type if found in category_path
    
    update wrk_clinical_data t
    set sample_type = null
    where exists
         (select 1 from wrk_clinical_data c
          where instr(c.category_path,t.sample_type) > 0);
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Remove sample_type if already in category_path',SQL%ROWCOUNT,stepCt,'Done');
    commit;
*/
    --    comment out may need later
    
    --    change any % to Pct and & and + to ' and ' and _ to space in data_label only
    
    update wrk_clinical_data
    set data_label=replace(replace(replace(replace(data_label,'%',' Pct'),'&',' and '),'+',' and '),'_',' ')
       ,data_value=replace(replace(replace(data_value,'%',' Pct'),'&',' and '),'+',' and ')
       ,category_cd=replace(replace(category_cd,'%',' Pct'),'&',' and ')
       ,category_path=replace(replace(category_path,'%',' Pct'),'&',' and ');

  --Trim trailing and leadling spaces as well as remove any double spaces, remove space from before comma, remove trailing comma

    update wrk_clinical_data
    set data_label  = trim(trailing ',' from trim(replace(replace(data_label,'  ', ' '),' ,',','))),
        data_value  = trim(trailing ',' from trim(replace(replace(data_value,'  ', ' '),' ,',','))),
--        sample_type = trim(trailing ',' from trim(replace(replace(sample_type,'  ', ' '),' ,',','))),
        visit_name  = trim(trailing ',' from trim(replace(replace(visit_name,'  ', ' '),' ,',',')));
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Remove leading, trailing, double spaces',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;

-- determine numeric data types

    execute immediate('truncate table tm_wz.wt_num_data_types');
  
    insert into wt_num_data_types
    (category_cd
    ,data_label
    ,visit_name
    )
    select category_cd,
           data_label,
           visit_name
    from wrk_clinical_data
    where data_value is not null
    group by category_cd
            ,data_label
            ,visit_name
      having sum(is_number(data_value)) = 0;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Insert numeric data into WZ wt_num_data_types',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;

    --    Check if any duplicate records of key columns (site_id, subject_id, visit_name, data_label, category_cd) for numeric data
    --    exist.  Raise error if yes
    
    execute immediate('truncate table tm_wz.wt_clinical_data_dups');
    
    insert into wt_clinical_data_dups
    (site_id
    ,subject_id
    ,visit_name
    ,data_label
    ,category_cd)
    select w.site_id, w.subject_id, w.visit_name, w.data_label, w.category_cd
    from wrk_clinical_data w
    where exists
         (select 1 from wt_num_data_types t
         where coalesce(w.category_cd,'@') = coalesce(t.category_cd,'@')
           and coalesce(w.data_label,'@') = coalesce(t.data_label,'@')
           and coalesce(w.visit_name,'@') = coalesce(t.visit_name,'@')
          )
    group by w.site_id, w.subject_id, w.visit_name, w.data_label, w.category_cd
    having count(*) > 1;
          
    pCount := SQL%ROWCOUNT;
          
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Check for duplicate key columns',pCount,stepCt,'Done');
              
    if pCount > 0 then
        raise duplicate_values;
    end if;
    
    --    check for multiple visit_names for category_cd, data_label, data_value
    
     select max(case when x.null_ct > 0 and x.non_null_ct > 0
                     then 1 else 0 end) into pCount
      from (select category_cd, data_label, data_value
                  ,sum(decode(visit_name,null,1,0)) as null_ct
                  ,sum(decode(visit_name,null,0,1)) as non_null_ct
            from lt_src_clinical_data
            where (category_cd like '%VISITNAME%' or
                   category_cd not like '%DATALABEL%')
            group by category_cd, data_label, data_value) x;
  
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Check for multiple visit_names for category/label/value ',pCount,stepCt,'Done');
              
    if pCount > 0 then
        raise multiple_visit_names;
    end if;
        
    update wrk_clinical_data t
    set data_type='N'
    where exists
         (select 1 from wt_num_data_types x
          where nvl(t.category_cd,'@') = nvl(x.category_cd,'@')
            and nvl(t.data_label,'**NULL**') = nvl(x.data_label,'**NULL**')
            and nvl(t.visit_name,'**NULL**') = nvl(x.visit_name,'**NULL**')
          );
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Updated data_type flag for numeric data_types',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;

    -- Build all needed leaf nodes in one pass for both numeric and text nodes
 
    execute immediate('truncate table tm_wz.wt_trial_nodes');
    
    insert into wt_trial_nodes
    (leaf_node
    ,category_cd
    ,visit_name
    ,data_label
    --,node_name
    ,data_value
    ,data_type
    )
    select DISTINCT 
    Case 
    --    Text data_type (default node)
    When a.data_type = 'T'
         then case when a.category_path like '%DATALABEL%' and a.category_path like '%VISITNAME%'
              then regexp_replace(topNode || replace(replace(a.category_path,'DATALABEL',a.data_label),'VISITNAME',a.visit_name) || '\' || a.data_value || '\','(\\){2,}', '\')
              when a.category_path like '%DATALABEL%'
              then regexp_replace(topNode || replace(a.category_path,'DATALABEL',a.data_label) || '\' || a.data_value || '\','(\\){2,}', '\')
              else REGEXP_REPLACE(topNode || a.category_path || 
                   '\'  || a.data_label || '\'|| a.visit_name || '\' || a.data_value || '\' ,
                   '(\\){2,}', '\') 
              end
    --    else is numeric data_type and default_node
    else case when a.category_path like '%DATALABEL%' and a.category_path like '%VISITNAME%'
              then regexp_replace(topNode || replace(replace(a.category_path,'DATALABEL',a.data_label),'VISITNAME',a.visit_name) || '\','(\\){2,}', '\')
              when a.category_path like '%DATALABEL%'
              then regexp_replace(topNode || replace(a.category_path,'DATALABEL',a.data_label) || '\','(\\){2,}', '\')
              else REGEXP_REPLACE(topNode || a.category_path || 
                   '\'  || a.data_label || '\' || a.visit_name || '\',
                   '(\\){2,}', '\')
              end
    end as leaf_node,
    a.category_cd,
    a.visit_name,
    a.data_label,
    decode(a.data_type,'T',a.data_value,null) as data_value
    ,a.data_type
    from  wrk_clinical_data a;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Create leaf nodes for trial',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    
    --    set node_name
    
    update wt_trial_nodes
    set node_name=parse_nth_value(leaf_node,length(leaf_node)-length(replace(leaf_node,'\',null)),'\');
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Updated node name for leaf nodes',SQL%ROWCOUNT,stepCt,'Done');
    commit;    
    
    -- execute immediate('analyze table tm_wz.wt_trial_nodes compute statistics');
    
    --    insert subjects into patient_dimension if needed
    
    execute immediate('truncate table tmp_subject_info');

    insert into tmp_subject_info
    (usubjid,
     age_in_years_num,
     sex_cd,
     race_cd
    )
    select a.usubjid,
          nvl(max(case when upper(a.data_label) = 'AGE'
                       then case when is_number(a.data_value) = 1 then 0 else to_number(a.data_value) end
                       when upper(a.data_label) like '%(AGE)' 
                       then case when is_number(a.data_value) = 1 then 0 else to_number(a.data_value) end
                       else null end),0) as age,
          --nvl(max(decode(upper(a.data_label),'AGE',data_value,null)),0) as age,
          nvl(max(case when upper(a.data_label) = 'SEX' then a.data_value
                   when upper(a.data_label) like '%(SEX)' then a.data_value
                   when upper(a.data_label) = 'GENDER' then a.data_value
                   else null end),'Unknown') as sex,
          --max(decode(upper(a.data_label),'SEX',data_value,'GENDER',data_value,null)) as sex,
          max(case when upper(a.data_label) = 'RACE' then a.data_value
                   when upper(a.data_label) like '%(RACE)' then a.data_value
                   else null end) as race
          --max(decode(upper(a.data_label),'RACE',data_value,null)) as race
    from wrk_clinical_data a
    --where upper(a.data_label) in ('AGE','RACE','SEX','GENDER')
    group by a.usubjid;
          
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Insert subject information into temp table',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;
    
    --    Delete dropped subjects from patient_dimension if they do not exist in de_subject_sample_mapping
    
    delete patient_dimension
    where sourcesystem_cd in
         (select distinct pd.sourcesystem_cd from patient_dimension pd
          where pd.sourcesystem_cd like TrialId || ':%'
          minus 
          select distinct cd.usubjid from wrk_clinical_data cd)
      and patient_num not in
          (select distinct sm.patient_id from de_subject_sample_mapping sm);
          
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete dropped subjects from patient_dimension',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;    
    
    --    update patients with changed information
    
    update patient_dimension pd
    set (sex_cd, age_in_years_num, race_cd, update_date) = 
        (select nvl(t.sex_cd,pd.sex_cd), t.age_in_years_num, nvl(t.race_cd,pd.race_cd), sysdate
         from tmp_subject_info t
         where t.usubjid = pd.sourcesystem_cd
           and (coalesce(pd.sex_cd,'@') != t.sex_cd or
                pd.age_in_years_num != t.age_in_years_num or
                coalesce(pd.race_cd,'@') != t.race_cd)
        )
    where exists
         (select 1 from tmp_subject_info x
          where pd.sourcesystem_cd = x.usubjid
            and (coalesce(pd.sex_cd,'@') != x.sex_cd or
                 pd.age_in_years_num != x.age_in_years_num or
                 coalesce(pd.race_cd,'@') != x.race_cd)
         );
          
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Update subjects with changed demographics in patient_dimension',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;    

    --    insert new subjects into patient_dimension
    
    insert into patient_dimension
    (patient_num,
     sex_cd,
     age_in_years_num,
     race_cd,
     update_date,
     download_date,
     import_date,
     sourcesystem_cd
    )
    select seq_patient_num.nextval,
           t.sex_cd,
           t.age_in_years_num,
           t.race_cd,
           sysdate,
           sysdate,
           sysdate,
           t.usubjid
    from tmp_subject_info t
    where t.usubjid in 
         (select distinct cd.usubjid from tmp_subject_info cd
          minus
          select distinct pd.sourcesystem_cd from patient_dimension pd
          where pd.sourcesystem_cd like TrialId || '%');
          
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Insert new subjects into patient_dimension',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;
        
    --    delete leaf nodes that will not be reused, if any
    
     FOR r_delUnusedLeaf in delUnusedLeaf Loop

    --    deletes unused leaf nodes for a trial one at a time

        i2b2_delete_1_node(r_delUnusedLeaf.c_fullname);
        stepCt := stepCt + 1;    
        cz_write_audit(jobId,databaseName,procedureName,'Deleted unused node: ' || r_delUnusedLeaf.c_fullname,SQL%ROWCOUNT,stepCt,'Done');

    END LOOP;    
    
    --    bulk insert leaf nodes
    
    update concept_dimension cd
    set name_char=(select t.node_name from wt_trial_nodes t
                   where cd.concept_path = t.leaf_node
                     and cd.name_char != t.node_name)
    where exists (select 1 from wt_trial_nodes x
                  where cd.concept_path = x.leaf_node
                    and cd.name_char != x.node_name);
          
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Update name_char in concept_dimension for changed names',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;
                            
    
    insert into concept_dimension
    (concept_cd
    ,concept_path
    ,name_char
    ,update_date
    ,download_date
    ,import_date
    ,sourcesystem_cd
    ,table_name
    )
    select concept_id.nextval
         ,x.leaf_node
         ,x.node_name
         ,sysdate
         ,sysdate
         ,sysdate
         ,TrialId
         ,'CONCEPT_DIMENSION'
    from (select distinct c.leaf_node
                ,to_char(c.node_name) as node_name
          from wt_trial_nodes c
          where not exists
            (select 1 from concept_dimension x
            where c.leaf_node = x.concept_path)
         ) x;
    
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Inserted new leaf nodes into I2B2DEMODATA concept_dimension',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    
    --    update i2b2 to pick up change in name, data_type for leaf nodes
    
    update i2b2 b
    set (c_name, c_columndatatype, c_metadataxml)=
        (select t.node_name, t.data_type
         ,case when t.data_type = 'T'
               then null
               else '<?xml version="1.0"?><ValueMetadata><Version>3.02</Version><CreationDateTime>08/14/2008 01:22:59</CreationDateTime><TestID></TestID><TestName></TestName><DataType>PosFloat</DataType><CodeType></CodeType><Loinc></Loinc><Flagstouse></Flagstouse><Oktousevalues>Y</Oktousevalues><MaxStringLength></MaxStringLength><LowofLowValue>0</LowofLowValue><HighofLowValue>0</HighofLowValue><LowofHighValue>100</LowofHighValue>100<HighofHighValue>100</HighofHighValue><LowofToxicValue></LowofToxicValue><HighofToxicValue></HighofToxicValue><EnumValues></EnumValues><CommentsDeterminingExclusion><Com></Com></CommentsDeterminingExclusion><UnitValues><NormalUnits>ratio</NormalUnits><EqualUnits></EqualUnits><ExcludingUnits></ExcludingUnits><ConvertingUnits><Units></Units><MultiplyingFactor></MultiplyingFactor></ConvertingUnits></UnitValues><Analysis><Enums /><Counts /><New /></Analysis></ValueMetadata>'
          end
         from wt_trial_nodes t
         where b.c_fullname = t.leaf_node
           and (b.c_name != t.node_name or b.c_columndatatype != t.data_type))
    where exists
        (select 1 from wt_trial_nodes x
         where b.c_fullname = x.leaf_node
           and (b.c_name != x.node_name or b.c_columndatatype != x.data_type));
    
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Updated name and data type in i2b2 if changed',SQL%ROWCOUNT,stepCt,'Done');
    commit;
               
    insert into i2b2
    (c_hlevel
    ,c_fullname
    ,c_name
    ,c_visualattributes
    ,c_synonym_cd
    ,c_facttablecolumn
    ,c_tablename
    ,c_columnname
    ,c_dimcode
    ,c_tooltip
    ,update_date
    ,download_date
    ,import_date
    ,sourcesystem_cd
    ,c_basecode
    ,c_operator
    ,c_columndatatype
    ,c_comment
    ,i2b2_id
    ,c_metadataxml
    )
    select (length(c.concept_path) - nvl(length(replace(c.concept_path, '\')),0)) / length('\') - 2 + root_level
          ,c.concept_path
          ,c.name_char
          ,'LA'
          ,'N'
          ,'CONCEPT_CD'
          ,'CONCEPT_DIMENSION'
          ,'CONCEPT_PATH'
          ,c.concept_path
          ,c.concept_path
          ,sysdate
          ,sysdate
          ,sysdate
          ,c.sourcesystem_cd
          ,c.concept_cd
          ,'LIKE'
          ,'T'        -- if i2b2 gets fixed to respect c_columndatatype then change to t.data_type
          ,'trial:' || TrialID 
          ,i2b2_id_seq.nextval
          ,case when t.data_type = 'T' then null
           else '<?xml version="1.0"?><ValueMetadata><Version>3.02</Version><CreationDateTime>08/14/2008 01:22:59</CreationDateTime><TestID></TestID><TestName></TestName><DataType>PosFloat</DataType><CodeType></CodeType><Loinc></Loinc><Flagstouse></Flagstouse><Oktousevalues>Y</Oktousevalues><MaxStringLength></MaxStringLength><LowofLowValue>0</LowofLowValue><HighofLowValue>0</HighofLowValue><LowofHighValue>100</LowofHighValue>100<HighofHighValue>100</HighofHighValue><LowofToxicValue></LowofToxicValue><HighofToxicValue></HighofToxicValue><EnumValues></EnumValues><CommentsDeterminingExclusion><Com></Com></CommentsDeterminingExclusion><UnitValues><NormalUnits>ratio</NormalUnits><EqualUnits></EqualUnits><ExcludingUnits></ExcludingUnits><ConvertingUnits><Units></Units><MultiplyingFactor></MultiplyingFactor></ConvertingUnits></UnitValues><Analysis><Enums /><Counts /><New /></Analysis></ValueMetadata>'
           end
    from concept_dimension c
        ,wt_trial_nodes t
    where c.concept_path = t.leaf_node
      and not exists
         (select 1 from i2b2 x
          where c.concept_path = x.c_fullname);
          
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Inserted leaf nodes into I2B2METADATA i2b2',SQL%ROWCOUNT,stepCt,'Done');
    COMMIT;

    --    delete from observation_fact all concept_cds for trial that are clinical data, exclude concept_cds from biomarker data
    
    delete from observation_fact f
    where (f.modifier_cd = TrialId or f.sourcesystem_cd = TrialId)
      and f.concept_cd not in
         (select distinct concept_code as concept_cd from de_subject_sample_mapping
          where trial_name = TrialId
            and concept_code is not null
          union
          select distinct platform_cd as concept_cd from de_subject_sample_mapping
          where trial_name = TrialId
            and platform_cd is not null
          union
          select distinct sample_type_cd as concept_cd from de_subject_sample_mapping
          where trial_name = TrialId
            and sample_type_cd is not null
          union
          select distinct tissue_type_cd as concept_cd from de_subject_sample_mapping
          where trial_name = TrialId
            and tissue_type_cd is not null
          union
          select distinct timepoint_cd as concept_cd from de_subject_sample_mapping
          where trial_name = TrialId
            and timepoint_cd is not null
          union
          select distinct concept_cd as concept_cd from de_subject_snp_dataset
          where trial_name = TrialId
            and concept_cd is not null);
          
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete clinical data for study from observation_fact',SQL%ROWCOUNT,stepCt,'Done');
    COMMIT;          
    
    --Insert into observation_fact
    
    insert into observation_fact
    (patient_num,
     concept_cd,
     modifier_cd,
     valtype_cd,
     tval_char,
     nval_num,
     sourcesystem_cd,
     import_date,
     valueflag_cd,
     provider_id,
     location_cd
    )
    select distinct c.patient_num,
           i.c_basecode,
           '@',
           a.data_type,
           case when a.data_type = 'T' then a.data_value
                else 'E'  --Stands for Equals for numeric types
                end,
           case when a.data_type = 'N' then a.data_value
                else null --Null for text types
                end,
           TrialId, 
          sysdate, 
           '@',
           '@',
           '@'
    from wrk_clinical_data a
        ,patient_dimension c
        ,wt_trial_nodes t
        ,i2b2 i
    where a.usubjid = c.sourcesystem_cd
      and nvl(a.category_cd,'@') = nvl(t.category_cd,'@')
      and nvl(a.data_label,'**NULL**') = nvl(t.data_label,'**NULL**')
      and nvl(a.visit_name,'**NULL**') = nvl(t.visit_name,'**NULL**')
      and decode(a.data_type,'T',a.data_value,'**NULL**') = nvl(t.data_value,'**NULL**')
      and t.leaf_node = i.c_fullname
      and not exists        -- don't insert if lower level node exists
         (select 1 from wt_trial_nodes x
          where x.leaf_node like t.leaf_node || '%_')
      and a.data_value is not null;  
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Insert trial into I2B2DEMODATA observation_fact',SQL%ROWCOUNT,stepCt,'Done');
    
    commit;

    --    update c_visualattributes for all nodes in study, done to pick up node that changed from leaf/numeric to folder/text
    
    update i2b2 a
    set c_visualattributes=(
        with upd as (select p.c_fullname, count(*) as nbr_children 
                 from i2b2 p
                     ,i2b2 c
                 where p.c_fullname like topNode || '%'
                   and c.c_fullname like p.c_fullname || '%'
                 group by p.c_fullname)
        select case when u.nbr_children = 1 
                    then 'L' || substr(a.c_visualattributes,2,2)
                    else 'F' || substr(a.c_visualattributes,2,1) ||
                         case when u.c_fullname = topNode and highlight_study = 'Y'
                              then 'J' else substr(a.c_visualattributes,3,1) end
               end
        from upd u
        where a.c_fullname = u.c_fullname)
    where a.c_fullname in
        (select x.c_fullname from i2b2 x
         where x.c_fullname like topNode || '%');

    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Update c_visualattributes for study',SQL%ROWCOUNT,stepCt,'Done');

    commit;
    
    -- final procs
  
    i2b2_fill_in_tree(TrialId, topNode, jobID);
    
    --    set sourcesystem_cd, c_comment to null if any added upper-level nodes
        
    update i2b2 b
    set sourcesystem_cd=null,c_comment=null
    where b.sourcesystem_cd = TrialId
      and length(b.c_fullname) < length(topNode);
      
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Set sourcesystem_cd to null for added upper-level nodes',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    
    i2b2_create_concept_counts(topNode, jobID);
    
    --    delete each node that is hidden after create concept counts
    
     FOR r_delNodes in delNodes Loop

    --    deletes hidden nodes for a trial one at a time

        i2b2_delete_1_node(r_delNodes.c_fullname);
        stepCt := stepCt + 1;
        tText := 'Deleted node: ' || r_delNodes.c_fullname;
        
        cz_write_audit(jobId,databaseName,procedureName,tText,SQL%ROWCOUNT,stepCt,'Done');

    END LOOP;      

   -- add the study to bio_experiment
    select count(*) into pExists
            from biomart.bio_experiment
            where accession = TrialId;
            
    if pExists = 0 then
         insert into biomart.bio_experiment
                (title, bio_experiment_type, accession, etl_id)
         select 'Metadata not available', 'BIO_CLINICAL_TRIAL'
                      ,TrialId
                      ,'METADATA:' || TrialId
                from dual;
                stepCt := stepCt + 1;
        cz_write_audit(jobId,databaseName,procedureName,'Insert trial/study into biomart.bio_experiment',SQL%ROWCOUNT,stepCt,'Done');
                commit;
    end if;
    
    i2b2_create_security_for_trial(TrialId, secureStudy, jobID);
    i2b2_load_security_data(jobID);

    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'End i2b2_load_clinical_data',0,stepCt,'Done');
    
    ---Cleanup OVERALL JOB if this proc is being run standalone
    if newJobFlag = 1
    then
        cz_end_audit (jobID, 'SUCCESS');
    end if;

    rtnCode := 0;
  
    exception
    when duplicate_values then
        stepCt := stepCt + 1;
        cz_write_audit(jobId,databaseName,procedureName,'Duplicate values found in key columns',0,stepCt,'Done');    
        cz_error_handler (jobID, procedureName);
        cz_end_audit (jobID, 'FAIL');
        rtnCode := 16;        
    when invalid_topNode then
        stepCt := stepCt + 1;
        cz_write_audit(jobId,databaseName,procedureName,'Path specified in top_node must contain at least 2 nodes',0,stepCt,'Done');    
        cz_error_handler (jobID, procedureName);
        cz_end_audit (jobID, 'FAIL');
        rtnCode := 16;    
    when multiple_visit_names then
        stepCt := stepCt + 1;
        cz_write_audit(jobId,databaseName,procedureName,'Multiple visit_names exist for category/label/value',0,stepCt,'Done');    
        cz_error_handler (jobID, procedureName);
        cz_end_audit (jobID, 'FAIL');
        rtnCode := 16;
    when others then
    --Handle errors.
        cz_error_handler (jobID, procedureName);
    --End Proc
        cz_end_audit (jobID, 'FAIL');
        rtnCode := 16;
    
end;
/

