<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.0 Strict//EN">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="shortcut icon" href="${resource(dir:'images',file:'searchtool.ico')}">
		<link rel="icon" href="${resource(dir:'images',file:'searchtool.ico')}">
		<link rel="stylesheet" href="${resource(dir:'js',file:'ext/resources/css/ext-all.css')}"></link>
		<link rel="stylesheet" href="${resource(dir:'js',file:'ext/resources/css/xtheme-gray.css')}"></link>
		<link rel="stylesheet" href="${resource(dir:'css',file:'main.css')}"></link>

	<!--[if IE 7]>
		<style type="text/css">
			div#gfilterresult,div#ptfilterresult,  div#jubfilterresult, div#dqfilterresult { width: 99%; }
			div#summary-div { margin-bottom:5px; }
		</style>
	<![endif]-->

		<g:javascript library="prototype" />
		<script type="text/javascript" src="${resource(dir:'js', file:'ext/adapter/ext/ext-base.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'ext/ext-all.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'ext/miframe.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'filtertree.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'maintabpanel.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'toggle.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'searchcombobox.js')}"></script>
	    <script type="text/javascript" src="${resource(dir:'js', file:'picklist.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'editfilterswindow.js')}"></script>
		<script type="text/javascript" src="${resource(dir:'js', file:'utilitiesMenu.js')}"></script>
		<script type="text/javascript" charset="utf-8">
			Ext.BLANK_IMAGE_URL = "${resource(dir:'js', file:'ext/resources/images/default/s.gif')}";

			// set ajax to 90*1000 milliseconds
			Ext.Ajax.timeout = 180000;

			// this overrides the above
			Ext.Updater.defaults.timeout = 180000;

			// qtip on
			Ext.QuickTips.init();


			// Create global object to pass all the counts and urls to the main tab panel
			var pageData = {
				//activeTab: "${session.searchFilter.acttab()}",
				//default to 0
				activeTab:"0" ,
				// flag to hideInternal tabs as well as the export resnet button
				<sec:ifAnyGranted roles="ROLE_PUBLIC_USER">
			         hideInternal:true,
				</sec:ifAnyGranted>
				<sec:ifNotGranted roles="ROLE_PUBLIC_USER">
				     hideInternal:false,
				</sec:ifNotGranted>

				trial: {
				    count: "${searchresult.trialCount}",
				    analysisCount: "${searchresult.analysisCount}",
				    resultsUrl: "${createLink(controller:'trial', action:'datasourceTrial')}",
				    teaResultsUrl: "${createLink(controller:'trial', action:'datasourceTrialTEA')}",
				    filterUrl: "${createLink(controller:'trial', action:'showTrialFilter')}"
			    },

				pretrial: {
				    count: "${searchresult.allAnalysiCount}",
				    mRNAAnalysisCount: "${searchresult.mRNAAnalysisCount}",
				    resultsUrl: "${createLink(controller:'experimentAnalysis', action:'datasourceResult')}",
				    teaResultsUrl: "${createLink(controller:'experimentAnalysis', action:'datasourceResultTEA')}",
				    filterUrl: "${createLink(controller:'experimentAnalysis', action:'showFilter')}"
				},
				profile: {
				    count: "${searchresult.profileCount}",
				    resultsUrl: "${createLink(controller:'expressionProfile', action:'datasourceResult')}"
				},

			    doc: {
				    count: "${searchresult.documentCount}",
				    resultsUrl: "${createLink(controller:'document', action:'datasourceDocument')}",
				    filterUrl: "${createLink(controller:'document', action:'showDocumentFilter')}"
			    },

			    trialFilterUrl: "${createLink(controller:'trial',action:'trialFilterJSON')}",
			    heatmapUrl: "${createLink(controller:'heatmap',action:'initheatmap')}",
				downloadTrialStudyUrl: "${createLink(controller:'trial', action:'downloadStudy')}",
				downloadTrialAnalysisUrl: "${createLink(controller:'trial', action:'downloadAnalysisTEA')}",
				downloadEaUrl: "${createLink(controller:'experimentAnalysis', action:'downloadAnalysis')}",
				downloadEaTEAUrl: "${createLink(controller:'experimentAnalysis', action:'downloadAnalysisTEA')}"
			};

			Ext.onReady(function(){
			    try {
			        document.execCommand("BackgroundImageCache", false, true);
			    } catch(err) {}

				var picklist = new Ext.app.PickList({
					id: "categories",
					storeUrl: "${createLink([controller:'search',action:'loadCategories'])}",
					renderTo: "search-categories",
					label: "Category:&nbsp;",
					disabledClass: "picklist-disabled",
					onSelect: function(record) {
				        var combo = Ext.getCmp("search-combobox");
				        combo.focus();
				        if ((record.id != "all") || (record.id == "all" && combo.getRawValue().length > 0)) {
							combo.doQuery(combo.getRawValue(), true);
				        }
					}
				});

				var combo = new Ext.app.SearchComboBox({
					id: "search-combobox",
					renderTo: "search-text",
					searchUrl: "${createLink([action:'loadSearch',controller:'search'])}",
					submitUrl: "${createLink([action:'newSearch',controller:'search'])}",
					submitFn: function(param, text) {
						var combo = Ext.getCmp("search-combobox");
						combo.setDisabled(true);
						combo.setRawValue("Searching for " + text + "...");
						var searchbtn = document.getElementById("search-button");
						searchbtn.disabled = true;
						var picklist = Ext.getCmp("categories");
						picklist.setDisabled(true);
						var linkbuttons = document.getElementById("linkbuttons-div");
						linkbuttons.innerHTML = '<span style="color:#a0a0a0;font-size:11px;text-decoration:underline;">browse<br />saved filters</span>';
						var idfield = document.getElementById("id-field");
						idfield.value = param;
						setTimeout("postSubmit();", 100);
						document.form.submit();
					},
					value: "${session?.searchFilter?.searchText}",
					width: 470,
			        onSelect: function(record) {
						this.collapse();
						if (record != null) {
							this.submitFn(record.data.id, record.data.keyword);
						}
					},
			        listeners: {
						"beforequery": {
							fn: function(queryEvent) {
					            var picklist = Ext.getCmp("categories");
					            var rec = picklist.getSelectedRecord();
								if (rec != null) {
									queryEvent.query = rec.id + ":" + queryEvent.query;
								}
							},
							scope: this
						}
			        }
				});

				var win = new Ext.app.EditFiltersWindow({
					id: "editfilters-window",
					loadUrl: "${createLink([action:'loadCurrentFilters',controller:'search'])}",
					splitUrl: "${createLink([action:'loadPathwayFilters',controller:'search'])}",
					searchUrl: "${createLink([action:'loadSearch',controller:'search'])}",
					submitUrl: "${createLink([action:'searchEdit',controller:'search'])}",
					categoriesUrl: "${createLink([controller:'search',action:'loadCategories'])}"
				});

				// build search tabs and toolbar
				var tabpanel = createMainTabPanel();
				if (pageData.hideInternal == true)  {
				    tabpanel.remove(Ext.getCmp("tab1"));
				    tabpanel.remove(Ext.getCmp("tab3"));
				
				} else  {
						       
				}
				
			    // set active tab
			    tabpanel.activate(getActiveTab("${session.searchFilter.acttabname()}"));

	            var helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
	            var contact = '${grailsApplication.config.com.recomdata.searchtool.contactUs}';
	            var appTitle = '${grailsApplication.config.com.recomdata.searchtool.appTitle}';
	            var buildVer = 'Build Version: <g:meta name="environment.BUILD_NUMBER"/> - <g:meta name="environment.BUILD_ID"/>';
			    
				var viewport = new Ext.Viewport({
					    layout: "border",
					    items: [new Ext.Panel({						    
					        region: "north",
						    autoHeight: true,
						    tbar: createUtilitiesMenu(helpURL, contact, appTitle,'${request.getContextPath()}', buildVer, 'utilities-div'), 
						    contentEl: "header-div"				
						}),
			            new Ext.Panel({
				            layout: "fit",
				            region: "center",
				            items: [ tabpanel ]
			            })
			         ]
				});
			});

			var delayedTask = new Ext.util.DelayedTask();

			function searchOnClick() {
				var combo = Ext.getCmp("search-combobox");
				var param = combo.getSelectedParam();
				if (param != null) {
					combo.submitFn(param, param);
				}
			}

			function postSubmit() {
				var searchcombo = document.getElementById("search-combobox");
				searchcombo.className += " searchcombobox-disabled";
				searchcombo.style.width = "442px";
			}

			function removeFilter(id) {
				window.location = String.format("${createLink([controller:'search',action:'remove'])}" + "?id={0}", id);
			}

			function splitFilter(id) {
			}

			function getActiveTab(sourceName){
			var tab = 0;
			// normal tab
				if(sourceName=="trial")
					tab =0;
				else if(sourceName=="pretrial")
					tab = 1;
				else if(sourceName =="profile")
					tab = 2;
				else if(sourceName =="doc")
					tab =3;
				else
					tab =5;
				// outside user tab
				<sec:ifAnyGranted roles="ROLE_PUBLIC_USER">
				if(sourceName=="trial")
					tab =-1;
				else if(sourceName=="pretrial")
					tab = 0;
				else if(sourceName =="profile")
					tab = 1;			
				else if(sourceName =="doc")
					tab =3;
				else
					tab =0;
				</sec:ifAnyGranted>
				return tab;
			}

			</script>
		<title>${grailsApplication.config.com.recomdata.searchtool.appTitle}</title>
		<!-- ************************************** -->
	    <!-- This implements the Help functionality -->
	    <script type="text/javascript" src="${resource(dir:'js', file:'help/D2H_ctxt.js')}"></script>
	    <script language="javascript">
	    	helpURL = '${grailsApplication.config.com.recomdata.searchtool.adminHelpURL}';
	    </script>
		<!-- ************************************** --> 
	</head>
	<body>
		<div id="header-div" style="overflow:hidden; margin-bottom: 2px;">
			<g:render template="/layouts/commonheader" model="['app':'search']" />
			<g:render template="/layouts/searchheader" model="['app':'search']" />
			<div id="summarycount-div" style="background:#dfe8f6; color:#000; padding:5px 10px 5px 10px;border-top:1px solid #36c;">
				<span id="summarycount-span" style="font-size:13px; font-weight:bold;">
					About ${searchresult?.totalCount()} results found
				</span>
			</div>
			<div id="summary-div" style="padding:5px 10px 5px 10px;font-size:12px;line-height:17px;">
				<b>Filters:</b>&nbsp;${session?.searchFilter?.summaryWithLinks}
				&nbsp;<a class="tiny" style="text-decoration:underline;color:blue;font-size:11px;"
					href="#" onclick="var win=Ext.getCmp('editfilters-window');win.show();return false;">advanced</a>
				&nbsp;<a class="tiny" style="text-decoration:underline;color:blue;font-size:11px;"
					href="${createLink(controller:'customFilter', action:'create')}">save</a>
				&nbsp;<a class="tiny" style="text-decoration:underline;color:blue;font-size:11px;"
					href="${createLink(controller:'search', action:'index')}">clear all</a>
			</div>
			<g:form controller="geneExprAnalysis" name="globalfilter-form" id="globalfilter-form" action="doSearch">
				<input type="hidden" name="selectedpath" value="">
			</g:form>
		</div>
	</body>
</html>