CREATE OR REPLACE PROCEDURE TM_CZ.I2B2_LOAD_EQTL_TOP50 AS 
BEGIN

execute immediate ('drop table biomart.tmp_analysis_count_eqtl');

execute immediate ('create table biomart.tmp_analysis_count_eqtl as
select count(*) as total, bio_assay_analysis_id
from biomart.bio_assay_analysis_eqtl
group by bio_assay_analysis_id');


execute immediate ('update biomart.bio_assay_analysis b
set b.data_count = (select a.total from biomart.tmp_analysis_count_eqtl  a where a.bio_assay_analysis_id =  b.bio_assay_analysis_id)
where exists(
select 1 from biomart.tmp_analysis_count_eqtl  a where a.bio_assay_analysis_id =  b.bio_assay_analysis_id
)');

--select * from bio_assay_analysis_gwas 
--where bio_assay_analysis_id = 419842521
--order by p_value asc, rs_id asc;

--select * from tmp_analysis_gwas_top500
--where bio_assay_analysis_id = 419842521
--order by p_value asc;


execute immediate ('drop table biomart.tmp_analysis_eqtl_top500');

execute immediate ('create table biomart.tmp_analysis_eqtl_top500 
as
select a.* 
from (
select 
bio_asy_analysis_eqtl_id,
bio_assay_analysis_id,
rs_id,
p_value,
log_p_value,
etl_id,
ext_data,
p_value_char,
gene,
cis_trans,
distance_from_gene,
row_number () over (partition by bio_assay_analysis_id order by p_value asc, rs_id asc) as rnum
from biomart.bio_assay_analysis_eqtl
) a
where 
a.rnum <=500');

execute immediate ('create index BIOMART.t_a_ge_t500_idx on BIOMART.TMP_ANALYSIS_eqtl_TOP500(RS_ID) tablespace "INDX"');
execute immediate ('create index BIOMART.t_a_gae_t500_idx on BIOMART.TMP_ANALYSIS_eqtl_TOP500(bio_assay_analysis_id) tablespace "INDX"');

execute immediate ('drop table biomart.bio_asy_analysis_eqtl_top50 cascade constraints');

execute immediate ('create table biomart.BIO_ASY_ANALYSIS_eqtl_TOP50
as 
SELECT baa.bio_assay_analysis_id,
baa.analysis_name AS analysis, info.chrom AS chrom, info.pos AS pos,
gmap.gene_name AS rsgene, DATA.rs_id AS rsid,
DATA.p_value AS pvalue, DATA.log_p_value AS logpvalue, data.gene as gene,
DATA.ext_data AS extdata , DATA.rnum
FROM biomart.tmp_analysis_eqtl_top500 DATA 
JOIN biomart.bio_assay_analysis baa 
ON baa.bio_assay_analysis_id = DATA.bio_assay_analysis_id
JOIN deapp.de_rc_snp_info info ON DATA.rs_id = info.rs_id and (hg_version='''||19||''')
LEFT JOIN deapp.de_snp_gene_map gmap ON  gmap.snp_name =info.rs_id');

--execute immediate ('select count(*) from BIO_ASY_ANALYSIS_eqtl_TOP50');

execute immediate ('create index BIOMART.B_ASY_eqtl_T50_IDX1 on BIOMART.BIO_ASY_ANALYSIS_eqtl_TOP50(bio_assay_analysis_id) parallel tablespace "INDX"');

execute immediate ('create index BIOMART.B_ASY_eqtl_T50_IDX2 on BIOMART.BIO_ASY_ANALYSIS_eqtl_TOP50(ANALYSIS) parallel tablespace "INDX"');

END I2B2_LOAD_EQTL_TOP50;
/
