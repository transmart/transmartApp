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
#BuildScatterDataFile
#Parse the i2b2 output file and create input files for a scatter plot.
###########################################################################

ScatterData.build <- 
function
(input.dataFile,
output.dataFile="outputfile.txt",
concept.dependent,
concept.independent
)
{
	#Read the input file.
	dataFile <- data.frame(read.delim(input.dataFile));
	
	#Set the column names.
	colnames(dataFile) <- c("PATIENT_NUM","SUBSET","CONCEPT_CODE","CONCEPT_PATH_SHORT","VALUE","CONCEPT_PATH")
	
	#Split the data by the CONCEPT_CD.
	splitData <- split(dataFile,dataFile$CONCEPT_PATH);
	
	#Create a matrix with unique patient_nums.
	finalData <- matrix(unique(dataFile$PATIENT_NUM));
	
	#Name the column.
	colnames(finalData) <- c("PATIENT_NUM")
	
	##########################################
	#Get a table of Y value/Patient
	splitConcept <- strsplit(concept.dependent,"\\|");
	splitConcept <- unlist(splitConcept);

	#This will be a temp matrix with VALUE.
	tempConceptMatrix <- matrix(ncol=1,nrow=0);

	#For each of the passed in concepts, append the rows onto the end of our temp matrix.
	for(entry in splitConcept)
	{
	  tempConceptMatrix <- rbind(tempConceptMatrix,splitData[[entry]][c('PATIENT_NUM','VALUE')])		
	}

	#Add column names to our temp matrix.
	colnames(tempConceptMatrix) <- c('PATIENT_NUM','VALUE')

	yValueMatrix <- tempConceptMatrix
	##########################################	
	
	##########################################
	#Get a table of X value/Patient
	splitConcept <- strsplit(concept.independent,"\\|");
	splitConcept <- unlist(splitConcept);

	#This will be a temp matrix with VALUE.
	tempConceptMatrix <- matrix(ncol=1,nrow=0);

	#For each of the passed in concepts, append the rows onto the end of our temp matrix.
	for(entry in splitConcept)
	{
	  tempConceptMatrix <- rbind(tempConceptMatrix,splitData[[entry]][c('PATIENT_NUM','VALUE')])		
	}

	#Add column names to our temp matrix.
	colnames(tempConceptMatrix) <- c('PATIENT_NUM','VALUE')

	xValueMatrix <- tempConceptMatrix
	##########################################	
	
	
	##########################################
	#Merge our X and patient nums.
	finalData<-merge(finalData,xValueMatrix[c('PATIENT_NUM','VALUE')],by="PATIENT_NUM")
	#Merge our Y and final data.
	finalData<-merge(finalData,yValueMatrix[c('PATIENT_NUM','VALUE')],by="PATIENT_NUM")
	
	#Create column names.
	colnames(finalData) <- c('PATIENT_NUM','X','Y')

	#We need MASS to dump the matrix to a file.
	require(MASS)

	#Write the final data file.
	write.matrix(finalData,output.dataFile,sep = "\t")
	##########################################
}
