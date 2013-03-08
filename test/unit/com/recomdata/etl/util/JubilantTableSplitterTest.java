/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

/**
 * $Id: JubilantTableSplitterTest.java 9178 2011-08-24 13:50:06Z mmcduffie $
 */
package com.recomdata.etl.util;

import groovy.util.GroovyTestCase;

/**
 * Unit test for the JubilantTableSplitter class
 * 
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
public class JubilantTableSplitterTest extends GroovyTestCase {
	
	/*
	 * This test was failing on the attempt to create a connection to a non-existent database in the 
	 * call MssqlConnectImpl.createStrangeLoveConnect() within JubilantTableSplitter.normalizeColumnNames, below.
	 * The code called looks like test/development code that is not referenced anywhere else in the project. 
	 * I have commented out the test for the time being, Februaru 14, 2013, Terry Weymouth
	 */

	public final void testNormalizeColumnNames() {
//		StringBuilder expected = new StringBuilder();
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Alpha-1 Antitrypsin', 'Alpha-1Antitrypsin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Alpha-1 Antitrypsin', 'Alpha-1Antitrypsin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Alpha-2 Macroglobulin', 'Alpha-2Macroglobulin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Alpha-2 Macroglobulin', 'Alpha-2Macroglobulin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Annotation Date', 'AnnotationDate', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Annotation Description', 'AnnotationDescription', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Annotation Transcript Cluster', 'AnnotationTranscriptCluster', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Apolipoprotein A1', 'ApolipoproteinA1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Apolipoprotein A1', 'ApolipoproteinA1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Apolipoprotein CIII', 'ApolipoproteinCIII', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Apolipoprotein CIII', 'ApolipoproteinCIII', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Apolipoprotein H', 'ApolipoproteinH', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Apolipoprotein H', 'ApolipoproteinH', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Archival UniGene Cluster', 'ArchivalUniGeneCluster', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03Patients.Asthma hosp or ER last year', 'AsthmahosporERlastyear', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03Patients.Baseline OCS use', 'BaselineOCSuse', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Beta-2 Microglobulin', 'Beta-2Microglobulin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Beta-2 Microglobulin', 'Beta-2Microglobulin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Brain-Derived Neurotrophic Factor', 'Brain-DerivedNeurotrophicFactor', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Brain-Derived Neurotrophic Factor', 'Brain-DerivedNeurotrophicFactor', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03Patients.Bronchodilator Reversibility', 'BronchodilatorReversibility', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.C Reactive Protein', 'CReactiveProtein', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.C Reactive Protein', 'CReactiveProtein', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Cancer Antigen 125', 'CancerAntigen125', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Cancer Antigen 125', 'CancerAntigen125', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Cancer Antigen 19-9', 'CancerAntigen19-9', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Cancer Antigen 19-9', 'CancerAntigen19-9', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Carcinoembryonic Antigen', 'CarcinoembryonicAntigen', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Carcinoembryonic Antigen', 'CarcinoembryonicAntigen', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.CD40 Ligand', 'CD40Ligand', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.CD40 Ligand', 'CD40Ligand', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Chromosomal Location', 'ChromosomalLocation', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'pubmedoraloader.Column 1', 'Column1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'pubmedoraloader.Column 11', 'Column11', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'pubmedoraloader.Column 13', 'Column13', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'pubmedoraloader.Column 15', 'Column15', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'pubmedoraloader.Column 17', 'Column17', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'pubmedoraloader.Column 19', 'Column19', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'pubmedoraloader.Column 21', 'Column21', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'pubmedoraloader.Column 3', 'Column3', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'pubmedoraloader.Column 5', 'Column5', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'pubmedoraloader.Column 7', 'Column7', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'pubmedoraloader.Column 9', 'Column9', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Complement 3', 'Complement3', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Complement 3', 'Complement3', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Creatine Kinase-MB', 'CreatineKinase-MB', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Creatine Kinase-MB', 'CreatineKinase-MB', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03Patients.Diagns Age Category  (>=12,<12)', 'DiagnsAgeCategory(>=12,<12)', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Factor VII', 'FactorVII', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Factor VII', 'FactorVII', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Fatty Acid Binding Protein', 'FattyAcidBindingProtein', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Fatty Acid Binding Protein', 'FattyAcidBindingProtein', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.FGF basic', 'FGFbasic', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.FGF basic', 'FGFbasic', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'clinicaltrials.GenderRestriction MFB', 'GenderRestrictionMFB', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Gene Ontology Biological Process', 'GeneOntologyBiologicalProcess', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Gene Ontology Cellular Component', 'GeneOntologyCellularComponent', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Gene Ontology Molecular Function', 'GeneOntologyMolecularFunction', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Gene Symbol', 'GeneSymbol', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Gene Title', 'GeneTitle', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.GeneChip Array', 'GeneChipArray', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Genome Version', 'GenomeVersion', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Glutathione S-Transferase', 'GlutathioneS-Transferase', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Glutathione S-Transferase', 'GlutathioneS-Transferase', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Growth Hormone', 'GrowthHormone', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Growth Hormone', 'GrowthHormone', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Lipoprotein  a', 'Lipoproteina', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Lipoprotein  a ', 'Lipoproteina', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.MGI Name', 'MGIName', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Prostate Specific Antigen  Free', 'ProstateSpecificAntigenFree', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Prostate Specific Antigen  Free ', 'ProstateSpecificAntigenFree', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Prostatic Acid Phosphatase', 'ProstaticAcidPhosphatase', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Prostatic Acid Phosphatase', 'ProstaticAcidPhosphatase', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.RefSeq Protein ID', 'RefSeqProteinID', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.RefSeq Transcript ID', 'RefSeqTranscriptID', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Representative Public ID', 'RepresentativePublicID', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03Patients.rev>=12 or sinusitis', 'rev>=12orsinusitis', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03Patients.rev>=12 or sinusitis, and late onset', 'rev>=12orsinusitis,andlateonset', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03Patients.Reversibility >=12', 'Reversibility>=12', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.RGD Name', 'RGDName', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Sequence Source', 'SequenceSource', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Sequence Type', 'SequenceType', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Serum Amyloid P', 'SerumAmyloidP', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Serum Amyloid P', 'SerumAmyloidP', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.SGD accession number', 'SGDaccessionnumber', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03Patients.Sinusitis (Y/N)', 'Sinusitis(Y/N)', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Species Scientific Name', 'SpeciesScientificName', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Stem Cell Factor', 'StemCellFactor', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Stem Cell Factor', 'StemCellFactor', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Target Description', 'TargetDescription', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Thyroid Stimulating Hormone', 'ThyroidStimulatingHormone', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Thyroid Stimulating Hormone', 'ThyroidStimulatingHormone', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Thyroxine Binding Globulin', 'ThyroxineBindingGlobulin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Thyroxine Binding Globulin', 'ThyroxineBindingGlobulin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Tissue Factor', 'TissueFactor', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Tissue Factor', 'TissueFactor', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.TNF RII', 'TNFRII', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.TNF RII', 'TNFRII', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Trans Membrane', 'TransMembrane', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Transcript ID(Array Design)', 'TranscriptID(ArrayDesign)', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Unigene Cluster Type', 'UnigeneClusterType', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.von Willebrand Factor', 'vonWillebrandFactor', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.von Willebrand Factor', 'vonWillebrandFactor', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Alpha-1 Antitrypsin', 'Alpha1 Antitrypsin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Alpha-1 Antitrypsin', 'Alpha1 Antitrypsin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Alpha-2 Macroglobulin', 'Alpha2 Macroglobulin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Alpha-2 Macroglobulin', 'Alpha2 Macroglobulin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Alpha-Fetoprotein', 'AlphaFetoprotein', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Alpha-Fetoprotein', 'AlphaFetoprotein', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Beta-2 Microglobulin', 'Beta2 Microglobulin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Beta-2 Microglobulin', 'Beta2 Microglobulin', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Brain-Derived Neurotrophic Factor', 'BrainDerived Neurotrophic Factor', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Brain-Derived Neurotrophic Factor', 'BrainDerived Neurotrophic Factor', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Cancer Antigen 19-9', 'Cancer Antigen 199', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Cancer Antigen 19-9', 'Cancer Antigen 199', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Creatine Kinase-MB', 'Creatine KinaseMB', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Creatine Kinase-MB', 'Creatine KinaseMB', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.ENA-78', 'ENA78', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.ENA-78', 'ENA78', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Endothelin-1', 'Endothelin1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Endothelin-1', 'Endothelin1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.EN-RAGE', 'ENRAGE', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.EN-RAGE', 'ENRAGE', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.G-CSF', 'GCSF', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.G-CSF', 'GCSF', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Glutathione S-Transferase', 'Glutathione STransferase', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Glutathione S-Transferase', 'Glutathione STransferase', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.GM-CSF', 'GMCSF', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.GM-CSF', 'GMCSF', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.ICAM-1', 'ICAM1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.ICAM-1', 'ICAM1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IFN-gamma', 'IFNgamma', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IFN-gamma', 'IFNgamma', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IGF-1', 'IGF1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IGF-1', 'IGF1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-10', 'IL10', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-10', 'IL10', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-12p40', 'IL12p40', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-12p40', 'IL12p40', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-12p70', 'IL12p70', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-12p70', 'IL12p70', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-13', 'IL13', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-13', 'IL13', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-15', 'IL15', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-15', 'IL15', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-16', 'IL16', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-16', 'IL16', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-17', 'IL17', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-17', 'IL17', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-18', 'IL18', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-18', 'IL18', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-1alpha', 'IL1alpha', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-1alpha', 'IL1alpha', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-1beta', 'IL1beta', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-1beta', 'IL1beta', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-1ra', 'IL1ra', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-1ra', 'IL1ra', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-2', 'IL2', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-2', 'IL2', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-23', 'IL23', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-23', 'IL23', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-3', 'IL3', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-3', 'IL3', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-4', 'IL4', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-4', 'IL4', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-5', 'IL5', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-5', 'IL5', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-6', 'IL6', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-6', 'IL6', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-7', 'IL7', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-7', 'IL7', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.IL-8', 'IL8', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.IL-8', 'IL8', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.MCP-1', 'MCP1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.MCP-1', 'MCP1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.MIP-1alpha', 'MIP1alpha', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.MIP-1alpha', 'MIP1alpha', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.MIP-1beta', 'MIP1beta', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.MIP-1beta', 'MIP1beta', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.MMP-2', 'MMP2', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.MMP-2', 'MMP2', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.MMP-3', 'MMP3', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.MMP-3', 'MMP3', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.MMP-9', 'MMP9', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.MMP-9', 'MMP9', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.PAI-1', 'PAI1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.PAI-1', 'PAI1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.PAPP-A', 'PAPPA', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.PAPP-A', 'PAPPA', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.TIMP-1', 'TIMP1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.TIMP-1', 'TIMP1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.TNF-alpha', 'TNFalpha', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.TNF-alpha', 'TNFalpha', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.TNF-beta', 'TNFbeta', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.TNF-beta', 'TNFbeta', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.VCAM-1', 'VCAM1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.VCAM-1', 'VCAM1', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Brain-Derived Neurotrophic Factor', 'Brain-Derived Neurotrophic Factor', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03geneexprexperiments.Prostate Specific Antigen  Free', 'Prostate Specific Antigen  Free', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Brain-Derived Neurotrophic Factor', 'Brain-Derived Neurotrophic Factor', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03RBM.Prostate Specific Antigen  Free ', 'Prostate Specific Antigen  Free ', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'HumanGeneInfo.Symbol_from_nomenclature_authority', 'Symbol_from_nomenclature_authority', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'HumanGeneInfo.Full_name_from_nomenclature_authority', 'Full_name_from_nomenclature_authority', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Gene Ontology Biological Process', 'Gene Ontology Biological Process', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Gene Ontology Cellular Component', 'Gene Ontology Cellular Component', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'AffyHGU133Annotation07072008.Gene Ontology Molecular Function', 'Gene Ontology Molecular Function', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03Patients.Diagns Age Category  (>=12,<12)', 'Diagns Age Category  (>=12,<12)', 'COLUMN';\n");
//		expected.append("EXEC sp_rename 'T03Patients.rev>=12 or sinusitis, and late onset', 'rev>=12 or sinusitis, and late onset', 'COLUMN';\n");
//
//		String actual = JubilantTableSplitter.normalizeColumnNames();
//
//		assertEquals(actual, expected.toString());
	}
}
