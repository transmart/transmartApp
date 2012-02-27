-- This is used to load the SNP and Probe definition data in a new platform to a temporary table de_snp_probe_new_temp
-- The SNP not yet in de_snp_info is loaded into de_snp_info
LOAD DATA
INFILE 'ADNI_2.txt'
APPEND
INTO TABLE deapp.de_snp_probe_new_temp
FIELDS TERMINATED BY X'09' TRAILING NULLCOLS
(	snp_probe_new_id EXPRESSION "snp_temp_id_seq.nextval",
	name,
	chrom,
	chrom_pos,
	genotype FILLER,
	copynumber FILLER
)
