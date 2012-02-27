#########################################################################
# tranSMART Ð translational medicine data mart
# 
# Copyright 2008-2012 Janssen Research & Development, LLC.
# 
# This product includes software developed at Janssen Research & Development, LLC.
# 
# This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
# as published by the Free Software  # Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
# 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
# 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
# 
# This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    # FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
# 
# You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
# 
#
###########################################################################
###########################################################################
#BuildCorrelationDataFile
#Parse the i2b2 output file and create input files for a correlation analysis.
###########################################################################

CorrelationData.build <- 
function
(
input.dataFile,
output.dataFile="outputfile.txt",
concept.variables="",
correlation.by = ""
)
{
	#Read the input file.
	dataFile <- data.frame(read.delim(input.dataFile));
	
	#Set the column names.
	colnames(dataFile) <- c("PATIENT_NUM","SUBSET","CONCEPT_CODE","CONCEPT_PATH_SHORT","VALUE","CONCEPT_PATH")
	
	#Split the data by the CONCEPT_PATH.
	splitData <- split(dataFile,dataFile$CONCEPT_PATH);	
	
	#Create a matrix with unique patient_nums.
	finalData <- matrix(unique(dataFile$PATIENT_NUM));	
	
	#Name the column.
	colnames(finalData) <- c("PATIENT_NUM")	
	
	#We assume we get a list of "|" separated concepts that represent the variables.
	splitConcept <- strsplit(concept.variables,"\\|");
	splitConcept <- unlist(splitConcept);

	#For each of the passed in concepts, append the rows onto the end of our temp matrix.
	for(entry in splitConcept)
	{
		#This will be a temp matrix with PATIENT_NUM,VALUE.
		tempConceptMatrix <- matrix(ncol=2,nrow=0);	
	
		tempConceptMatrix <- rbind(tempConceptMatrix,splitData[[entry]][c('PATIENT_NUM','VALUE')])		
		
		#Make the column name pretty.
		entry <- sub(pattern="^\\\\(.*?\\\\){3}",replacement="",x=entry,perl=TRUE)
		entry <- gsub("^\\s+|\\s+$", "",entry)
		entry <- gsub("^\\\\|\\\\$", "",entry)
		entry <- gsub("\\\\", "-",entry)
		
		#Add column names to our temp matrix.
		colnames(tempConceptMatrix) <- c('PATIENT_NUM',entry)
		
		#Merge the new category column into our final data matrix.
		finalData<-merge(finalData,tempConceptMatrix[c('PATIENT_NUM',entry)],by="PATIENT_NUM")		
	}			
	
	#If this is by variable, we remove the patient_num column.
	if(correlation.by == "variable")
	{
		finalData$PATIENT_NUM <- NULL
	}
	
	#We need MASS to dump the matrix to a file.
	require(MASS)
	
	#Write the final data file.
	write.matrix(finalData,"outputfile.txt",sep = "\t")
}
