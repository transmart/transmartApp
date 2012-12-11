<%-- Set up URLs for AJAX --%>
<script type="text/javascript" charset="utf-8">
var searchResultsURL = "${createLink([controller:'RWG', action:'loadSearchResults'])}";
var facetResultsURL = "${createLink([controller:'fmFolder', action:'getAllPrograms'])}";
var facetTableResultsURL = "${createLink([controller:'RWG', action:'getFacetResultsForTable'])}";
var newSearchURL = "${createLink([controller:'RWG', action:'newSearch'])}";
var visualizationURL = "${createLink([controller:'RWG', action:'newVisualization'])}";
var tableURL = "${createLink([controller:'RWG', action:'newTable'])}";
var treeURL = "${createLink([controller:'RWG', action:'getDynatree'])}";
var sourceURL = "${createLink([controller:'RWG', action:'searchAutoComplete'])}";	      
var getCategoriesURL = "${createLink([controller:'RWG', action:'getSearchCategories'])}";
var getHeatmapNumberProbesURL = "${createLink([controller:'RWG', action:'getHeatmapNumberProbes'])}";
var getHeatmapDataURL = "${createLink([controller:'RWG', action:'getHeatmapData'])}";
var getHeatmapDataForExportURL = "${createLink([controller:'RWG', action:'getHeatmapDataForExport2'])}";
var getBoxPlotDataURL = "${createLink([controller:'RWG', action:'getBoxPlotData'])}";
var getLinePlotDataURL = "${createLink([controller:'RWG', action:'getLinePlotData'])}";	        
var saveSearchURL = "${createLink([controller:'RWG', action:'saveFacetedSearch'])}";
var loadSearchURL = "${createLink([controller:'RWG', action:'loadFacetedSearch'])}";
var deleteSearchURL = "${createLink([controller:'RWG', action:'deleteFacetedSearch'])}";
var exportAsImage = "${createLink([controller:'RWG', action:'exportAsImage'])}";

var getStudyAnalysesUrl = "${createLink([controller:'RWG',action:'getTrialAnalysis'])}";
var experimentDataUrl = "${createLink(controller:'experimentAnalysis',action:'expDetail')}";
var fileDataUrl = "${createLink([controller:'RWG',action:'getFileDetails'])}";

//These are the URLS for the different browse windows.
var studyBrowseWindow = "${createLink([controller:'experiment',action:'browseExperimentsMultiSelect'])}";
var analysisBrowseWindow = "${createLink([controller:'experimentAnalysis',action:'browseAnalysisMultiSelect'])}";
var regionBrowseWindow = "${createLink([controller:'RWG',action:'getRegionFilter'])}";
var dataTypeBrowseWindow = "${createLink([controller:'RWG',action:'browseDataTypesMultiSelect'])}";
var getTableDataURL = "${createLink([controller:'search',action:'getTableResults'])}";
var getAnalysisDataURL = "${createLink([controller:'search',action:'getAnalysisResults'])}";
var getQQPlotURL = "${createLink([controller:'search',action:'getQQPlotImage'])}";

var webStartURL = "${createLink([controller:'search',action:'webStartPlotter'])}";
var datasetExplorerURL = "${createLink([controller:'datasetExplorer'])}";

var crossImageURL = "${resource([dir:'images', file:'small_cross.png'])}";
</script>