-- latest heatmap fixes

-- 1. refresh data for x01
alter table deapp.de_subject_microarray_data truncate partition C0743X01;

insert into deapp.de_subject_microarray_data
  select PROBESET, RAWINTENSITY as RAW_INTENSITY, null as zscore,
    PVALUE, NULL as REFSEQ, GENENAME as GENE_SYMBOL, EXPID as ASSAYID,
    NPTID AS PATIENT_ID, NULL AS SUBJECT_ID, 'C0743X01' AS TRIAL_NAME,
    'Week 0' AS TIMEPOINT, NULL as ZSCORE_ORG,
    NULL as MEAN_VALUE, NULL as STDDEV_VALUE
  from centclinrd.DNACHIP3C0743X01;
  
-- 2. precalculate log intensities:

create table deapp.de_subject_microarray_logs parallel nologging compress
as select PROBESET,RAW_INTENSITY,PVALUE,REFSEQ,GENE_SYMBOL,ASSAY_ID,
  PATIENT_ID,SUBJECT_ID,TRIAL_NAME,TIMEPOINT,
  log(2,raw_intensity) as log_intensity
from deapp.de_subject_microarray_data d;

-- drop table deapp.de_subject_rbm_logs;
create table deapp.de_subject_rbm_logs parallel nologging compress
as select TRIAL_NAME,ANTIGEN_NAME,N_VALUE,PATIENT_ID,GENE_SYMBOL,GENE_ID,
  ASSAY_ID,NORMALIZED_VALUE,CONCEPT_CD,TIMEPOINT,DATA_UID, VALUE,
  log(2,VALUE) as log_intensity
from deapp.de_subject_rbm_data
;

create table deapp.de_subject_protein_logs parallel nologging compress
as select TRIAL_NAME,COMPONENT,INTENSITY,PATIENT_ID,SUBJECT_ID,GENE_SYMBOL,
  GENE_ID,ASSAY_ID,TIMEPOINT,N_VALUE, log(2,intensity) as log_intensity
from deapp.de_subject_protein_data d;

-- 3. calculate mean and stddev per experiment, probe:

create table deapp.de_subject_microarray_calcs parallel nologging compress as
select d.trial_name, d.gene_symbol, d.probeset, 
  avg(log_intensity)    as mean_intensity,
  median(log_intensity) as median_intensity,
  stddev(log_intensity) as stddev_intensity
from deapp.de_subject_microarray_logs d
group by d.trial_name, d.gene_symbol, d.probeset;

-- drop table deapp.de_subject_rbm_calcs;
create table deapp.de_subject_rbm_calcs parallel nologging compress as
select d.trial_name, NVL(d.gene_symbol,'**NULL**') as gene_symbol, d.antigen_name, 
  avg(log_intensity)    as mean_intensity,
  median(log_intensity) as median_intensity,
  stddev(log_intensity) as stddev_intensity
from deapp.de_subject_rbm_logs d
group by d.trial_name, d.antigen_name, NVL(d.gene_symbol,'**NULL**');

create table deapp.de_subject_protein_calcs parallel nologging compress as
select d.trial_name, d.component, d.gene_symbol,
  avg(log_intensity)    as mean_intensity,
  median(log_intensity) as median_intensity,
  stddev(log_intensity) as stddev_intensity
from deapp.de_subject_protein_logs d
group by d.trial_name, d.component, d.gene_symbol;

-- craete new tables, centering around median
-- ORACLE, for some odd reason, think that null values in 
-- the column probest in deapp.de_subject_microarray_calcs
-- are different from null values in deapp.de_subject_microarray_data
-- so this statement is a little weird:

create table deapp.de_subject_microarray_med parallel nologging compress as
select d.probeset, d.raw_intensity, d.log_intensity, d.gene_symbol, d.assay_id,
  d.patient_id, d.subject_id, d.trial_name, d.timepoint, d.pvalue, d.refseq,
  c.mean_intensity, c.stddev_intensity, c.median_intensity,
  CASE WHEN stddev_intensity=0
  THEN 0
  ELSE (log_intensity - median_intensity ) / stddev_intensity 
  END as zscore
from deapp.de_subject_microarray_logs d,
  deapp.de_subject_microarray_calcs c
where d.trial_name=c.trial_name 
  and d.probeset=c.probeset
  and d.gene_symbol=c.gene_symbol
  and d.TRIAL_NAME not like 'BRC%'
union
select d.probeset, d.raw_intensity, d.log_intensity, d.gene_symbol, d.assay_id,
  d.patient_id, d.subject_id, d.trial_name, d.timepoint, d.pvalue, d.refseq,
  c.mean_intensity, c.stddev_intensity, c.median_intensity,
  CASE WHEN stddev_intensity=0
  THEN 0
  ELSE (log_intensity - median_intensity ) / stddev_intensity 
  END as zscore
from deapp.de_subject_microarray_logs d,
  deapp.de_subject_microarray_calcs c
where d.trial_name=c.trial_name 
  -- and d.probeset=c.probeset
  and d.gene_symbol=c.gene_symbol
  and d.TRIAL_NAME like 'BRC%'
;  

-- drop table deapp.de_subject_rbm_med;
create table deapp.de_subject_rbm_med parallel nologging compress as
select 
  d.trial_name, d.antigen_name, d.n_value, d.patient_id, 
  CASE WHEN d.gene_symbol='**NULL**' THEN NULL ELSE d.gene_symbol END as gene_symbol,
  d.gene_id, d.assay_id, d.normalized_value, d.concept_cd, d.timepoint,
  d.data_uid, d.log_intensity, d.value,
  c.mean_intensity, c.stddev_intensity, c.median_intensity,
  CASE WHEN stddev_intensity=0
  THEN 0 
  ELSE (log_intensity - median_intensity ) / stddev_intensity 
  END as zscore
from deapp.de_subject_rbm_logs d inner join deapp.de_subject_rbm_calcs c
on d.trial_name=c.trial_name 
  and d.antigen_name=c.antigen_name 
  and NVL(d.gene_symbol,'**NULL**')=c.gene_symbol;

create table deapp.de_subject_protein_med parallel nologging compress as
select
  d.trial_name, d.component, d.intensity, d.patient_id, d.subject_id,
  d.gene_symbol, d.gene_id, d.assay_id, d.timepoint, d.n_value,
  c.mean_intensity, c.stddev_intensity, c.median_intensity,
  CASE WHEN stddev_intensity=0
  THEN 0
  ELSE (log_intensity - median_intensity ) / stddev_intensity 
  END as zscore
from deapp.de_subject_protein_logs d inner join deapp.de_subject_protein_calcs c
on d.trial_name=c.trial_name 
  and d.component=c.component
  and d.gene_symbol=c.gene_symbol;
  
-- capped with medians
-- drop table deapp.de_subject_microarray_mcapped;
create table deapp.de_subject_microarray_mcapped parallel nologging compress as
select d.probeset, d.raw_intensity, d.log_intensity, d.gene_symbol, d.assay_id,
  d.patient_id, d.subject_id, d.trial_name, d.timepoint, d.pvalue, d.refseq,
  mean_intensity, stddev_intensity, median_intensity,
  CASE
    WHEN zscore < -2.5 THEN -2.5
    WHEN zscore >  2.5 THEN  2.5
    ELSE round(zscore,5)
  END as zscore
from deapp.de_subject_microarray_med d
;  

--drop table deapp.de_subject_rbm_mcapped;
create table deapp.de_subject_rbm_mcapped parallel nologging compress as
select 
  d.trial_name, d.antigen_name, d.n_value, d.patient_id, 
  d.gene_symbol,
  d.gene_id, d.assay_id, d.normalized_value, d.concept_cd, d.timepoint,
  d.data_uid, d.value, d.log_intensity,
  d.mean_intensity, d.stddev_intensity, d.median_intensity,
  CASE
    WHEN zscore < -2.5 THEN -2.5
    WHEN zscore >  2.5 THEN  2.5
    ELSE round(zscore,5)
  END as zscore
from deapp.de_subject_rbm_med d;

-- drop table deapp.de_subject_protein_mcapped;
create table deapp.de_subject_protein_mcapped parallel nologging compress as
select
  d.trial_name, d.component, d.intensity, d.patient_id, d.subject_id,
  d.gene_symbol, d.gene_id, d.assay_id, d.timepoint, d.n_value,
  d.mean_intensity, d.stddev_intensity, d.median_intensity,
   CASE
    WHEN zscore < -2.5 THEN -2.5
    WHEN zscore >  2.5 THEN  2.5
    ELSE round(zscore,5)
  END as zscore 
from deapp.de_subject_protein_med d;

-- add indexes
create bitmap index deapp.de_subject_microarray_mcidx1  
  on deapp.de_subject_microarray_mcapped(trial_name) parallel nologging; 

create bitmap index deapp.de_subject_microarray_mcidx2  
  on deapp.de_subject_microarray_mcapped(probeset) parallel nologging;

create bitmap index deapp.de_subject_microarray_mcidx3  
  on deapp.de_subject_microarray_mcapped(patient_id) parallel nologging;

create bitmap index deapp.de_subject_microarray_mcidx4  
  on deapp.de_subject_microarray_mcapped(gene_symbol) parallel nologging;

create bitmap index deapp.de_subject_rbm_mcidx1  
  on deapp.de_subject_rbm_mcapped(trial_name) parallel nologging; 

create bitmap index deapp.de_subject_rbm_mcidx2  
  on deapp.de_subject_rbm_mcapped(antigen_name) parallel nologging;

create bitmap index deapp.de_subject_rbm_mcidx3  
  on deapp.de_subject_rbm_mcapped(patient_id) parallel nologging;
  
create bitmap index deapp.de_subject_rbm_mcidx4  
  on deapp.de_subject_rbm_mcapped(gene_symbol) parallel nologging;

create bitmap index deapp.de_subject_protein_mcidx1  
  on deapp.de_subject_protein_mcapped(trial_name) parallel nologging; 

create bitmap index deapp.de_subject_protein_mcidx2  
  on deapp.de_subject_protein_mcapped(component) parallel nologging;

create bitmap index deapp.de_subject_protein_mcidx3  
  on deapp.de_subject_protein_mcapped(patient_id) parallel nologging;
  
create bitmap index deapp.de_subject_protein_mcidx4  
  on deapp.de_subject_protein_mcapped(gene_symbol) parallel nologging;

--
--create bitmap index deapp.de_subject_rbm_midx1  
--  on deapp.de_subject_rbm_med(trial_name) parallel nologging; 
--
--create bitmap index deapp.de_subject_rbm_midx2  
--  on deapp.de_subject_rbm_med(antigen_name) parallel nologging;
--
--create bitmap index deapp.de_subject_rbm_midx3  
--  on deapp.de_subject_rbm_med(patient_id) parallel nologging;
--  
--create bitmap index deapp.de_subject_rbm_midx4  
--  on deapp.de_subject_rbm_med(gene_symbol) parallel nologging;

-- change to capped median version

alter table deapp.de_subject_microarray_data
  rename to de_subject_microarray_old;

alter table deapp.de_subject_microarray_mcapped
  rename to de_subject_microarray_data;


alter table deapp.de_subject_protein_data
  rename to de_subject_protein_old;

alter table deapp.de_subject_protein_mcapped
  rename to de_subject_protein_data;


alter table deapp.de_subject_rbm_data
  rename to de_subject_rbm_old;

alter table deapp.de_subject_rbm_mcapped
  rename to de_subject_rbm_data;
  
-- go backwards
--alter table deapp.de_subject_microarray_data
--  rename to de_subject_microarray_mcapped;
--
--alter table deapp.de_subject_microarray_old
--  rename to de_subject_microarray_data;
--
--alter table deapp.de_subject_protein_data
--  rename to de_subject_protein_mcapped;
--
--alter table deapp.de_subject_protein_old
--  rename to de_subject_protein_data;
--  
--alter table deapp.de_subject_rbm_data
--  rename to de_subject_rbm_mcapped;
--
--alter table deapp.de_subject_rbm_old
--  rename to de_subject_rbm_data;