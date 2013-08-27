CREATE OR REPLACE PROCEDURE TM_CZ.I2B2_LOAD_GWAS_TOP50 AS 
BEGIN

--select * from bio_assay_analysis_gwas 
--where bio_assay_analysis_id = 419842521
--order by p_value asc, rs_id asc;

--select * from tmp_analysis_gwas_top500
--where bio_assay_analysis_id = 419842521
-- order by p_value asc;


execute immediate('drop table BIOMART.tmp_analysis_gwas_top500');

execute immediate('create table BIOMART.tmp_analysis_gwas_top500 
as
select a.* 
from (
select 
bio_asy_analysis_gwas_id,
bio_assay_analysis_id,
rs_id,
p_value,
log_p_value,
etl_id,
ext_data,
p_value_char,
row_number () over (partition by bio_assay_analysis_id order by p_value asc, rs_id asc) as rnum
from BIOMART.bio_assay_analysis_gwas
--where bio_assay_analysis_id = 419842521
) a
where 
a.rnum <=500');

execute immediate('create index t_a_g_t500_idx on BIOMART.TMP_ANALYSIS_GWAS_TOP500(RS_ID) tablespace "INDX"');
execute immediate('create index t_a_ga_t500_idx on BIOMART.TMP_ANALYSIS_GWAS_TOP500(bio_assay_analysis_id) tablespace "INDX"');

execute immediate('drop table BIOMART.bio_asy_analysis_gwas_top50');

execute immediate('create table BIOMART.BIO_ASY_ANALYSIS_GWAS_TOP50
as 
SELECT baa.bio_assay_analysis_id,
baa.analysis_name AS analysis, info.chrom AS chrom, info.pos AS pos,
gmap.gene_name AS rsgene, DATA.rs_id AS rsid,
DATA.p_value AS pvalue, DATA.log_p_value AS logpvalue,
DATA.ext_data AS extdata , DATA.rnum
FROM biomart.tmp_analysis_gwas_top500 DATA 
JOIN biomart.bio_assay_analysis baa 
ON baa.bio_assay_analysis_id = DATA.bio_assay_analysis_id
JOIN deapp.de_rc_snp_info info ON DATA.rs_id = info.rs_id and (hg_version='''||19||''')
LEFT JOIN deapp.de_snp_gene_map gmap ON  gmap.snp_name =info.rs_id') ;

--select count(*) from BIO_ASY_ANALYSIS_GWAS_TOP50;

execute immediate('create index BIOMART.B_ASY_GWAS_T50_IDX1 on BIOMART.BIO_ASY_ANALYSIS_GWAS_TOP50(bio_assay_analysis_id) parallel tablespace "INDX"');

execute immediate('create index BIOMART.B_ASY_GWAS_T50_IDX2 on BIOMART.BIO_ASY_ANALYSIS_GWAS_TOP50(ANALYSIS) parallel tablespace "INDX"');

END I2B2_LOAD_GWAS_TOP50;
/
