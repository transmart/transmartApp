--	make sure there's a mapping in the subject_sample data, if the count does not equal the number of records in the data file, 
--	use the subsequent sql to identify the expr_id/samples that are not mapped

select count(*)
from biomart_lz.BHTCHRJ_mrna_data_extrnl sj
,biomart_lz.BHTCHRJ_mrna_samp_extrnl se
where sj.expr_id = se.expr_id

--	find expr_id's with no subject mapping, if any exist, check the data and discuss with curator

select distinct d.expr_id from biomart_lz.BHTCHRJ_mrna_data_extrnl d
where not exists
     (select 1 from biomart_lz.BHTCHRJ_mrna_samp_extrnl s
      where d.expr_id = s.expr_id)

--	check if any raw_intensity values are not numeric.  If there are, remove them from the data file

select distinct raw_intensity from biomart_lz.BHTCHRJ_mrna_data_extrnl
order by raw_intensity desc

--	check if any raw_intensity is 0 or negative, these records will not be loaded.  Just let the curator know the count

select distinct to_number(d.raw_intensity) from biomart_lz.BHTCHRJ_mrna_data_extrnl d
where to_number(d.raw_intensity) <= 0

--	map microarray patients to patient_dimension - run select to check out data and make the same changes in the insert statement below

select sj.probeset
,sj.expr_id
,to_number(sj.raw_intensity) as raw_intensity
,to_number(se.assay_id) as assay_id  --  for public studies, take the numeric part of the sample id (GSMXXXXXX)
,pd.patient_num as patient_id
,pd.sourcesystem_cd as subject_id
,'BHTCHRJ' as trial_name
,null 								-- this may come from the subject-sample mapping file as timepoint
,'Lung Tissue'    					-- this may be a fixed string or come from the subject-sample mapping file as sample_type
,'GPL91'  							-- this may come from the subject-sample mapping file as platform
from biomart_lz.BHTCHRJ_mrna_data_extrnl sj
,biomart_lz.BHTCHRJ_mrna_samp_extrnl se
,i2b2_lz.patient_info pi
,i2b2demodata.patient_dimension pd
where sj.expr_id = se.expr_id
and se.subject_id = pi.subject_id
and pi.study_id = 'BHTCHRJ'
and sj.raw_intensity is not null
and sj.raw_intensity != 'NA'
and pi.usubjid = pd.sourcesystem_cd
;

execute immediate ('truncate table deapp_wz.stg_subject_mrna_data')
;

--	load data into stg_subject_mrna_data.  make the same changes here that were made in the select above.  

insert into deapp_wz.stg_subject_mrna_data
(probeset
,expr_id
,raw_intensity
,assay_id
,patient_id
,subject_id
,trial_name
,timepoint
,sample_type
,platform
)
select sj.probeset
,sj.expr_id
,to_number(sj.raw_intensity) as raw_intensity
,to_number(se.assay_id) as assay_id
,pd.patient_num as patient_id
,pd.sourcesystem_cd as subject_id
,'BHTCHRJ' as trial_name
,null
,'Lung Tissue'
,'GPL91'
from biomart_lz.BHTCHRJ_mrna_data_extrnl sj
,biomart_lz.BHTCHRJ_mrna_samp_extrnl se
,i2b2_lz.patient_info pi
,i2b2demodata.patient_dimension pd
where sj.expr_id = se.expr_id
and se.subject_id = pi.subject_id
and pi.study_id = 'BHTCHRJ'
and sj.raw_intensity is not null
and sj.raw_intensity != 'NA'
and pi.usubjid = pd.sourcesystem_cd
;
commit;

--	load mRNA data, set last parameter to R if gene expression data values are raw values and to T if the values are
--	already transformed.  The curator should let you know which parameter to use.  The default if none specified is R.

declare
TrialID		varchar2(100);

begin
TrialID :=	'BHTCHRJ';

i2b2_process_mrna_data(TrialID,null,'R');

end;
