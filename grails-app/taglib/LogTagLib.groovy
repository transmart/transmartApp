
class LogTagLib {
    /**
     * Allows gsp pages to include logging tags
     *
     * <g:logMsg>Any message with ${variables}</g:logMsg>
     *
     * also <g:logMsg level="debug"> (default here is info) etc.
     *
     */
    def logMsg = { attrs, body ->
        def level = (attrs['level'] = attrs['level'] ?: 'info').toLowerCase()
        log."${level}"(body())
    }
}
    
            