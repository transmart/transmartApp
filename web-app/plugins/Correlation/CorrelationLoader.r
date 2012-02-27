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
#Correlation
#This will load our input files into variables so we can run the correlation.
###########################################################################

Correlation.loader <- function(
	input.filename,
	output.file="Correlation",
	correlation.by = "",
	correlation.method = ""
)
{
	######################################################
	library(Cairo)
	######################################################
	
	#To be precise I take it this means the significance probability,
	#that is the chance of getting a value of the correlation as far
	#from zero in absolute value or more so as the one you got...
	
	cor.pvalue <- function(X,method="pearson", use="complete" ) 
	{
		dfr = nrow(X) - 2
		R <- cor(X,method=method, use= use)
		above <- row(R) < col(R)
		r2 <- R[above]^2
		Fstat <- r2 * dfr / (1 - r2)
		R[above] <- 1 - pf(Fstat, 1, dfr)
		R
	}
	
	
	######################################################
	#Read the correlation data.
	line.data<-read.delim(input.filename,header=T)
	
	#This is the name of the output file for the correlation.
	correlationResultsFile <- "Correlation.txt"
	
	if(correlation.by == "subject")
	{
		write.table(format(cor(line.data,method = correlation.method),digits=3),correlationResultsFile,quote=F,sep="\t",row.names=F,col.names=T)
	}
	
	if(correlation.by == "variable")
	{
		write.table(format(cor.pvalue(line.data,method = correlation.method),digits=5),correlationResultsFile,quote=F,sep="\t",row.names=F,col.names=T)
		
		#Put (absolute) correlations on the upper panels,
		#with size proportional to the correlations.
		panel.cor <- function(x, y, digits=2, prefix="", cex.cor, ...)
		{
			usr <- par("usr"); on.exit(par(usr))
			par(usr = c(0, 1, 0, 1))
			r <- abs(cor(x, y, method = correlation.method))
			txt <- format(c(r, 0.123456789), digits=digits)[1]
			txt <- paste(prefix, txt, sep="")
			if(missing(cex.cor)) cex.cor <- 0.8/strwidth(txt)
			text(0.5, 0.5, txt, cex = cex.cor * r)
		}

		#Put histograms on the diagonal
		panel.hist <- function(x, ...)
		{
			usr <- par("usr"); on.exit(par(usr))
			par(usr = c(usr[1:2], 0, 1.5) )
			h <- hist(x, plot = FALSE)
			breaks <- h$breaks; nB <- length(breaks)
			y <- h$counts; y <- y/max(y)
			rect(breaks[-nB], 0, breaks[-1], y, col="skyblue", ...)
		}

		#Put a scatter plot with a fitted line on the bottom left.
		panel.lm <- function(x, y, digits=2, prefix="", ...)
		{
			usr <- par("usr"); on.exit(par(usr))
			par(usr = c(0, 1, 0, 1))     
			par(new = TRUE) 
			plot(y~x,pch=20)
			title(font.main=10,font.sub=9)
			abline(lm(y~x))
		}		
		
		#This is the name of the output image file.
		imageFileName <- paste(output.file,".png",sep="")
		
		#This initializes our image capture object.
		CairoPNG(file=imageFileName, width=1200, height=600,units = "px")	
		
		#Generate the image.
		pairs(line.data,upper.panel=panel.cor,diag.panel=panel.hist,lower.panel=panel.lm)
		
		#Turn of the graphics device to save the image.
		dev.off()		
		
	}
	
	######################################################
}
