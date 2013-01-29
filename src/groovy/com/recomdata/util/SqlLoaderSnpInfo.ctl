-- This is used to load the SNP Array annotation data into de_snp_loading_temp
LOAD DATA
INFILE 'Mapping250K_Sty.na31.annot.loading.txt'
APPEND
INTO TABLE deapp.de_snp_loading_temp
FIELDS TERMINATED BY X'09' TRAILING NULLCOLS
(snp_temp_id EXPRESSION "snp_temp_id_seq.nextval",
name,
chrom,
chrom_pos,
probe_name,
entrez_id
)
