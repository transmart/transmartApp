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
#SurvivalCurveLoader
#This will load our input files into variables so we can run the survival curve.
###########################################################################

SurvivalCurve.loader <- function(
	input.filename,
	output.name="SurvivalCurve",
	time.field="TIME",
	censor.field="CENSOR",
	concept.time=NA,
	time.conversion=1
  )
 {
	######################################################
	#Clean parameters.
	#Convert the time conversion parameter to a number.
	time.conversion<-as.numeric(time.conversion)
	######################################################
	
	######################################################
	#Input files/Variables
	surv.data<-read.delim(input.filename,header=T)
	
	#We need to create a label for the legend based on the categories.
	classList <- as.vector(gsub("\\s","_",gsub("^\\s+|\\s+$", "",(surv.data$'CATEGORY'))))
	legendLabels <- as.vector(unique(gsub("^\\s+|\\s+$", "",surv.data$'CATEGORY')))
	
	#Pull the time and status fields out of the survival data.
	time <- surv.data[[time.field]]
	status <- surv.data[[censor.field]]
	######################################################
	
	######################################################
	#Load package
	require(splines,quietly=T)
	require(survival,quietly=T)
	######################################################

	######################################################
	#Run survival curve, and survival fit.
	#If we have less than 2 classes, we need special syntax for the Survival formula.
	if(length(legendLabels)<2)
	{
		fitted = survfit(
							Surv(time,status)~1,
							data=surv.data,
							type="kaplan-meier",
							error="greenwood",
							conf.int=.95,
							conf.type="log"
						)
	}
	else
	{
		fitted = survfit(
							Surv(time,status)~classList,
							data=surv.data,
							type="kaplan-meier",
							error="greenwood",
							conf.int=.95,
							conf.type="log"
						)
	}	
	######################################################

	######################################################
	#Determine title for outputs.
	title<-"#  Kaplan-Meier estimator"
	######################################################

	######################################################
	#Print the summary results.

	fit.name <- paste(output.name,"_FitSummary.txt",sep="")
	table.name <- paste(output.name,"_Table.txt",sep="")

	write.table(title,fit.name,quote=F,sep="\t",row.names=F,col.names=F)
	write.table(title,table.name,quote=F,sep="\t",row.names=F,col.names=F)
	write.table("",fit.name,quote=F,sep="\t",row.names=F,col.names=F,append=T)
	write.table("",table.name,quote=F,sep="\t",row.names=F,col.names=F,append=T)

	write.table(capture.output(fitted),fit.name,quote=F,sep="\t",row.names=F,col.names=F,append=T)
	write.table(capture.output(summary(fitted)),table.name,quote=F,sep="\t",row.names=F,col.names=F,append=T)
	######################################################

	######################################################
	#Plotting survival curve
	#X Axis range
	x.max<-max(time)/time.conversion

	#This is the default X axis label.
	x.axis.label="Time"
	
	#The x axis can be a trimmed version of the concept path.
	if(!is.na(concept.time)) x.axis.label <- sub(pattern="^\\\\(.*?\\\\){3}",replacement="",x=concept.time,perl=TRUE)
	
	y.axis.label="Fraction of Patients"

	#Determine output type.
	png(paste(output.name, ".png", sep=""))

	#Set the limits for the axis.
	xlim <- c(0, x.max)
	ylim <- c(0,1)

	#Plot the graph.
	suppressWarnings(plot(
							fitted, 
							lty=1, 
							col=c("blue","red","black","green","orange","purple"), 
							lwd=1, 
							firstx=0, 
							mark.time=TRUE, 
							mark=3, 
							xscale=time.conversion, 
							xlim=xlim, 
							ylim=ylim, 
							xlab=x.axis.label, 
							ylab=y.axis.label, 
							fun="log", 
							main=title, 
							cex.axis=1
							))
	#Draw a box around the plot.
	box(lwd=1)

	#Add legend if required.
	legend(x="topright", legend=legendLabels, lty=1, col=c("blue","red","black","green","orange","purple"), lwd=1, inset=0.02)

	#Close any open devices.
	dev.off()
	######################################################
}
