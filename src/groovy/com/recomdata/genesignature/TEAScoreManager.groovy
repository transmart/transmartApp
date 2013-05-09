/*************************************************************************
 * tranSMART - translational medicine data mart
 * 
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 * 
 * This product includes software developed at Janssen Research & Development, LLC.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 *
 ******************************************************************/
  

package com.recomdata.genesignature

import com.recomdata.util.BinomialDistribution
import org.apache.log4j.Logger
import org.transmart.biomart.BioAssayAnalysisData;

/**
 * manager class for TEA scoring logic
 * $Id: TEAScoreManager.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 *@author $Author: mmcduffie $
 *@version $Revision: 9178 $
 **/
public class TEAScoreManager {

	static Logger log = Logger.getLogger(TEAScoreManager.class);

	// this is a cutoff for UI display
	static double TEA_SIGNIFICANCE_CUTOFF = 0.05

	// number of genes used in entire search analysis
	def geneCount = 0

	/**
	 * applies the TEA scoring algorithm for the specified AnalysisResult based on its value object list
	 * The algorithm populates the AnalysisResult with the various TEA metrics
	 */
	def assignTEAMetrics(analysisResult) {

		// list of bio markers to score
		def valueList = analysisResult.assayAnalysisValueList
		Map mapMarkers = new HashMap()
		def currMarker

		if(valueList.size()==0) {
			analysisResult.TEAScore = null;
			return;
		}

		// counters
		int pValCtUp = 0
		int pValCtDown = 0

		// log sums
		double pValSumUp = 0
		double pValSumDown = 0

		//tmp vars
		def baad
		def compFoldChg
		def gsFoldChg
		def NPV
		def bm

		// iterate items
		for(value in valueList) {
			baad = value.analysisData
			bm = value.bioMarker

			// track each biomarker that has been evaluated
			currMarker = mapMarkers.get(bm.id)
			if(currMarker != null) {
				log.warn("skipping duplicate bioMarker ("+bm.name+"): 1) Comp fold chg:"+compFoldChg+"; 2) NPV: "+NPV+"; 3) Regulation fold chg: "+gsFoldChg)
				continue;
			}

			// track evaluated marker
			mapMarkers.put(bm.id, bm)

			// data used in calc
			compFoldChg = baad.foldChangeRatio
			NPV = baad.teaNormalizedPValue
			gsFoldChg = value.valueMetric

		//	log.info("evaluating bioMarker ("+bm.name+"): 1) Comp fold chg:"+compFoldChg+"; 2) NPV: "+NPV+"; 3) Regulation fold chg: "+gsFoldChg)

			if(gsFoldChg==null || gsFoldChg==0) {
				// a) genes and pathways
				if(compFoldChg>0) {
					pValCtUp++
					pValSumUp += -Math.log(NPV)
				} else {
					pValCtDown++
					pValSumDown += -Math.log(NPV)
				}
			}
			else {
				// b) gene lists and signatures
				if((gsFoldChg>0 && compFoldChg>0) || (gsFoldChg<0 && compFoldChg<0)) {
					pValCtUp++
					pValSumUp += -Math.log(NPV)
				} else {
					pValCtDown++
					pValSumDown += -Math.log(NPV)
				}
			}
		}

		// final TEA scores (set initially to a large number)
		double TEAScoreUp = 1.1;
		double TEAScoreDown = 1.1;

		//log.info(">> TEA Summary <<")

		log.info("1) up count: "+pValCtUp + "; down count: "+pValCtDown)

		// up score
		if(pValCtUp>0) TEAScoreUp = calcTEAScore(pValCtUp, pValSumUp, "up")

		// down score
		if(pValCtDown>0) TEAScoreDown = calcTEAScore(pValCtDown, pValSumDown, "down")

		// assign TEA metrics (retain lower of the two)
		def teaScore = Math.min(TEAScoreUp,TEAScoreDown);
		analysisResult.teaScore = new Double(teaScore);

		// enrichment status
		if(pValCtDown==0 && pValCtUp>0) analysisResult.bTeaScoreCoRegulated = true;

		if(pValCtDown>0 && pValCtUp==0) analysisResult.bTeaScoreCoRegulated = false;

		if(pValCtDown>0 && pValCtUp>0) analysisResult.bTeaScoreCoRegulated = (TEAScoreUp<=TEAScoreDown);

		// significant TEA score?
		analysisResult.bSignificantTEA = analysisResult.teaScore.doubleValue() <= TEA_SIGNIFICANCE_CUTOFF
		//log.info("3) TEA Result [ score: "+analysisResult.teaScore+"; co-regulated? "+analysisResult.bTeaScoreCoRegulated+"; significant? "+analysisResult.bSignificantTEA+" ]")
	}

	/**
	 * calc TEA score for indicated side
	 */
	double calcTEAScore(int sideCt, double sideSum, String side) {
		def pValAvg = Math.exp(-sideSum/sideCt)
		BinomialDistribution bd = new BinomialDistribution(geneCount, pValAvg)
		def tea = 1 - bd.getCDF(sideCt)
		//log.info("2?) ("+side+") TEA Score: "+tea+"; pv_ave p-value: "+pValAvg);
		return tea
	}
}
