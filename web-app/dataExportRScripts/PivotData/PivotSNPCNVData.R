##########################################################################
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
 #########################################################################


##########################################################################
#PivotSNPCNVData
#Parse the SNP CNV files for each subject and create a combined and pivoted file.
###########################################################################

PivotSNPCNVData.pivot <- 
function
(
subjectsStr, delimiter, filesPath
)
{
  subjectList <- strsplit(subjectsStr, delimiter);
  setwd(filesPath);
  
  #Initialize a vector of unique probes
  probes <- character(0)
  
  for (subject in subjectList) {
    #Read the input file.
	  dataFile <- data.frame(read.delim(paste(subject,".CNV")))
    
    #Split the data by the PATIENT_ID+'_'+SAMPLE.
    splitData <- split(dataFile,paste(dataFile$PATIENT.ID,dataFile$SAMPLE, sep = '_'))
    
    #Create a vector of unique probes with PROBE.ID from all the files read
    probes <- unique(c(probes, dataFile$PROBE.ID))
  
    #Create a matrix with unique PROBE.ID.
    tempData <- matrix(unique(dataFile$PROBE.ID))
    #Name the column.
    colnames(tempData) <- c("PROBE.ID")
    
    #Get the unique list of samples.
    sampleList <- unique(paste(dataFile$PATIENT.ID,dataFile$SAMPLE, sep = '_'))
    
    #For each of the passed in (patient+sample)s, append the rows onto the end of our temp matrix.
    for(entry in sampleList)
  	{
  		#For each (patient+sample) we merge the data against the PROBE.ID.
  		tempData <- merge(tempData,splitData[[entry]][c('PROBE.ID','COPY_NUMBER')],by="PROBE.ID",all.x=TRUE)	
  	}
    
    colnames(tempData) <- c("PROBE_ID", paste(sampleList))
    
    # may need to replace merge with something else based on the real data
    finalData <- merge(finalData, tempData, by="PROBE.ID", all.x=TRUE)
    
    #Remove the subject.CNV file
    file.remove(paste(subject,".CNV"))
  }
  
  #We need MASS to dump the matrix to a file.
  require(MASS)
  
  #Write the final data file, rename temp to something more meaningful.
  write.matrix(finalData,"temp.CNV",sep = "\t")
}
