-- This is used to load the SNP Array annotation data into de_snp_data_by_probe. SNP information is updated later by stored procedure
LOAD DATA
INFILE 'ADNI_SNP_By_Probe_Loading_Data.txt'
APPEND
INTO TABLE de_snp_data_by_probe
FIELDS TERMINATED BY '\t' TRAILING NULLCOLS
(snp_data_by_probe_id EXPRESSION "snp_data_by_probe_id_seq.nextval",
snp_name,
trial_name,
data_by_probe CHAR(10000000)
)
