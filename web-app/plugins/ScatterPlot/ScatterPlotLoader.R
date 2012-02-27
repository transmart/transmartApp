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
#ScatterPlot
#This will load our input files into variables so we can run the scatter plot.
###########################################################################

ScatterPlot.loader <- function(
  input.filename,
  output.file ="ScatterPlot",
  concept.dependent = "",
  concept.independent = ""
  )
 {
 	
	library(plyr)
	library(ggplot2)
	library(Cairo)
	
	######################################################
	#Read the line graph data.
	line.data<-read.delim(input.filename,header=T)
	
	#This is the name of the output file for the linear regression.
	linearResultsFile <- "LinearRegression.txt"
	
	#Place the linear model into a variable.
	linearResults <- lm(line.data$Y ~ line.data$X)
	linearSummary <- summary(linearResults)
	
	#Write the results of the linear regression to the file.
	write(paste("n=",nrow(line.data),sep=""), file=linearResultsFile)
	write(paste("intercept=",format(linearResults$coefficients[[1]],digits=3),sep=""), file=linearResultsFile,append=T)
	write(paste("slope=",format(linearResults$coefficients[[2]],digits=3),sep=""), file=linearResultsFile,append=T)
	write(paste("nr2=",format(linearSummary[[8]],digits=3),sep=""), file=linearResultsFile,append=T)
	write(paste("ar2=",format(linearSummary[[9]],digits=3),sep=""), file=linearResultsFile,append=T)	
	write(paste("p=",format(pf(linearSummary$fstatistic[1],linearSummary$fstatistic[2],linearSummary$fstatistic[3],lower.tail=FALSE),digits=3),sep=""), file=linearResultsFile,append=T)
	######################################################
	
	######################################################
	#Plotting the line.		
	
	#Get the labels for the x and y axis.
	xAxisLabel <- sub(pattern="^\\\\(.*?\\\\){3}",replacement="",x=concept.independent,perl=TRUE)
	yAxisLabel <- sub(pattern="^\\\\(.*?\\\\){3}",replacement="",x=concept.dependent,perl=TRUE)
	
	CairoPNG(file=paste(output.file,".png",sep=""))
	tmp <- ggplot(line.data, aes(X, Y)) + geom_point() + stat_smooth(method="lm", se=FALSE) + scale_x_continuous(xAxisLabel) + scale_y_continuous(yAxisLabel)
	
	print (tmp)

	#ggsave(file=paste(output.file,".png",sep=""))
	#Close any open devices.
	dev.off()	
	######################################################
}
