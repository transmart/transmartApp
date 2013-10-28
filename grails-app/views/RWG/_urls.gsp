<%-- Set up URLs for AJAX --%>
<script type="text/javascript" charset="utf-8">
var searchResultsURL = "${createLink([controller:'RWG', action:'loadSearchResults'])}";
var facetResultsURL = "${createLink([controller:'RWG', action:'getFacetResults'])}";
var clearSearchFilterURL = "${createLink([controller:'RWG', action:'clearSearchFilter'])}";
var newSearchURL = "${createLink([controller:'RWG', action:'newSearch'])}";
var visualizationURL = "${createLink([controller:'RWG', action:'newVisualization'])}";
var tableURL = "${createLink([controller:'RWG', action:'newTable'])}";
var treeURL = "${createLink([controller:'RWG', action:'getDynatree'])}";
var sourceURL = "${createLink([controller:'RWG', action:'searchAutoComplete'])}";	      
var getCategoriesURL = "${createLink([controller:'RWG', action:'getSearchCategories'])}";
var getFilterCategoriesURL = "${createLink([controller:'RWG', action:'getFilterCategories'])}";
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

var exportAddURL = "${createLink([controller:'fileExport', action:'add'])}";
var exportRemoveURL = "${createLink([controller:'fileExport', action:'remove'])}";
var exportViewURL = "${createLink([controller:'fileExport', action:'view'])}";
var exportURL = "${createLink([controller:'fileExport', action:'export'])}";

var folderContentsURL = "${createLink([controller:'fmFolder',action:'getFolderContents'])}";
var folderDetailsURL = "${createLink(controller:'fmFolder',action:'folderDetail')}";
var analysisDataURL = "${createLink([controller:'fmFolder',action:'analysisTable'])}";
var editMetaDataURL = "${createLink([controller:'fmFolder',action:'editMetaData'])}";
var createAnalysisURL = "${createLink([controller:'fmFolder',action:'createAnalysis'])}";
var createAssayURL = "${createLink([controller:'fmFolder',action:'createAssay'])}";
var createFolderURL = "${createLink([controller:'fmFolder',action:'createFolder'])}";
var createStudyURL = "${createLink([controller:'fmFolder',action:'createStudy'])}";
var createProgramURL = "${createLink([controller:'fmFolder',action:'createProgram'])}";
var saveMetaDataURL = "${createLink([controller:'fmFolder',action:'updateMetaData'])}";
var saveAssayURL = "${createLink([controller:'fmFolder',action:'saveAssay'])}";
var saveAnalysisURL = "${createLink([controller:'fmFolder',action:'saveAnalysis'])}";
var saveStudyURL = "${createLink([controller:'fmFolder',action:'saveStudy'])}";
var saveFolderURL = "${createLink([controller:'fmFolder',action:'saveFolder'])}";
var saveProgramURL = "${createLink([controller:'fmFolder',action:'saveProgram'])}";

var welcomeURL = "${createLink([controller:'RWG', action:'ajaxWelcome'])}";
var searchLogURL = "${createLink([controller:'RWG', action:'searchLog'])}";

var deleteFileURL = "${createLink([controller:'fmFolder',action:'deleteFile'])}";
var deleteFolderURL = "${createLink([controller:'fmFolder',action:'deleteFolder'])}";

var updateSearchCategoryURL = "${createLink([controller:'RWG', action:'updateSearchCategory'])}";

var platformTypesUrl = '${createLink([action:'platformsForVendor',controller:'bioAssayPlatform'])}';

var ajaxPlatformsURL = '${createLink([action:'ajaxPlatforms',controller:'fmFolder'])}';

var addNodeRwgURL = "${createLink([controller:'RWG', action:'addOpenedNodeRWG'])}";
var removeNodeRwgURL = "${createLink([controller:'RWG', action:'removeOpenedNodeRWG'])}";
var resetNodesRwgURL = "${createLink([controller:'RWG', action:'resetOpenedNodes'])}";

var addNodeDseURL = "${createLink([controller:'RWG', action:'addOpenedNodeDSE'])}";
var removeNodeDseURL = "${createLink([controller:'RWG', action:'removeOpenedNodeDSE'])}";

</script>