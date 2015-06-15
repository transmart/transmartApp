import grails.util.Environment

def envSwitch = { devValue, otherValue ->
    Environment.current == Environment.DEVELOPMENT ? devValue : otherValue
}

modules = {
    main_mod {
        dependsOn 'jquery-ui'
        resource url: 'images/searchtool.ico'
        resource url: 'css/main.css'
    }

    session_timeout {
        dependsOn 'jquery-ui', 'session_timeout_nodep'
    }

    session_timeout_nodep {
        /* this variant is needed for legacy reasons. _commonheader is a
         * template that requires these JavaScript files, but it's rendered
         * after </head>. Therefore, we cannot include resources with
         * disposition 'head' at this point. Of course, jquery-ui better have
         * been included through some other mechanism. */
        resource url: 'js/jquery/jquery.idletimeout.js', disposition: 'head'
        resource url: 'js/jquery/jquery.idletimer.js', disposition: 'head'
        resource url: 'js/sessiontimeout.js', disposition: 'head'
    }

    extjs {
        resource url: 'js/ext/resources/css/ext-all.css'
        resource url: 'js/ext/resources/css/xtheme-gray.css'

        resource url: 'js/ext/adapter/ext/ext-base.js', disposition: 'head'
        resource url: 'js/ext/' + envSwitch('ext-all-debug.js', 'ext-all.js'), disposition: 'head'
        resource url: 'js/ext-ux/miframe.js', disposition: 'head'
    }
     
    overrides {
        'jquery-theme' {
            resource id:'theme', url:'css/jquery/ui/jquery-ui.min.css'
        }
    }
    
    'jquery-plugins' {
        dependsOn 'jquery', 'jquery-ui'

        resource url: 'js/jquery/jquery.migrate.js', disposition: 'head'
        resource url: 'js/jquery/jquery.tablesorter.js', disposition: 'head'
        resource url: 'js/jquery/jquery.cookie.js', disposition: 'head'
        resource url: 'js/jquery/jquery.dynatree.js', disposition: 'head'
        resource url: 'js/jquery/jquery.paging.js', disposition: 'head'
        resource url: 'js/jquery/jquery.loadmask.js', disposition: 'head'
        resource url: 'js/jquery/jquery.ajaxmanager.js', disposition: 'head'
        resource url: 'js/jquery/jquery.numeric.js', disposition: 'head'
        resource url: 'js/jquery/jquery.colorbox.js', disposition: 'head'
        resource url: 'js/jquery/jquery.simplemodal.js', disposition: 'head'
        resource url: 'js/jquery/jquery.dataTables.js', disposition: 'head'
        resource url: 'js/jquery/jquery.validate.js', disposition: 'head'
        resource url: 'js/jquery/ui.multiselect.js', disposition: 'head'

        resource url: 'css/jquery.loadmask.css'
        resource url: 'css/jquery/multiselect/ui.multiselect.css'
        resource url: 'css/jquery/skin/ui.dynatree.css'
    }

    browseTab {
        dependsOn 'jquery', 'jquery-ui', 'jquery-plugins', 'extjs', 'session_timeout'

        resource url: 'js/facetedSearch/facetedSearchBrowse.js', disposition: 'head'
        resource url: 'js/ColVis.min.js', disposition: 'head'
        resource url: 'js/ColReorderWithResize.js', disposition: 'head'
        resource url: 'js/rwg.js', disposition: 'head'
        resource url: 'js/rwgsearch.js', disposition: 'head'
        resource url: 'js/maintabpanel.js', disposition: 'head'

        resource url: 'css/main.css'
        resource url: 'css/sanofi.css'
        resource url: 'css/rwg.css'
        resource url: 'css/colorbox.css'
        resource url: 'css/jquery/jqueryDatatable.css'
    }

    analyseTab {
        dependsOn 'jquery', 'jquery-ui', 'jquery-plugins', 'extjs', 'session_timeout'

        resource url: 'js/advancedWorkflowFunctions.js', disposition: 'head'
        resource url: 'js/ajax_queue.js', disposition: 'head'
        resource url: 'js/fixconsole.js', disposition: 'head'
        resource url: 'js/myJobs.js', disposition: 'head'
        resource url: 'js/rwgsearch.js', disposition: 'head'
        resource url: 'js/utilitiesMenu.js', disposition: 'head'
        resource url: 'js/datasetExplorer/datasetExplorer.js', disposition: 'head'
        resource url: 'js/datasetExplorer/datasetExplorerLaunchers.js', disposition: 'head'
        resource url: 'js/datasetExplorer/ext-i2b2.js', disposition: 'head'
        resource url: 'js/datasetExplorer/gridView.js', disposition: 'head'
        resource url: 'js/datasetExplorer/highDimensionData.js', disposition: 'head'
        resource url: 'js/datasetExplorer/i2b2common.js', disposition: 'head'
        resource url: 'js/datasetExplorer/querypanels.js', disposition: 'head'
        resource url: 'js/datasetExplorer/reports.js', disposition: 'head'
        resource url: 'js/datasetExplorer/requests.js', disposition: 'head'
        resource url: 'js/datasetExplorer/sampleQuery.js', disposition: 'head'
        resource url: 'js/datasetExplorer/workflowStatus.js', disposition: 'head'
        resource url: 'js/datasetExplorer/workspace.js', disposition: 'head'
        resource url: 'js/datasetExplorer/modifiers.js', disposition: 'head'
        resource url: 'js/datasetExplorer/exportData/dataTab.js', disposition: 'head'
        resource url: 'js/datasetExplorer/exportData/exportJobsTab.js', disposition: 'head'
        resource url: 'js/facetedSearch/facetedSearchBrowse.js', disposition: 'head'
        resource url: 'js/utils/dynamicLoad.js', disposition: 'head'

        // Adding these validation functions to get the Forest Plot to work.
        // These might be able to be blended into the javascript object that controls the advanced workflow validation
        resource url: 'js/datasetExplorer/workflowValidationFunctions.js', disposition: 'head'

        resource url: 'css/datasetExplorer.css'
        resource url: 'css/querypanels.css'
        resource url: 'css/sanofi.css'
    }

    sampleTab {
        dependsOn 'jquery', 'jquery-ui', 'jquery-plugins', 'extjs', 'session_timeout'

        resource url: 'js/myJobs.js', disposition: 'head'
        resource url: 'js/sampleExplorer/sampleExplorerMenu.js', disposition: 'head'
        resource url: 'js/sampleExplorer/sampleExplorer.js', disposition: 'head'
        resource url: 'js/sampleExplorer/sampleExplorer.utilities.js', disposition: 'head'
        resource url: 'js/utilitiesMenu.js', disposition: 'head'

        resource url: 'css/main.css'
        resource url: 'css/sanofi.css'
        resource url: 'css/sampleExplorer.css'
    }

    signatureTab {
        dependsOn 'jquery', 'jquery-ui', 'jquery-plugins', 'extjs', 'session_timeout'

        resource url: 'js/maintabpanel.js', disposition: 'head'
        resource url: 'js/toggle.js', disposition: 'head'

        resource url: 'css/main.css'
        resource url: 'css/sanofi.css'
        resource url: 'css/genesignature.css'
    }
}
