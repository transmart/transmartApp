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
#PivotSNPCNVData
#Parse the SNP CNV files for each subject and create a combined and pivoted file.
###########################################################################

PivotSNPCNVData.pivot <- 
function
(
subjectsStr, delimiter, filesPath, platformName
)
{
  library(data.table)
  subjectList <- as.vector(unlist(strsplit(subjectsStr, delimiter)),mode="list");
  setwd(filesPath);
  
  finalDataExists <- FALSE
  firstTime <- TRUE
  
  for (subject in subjectList) {
    print("Subject :: ")
    print(subject)
    #When only 1 subject in list, the str
    subject <- gsub("^\\s+|\\s+$", "",subject)
    #Read the input file.
    try(
      dataFile <- data.frame(read.delim(paste(subject,".CNV", sep=""))), silent=TRUE)
      print("file exists!!!")
      #Split the data by the PATIENT_ID+'_'+SAMPLE.
      splitData <- split(dataFile,paste(dataFile$PATIENT.ID,dataFile$SAMPLE, sep = '_'))
    
      #Create a matrix with unique PROBE.ID.
      tempData <- data.table(matrix(unique(dataFile$PROBE.ID)))
      
      #Name the column.
      colnames(tempData) <- c("PROBE")
      setkey(tempData, PROBE)
      
      #Get the unique list of samples.
      sampleList <- unique(paste(dataFile$PATIENT.ID,dataFile$SAMPLE, sep = '_'))
    	#delete data file matrix
    	rm(dataFile)
    	gc()
      
      #For each of the passed in (patient+sample)s, append the rows onto the end of our temp matrix.
      for(entry in sampleList)
    	{
    		#For each (patient+sample) we merge the data against the PROBE.ID.
    		sampleData=data.table(splitData[[entry]][c('PROBE.ID','COPY.NUMBER')])
    		colnames(sampleData) <- c("PROBE", "COPY NUMBER")
    		setkey(sampleData, PROBE)
    		
    		tempData <- tempData[sampleData]
    		rm(sampleData)
    		gc()
    	}
      
      colnames(tempData) <- c("PROBE", paste(sampleList))
      
      if (firstTime) {
        finalData <- tempData
      } else {
      	 setkey(finalData, PROBE)
        finalData <- finalData[tempData]
      }
      
      finalDataExists <- TRUE
      firstTime <- FALSE
    
      #Remove the subject.CNV file
      file.remove(paste(subject,".CNV", sep=""))
    #}
  }
  
  #We need MASS to dump the matrix to a file.
  require(MASS)
  
  #Write the final data file, rename temp to something more meaningful.
  if (finalDataExists) {
    # write.matrix(finalData,paste(platformName, ".CNV", sep=""),sep = "\t")
    # Using write.table because write.matrix writes out trailing whitespace - see JIRA item TRANSREL-24
    write.table(finalData,filename, sep = "\t", quote = FALSE, row.names = FALSE)
  }
}
