CREATE OR REPLACE PROCEDURE TM_CZ.I2B2_REMOVE_ANALYSIS
(
    etlID NUMBER,
    currentJobID NUMBER := null
)
AS
 --Audit variables
  newJobFlag INTEGER(1);
  databaseName VARCHAR(100);
  procedureName VARCHAR(100);
  jobID number(18,0);
  stepCt number(18,0);
  analysis_id number(18,0);
  E_ID number(18,0);

  BEGIN    
    E_ID := etlID;
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
    --get etl_id 
    SELECT bio_assay_analysis_id into analysis_id from BIOMART.BIO_ASSAY_ANALYSIS where ETL_ID_SOURCE = E_ID;
    
    --delete data from bio_assay_analysis_data
    DELETE from biomart.bio_assay_analysis_data where bio_assay_analysis_id=analysis_id;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing data in bio_assay_analysis_data',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    --delete data from bio_assay_analysis_data_tea
    DELETE from biomart.bio_assay_analysis_data_tea where bio_assay_analysis_id=analysis_id;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing data in bio_assay_analysis_data_tea',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    --delete data from bio_asy_analysis_dataset
    DELETE from biomart.bio_asy_analysis_dataset where bio_assay_analysis_id=analysis_id;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing data in bio_assay_analysis_dataset',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    --delete from bio_assay_analysis_EQTL
    DELETE from biomart.bio_assay_analysis_eqtl where bio_assay_analysis_id=analysis_id;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing data in bio_assay_analysis_eqtl',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    --delete from bio_assay_analysis_EXT
    DELETE from biomart.bio_assay_analysis_ext where bio_assay_analysis_id=analysis_id;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing data in bio_assay_analysis_ext',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    --delete from bio_assay_analysis_GWAS
    DELETE from biomart.bio_assay_analysis_gwas where bio_assay_analysis_id=analysis_id;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing data in bio_assay_analysis_gwas',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    --delete from bio_asy_analysis_EQTL_TOP50
    DELETE from biomart.bio_asy_analysis_eqtl_top50 where bio_assay_analysis_id=analysis_id;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing data in bio_asy_analysis_eqtl_top50',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    --delete from bio_asy_analysis_gwas_top50
    DELETE from biomart.bio_asy_analysis_gwas_top50 where bio_assay_analysis_id=analysis_id;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing data in bio_asy_analysis_gwas_top50',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    --delete from bio_data_observation
    DELETE from biomart.bio_data_observation where bio_data_id=analysis_id;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing metadata in bio_data_observation',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    --delete from bio_data_platform
    DELETE from biomart.bio_data_platform where bio_data_id=analysis_id;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing metadata in bio_data_platform',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    --delete from bio_data_disease
    DELETE from biomart.bio_data_disease where bio_data_id=analysis_id;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing metadata in bio_data_disease',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    --delete from bio_assay_analysis
    DELETE from biomart.bio_assay_analysis where bio_assay_analysis_id=analysis_id;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing metadata in bio_assay_analysis',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    --delete from tm_lz.lz_src_analysis_metadata
    delete from lz_src_analysis_metadata where ETL_ID=etlID ;
    stepCt := stepCt + 1;
    cz_write_audit(jobId,databaseName,procedureName,'Delete existing metadata in lz_src_study_metadata',SQL%ROWCOUNT,stepCt,'Done');
    commit;
    
    cz_write_audit(jobId,databaseName,procedureName,'End i2b2_remove_analysis',0,stepCt,'Done');
    stepCt := stepCt + 1;
    
    cz_end_audit(jobId, 'Success');
    EXCEPTION
    WHEN OTHERS THEN
    --Handle errors.
    cz_error_handler (jobID, procedureName);
    --End Proc
    cz_end_audit (jobID, 'FAIL');
    
END;
/