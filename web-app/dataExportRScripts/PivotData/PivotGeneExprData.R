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


##########################################################################
#PivotGeneExprData
#Parse the gene output file and create pivoted file.
###########################################################################

PivotGeneExprData.pivot <- 
function
(
input.dataFile, multipleStudies, study
)
{
	print("-------------------")
	print("PivotGeneExprData.R")
	print("PIVOTING GENE EXPRESSION DATA")
	
	library(reshape2)

	#We need MASS to dump the final matrix to a file.
	require(MASS)	
	
	#Read the input file.
	dataFile <- data.frame(read.delim(input.dataFile));

	notLog2edDataFile <- subset(dataFile, dataFile$LOG2ED==0)
	log2edDataFile <- subset(dataFile, dataFile$LOG2ED==1)
	
	#Unfortunately the code-snippet within the if condition could not be moved into a function as it was not getting invoked by R from within the application
	if (length(notLog2edDataFile$LOG2ED) > 0) 
	{
	  filename <- "mRNA_trans_zscored.txt"
	  if (multipleStudies) filename <- paste(study, "_mRNA_trans_zscored.txt")
		PivotGeneExprData.pivot.single(notLog2edDataFile, filename)
	}
	
	if (length(log2edDataFile$LOG2ED) > 0) 
	{
	  filename <- "mRNA_trans.txt"
	  if (multipleStudies) filename <- paste(study, "_mRNA_trans.txt")
		PivotGeneExprData.pivot.single(log2edDataFile, filename)
	}

	file.remove(input.dataFile)
	print("-------------------")
}

PivotGeneExprData.pivot.single <- 
function
(
dataToPivot,
fileName
)
{
	print("SUBSESSTING DATA")
	#Remove the PROBESET.ID, LOG2ED and ZSCORE column from the data.
	dataFile <- subset(dataToPivot, select=-c(PROBESET.ID,LOG2ED,GENE_ID,GENE_SYMBOL))
	
	print("MELTING DATA")
	#Melt the data, leaving the 4 id columns.
  #We may not need a melt here, as it is changing the value when we perform a dcast next
  #1329262 obs. of 6 variables of dataFile were changed to 2658524 obs. of 6 variables of meltedData
  #and when we cast it back, somehow the values were being changed
	#meltedData <- melt(dataFile, id=c("PATIENT.ID","ASSAY.ID","PROBE.ID","SAMPLE"),measured=c('VALUE'))
	
	print("CASTING DATA")
	#Cast the data, average any cases where there are multiple values for a given probe.id per patient.
	finalData <- dcast(dataFile, PROBE.ID ~ PATIENT.ID + SAMPLE, value.var="VALUE", median)
	
	print("WRITING DATA")
	#Remove the . from the first column name.
	colnames(finalData)[1] <- "PROBE ID"
	
	#Write the final data file.
	write.matrix(finalData,fileName,sep = "\t")

}




