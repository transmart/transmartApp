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
#PivotGeneExprGCTData
#Parse the gene output GCT file and create pivoted file.
###########################################################################

PivotGSEAExportGCTData.pivot <- 
function
(
input.dataFile
)
{
	#Read the input file.
	dataFile <- data.frame(read.delim(input.dataFile));
  
  #We use reshape2 package to do the conversion
  require(reshape2)
  # without the mean function in dcast the conversion is done in 1 second, else it takes 7 seconds
  finalData <- dcast(dataFile, PROBE.ID + Description ~ PATIENT.ID+SAMPLE, value.var = 'VALUE', median)
  
	#We need MASS to dump the matrix to a file.
	require(MASS)
  #Write the final data file.
  write(paste('#1.2','',sep='\t'),'GSEA.GCT',sep='\t')
  # Number of Probes == dim(finalData)[1] and Number of Samples == dim(finalData)[2] - 2,
  # dim(finalData)[2] - 2 because we only want to count the distinct PATIENT.ID_ASSAY.ID
  write(paste(dim(finalData)[1], dim(finalData)[2] - 2,sep='\t'),'GSEA.GCT',sep='\t', append=TRUE)
  # Write the finalData without quotes
  write.table(finalData,"GSEA.GCT",sep = "\t", append=TRUE, quote=FALSE, row.names=FALSE)
  
  file.remove(input.dataFile)
}