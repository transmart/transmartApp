import grails.util.Holders

class BuildInfoController {
 
    static final List buildInfoProperties = [
            'scm.version',
            'build.date',
            'build.timezone',
            'build.java',
            'env.os',
            'env.username',
            'env.computer',
            'env.proc.type',
            'env.proc.cores'
    ]

    def index = { 
        def buildInfoConfig = Holders.config?.buildInfo
        def customProperties = buildInfoProperties
        if (buildInfoConfig?.properties?.exclude){
            customProperties -= buildInfoConfig.properties.exclude
        }
        if (buildInfoConfig?.properties?.include){
            customProperties += buildInfoConfig.properties.include
        }

        Map model = [buildInfoProperties: customProperties.sort()]
        render(view:'index', model:model)
    }
}
