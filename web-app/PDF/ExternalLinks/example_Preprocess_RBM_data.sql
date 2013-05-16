truncate table deapp_wz.stg_subject_rbm_data;

---1. Load pivoted/raw data into the staging Subject RBM Data
INSERT
INTO deapp_wz.STG_SUBJECT_RBM_DATA
  (
    TRIAL_NAME,
    ANTIGEN_NAME,
    VALUE_TEXT,
    TIMEPOINT,
    ASSAY_ID,
    SAMPLE_ID,
    SUBJECT_ID,
	site_id
  )  
  SELECT 
  'C0524T06',
  ANTIGEN,
  VALUE_TEXT,
  --Take the text in visit_name before "BIOMARKER SUBSET"
  trim(substr(visit_name,1,instr(visit_name,'BIOMARKER SUBSET')-1)),
  assay_id,
  SAMPLE_ID,
  TO_NUMBER(SUBJECT),
  site
FROM BIOMART_LZ.C0524T06_RBM;
COMMIT;

--	visit_name formatted like WK 0 BIOMARKER SUBSET

--	check timepoints (visit_name) are in node_curation

select distinct timepoint from deapp_wz.stg_subject_rbm_data
order by timepoint

insert into control.node_curation
(node_type
,node_name
,display_name
,display_in_ui
,data_type
,global_flag
,study_id
,curator_name
,curation_date
,active_flag
)
values ('VISIT_NAME'
,'WK 4'
,'Week 004'
,'Y'
,'T'
,'N'
,'C0524T06'
,'JEA'
,sysdate
,'Y'
)

--	check antigen_names are valid

select distinct t.antigen_name
from deapp_wz.stg_subject_rbm_data
where not exists
      (select 1 from deapp.stg_rbm_antigen_gene g
	   where t.antigen_name = g.antigen_name);
	   
--	if any antigen_names are not in the deapp.stg_rbm_antigen_gene table, check the list of valid names to see if there's a close match
select * from deapp.stg_rbm_antigen_gene
order by antigen_name;

--	if there's a close match, check the existing ANTIGEN values in node_curation
select * from control.node_curation
where node_type = 'ANTIGEN'
order by display_name

--	If there are no records with the antigen_name from deapp_wz.stg_subject_rbm_data, add the antigen_name to the node_curation table (in CAPS)
--	if you can't decipher the antigen name, ask the curator

insert into control.node_curation
(node_type
,node_name
,display_name
,display_in_ui
,data_type
,global_flag
,study_id
,curator_name
,curation_date
,active_flag
)
values ('ANTIGEN'
,'GROWTH_HORMONE'
,'Growth Hormone'
,'Y'
,null
,'Y'
,null
,'JAvitabile'
,sysdate
,'Y'
)

--	Check that the subject can be mapped to the patient_dimension

select t.* from deapp_wz.stg_subject_rbm_data t
where not exists
     (select 1 from patient_dimension pd
                   ,i2b2_lz.patient_info pi
      where p1.subject_id = t.subject_id
		and nvl(pi.site_id,'**NULL**') = nvl(t.site_id,'**NULL**')
		and pd.sourcesystem_cd = pi.usubjid
        and pi.study_id = 'C0524T06'
	  );
--	if there are records that can't be mapped, let the curator know the site/subject id's and ask the curator how to proceed
			
--	load the rbm data

declare
TrialID varchar2(100);
begin
TrialID := 'C0524T06';

i2b2_load_rbm_data(TrialID); 	

end;

--	After the load has completed, check the audit log and verify that the number of records in the insert to deapp_wz.stg_subject_rbm_data_raw step is the same as
--	the insert to deapp_wz.de_subject_rbm_data step

I2B2_LOAD_RBM_DATA	Insert data for trial into DEAPP_WZ stg_subject_rbm_data_raw	Done	14904	
I2B2_LOAD_RBM_DATA	Inserted trial into DEAPP_WZ de_subject_rbm_data				Done	14888	

--	if not, determine which records were not added.  
--	check the counts in the audit log for

I2B2_LOAD_RBM_DATA	Delete records with null antigen_name and value_text in DEAPP_WZ stg_subject_rbm_data
I2B2_LOAD_RBM_DATA	Delete records with N/A or Not Requested value_text in DEAPP_WZ stg_subject_rbm_data
I2B2_LOAD_RBM_DATA	Delete antigens with > 50% LOW values in DEAPP_WZ stg_subject_rbm_data
	
--	if these counts don't account for the difference, it may be an antigen_name issue, patient mapping issue, non-numeric data in VALUE_TEXT, multiple records with the same
--	subject, site and antigen or something else.  The sql to check for valid antigen_names and how to correct is above in the script.  The sql to check for patient
--	mapping and what to do is above in the script.  Multiple records for the same subect, site and antigen are ok, the values get averaged into one record.

--	what didn't get loaded

select t.* from deapp_wz.stg_subject_rbm_data t
where not exists
     (select 1 from deapp_wz.de_subject_rbm_data r
                   ,patient_dimension pd
                   ,i2b2_lz.patient_info pi
      where r.trial_name = 'NORMALS'
        and r.antigen_name = t.antigen_name
        and r.patient_id = pd.patient_num
        and pd.sourcesystem_cd = pi.usubjid
		and p1.subject_id = t.subject_id
		and nvl(pi.site_id,'**NULL**') = nvl(t.site_id,'**NULL**')
        and pi.study_id = r.trial_name);
		
--	multiple records for subject, site, antigen

select t.subject_id
	  ,t.site_id
	  ,t.antigen_name
from deapp_wz.stg_subject_rbm_data_raw t
group by t.subject_id
        ,t.site_id
		,t.antigen_name
having count(*) > 1;



     


