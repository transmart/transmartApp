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
	library(reshape2)

	#Read the input file.
	dataFile <- data.frame(read.delim(input.dataFile))

	#Fix the patient ID column.
	dataFile$PATIENT.ID <- gsub("^\\s+|\\s+$", "",dataFile$PATIENT.ID)
	
	#Pull only the columns we are interested in.
	reducedData <- dataFile[c("PATIENT.ID","SUBSET","CONCEPT.PATH","VALUE")]
	
	#Melt the data.
	meltedData <- melt(reducedData, id=c("PATIENT.ID","SUBSET","CONCEPT.PATH"))

	#Get a list of the unique concepts, these become column names and we need to get the non-safe name.
	conceptNames <- data.frame(unique(reducedData$CONCEPT.PATH))
	
	colnames(conceptNames) <- c('unsafename')
	
	#Create another column in the above frame that will be the distorted columns after safe.name is applied.
	conceptNames$safename <- make.names(conceptNames$unsafename)
	
	#Cast the data back into shape.
	finalData <- data.frame(dcast(meltedData, PATIENT.ID + SUBSET ~ CONCEPT.PATH, paste, collapse="; "))
	
	#Replace the safe column names with the unsafe ones.
	#This is the function that does the swap for us.
	swapColumnNames <- function
       (
         currentColumnName,
         finalData,
         conceptNames
         )
      {
         if(length(as.character(conceptNames$unsafename[which(conceptNames$safename == currentColumnName)]) > 0))
         {
           colnames(finalData)[which(colnames(finalData) == currentColumnName)] <- as.character(conceptNames$unsafename[which(conceptNames$safename == currentColumnName)])
         }
         else
         {
           colnames(finalData)[which(colnames(finalData) == currentColumnName)] <- currentColumnName
         }
       }	
	
	colnames(finalData) <- lapply(colnames(finalData),swapColumnNames,finalData,conceptNames)	
	
	#If SNP data exists create a different data table with the info.
	if (snpDataExists)
	{
		snpPEDFileData <- unique(subset(dataFile[c("PATIENT.ID", "SNP.PED.File")], SNP.PED.File != ""))
		colnames(snpPEDFileData) <- c("PATIENT.ID", "SNP.PED.File")
	}
	
	#Merge the SNP Data in if it exists.
	if (snpDataExists) 
	{
		finalData <- merge(finalData, snpPEDFileData, by="PATIENT.ID", all.x=TRUE)
		colnames(finalData) <- c("PATIENT ID",colnames(finalData), "SNP PED File")
	}

	#We need MASS to dump the matrix to a file.
	require(MASS)
	filename <- "clinical_i2b2trans.txt"
	if (multipleStudies) filename <- paste(study, "_clinical_i2b2trans.txt")
	#Write the final data file.
	write.matrix(finalData,filename,sep = "\t")

	file.remove(input.dataFile)
}
