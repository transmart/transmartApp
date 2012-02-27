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

CoxRegression.loader <- function(
  input.filename,
  output.file						="CoxRegression_result",
  time								="TIME",
  status							="CENSOR",
  variable.continuous				="CATEGORY",
  variable.category					="NA",
  variable.interaction.terms		="NA",
  strata							="NA",
  input.subgroup					="NA", 		
  variable.selection				="none"    	
  )
 {
	######################################################
	#Hardcode some variables.
	tie.data.handling="efron"
	robust.variance="F"
	######################################################
	
	######################################################
	#Read the survival data.
	surv.data<-read.delim(input.filename,header=T)
	######################################################
	
	######################################################
	#Load packages for doing the regression.
	library(survival)
	######################################################
	
	######################################################
	#Clean up the variables, Replace commas with "+" signs.
	classList <- as.vector(gsub("\\s","_",gsub("^\\s+|\\s+$", "",(surv.data$'CATEGORY'))))
	######################################################
	
	######################################################
	#Running the actual Cox analysis.

	#Only include Tilde if we had multiple groups.
	if(length(unique(classList)) < 2)
	{
		return()
		#coxph.fit<-coxph(Surv(TIME,CENSOR)~1,data=surv.data,method="efron",robust="F")
	}
	else
	{
		coxph.fit<-coxph(Surv(TIME,CENSOR)~classList,data=surv.data,method="efron",robust="F")
	}

	fit.vector<-unlist(coxph.fit)  
	######################################################
	
	######################################################
	#If we need to further analyze the data based on variable selection, do it here.
	if (variable.selection!="none")
	{
		if (robust.variance=="T")
		{
			stop("### Robust variance cannot be used in variable selection! ###")
		}
		
		coxph.fit<-stepAIC(coxph.fit,direction=variable.selection,trace=0)
		fit.vector<-unlist(coxph.fit)
		
		# null model?
		if (names(fit.vector[1])=="loglik")
		{
			stop("### Null model! No variable is selected! ###")
		}  # end null model?
	}
	######################################################
	
	######################################################
	#Number of plots
	num.plot<-0
	
	for (n in 1:length(fit.vector))
	{
		if (regexpr("^coefficients.",names(fit.vector[n]))!=-1)
		{
			num.plot<-num.plot+1
		}
	}

	if (num.plot==1)
	{
		num.row.col.plot<-1
	}else{
		num.row.col.plot<-as.integer(sqrt(num.plot)+1)
	}
	######################################################
	
	######################################################
	# Text output
	
	#Set the output file name.
	output.file <- paste(output.file,".txt",sep="")

	#Determine how we display the tie method.
	if (tie.data.handling=="efron"){method<-"tie data handling:  Efron approximation"}
	if (tie.data.handling=="breslow"){method<-"tie data handling:  Breslow approximation"}
	if (tie.data.handling=="exact"){method<-"tie data handling:  Exact method"}

	#Determine how to display the selection.
	if (variable.selection=="none"){selection<-"variable selection:  none"}
	if (variable.selection=="both"){selection<-"variable selection:  Stepwise selection"}
	if (variable.selection=="forward"){selection<-"variable selection:  Forward selection"}
	if (variable.selection=="backward"){selection<-"variable selection:  Backward selection"}

	#Write the tables with the analysis data.
	write.table("# Cox regression results",output.file,quote=F,sep="\t",row.names=F,col.names=F)
	write.table("",output.file,quote=F,sep="\t",row.names=F,col.names=F,append=T)
	write.table(method,output.file,quote=F,sep="\t",row.names=F,col.names=F,append=T)
	write.table(selection,output.file,quote=F,sep="\t",row.names=F,col.names=F,append=T)
	write.table("",output.file,quote=F,sep="\t",row.names=F,col.names=F,append=T)

	write.table(capture.output(summary(coxph.fit)),output.file,quote=F,sep="\t",row.names=F,col.names=F,append=T)
	######################################################
}
