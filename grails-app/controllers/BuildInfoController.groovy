import grails.util.Holders

class BuildInfoController {
 
    static final List buildInfoProperties = [
            'scm.version',
            'build.date',
            'build.timezone',
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
        if (buildInfoConfig?.properties?.add){
            customProperties += buildInfoConfig.properties.add
        }

        Map model = [buildInfoProperties: customProperties]
        render(view:'index', model:model)
    }
}
