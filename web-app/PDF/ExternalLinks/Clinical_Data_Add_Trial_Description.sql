--	Adds the data displayed when the trial name is right-clicked in Dataset Explorer.  Also adds the tags for the Dataset Explorer Search Terms.
--	source of data is ClinicalTrialMetaData spreadsheet.  Latest version is ClinicalTrialMetaData_09232009.xls

insert into biomart.bio_experiment
(bio_experiment_type,title, description, design, start_date, completion_date, primary_investigator, contact_field, etl_id, status, overall_design,accession)
values
('Clinical Trials'
,'A Phase 1, Multi-Center, Open-Label, Ascending-Dose Study of the Safety of the Human Monoclonal Antibody to Human aV Integrins (CNTO 95) in Combination with Docetaxel in Subjects with Metastatic Hormone Refractory Prostate Cancer'
,'The primary objective is to evaluate the safety and tolerability of CNTO 95 when administered in combination with docetaxel in subjects with metastatic hormone-refractory prostate cancer (HRPC). The secondary objective is to evaluate the efficacy of CNTO 95 administered in combination with docetaxel in the treatment of subjects with metastatic HRPC. The pharmacokinetics and pharmacodynamics of docetaxel alone and in combination with CNTO 95 will also be evaluated as a secondary objective.'
,'Non-randomized; cohort study and dose escalation'
,null
,to_date('20050922','YYYYMMDD')
,'N/A'
,null
,'C1034T06'
,null
,null
,'C1034T06'
)

--	get the bio_experiment_id that was just assigned to trial

select bio_experiment_id from biomart.bio_experiment
where accession = 'C1034T06'

insert into biomart.bio_clinical_trial
(trial_number
,study_owner
,study_phase
,blinding_procedure
,studytype
,duration_of_study_weeks
,number_of_patients
,number_of_sites
,route_of_administration
,dosing_regimen
,group_assignment
,type_of_control
,completion_date
,primary_end_points
,secondary_end_points
,inclusion_criteria
,exclusion_criteria
,subjects
,gender_restriction_mfb
,min_age
,max_age
,secondary_ids
,bio_experiment_id
)
values('C1034T06'
,'Zhihui Lang'
,'N/A'
,'N/A'
,'Safety'
,24
,15
,5
,'Intravenous'
,'Two different dose levels of CNTO 95 (5 mg/kg and 10 mg/kg) will be administered in combination with 75 mg/m2 docetaxel. Cohort 1 will be administered lower dose first, and after 2 cycles and evaluation dose escalation will be done for Cohort 2.'
,'Parallel'
,'N/a'
,to_date('20050922','YYYYMMDD')
,'The primary endpoint is the occurrence of DLTs. A DLT is defined as any Grade 3 or higher AE identified by the SDMC as attributable to CNTO 95 except for hypersensitivity reactions, which will not be considered DLTs unless 2 or more subjects experience a Grade 2 or higher hypersensitivity reactions within a dose cohort.'
,'Incidences of all AEs; grade 3 or higher AEs; SAEs; infusion reactions; clinically important abnormal laboratory parameters. The immune response endpoint is the incidence of antibodies to CNTO 95.'
,'1. Histologically or cytologically confirmed adenocarcinoma of the prostate. 2. Radiological or clinical evidence of metastatic disease. 3. Progressive hormone-refractory disease after orchiectomy or gonadotropin-releasing hormone analog and/or antiandrogen treatment based on one of the following: a. Transaxial imaging tumor progression; b. Rise in two consecutive PSA values obtained at least 14 days apart; c. Radionucleotide bone scan. 4. Subjects must have discontinued flutamide =4 weeks prior to screening, or nilutamide or bicalutamide =6 weeks prior to screening. 5. Life expectancy of =3 months at screening. 6. ECOG score =2. 7. Screening laboratory values for study entry: a. Absolute neutrophil count (ANC) =1.5 x 109/L; b. Hemoglobin =10.0 g/dL (without transfusion); c. Platelets =100 x 109/L; d. Activated partial thromboplastin time (aPTT) and prothrombin time (PT) =1.5 x upper limit of normal (ULN); e. Aspartate aminotransferase (AST) and alanine aminotransferase (ALT) <1.5 x ULN. However:
- AST and ALT may be up to 2.5 x ULN if alkaline phosphatase is <2.5 x ULN.
- Alkaline phosphatase may be up to 5 x ULN if AST, ALT, and bilirubin are within normal limits; f. Total bilirubin =1.5 x ULN; g. Creatinine =1.5 mg/dL; h. Testosterone <50 ng/mL for subjects without surgical castration; i. Serum PSA =5.0 ng/mL. 8. At least 4 weeks since prior radiotherapy or major surgery. Other criteria mentioned; please consult protocol.'
,'1. Prostate cancer that does not express serum PSA or PSA is <5.0 ng/mL at screening. 2. Received any investigational drug/agent within 30 days or 5 half-lives prior to study entry, whichever is longer. 3. Received prior chemotherapy for metastatic disease. 4. Prior malignancy (other than prostate cancer) except adequately treated basal cell or squamous cell carcinoma of the skin or other cancer for which the subject has been disease-free for =5 years. 5. Central nervous system metastases. 6. Received any over-the-counter or herbal treatment for prostate cancer (eg, PC-SPES within 4 weeks prior to screening. 7. Known human immunodeficiency virus (HIV) seropositivity or known hepatitis B or C infection. 8. History of receiving known av integrin modulators. 9. History of prior therapy with av integrin antagonists. 10. Planned major surgery during the study. 11. Received radiotherapy to >25% of the marrow-containing skeleton. Other criteria mentioned; please consult protocol.'
,'Males >=18 years with histologically or cytologically confirmed adenocarcinoma of the prostate, and documented hormone-refractory metastatic disease. Prior chemotherapy for metastatic disease is not allowed.'
,'M'
,18
,null
,'N/A'
,370111494		--	this is the bio_experiment_id from bio_experiment
)

--	the first value is the bio_experiment_id, the second value is always EXP: plus the trial id, the third is BIO_CLINICAL_TRIAL

insert into biomart.bio_data_uid
values(370111494,'EXP:C1034T06','BIO_CLINICAL_TRIAL')

--	if trial has compound

select bio_compound_id from biomart.bio_compound
where generic_name = 'CNTO95'		--	check either generic_name or brand_name

insert into biomart.bio_data_compound
(bio_data_id
,bio_compound_id
)
values(370111494		--	bio_experiment_id
,197423536				--	bio_compound_id
);

--	add search tags

--	find last tag_id

select max(tag_id) from i2b2metadata.i2b2_tags

-	the first value is the tag_id which must be greater than the last tag_id used, the second value is the top-level path to the trial, the third value is the text that will appear in the search box
--	the fourth value is the tag type
--	add up to 3 records: AREA, DISEAST, and COMPOUND.	If you have questions, ask the curator what values should be used as the text for the search

insert into i2b2metadata.i2b2_tags
values(tag_id+1,'\Clinical Trials\C1034T06\','Prostate Cancer','DISEASE')

insert into i2b2metadata.i2b2_tags
values(tag_id+2,'\Clinical Trials\C1034T06\','CNTO95','COMPOUND')

insert into i2b2metadata.i2b2_tags
values(tag_id+3,'\Clinical Trials\C1034T06\','Oncology','AREA')

--	if the trial is to be secured in the application, use the "SecureObject Create" and "SecureObjectPath Create" functions from the Admin tab in the application.
--	Add a dummy user, add user to trial (AuthUserSecureAccess Create) and test that user can access the trial

