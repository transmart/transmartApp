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
        dependsOn 'jquery', 'jqueryui', 'session_timeout_nodep'
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

    jqueryui {
        dependsOn 'jquery'

        resource url: '/js/jQuery/jquery-ui-1.8.17.custom.min.js'
        resource url: '/css/jQueryUI/smoothness/jquery-ui-1.8.17.custom.css'
    }
}
