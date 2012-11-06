package de
class DeSNPInfo {
		Long id
		String aminoAcidChange
		String codonChange
		String rsId
		String ref
		String alt
		String geneName
		String geneId
		String variationClass
		String strand
		String clinsig
		String disease
		Long maf
		String geneBiotype
		String impact
		String transcriptId
		String functionalClass
		String effect
		String exonId
 static mapping = {
	 table 'DE_RC_SNP_INFO'
	 version false
	 id column:'SNP_INFO_ID'
	 id generator:'sequence', params:[sequence:'SEQ_DE_DATA_ID']
	 columns {
		aminoAcidChange column:'AMINO_ACID_CHANGE'
		codonChange column:'CODON_CHANGE'
		rsId column:'RS_ID'
		ref column:'REF'
		alt column:'ALT'
		geneName column:'GENE_NAME'
		geneId column:'GENE_ID'
		variationClass column:'VARIATION_CLASS'
		strand column:'STRAND'
		clinsig column:'CLINSIG'
		disease column:'DISEASE'
		maf column:'MAF'
		geneBiotype column:'GENE_BIOTYPE'
		impact column:'IMPACT'
		transcriptId column:'TRANSCRIPT_ID'
		functionalClass column:'FUNCTIONAL_CLASS'
		effect column:'EFFECT'
		exonId column:'EXON_ID'
		}
	}
 static constraints = {
	
	}
}