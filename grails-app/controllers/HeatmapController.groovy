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
  

import grails.converters.*
import com.recomdata.util.*
import org.hibernate.*
import org.transmart.GlobalFilter;
import org.transmart.SearchFilter;
import org.transmart.biomart.BioAssayAnalysis;
import org.transmart.biomart.BioAssayAnalysisData;

import org.transmart.biomart.BioMarker

/**
 * $Id: HeatmapController.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 *
 */

public class HeatmapController{

	def trialQueryService
	def heatmapService
	def searchKeywordService

	def initheatmap = {
		session.searchFilter.heatmapFilter.reset();
	}

	def filterheatmap = {
		//			 set session variable
		session.searchFilter.heatmapFilter.heatmapfiltertype=params.heatmapfiltertype

		if (params.id != null && params.id.length() > 0) {
			session.searchFilter.heatmapFilter.searchTerm = org.transmart.searchapp.SearchKeyword.get(Long.valueOf(params.id))

		}
		render(view:'initheatmap')
	}

	def showheatmap = {

		def dataResult = generateHeatmaps()

		def hmapcount = 1;
		def comtable =null;
		def cortable =null;
		def rbmtable =null;
		def rhotable =null;

		if(!dataResult['comresult']['datatable'].isEmpty()){
			comtable =(dataResult['comresult']['datatable'] as JSON)
		}
		if(!dataResult['corresult']['datatable'].isEmpty()){
			hmapcount++;
			cortable =(dataResult['corresult']['datatable'] as JSON);
		}
		if(!dataResult['rbmresult']['datatable'].isEmpty()){
			hmapcount++;
			rbmtable = (dataResult['rbmresult']['datatable'] as JSON)
		}
		if(!dataResult['rhoresult']['datatable'].isEmpty()){
			hmapcount++;
			rhotable=(dataResult['rhoresult']['datatable'] as JSON)
		}

		def allanalysis = new ArrayList<BioAssayAnalysis>()
		allanalysis.addAll(dataResult['comresult']['analysislist'])
		allanalysis.addAll(dataResult['corresult']['analysislist'])
		allanalysis.addAll(dataResult['rbmresult']['analysislist'])
		allanalysis.addAll(dataResult['rhoresult']['analysislist'])

		// sort on short tile
		def ac = [ compare: { a,b-> a.shortDescription.toLowerCase().compareTo(b.shortDescription.toLowerCase()) } ] as Comparator
		Collections.sort(allanalysis, ac)

		def hmapwidth=100/hmapcount;
		// Convert table object to JSON format

		//log.info (heatmap.rhotable==null)?"empty":"not empty"
		return ["comtable":comtable,
		"cortable":cortable,
		"rbmtable":rbmtable,
		"rhotable":rhotable,
		"hmapwidth":hmapwidth,
		"contentlist":allanalysis]
	}

	def downloadheatmapexcel = {

		def dataResult = generateHeatmaps()

		//	log.info dataResult
		def sheets = []

		if(!dataResult['comresult']['datatable'].isEmpty()){

			//log.info "has comparison data"

			sheets.add(createExcelSheet(dataResult['comresult']['datatable'].table,"Gene Expression Comparison"))
		}
		if(!dataResult['corresult']['datatable'].isEmpty()){

			sheets.add(createExcelSheet(dataResult['corresult']['datatable'].table, "Gene Expression Correlation"))
		}
		if(!dataResult['rbmresult']['datatable'].isEmpty()){

			sheets.add(createExcelSheet(dataResult['rbmresult']['datatable'].table, "RBM"))
		}
		if(!dataResult['rhoresult']['datatable'].isEmpty()){
			sheets.add(createExcelSheet(dataResult['rhoresult']['datatable'].table, "RBM Spearman Correlation"))
		}

		//log.info sheets

		def gen = new ExcelGenerator()
		response.setHeader("Content-Type", "application/vnd.ms-excel; charset=utf-8")
		response.setHeader("Content-Disposition", "attachment; filename=\"heatmap.xls\"")
		response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
		response.setHeader("Pragma", "public");
		response.setHeader("Expires", "0");
		response.outputStream << gen.generateExcel(sheets);

	}

	/**
	 *  generate heatmap results
	 */
	def generateHeatmaps(){

		// use filter values from session to generate heatmap
		def sfilter = session.searchFilter

		// need to decide which algorithm to run
		// we have 3 algorithms
		//1) search top gene
		//2) search pathway from heatmap filter
		//3) search genes from global filters

		boolean searchTopGene = false;
		boolean searchHeatmapFilter = false;

		// for genes to be displayed in the heatmap - this is used for searchHeatmapFilter and search global filter
		def orderedGenes = new LinkedHashSet()
		def searchGeneIds = []
		def searchAnalysisIds =org.transmart.biomart.BioAssayAnalysisData.executeQuery(trialQueryService.createAnalysisIDSelectQuery(sfilter), [max:100])


		if("topgene".equalsIgnoreCase(sfilter.heatmapFilter.heatmapfiltertype)){
			searchTopGene = true;

		}else{
			def keyword = session.searchFilter.heatmapFilter.searchTerm
			if(keyword!=null){
				searchHeatmapFilter = true;

				if(keyword.dataCategory.equalsIgnoreCase("PATHWAY")||
				keyword.dataCategory.equalsIgnoreCase("GENESIG")||
				keyword.dataCategory.equalsIgnoreCase("GENELIST")
				){
					// pathway
					List allGenes = searchKeywordService.expandAllListToGenes(keyword.bioDataId, 200);

					//		log.info allGenes
					for(k in allGenes){
						searchGeneIds.add(k.bioDataId)
					}
					orderedGenes.addAll(allGenes);

				}else{ // gene

					orderedGenes.add(keyword)

					searchGeneIds.add(keyword.bioDataId)
				}
			}
		}
		// if not by top gene nor by heatmap filer then use global filters
		if(!searchTopGene && !searchHeatmapFilter){
			// otherwise use global filters

			def allPathwayGenes = [];
			if(sfilter.globalFilter.hasAnyListFilters())
				allPathwayGenes = searchKeywordService.expandAllListToGenes(sfilter.globalFilter.getAllListFilters().getKeywordDataIdString(), 200);
			def genes = sfilter.globalFilter.getGeneFilters()
			orderedGenes.addAll(genes)
			orderedGenes.addAll(allPathwayGenes)
			for(g in genes){
				searchGeneIds.add(g.bioDataId)

			}
			for(pg in allPathwayGenes){
				searchGeneIds.add(pg.bioDataId)
			}
		}

		// now it's time to get the data back

		def dataList = [:];
		def maxshortdescr = 39;

		// comparison

		dataList['comresult']= createHeatmapData("comparison", "Gene Expression", searchTopGene, searchAnalysisIds,searchGeneIds, orderedGenes, maxshortdescr)
		// correlation

		dataList['corresult']= createHeatmapData("correlation", "Gene Expression", searchTopGene, searchAnalysisIds,searchGeneIds, orderedGenes, maxshortdescr+2)

		// rbm comparison

		dataList['rbmresult']= createHeatmapData("comparison", "RBM", searchTopGene, searchAnalysisIds,searchGeneIds, orderedGenes, maxshortdescr)

		// rbm spearman
		dataList['rhoresult']= createHeatmapData("spearman correlation", "RBM", searchTopGene, searchAnalysisIds, searchGeneIds, orderedGenes, maxshortdescr)

		//println(dataList['comresult'].analysislist?.size());
		//println(dataList['corresult'].analysislist?.size());
		//println(dataList['rbmresult'].analysislist?.size());
		//println(dataList['rhoresult'].analysislist?.size());
		//println("searchanalysisids"+searchAnalysisIds.size());

		return dataList;

	}

	/*
	 * create heatmap result data
	 */
	def createHeatmapData( method,
	dataType,
	searchTopGene,
	searchAnalysisIds,
	searchGeneIds,
	orderedGenes,
	maxcolLength){

		def sfilter = session.searchFilter

		def dataList = heatmapService.createHeatMapData(sfilter, method, dataType, searchTopGene, searchGeneIds,searchAnalysisIds);

		//println("datalist:"+dataList?.size());
		def cutoff = 4.5

		if(dataList!=null && !dataList.isEmpty()){
			// create column name list and map using analysis name
			// get all analysis out of the bioDataFact object and format their names

			def columnList=[]
			// columns
			columnList.add([type:"n",label:'Gene Name',pattern:"",id:0])
			def ccount=0
			def columnPosMap = [:]
			def analysisId = null
			def analysis = null
			def analysisName = null
			def analysisNameMap = [:]
			def assayAnalysisList =[]
			for(data in dataList){

				//analysisId = data.assayAnalysisId
				//log.info "data:"+data.id+":"+data.bioMarkerId
				analysisName = analysisNameMap.get(data.assayAnalysisId)
				// reformat & shorten analysis name
				if(analysisName==null){
					analysis = org.transmart.biomart.BioAssayAnalysis.get(data.assayAnalysisId)
					analysisName = analysis.shortDescription
					if(analysisName == null){
						analysisName = analysis.name
					}

					analysisName = analysisName.replaceAll("\\s+", "_");
					analysisName = analysisName.replaceAll("'", "*");

					if(analysisName.length()>maxcolLength){
						analysisName= analysisName.substring(0, maxcolLength-3)+"..."
					}else {
						def paddingnum = maxcolLength - analysisName.length();
						def sp = new StringBuilder(maxcolLength).append(analysisName)
						for(pi in 0..paddingnum-1){
							sp.append(" ")
						}
						analysisName = sp.toString();
					}
					analysisNameMap.put(data.assayAnalysisId, analysisName)
					assayAnalysisList.add(analysis)

					// add into column list
					columnList.add([type:"n",label:analysisName.toUpperCase(),pattern:"",id:ccount])
					columnPosMap.put(data.assayAnalysisId, ccount)
					ccount++;
				}
			}

			// rows
			def rowmap =new TreeMap()
			def totalcols = columnList.size()-1;

			// build empty data structure first
			// for each data object build a row with bioMaker as first column value
			def datavalue = null
			def assayData = null
			def rowArray = null
			// handle ordered gene first
			// this is the case that we need to display all genes in pathways

			if(!orderedGenes.isEmpty()){
				for(keyword in orderedGenes){
					rowmap.put(keyword.keyword, new Object[totalcols])
				}
			}

			// data list later
			for (data in dataList) {

				rowArray = rowmap.get(data.bioMarkerName)

				if (rowArray == null) {
					rowArray = new Object[totalcols]
					rowmap.put(data.bioMarkerName, rowArray);
				}

				// find column index by analysis id
				def columnIndex = columnPosMap.get(data.assayAnalysisId)

				if (rowArray != null){

					if("correlation".equals(method)){
						datavalue= data.rValue
					}else if("spearman correlation".equals(method)){
						datavalue = data.rhoValue
					}else {
						datavalue = data.foldChangeRatio
					}
					rowArray[columnIndex] =datavalue
				}
				//	log.info "loading:"+data.bioMarkerId
			}


			def rowlist=[]
			def rowcount =0;
			for (entryset in rowmap.entrySet()) {

				def row = []
				// this is the gene name
				String rowname = entryset.key

				if(rowname==null){
					rowname = ""
				}else {
					rowname = rowname.replaceAll("'", "*");
				}

				// this is an array
				def rvalues = entryset.value;
				//log.info rvalues
				// handle null value rows
				def hasRowValue = false;
				// if not RBM
				if("RBM".equalsIgnoreCase(dataType)){
					for(value in rvalues){
						if(value!=null){
							hasRowValue = true;
							break;
						}
					}
				}else {
					hasRowValue = true;
				}

				if(hasRowValue){
					row.add([v:rowname,f:rowname]);
				}else {
					row.add([v:"N/A", f:"N/A"]);
				}

				for(value in rvalues){
					def vvalue = value;
					if (vvalue != null) {
						if (vvalue > cutoff)
							vvalue = cutoff
						if (vvalue < -cutoff)
							vvalue = -cutoff
					}
					row.add([v:vvalue,f:value])
				}

				rowlist.add(row)

			}

			def table =[table:[cols:columnList, rows:rowlist]]

			return ['datatable':table, 'analysislist':assayAnalysisList]
		}else {
			return ['datatable':[], 'analysislist':[]]
		}

	}



	def createExcelSheet(table, name){
		def cols = []
		for (col in table.cols) {
			cols.add(col.label)
		}
		def rows = []

		for (row in table.rows) {
			//log.info row
			if(row[0].f!="N/A"){
				def newrow = []
				for (value in row) {
					newrow.add(value.f)
				}
				rows.add(newrow)
			}
		}
		return new ExcelSheet(name:name, headers:cols, values:rows)

	}


	def noResult = {
		render(view:'noresult')
	}
}
