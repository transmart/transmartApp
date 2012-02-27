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
#CoxRegression
#This will load our input files into variables so we can run the cox regression.
###########################################################################

LineGraph.loader <- function(
	input.filename,
	output.file="LineGraph",
	graphType=""
)
{
 	######################################################
	#We need this package for a str_extract when we take text out of the concept.
	library(stringr)
	library(plyr)
	library(ggplot2)
	library(Cairo)
	######################################################
	
	######################################################
	#Read the line graph data.
	line.data<-read.delim(input.filename,header=T)
	
	#We need to convert the value column from a factor to a numeric.
	#finalData$VALUE <- as.numeric(levels(finalData$VALUE))[as.integer(finalData$VALUE)]

	#Aggregate the data to get rid of patient numbers. We add a standard error column so we can use it in the error bars.
	dataOutput <- ddply(line.data, .(CONCEPT_PATH,GROUP_VAR), 
	  summarise,
	  MEAN 		= mean(VALUE),
	  SD 		= sd(VALUE),
	  SE 		= sd(VALUE)/sqrt(length(VALUE)),
	  MEDIAN 	= median(VALUE)
	)

	#Adjust the column names.
	colnames(dataOutput) <- c('TIMEPOINT','GROUP','MEAN','SD','SE','MEDIAN')

	#Use a regular expression trim out the timepoint from the concept.
	#dataOutput$TIMEPOINT <- str_extract(dataOutput$TIMEPOINT,"Week [0-9]+")
	dataOutput$TIMEPOINT <- str_extract(dataOutput$TIMEPOINT,"(\\\\.+\\\\.+\\\\)+?$")
	
	#Convert the timepoint field to a factor.
	dataOutput$TIMEPOINT <- factor(dataOutput$TIMEPOINT)
		
	#Convert the group field to a factor.
	dataOutput$GROUP <- factor(dataOutput$GROUP)
	######################################################

	######################################################
	#Plotting the line.

	#Depending on the graph type, we create a different graph.
	if(graphType=="MERR")
	{
		limits <- aes(ymax = MEAN + SE, ymin = MEAN - SE)
		
		p <- ggplot(
			data=dataOutput,
			aes(x=TIMEPOINT, 
				y=MEAN,
				group=GROUP, 
				colour=GROUP
				)
			)

	}

	if(graphType=="MSTD")
	{
		limits <- aes(ymax = MEAN + SD, ymin = MEAN - SD)
	
		p <- ggplot(
			data=dataOutput,
			aes(x=TIMEPOINT, 
				y=MEAN,
				group=GROUP, 
				colour=GROUP
				)
			)
			
	}
	
	if(graphType=="MEDER")
	{
		limits <- aes(ymax = MEDIAN + SE, ymin = MEDIAN - SE)
		
		p <- ggplot(
			data=dataOutput,
			aes(x=TIMEPOINT, 
				y=MEDIAN,
				group=GROUP, 
				colour=GROUP
				)
			)
	}	
	
	p <- p + geom_line(size=1.5) + geom_errorbar(limits,width=0.2) + scale_colour_brewer() 
	
	#This sets the color theme of the background/grid.
	p <- p + theme_bw();
	
	#Set the text options for the axis.
	p <- p + opts(axis.text.x = theme_text(size = 17,face="bold",angle=5));
	p <- p + opts(axis.text.y = theme_text(size = 17,face="bold"));
	
	#Set the text options for the title.
	p <- p + opts(axis.title.x = theme_text(vjust = -.5,size = 20,face="bold"));
	p <- p + opts(axis.title.y = theme_text(vjust = .35,size = 20,face="bold",angle=90));
	
	#Set the legend attributes.
	p <- p + opts(legend.title = theme_text(size = 20,face="bold"));
	p <- p + opts(legend.text = theme_text(size = 15,face="bold"));
	p <- p + opts(legend.title=theme_blank())

	p <- p + geom_point(size=4);
	
	#This is the name of the output image file.
	imageFileName <- paste(output.file,".png",sep="")
	
	#This initializes our image capture object.
	CairoPNG(file=imageFileName, width=1200, height=600,units = "px")	
	
	#Printing actually puts the plot in the image.
	print(p)
	
	#Turn of the graphics device to save the image.
	dev.off()
	######################################################
}
