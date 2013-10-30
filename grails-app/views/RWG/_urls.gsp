<%-- Set up URLs for AJAX --%>
<script type="text/javascript" charset="utf-8">
    var searchResultsURL = "${createLink([action:'loadSearchResults'])}";
    var facetResultsURL = "${createLink([controller:'RWG', action:'getFacetResults2'])}";
    var clearSearchFilterURL = "${createLink([controller:'RWG', action:'clearSearchFilter'])}";
    var facetTableResultsURL = "${createLink([action:'getFacetResultsForTable'])}";
    var newSearchURL = "${createLink([controller:'RWG', action:'newSearch'])}";
    var visualizationURL = "${createLink([action:'newVisualization'])}";
    var tableURL = "${createLink([action:'newTable'])}";
    var treeURL = "${createLink([action:'getDynatree'])}";
    var sourceURL = "${createLink([controller:'RWG', action:'searchAutoComplete'])}";
    var getCategoriesURL = "${createLink([controller:'RWG', action:'getSearchCategories'])}";
    var getFilterCategoriesURL = "${createLink([controller:'RWG', action:'getFilterCategories'])}";
    var crossImageURL = "${resource([dir:'images', file:'small_cross.png'])}";
    var getHeatmapNumberProbesURL = "${createLink([action:'getHeatmapNumberProbes'])}";
    var getHeatmapDataURL = "${createLink([action:'getHeatmapData'])}";
    var getHeatmapDataForExportURL = "${createLink([action:'getHeatmapDataForExport2'])}";
    var getBoxPlotDataURL = "${createLink([action:'getBoxPlotData'])}";
    var getLinePlotDataURL = "${createLink([action:'getLinePlotData'])}";
    var saveSearchURL = "${createLink([action:'saveFacetedSearch'])}";
    var loadSearchURL = "${createLink([action:'loadFacetedSearch'])}";
    var deleteSearchURL = "${createLink([action:'deleteFacetedSearch'])}";
    var exportAsImage = "${createLink([action:'exportAsImage'])}";

    var getStudyAnalysesUrl = "${createLink([controller:'RWG',action:'getTrialAnalysis'])}";

    //These are the URLS for the different browse windows.
    var studyBrowseWindow = "${createLink([controller:'experiment',action:'browseExperimentsMultiSelect'])}";
    var analysisBrowseWindow = "${createLink([controller:'experimentAnalysis',action:'browseAnalysisMultiSelect'])}";
    var regionBrowseWindow = "${createLink([controller:'RWG',action:'getRegionFilter'])}";
    var dataTypeBrowseWindow = "${createLink([controller:'RWG',action:'browseDataTypesMultiSelect'])}";
    var getTableDataURL = "${createLink([controller:'search',action:'getTableResults'])}";
    var getAnalysisDataURL = "${createLink([controller:'search',action:'getAnalysisResults'])}";
    var getQQPlotURL = "${createLink([controller:'search',action:'getQQPlotImage'])}";

    var webStartURL = "${createLink([controller:'search',action:'webStartPlotter'])}";

    var sessionSearch = "${rwgSearchFilter}";
    var sessionOperators = "${rwgSearchOperators}";
    var sessionSearchCategory = "${rwgSearchCategory}";

    var updateSearchCategoryURL = "${createLink([controller:'RWG', action:'updateSearchCategory'])}";
    var welcomeURL = "${createLink([controller:'RWG', action:'ajaxWelcome'])}";

</script>