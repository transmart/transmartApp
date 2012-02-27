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
#BuildSurvivalDataFile
#Parse the i2b2 output file and create input files for Cox/Survival Curve.
###########################################################################

SurvivalData.build <- 
function
(
input.dataFile,
output.dataFile="input",
concept.time,
concept.category = "",
concept.eventYes = "",
binning.enabled = FALSE,
binning.bins = "",
binning.type = "",
binning.manual = FALSE,
binning.binrangestring = "",
binning.variabletype = ""
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
	
	#Add the value for the time to the final data.
	finalData<-merge(finalData,splitData[[concept.time]][c('PATIENT_NUM','VALUE')],by="PATIENT_NUM")
	
	#If no event was selected, we consider everyone to have had the event.
	if(concept.eventYes=="")
	{
		finalData<-cbind(finalData,1)
	}
	else
	{
		#We merge the Yes events in, everything else gets set to NA. We will mark them as censored later.
		finalData<-merge(finalData,splitData[[concept.eventYes]][c('PATIENT_NUM','VALUE')],by="PATIENT_NUM",all.x=TRUE)	
	}
	
	#If no group was selected, everyone is put in the same category.
	if(concept.category=="")
	{
		finalData<-cbind(finalData,"STUDY")
	}
	else
	{
		#We assume we might get a list of concepts, split them out here.
		splitConcept <- strsplit(concept.category,"\\|");
		splitConcept <- unlist(splitConcept);

		#This will be a temp matrix with PATIENT_NUM,VALUE.
		tempConceptMatrix <- matrix(ncol=2,nrow=0);
		
		#For each of the passed in concepts, append the rows onto the end of our temp matrix.
		for(entry in splitConcept)
		{
			tempConceptMatrix <- rbind(tempConceptMatrix,splitData[[entry]][c('PATIENT_NUM','VALUE')])		
		}

		#Add column names to our temp matrix.
		colnames(tempConceptMatrix) <- c('PATIENT_NUM','VALUE')
		
		#Merge the new category column into our final data matrix.
		finalData<-merge(finalData,tempConceptMatrix[c('PATIENT_NUM','VALUE')],by="PATIENT_NUM")		
	}
		
	#Rename the columns.
	colnames(finalData) <- c("PATIENT_NUM","TIME","CENSOR","CATEGORY")
	
	#Make sure we have the value levels for the CENSOR column. This may throw a warning for duplicate values, but we can ignore it.
	finalData$CENSOR <- factor(finalData$CENSOR, levels = c(levels(finalData$CENSOR), "1"))
	finalData$CENSOR <- factor(finalData$CENSOR, levels = c(levels(finalData$CENSOR), "0"))

	#Replace the NA values in the CENSOR column with 0 (Censored).
	finalData$'CENSOR'[is.na(finalData$'CENSOR')] <- 0
	
	#Everything that isn't a 0 in the CENSOR column needs to be a 1 (Event happened).
	finalData$'CENSOR'[!finalData$'CENSOR'=='0'] <- 1		
	
	#Binning Code.
	if(binning.enabled == TRUE)
	{
		#Add an empty bins column.
		finalData$bins <- NA
		
		numberOfBins <- binning.bins
		
		if(!is.numeric(numberOfBins))
		{
			numberOfBins <- as.numeric(numberOfBins)
		}
		
		binningType <- binning.type	

		#ESB
		if(binningType == "ESB"  && binning.manual == FALSE)
		{
			finalData$CATEGORY = as.numeric(levels(finalData$CATEGORY))[as.integer(finalData$CATEGORY)]

			#This is our high value.
			highValue <- max(finalData$CATEGORY)

			#This is our min value.
			lowValue <- min(finalData$CATEGORY)

			#This is the step value for our bins.
			stepvalue <- ceiling((highValue - lowValue) / numberOfBins)

			#Add all items to the top bin.
			finalData$bins = numberOfBins

			#This is the first lower bound.
			binEnd <- highValue - stepvalue

			#Loop over all bins but the last one.
			for(i in seq((numberOfBins-1),1,-1))
			{ 
		  
			  #We need all the values which are less than the end to be in the next lowest bin.
			  finalData$bins[finalData$CATEGORY <= binEnd] = i
			  
			  #This is the ending row number.
			  binEnd <- binEnd - stepvalue  
			}
		}

		#EDP
		if(binningType == "EDP"  && binning.manual == FALSE)
		{
			#Add an empty bins column.
			finalData$bins <- NA

			#If we are EDP'ing we need to find out how to evenly split the population.
			totalPopulation <- length(finalData$PATIENT_NUM)

			#Find the bin size (Rounded up, we will be heavy in the lesser bins.)
			binSize <- ceiling(totalPopulation / numberOfBins)

			#Sort on the category so our bins align properly.
			finalData <- finalData[with(finalData,order(CATEGORY)), ]	
			
			#This is the starting row number.
			binStart <- 1
			binEnd <- 1

			#Loop over all bins but the last one.
			for(i in 1:(numberOfBins-1))
			{
				if(i>1) binStart <- binStart + binSize

				#This is the ending row number.
				binEnd <- binStart + binSize - 1

				#We need to loop and assign binning groups.
				finalData$bins[binStart:binEnd] = i

				#If any row in any bin has a value that matches the first row in this bin, add those rows to the previous bin.
				if(i>1) finalData$bins[(finalData$CATEGORY==finalData$CATEGORY[binStart]) & (finalData$bins==i)] = i-1
		  
			}

			#The last bin has everybody that is left.
			finalData$bins[is.na(finalData$bins)] = numberOfBins
		}

		
		if(binning.manual == TRUE)
		{
			if(binning.variabletype == "Categorical")
			{
				
			}
			
			if(binning.variabletype == "Continuous")
			{
				#Convert the category column to a number.
				finalData$CATEGORY = as.numeric(levels(finalData$CATEGORY))[as.integer(finalData$CATEGORY)]			
			
				#This is used for a str_extract later.
				library(stringr)
			
				#We assume we get a list of ranges with "|" in between.
				splitTotalRangeString <- strsplit(binning.binrangestring,"\\|");
				splitTotalRangeString <- unlist(splitTotalRangeString);

				#Each entry is a bin.
				for(currentBin in splitTotalRangeString)
				{

				  #For each range we have name,low#,high#
				  splitRange <- strsplit(currentBin,",");
				  splitRange <- unlist(splitRange);

				  #In order to set the bin field, we extract the ranges.
				  binName <- splitRange[1]
				  lowRange <- as.numeric(str_extract(splitRange[2],"\\d*\\.?\\d*"))
				  highRange <- as.numeric(str_extract(splitRange[3],"\\d*\\.?\\d*"))
				  
				  #Assign everybody in this range to the current bin.
				  finalData$bins[(finalData$CATEGORY > lowRange) & (finalData$CATEGORY <= highRange)] = binName
				  
				}

				#Remove anyone who is NA.
				finalData <- finalData[!is.na(finalData$bins),]
			}		
		}
		
		#After we set the bin column, copy it to category and remove bin.
		finalData$CATEGORY = finalData$bins
		finalData$bins <- NULL
		
	}
	###################################	
	
	#We need MASS to dump the matrix to a file.
	require(MASS)
	
	#Write the final data file.
	write.matrix(finalData,"outputfile",sep = "\t")
}
