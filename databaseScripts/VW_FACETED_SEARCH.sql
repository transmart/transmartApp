--------------------------------------------------------
--  File created - Thursday-October-25-2012   
--------------------------------------------------------
--------------------------------------------------------
--  DDL for View VW_FACETED_SEARCH
--------------------------------------------------------

  CREATE OR REPLACE FORCE VIEW "BIOMART"."VW_FACETED_SEARCH" ("ANALYSIS_ID", "STUDY", "STUDY_ID", "DISEASE", "ANALYSES", "DATA_TYPE", "PLATFORM", "OBSERVATION", "FACET_ID") AS 
  select ba.bio_assay_analysis_id as ANALYSIS_ID
,be.bio_experiment_id as STUDY
,be.bio_experiment_id as STUDY_ID
,bd.disease as DISEASE
,ba.analysis_type as ANALYSES
,ba.bio_assay_data_type as DATA_TYPE 
,bplat.platform_accession as PLATFORM
,bpobs.obs_name as OBSERVATION
,row_number() over (order by ba.bio_assay_analysis_id) as FACET_ID
from bio_assay_analysis ba
Join bio_experiment be 
	 on ba.etl_id = be.accession
Left outer join bio_data_disease bdd
	 on ba.bio_assay_analysis_id = bdd.bio_data_id
left outer join bio_disease bd
	 on bdd.bio_disease_id = bd.bio_disease_id
left outer join bio_data_platform bdplat
	 on ba.bio_assay_analysis_id = bdplat.bio_data_id
left outer join bio_assay_platform bplat
	 on bdplat.bio_assay_platform_id = bplat.bio_assay_platform_id
left outer join bio_data_observation bdpobs
	 on ba.bio_assay_analysis_id = bdpobs.bio_data_id
left outer join bio_observation bpobs
	 on bdpobs.bio_observation_id = bpobs.bio_observation_id
where ba.bio_assay_data_type in ('GWAS','Metabolic GWAS','EQTL');
