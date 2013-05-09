###########################################################################
 # tranSMART - translational medicine data mart
 # 
 # Copyright 2008-2012 Janssen Research & Development, LLC.
 # 
 # This product includes software developed at Janssen Research & Development, LLC.
 # 
 # This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 # as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 # 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 # 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 # 
 # This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 # 
 # You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 # 
 #
 ##########################################################################


###########################################################################
#PivotClinicalData
#Parse the i2b2 output file and create input files for Cox/Survival Curve.
###########################################################################

PivotClinicalData.pivot <- 
function
(
input.dataFile, snpDataExists, multipleStudies, study
)
{
  print(snpDataExists)
  
	#Read the input file.
	df <- data.frame(read.delim(input.dataFile));
  #We use reshape2 package to do the conversion
  require(reshape2)
  
  finalData <- dcast(df, PATIENT.ID + ASSAY.ID ~ CONCEPT.PATH, value.var = 'VALUE')
  
  unqSubjectData <- unique(subset(finalData, select = -ASSAY.ID))
  tdf <- aggregate(ASSAY.ID ~ PATIENT.ID, data=finalData, FUN=paste, collapse=" | ")
  finalData <- merge(unqSubjectData, tdf, by="PATIENT.ID", all.x=TRUE)
  
  if (snpDataExists) {
    snpPEDFileData <- unique(subset(df[c("PATIENT.ID", "SNP.PED.File")], SNP.PED.File != ""))
    colnames(snpPEDFileData) <- c("PATIENT.ID", "SNP.PED.File")
    finalData <- merge(finalData, snpPEDFileData, by="PATIENT.ID", all.x=TRUE)
  }
  
  filename <- "clinical_i2b2trans.txt"
  if (multipleStudies) filename <- paste(study, "_clinical_i2b2trans.txt")
  write.table(finalData, filename, quote=FALSE, sep = "\t", row.names=FALSE)
  
  file.remove(input.dataFile)
}

