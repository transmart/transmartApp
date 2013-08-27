CREATE OR REPLACE PROCEDURE TM_CZ."I2B2_MOVE_ANALYSIS_TO_PROD_NEW" 
(i_etl_id        number    := -1
,i_job_id        number    := null
)
AS
    -- create indexes using parallele 8  -zhanh101 5/10/2013 use ~20-30% original time  
    --Audit variables
    newJobFlag     INTEGER(1);
    databaseName     VARCHAR(100);
    procedureName VARCHAR(100);
    jobID         number(18,0);
    stepCt         number(18,0);
    
    v_etl_id                    number(18,0);
    v_bio_assay_analysis_id        number(18,0);
    v_data_type                    varchar2(50);
    v_sqlText                    varchar2(2000);
    v_exists                    int;
    v_GWAS_staged                int;
    v_EQTL_staged                int;
    
    type stage_rec  is record
    (bio_assay_analysis_id        number(18,0)
    ,etl_id                        number(18,0)
    ,study_id                    varchar2(500)
    ,data_type                    varchar2(50)
    ,orig_data_type                varchar2(50)
    ,analysis_name                varchar2(1000)
    );

    type stage_table is table of stage_rec; 
    stage_array stage_table;
    
    type stage_table_names_rec is record
    (table_name                    varchar2(500)
    );
    
    type stage_table_names is table of stage_table_names_rec;
    stage_table_array stage_table_names;
    
    no_staged_data    exception;
    
    BEGIN    
    
    --Set Audit Parameters
    newJobFlag := 0; -- False (Default)
    jobID := -1;

    SELECT sys_context('USERENV', 'CURRENT_SCHEMA') INTO databaseName FROM dual;
    procedureName := $$PLSQL_UNIT;

    --Audit JOB Initialization
    --If Job ID does not exist, then this is a single procedure run and we need to create it
    IF(jobID IS NULL or jobID < 1)
    THEN
        newJobFlag := 1; -- True
        cz_start_audit (procedureName, databaseName, jobID);
    END IF;
        
    stepCt := 1;    
    cz_write_audit(jobId,databaseName,procedureName,'Starting i2b2_move_analysis_to_prod',0,stepCt,'Done');
    
    --    load staged analysis to array
    
    select baa.bio_assay_analysis_id
          ,lz.etl_id
          ,lz.study_id
          ,case when lz.data_type = 'Metabolic GWAS' then 'GWAS' else lz.data_type end as data_type
          ,lz.data_type as orig_data_type
          ,lz.analysis_name
    bulk collect into stage_array
    from tm_lz.lz_src_analysis_metadata lz
        ,biomart.bio_assay_analysis baa
    where lz.status = 'STAGED'
      and lz.study_id = baa.etl_id
      and lz.etl_id = baa.etl_id_source
      and case when i_etl_id = -1 then 1
               when lz.etl_id = i_etl_id then 1
               else 0 end = 1;
               
    v_exists := SQL%ROWCOUNT;
    
    if v_exists = 0 then
        raise no_staged_data;
    end if;

    --    set variables if staged data contains GWAS and/or EQTL data
    
    v_GWAS_staged := 0;
    v_EQTL_staged := 0;
    
    for i in stage_array.first .. stage_array.last
    loop    
        if stage_array(i).data_type = 'GWAS' then
            v_GWAS_staged := 1;
        end if;
        
        if stage_array(i).data_type = 'EQTL' then
            v_EQTL_staged := 1;
        end if;    
        
    end loop;
    
    --    drop indexes if loading GWAS data
    
    if v_GWAS_staged = 1 then
        select count(*) into v_exists
        from all_indexes
        where owner = 'BIOMART'
          and table_name = 'BIO_ASSAY_ANALYSIS_GWAS'
          and index_name = 'BIO_ASSAY_ANALYSIS_GWAS_PK';
          
        if v_exists > 0 then
            execute immediate('drop index biomart.bio_assay_analysis_gwas_pk');
        end if;

        select count(*) into v_exists
        from all_indexes
        where owner = 'BIOMART'
          and table_name = 'BIO_ASSAY_ANALYSIS_GWAS'
          and index_name = 'BIO_ASSAY_ANALYSIS_GWAS_IDX2';
          
        if v_exists > 0 then
            execute immediate('drop index biomart.bio_assay_analysis_gwas_idx2');
        end if;        
    end if;

    --    delete any existing data in bio_assay_analysis_gwas and bio_assay_analysis_eqtl
               
    if v_GWAS_staged = 1 then
        delete from biomart.bio_assay_analysis_gwas g
        where g.bio_assay_analysis_id in
             (select x.bio_assay_analysis_id
              from tm_lz.lz_src_analysis_metadata t
                  ,biomart.bio_assay_analysis x
              where t.status = 'STAGED'
                and t.data_type in ('GWAS','Metabolic GWAS')
                and t.study_id = x.etl_id
                and t.etl_id = x.etl_id_source
                and case when i_etl_id = -1 then 1
                         when t.etl_id = i_etl_id then 1
                         else 0 end = 1);
        stepCt := stepCt + 1;
        cz_write_audit(jobId,databaseName,procedureName,'Delete exising data for staged analyses from BIOMART.BIO_ASSAY_ANALYSIS_GWAS',SQL%ROWCOUNT,stepCt,'Done');
        commit;    
    end if;

    if v_EQTL_staged = 1 then
        delete from biomart.bio_assay_analysis_eqtl g
        where g.bio_assay_analysis_id in
             (select x.bio_assay_analysis_id
              from tm_lz.lz_src_analysis_metadata t
                  ,biomart.bio_assay_analysis x
              where t.status = 'STAGED'
                and t.data_type = 'EQTL'
                and t.study_id = x.etl_id
                and t.etl_id = x.etl_id_source
                and case when i_etl_id = -1 then 1
                         when t.etl_id = i_etl_id then 1
                         else 0 end = 1);
        stepCt := stepCt + 1;
        cz_write_audit(jobId,databaseName,procedureName,'Delete exising data for staged analyses from BIOMART.BIO_ASSAY_ANALYSIS_EQTL',SQL%ROWCOUNT,stepCt,'Done');
        commit;    
    end if;
    
    if v_GWAS_staged = 1 then
        select count(*) into v_exists
        from all_indexes
        where owner = 'BIOMART'
          and table_name = 'BIO_ASSAY_ANALYSIS_GWAS'
          and index_name = 'BIO_ASSAY_ANALYSIS_GWAS_IDX1';
          
        if v_exists > 0 then
            execute immediate('drop index biomart.BIO_ASSAY_ANALYSIS_GWAS_IDX1');
        end if;    
    end if;
    
    for i in stage_array.first .. stage_array.last
    loop
        
        cz_write_audit(jobId,databaseName,procedureName,'Loading ' || stage_array(i).study_id || ' ' || stage_array(i).orig_data_type || ' ' ||
                       stage_array(i).analysis_name,0,stepCt,'Done');
                       
        v_etl_id := stage_array(i).etl_id;
        v_bio_assay_analysis_id := stage_array(i).bio_assay_analysis_id;
        v_data_type := stage_array(i).data_type;
        
        if v_data_type = 'EQTL' then
            insert into biomart.bio_assay_analysis_eqtl
            (bio_asy_analysis_eqtl_id
            ,bio_assay_analysis_id
            ,rs_id
            ,gene
            ,p_value
            ,p_value_char
            ,cis_trans
            ,distance_from_gene
            ,etl_id
            ,ext_data
            ,log_p_value)
            select bio_asy_analysis_eqtl_id
                  ,bio_assay_analysis_id
                  ,rs_id
                  ,gene
                  ,to_binary_double(p_value_char)
                  ,p_value_char
                  ,cis_trans
                  ,distance_from_gene
                  ,etl_id
                  ,ext_data
                  ,log(10,to_binary_double(p_value_char))*-1
            from biomart_stage.bio_assay_analysis_eqtl
            where bio_assay_analysis_id = v_bio_assay_analysis_id;
            stepCt := stepCt + 1;
            cz_write_audit(jobId,databaseName,procedureName,'Insert data for analysis from BIOMART_STAGE.BIO_ASSAY_ANALYSIS_' || v_data_type,SQL%ROWCOUNT,stepCt,'Done');
         
            commit;        
        else
            insert into biomart.bio_assay_analysis_gwas
            (bio_asy_analysis_gwas_id
            ,bio_assay_analysis_id
            ,rs_id
            ,p_value
            ,p_value_char
            ,etl_id
            ,ext_data
            ,log_p_value)
            select bio_asy_analysis_gwas_id
                  ,bio_assay_analysis_id
                  ,rs_id
                  ,to_binary_double(p_value_char)
                  ,p_value_char
                  ,etl_id
                  ,ext_data
                  ,log(10,to_binary_double(p_value_char))*-1
            from biomart_stage.bio_assay_analysis_gwas
            where bio_assay_analysis_id = v_bio_assay_analysis_id;
            stepCt := stepCt + 1;
            cz_write_audit(jobId,databaseName,procedureName,'Insert data for analysis from BIOMART_STAGE.BIO_ASSAY_ANALYSIS_' || v_data_type,SQL%ROWCOUNT,stepCt,'Done');
            commit;    
        end if;

        if i_etl_id > -1 then

            v_sqlText := 'delete from biomart_stage.bio_assay_analysis_' || v_data_type || 
                         ' where bio_assay_analysis_id = ' || to_char(v_bio_assay_analysis_id);
            --dbms_output.put_line(v_sqlText);
            execute immediate(v_sqlText);
            stepCt := stepCt + 1;
            cz_write_audit(jobId,databaseName,procedureName,'Delete data for analysis from BIOMART_STAGE.BIO_ASSAY_ANALYSIS_' || v_data_type,SQL%ROWCOUNT,stepCt,'Done');
            commit;    
        end if;    
        
        update tm_lz.lz_src_analysis_metadata
        set status='PRODUCTION'
        where etl_id = v_etl_id;
        stepCt := stepCt + 1;
        cz_write_audit(jobId,databaseName,procedureName,'Set status to PRODUCTION in tm_lz.lz_src_analysis_metadata',SQL%ROWCOUNT,stepCt,'Done');
        commit;                
            
    end loop;
    
    if i_etl_id = -1 then
    
        select table_name
        bulk collect into stage_table_array
        from all_tables
        where owner = 'BIOMART_STAGE'
          and table_name like 'BIO_ASSAY_ANALYSIS%';
          
        for i in stage_table_array.first .. stage_table_array.last
        loop
            v_sqlText := 'truncate table biomart_stage.' || stage_table_array(i).table_name;
            --dbms_output.put_line(v_sqlText);
            execute immediate(v_sqlText);
            stepCt := stepCt + 1;
            cz_write_audit(jobId,databaseName,procedureName,'Truncated biomart_stage.' || stage_table_array(i).table_name,0,stepCt,'Done');
        end loop;
    end if;
    
    --    recreate GWAS indexes if needed
    
    if v_GWAS_staged = 1 then
        execute immediate('create index biomart.bio_assay_analysis_gwas_idx1 on biomart.bio_assay_analysis_gwas (bio_assay_analysis_id) tablespace "INDX" parallel 8');
        stepCt := stepCt + 1;
        cz_write_audit(jobId,databaseName,procedureName,'Created index bio_assay_analysis_gwas_idx1',0,stepCt,'Done');
        execute immediate('create index biomart.bio_assay_analysis_gwas_idx2 on biomart.bio_assay_analysis_gwas (rs_id) tablespace "INDX" parallel 8');
        stepCt := stepCt + 1;
        cz_write_audit(jobId,databaseName,procedureName,'Created index bio_assay_analysis_gwas_idx2',0,stepCt,'Done');
        execute immediate('create unique index biomart.bio_assay_analysis_gwas_pk on biomart.bio_assay_analysis_gwas (bio_asy_analysis_gwas_id) tablespace "INDX" parallel 8 ');
        stepCt := stepCt + 1;
        cz_write_audit(jobId,databaseName,procedureName,'Created index bio_assay_analysis_gwas_pk',0,stepCt,'Done');
        
        
    I2B2_LOAD_EQTL_TOP50();
    stepCt := stepCt + 1;
        cz_write_audit(jobId,databaseName,procedureName,'Created top 50 EQTL',0,stepCt,'Done');
    I2B2_LOAD_GWAS_TOP50();
    stepCt := stepCt + 1;
        cz_write_audit(jobId,databaseName,procedureName,'Created top 50 GWAS',0,stepCt,'Done');

    end if;
    
    --Insert data_count to bio_assay_analysis table. added by Haiyan Zhang 01/22/2013
    for i in stage_array.first .. stage_array.last
    loop
        v_bio_assay_analysis_id := stage_array(i).bio_assay_analysis_id;
        v_data_type := stage_array(i).data_type;
        if v_data_type = 'EQTL' then
          
            update biomart.bio_assay_analysis set data_count=(select count(*) from biomart.bio_assay_analysis_eqtl 
            where bio_assay_analysis_eqtl.bio_assay_analysis_id=v_bio_assay_analysis_id) 
            where bio_assay_analysis.bio_assay_analysis_id=v_bio_assay_analysis_id;
            stepCt := stepCt +1;
            cz_write_audit(jobId,databaseName,procedureName,'Update data_count for analysis ' || v_data_type,SQL%ROWCOUNT,stepCt,'Done');
            commit;
        else
          
            update biomart.bio_assay_analysis set data_count=(select count(*) from biomart.bio_assay_analysis_gwas 
            where bio_assay_analysis_gwas.bio_assay_analysis_id=v_bio_assay_analysis_id) 
            where bio_assay_analysis.bio_assay_analysis_id=v_bio_assay_analysis_id;
            stepCt := stepCt +1;
            cz_write_audit(jobId,databaseName,procedureName,'Update data_count for analysis ' || v_data_type,SQL%ROWCOUNT,stepCt,'Done');
            commit;
        end if;
    end loop; 
    ---end added by Haiyan Zhang
    
    cz_write_audit(jobId,databaseName,procedureName,'End i2b2_move_analysis_to_prod',0,stepCt,'Done');
    stepCt := stepCt + 1;
    
    cz_end_audit(jobId, 'Success');
    
    exception
    when no_staged_data then
        cz_write_audit(jobId, databaseName, procedureName, 'No staged data - run terminating normally',0,stepCt,'Done');
        cz_end_audit(jobId, 'Success');
    when others then
    --Handle errors.
        cz_error_handler (jobID, procedureName);
    --End Proc
        cz_end_audit (jobID, 'FAIL');
    
END;
/
