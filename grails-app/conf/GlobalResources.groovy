import grails.util.Environment

def envSwitch = { devValue, otherValue ->
    Environment.current == Environment.DEVELOPMENT ? devValue : otherValue
}

modules = {
    main_mod {
        resource url: '/images/searchtool.ico'
        resource url: '/css/main.css'
    }

	datasetExplorer {
		dependsOn 'jquery-ui', 'extjs'
		resource url: '/js/jQuery/jquery.tablesorter.min.js'
		resource url: '/js/jQuery/jquery.cookie.js'
		resource url: '/js/jQuery/jquery.dynatree.min.js'
		resource url: '/js/jQuery/jquery.paging.min.js'
		resource url: '/js/jQuery/jquery.loadmask.min.js'
		resource url: '/js/jQuery/jquery.ajaxmanager.js'
		resource url: '/js/jQuery/jquery.numeric.js'
		resource url: '/js/jQuery/jquery.colorbox-min.js'
		resource url: '/js/jQuery/jquery.simplemodal.min.js'
		resource url: '/js/jQuery/jquery.dataTables.js'
		resource url: '/js/facetedSearch/facetedSearchBrowse.js'
		resource url: '/js/jQuery/jquery.validate.min.js'
		resource url: '/js/jQuery/additional-methods.min.js'
		resource url: '/js/ajax_queue.js'
		resource url: '/js/ext-ux/miframe.js'
		resource url: '/js/datasetExplorer/i2b2common.js'
		resource url: '/js/datasetExplorer/requests.js'
		resource url: '/js/datasetExplorer/ext-i2b2.js'
		resource url: '/js/datasetExplorer/workflowStatus.js'
		resource url: '/js/myJobs.js'
		resource url: '/js/datasetExplorer/reports.js'
		resource url: '/js/datasetExplorer/workspace.js'
		resource url: '/js/datasetExplorer/exportData/dataTab.js'
		resource url: '/js/datasetExplorer/exportData/exportJobsTab.js'
		resource url: '/js/fixconsole.js'
		resource url: '/js/browserDetect.js'
		resource url: '/js/utils/json2.js'
		resource url: '/js/utils/dynamicLoad.js'
		resource url: '/js/datasetExplorer/highDimensionData.js'
		resource url: '/js/datasetExplorer/gridView.js'
		resource url: '/js/datasetExplorer/datasetExplorer.js'
		resource url: '/js/datasetExplorer/datasetExplorerLaunchers.js'
		resource url: '/js/datasetExplorer/sampleQuery.js'
		resource url: '/js/rwgsearch.js'
		resource url: '/js/advancedWorkflowFunctions.js'
		resource url: '/js/datasetExplorer/highDimensionData.js'
		resource url: '/js/Galaxy/galaxyExport.js'
		resource url: '/js/datasetExplorer/workflowValidationFunctions.js'
		resource url: '/js/jQuery/ui.multiselect.js'
	}
	
    session_timeout {
        dependsOn 'jquery', 'jquery-ui', 'session_timeout_nodep'
    }

    session_timeout_nodep {
        /* this variant is needed for legacy reasons. _commonheader is a
         * template that requires these JavaScript files, but it's rendered
         * after </head>. Therefore, we cannot include resources with
         * disposition 'head' at this point. Of course, jquery-ui better have
         * been included through some other mechanism. */
        resource url: '/js/jQuery/jquery.idletimeout.js'
        resource url: '/js/jQuery/jquery.idletimer.js'
        resource url: '/js/sessiontimeout.js'
    }

    extjs {
        resource url: '/js/ext/resources/css/ext-all.css'
        resource url: '/js/ext/resources/css/xtheme-gray.css'

        resource url: '/js/ext/adapter/ext/ext-base.js'
        resource url: '/js/ext/' + envSwitch('ext-all-debug.js', 'ext-all.js')
    }
	
	overrides {
		'jquery-theme' {
			resource id:'theme', url:'/css/jquery/ui/jquery-ui-1.9.1.custom.css'
		}
	}
}
