import grails.util.Environment

def envSwitch = { devValue, otherValue ->
    Environment.current == Environment.DEVELOPMENT ? devValue : otherValue
}

modules = {
    main_mod {
        resource url: '/images/searchtool.ico'
        resource url: '/css/main.css'
    }

    session_timeout {
        dependsOn 'jqueryui', 'session_timeout_nodep'
    }

    session_timeout_nodep {
        /* this variant is needed for legacy reasons. _commonheader is a
         * template that requires these JavaScript files, but it's rendered
         * after </head>. Therefore, we cannot include resources with
         * disposition 'head' at this point. Of course, jquery-ui better have
         * been included through some other mechanism. */
        resource url: '/js/jquery/jquery.idletimeout.js'
        resource url: '/js/jquery/jquery.idletimer.js'
        resource url: '/js/sessiontimeout.js'
    }

    extjs {
        resource url: '/js/ext/resources/css/ext-all.css'
        resource url: '/js/ext/resources/css/xtheme-gray.css'

        resource url: '/js/ext/adapter/ext/ext-base.js', disposition: 'head'
        resource url: '/js/ext/' + envSwitch('ext-all-debug.js', 'ext-all.js'), disposition: 'head'
        resource url: '/js/ext-ux/miframe.js', disposition: 'head'
    }

    jqueryui {
        dependsOn 'jquery'

        resource url: '/js/jquery/jquery-ui-1.8.17.custom.min.js', disposition: 'head'
        resource url: '/css/jQueryUI/smoothness/jquery-ui-1.8.17.custom.css'
    }
    
    'jquery-plugins' {
        dependsOn 'jquery', 'jqueryui'

        resource url: '/js/jquery/jquery.migrate.js', disposition: 'head'
        resource url: '/js/jquery/jquery.tablesorter.js', disposition: 'head'
        resource url: '/js/jquery/jquery.cookie.js', disposition: 'head'
        resource url: '/js/jquery/jquery.dynatree.js', disposition: 'head'
        resource url: '/js/jquery/jquery.paging.js', disposition: 'head'
        resource url: '/js/jquery/jquery.loadmask.js', disposition: 'head'
        resource url: '/js/jquery/jquery.ajaxmanager.js', disposition: 'head'
        resource url: '/js/jquery/jquery.numeric.js', disposition: 'head'
        resource url: '/js/jquery/jquery.colorbox.js', disposition: 'head'
        resource url: '/js/jquery/jquery.simplemodal.js', disposition: 'head'
        resource url: '/js/jquery/jquery.dataTables.js', disposition: 'head'
    }

    datasetExplorer {
        dependsOn 'jquery', 'jqueryui', 'jquery-plugins', 'extjs'

        resource url: '/js/advancedWorkflowFunctions.js', disposition: 'head'
        resource url: '/js/ajax_queue.js', disposition: 'head'
        resource url: '/js/fixconsole.js', disposition: 'head'
        resource url: '/js/myJobs.js', disposition: 'head'
        resource url: '/js/rwgsearch.js', disposition: 'head'
        resource url: '/js/utilitiesMenu.js', disposition: 'head'
        resource url: '/js/datasetExplorer/datasetExplorer.js', disposition: 'head'
        resource url: '/js/datasetExplorer/datasetExplorerLaunchers.js', disposition: 'head'
        resource url: '/js/datasetExplorer/ext-i2b2.js', disposition: 'head'
        resource url: '/js/datasetExplorer/gridView.js', disposition: 'head'
        resource url: '/js/datasetExplorer/highDimensionData.js', disposition: 'head'
        resource url: '/js/datasetExplorer/highDimensionData.js', disposition: 'head'
        resource url: '/js/datasetExplorer/i2b2common.js', disposition: 'head'
        resource url: '/js/datasetExplorer/reports.js', disposition: 'head'
        resource url: '/js/datasetExplorer/requests.js', disposition: 'head'
        resource url: '/js/datasetExplorer/sampleQuery.js', disposition: 'head'
        resource url: '/js/datasetExplorer/workflowStatus.js', disposition: 'head'
        resource url: '/js/datasetExplorer/workspace.js', disposition: 'head'
        resource url: '/js/datasetExplorer/exportData/dataTab.js', disposition: 'head'
        resource url: '/js/datasetExplorer/exportData/exportJobsTab.js', disposition: 'head'
        resource url: '/js/facetedSearch/facetedSearchBrowse.js', disposition: 'head'
        resource url: '/js/galaxy/galaxyExport.js', disposition: 'head'
        resource url: '/js/jquery/ui.multiselect.js', disposition: 'head'
        resource url: '/js/utils/dynamicLoad.js', disposition: 'head'
        resource url: '/js/utils/json2.js', disposition: 'head'

        // Adding these validation functions to get the Forest Plot to work.
        // These might be able to be blended into the javascript object that controls the advanced workflow validation
        resource url: '/js/datasetExplorer/workflowValidationFunctions.js', disposition: 'head'

        resource url: '/css/datasetExplorer.css'
    }
}
