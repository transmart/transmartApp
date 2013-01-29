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
  


package com.recomdata.util

import java.io.File;

class SnpViewerConverter {
	
	private String gtcFileName = null;
	private String snpFileName = null;
	private String sampleFileName = null;
	
	def generateSNPFile = {
		File gtcFile = new File(gtcFileName);
		
		File snpFile = new File(snpFileName);
		
		File sampleFile = new File(sampleFileName);
		
		String[] colNames = null;
		int numSet = 0;
		
		gtcFile.eachLine { line ->
		    int pos = line.indexOf("#");
			if (pos < 0) {
				if (line.indexOf("AFFX-SNP") != 0 && 
					line.lastIndexOf("SNP_") > 0) {
					System.out.println("Series Error: input file has concatenated lines!");
					System.out.println(line);
				}
					
			    if (line.indexOf("Probe Set ID") != -1){
					colNames = line.split("\\t");
					numSet = (colNames.length - 1 - 3) / 7;
					String[] setNames = new String[numSet];
					
					snpFile << "SNP\tChromosome\tPhysicalPosition";
					
					for (int i = 0; i < numSet; i++) {
						String setName = colNames[ 1 + i * 7];
						setName = setName.substring(0, setName.indexOf("."));
						setNames[i] = setName;
						
						String setHeader = "\t" + setName + "_Allele_A\t" + setName + "_Allele_B\t" + 
							setName + " Call";
						snpFile << setHeader;
					}
					snpFile << "\n";
					
					// Prepare the sampleInfo.text for CopyNumberDevideByNormals
					sampleFile << ("Array\tSample\tType\tPloidy(numeric)\tGender\tPaired\n");
					for (int j = 0; j < numSet; j = j + 2) {
						String normalName = null, tumorName = null;
						for (int k = 0; k < 2; k++) {
							String setName = setNames[j + k];
							if (setName.endsWith("N"))
								normalName = setName;
							else if (setName.endsWith("T"))
								tumorName = setName;							
						}
						sampleFile << (normalName + "\t" + normalName + "\tcontrol\t2\tF\tYes\n");
						sampleFile << (tumorName + "\t" + tumorName + "\tovarian_tumor\t2\tF\t" + normalName + "\n");
					}
			    }
				else {
					String[] values = line.split("\\t");
					if (values.length == (1 + numSet * 7 + 2 + 1) ) {	
						// Some line has "rs11111	---" at the end. Skip this kind of lines
						if (values[1 + numSet * 7 + 1].equalsIgnoreCase("MT") == false) {
							// Affy 6.0 SNP Array has probes for mitochondria DNA. GenePattern SNPFileSorter cannot handle that. Skip
							String probeId = values[0];
							String chrom = values[1 + numSet * 7 + 1];
							String chromPos = values[1 + numSet * 7 + 2];
							snpFile << (probeId + "\t" + chrom + "\t" + chromPos);
							
							for (int i = 0; i < numSet; i++) {
								String value_a = values[1 + i * 7 + 3];
								String value_b = values[1 + i * 7 + 4];
								String value_call = values[1 + i * 7];
								if (value_call.equalsIgnoreCase("NoCall"))
									value_call = "No";
								
								snpFile << ("\t" + value_a + "\t" + value_b + "\t" + value_call);
							}
							snpFile << "\n";
						}
					}
				}
			}
		}
	}
	
	def checkGTCFile = {
		String gtcFileName = """C:\\Project\\Transmart\\SNPView\\Background\\GSE19539_Result\\GSE19539_genotype_022-201_10.txt""";
		File gtcFile = new File(gtcFileName);

		String gtcFileNewName = """C:\\Project\\Transmart\\SNPView\\Background\\GSE19539_Result\\GSE19539_genotype_022-201_10_new.txt""";
		File gtcFileNew = new File(gtcFileNewName);

		gtcFile.eachLine { line ->
			if (line.indexOf("#") != 0 && line.indexOf("AFFX-SNP") != 0 && line.lastIndexOf("SNP_") > 0) {
				System.out.println("Series Error: input file has concatenated lines!");
				System.out.println(line);
				
				String lineNew = line.replaceAll("SNP_", "\nSNP_");
				lineNew = lineNew.trim();
				gtcFileNew << (lineNew + "\n");
			}
			else {
				gtcFileNew << (line + "\n");
			}
		}
	}
	
	private void loadConfiguration(String file) throws IOException {
		
		Properties prop = new Properties();
		FileInputStream fis = new FileInputStream(file);
		prop.load(fis);
		fis.close();
		
		String sourceDirectory = prop.getProperty("source_directory"); 
		String destinationDirectoy = prop.getProperty("destination_directory");
		String rawSnpFile = prop.getProperty("raw_snp_file");
		String outputSnpFile = prop.getProperty("output_snp_file");
		String sampleFile = prop.getProperty ("sample_file");
		
		gtcFileName = sourceDirectory + "/" + rawSnpFile;
		snpFileName = destinationDirectoy + "/" + outputSnpFile;
		sampleFileName = destinationDirectoy + "/" + sampleFile;
		
	}
	
	public static void main(String[] args) {
		SnpViewerConverter svc = new SnpViewerConverter();
		
		File path = new File(SnpViewerConverter.class.getProtectionDomain().getCodeSource().getLocation().getPath());
		svc.loadConfiguration(path.getParent() + File.separator + "SnpViewer.properties");
		svc.generateSNPFile();
		
	}
	
}
