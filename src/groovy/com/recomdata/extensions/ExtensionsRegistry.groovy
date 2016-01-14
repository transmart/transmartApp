package com.recomdata.extensions

/**
 * Date: 14-Jan-16
 * Time: 18:08
 */
class ExtensionsRegistry {
    def analysisTabExtensions = []

    void registerAnalysisTabExtension(Map<String, Object> config = [:], String extensionId, String resourcesUrl, String bootstrapFunction) {
        analysisTabExtensions.add([extensionId: extensionId, resourcesUrl: resourcesUrl, bootstrapFunction: bootstrapFunction, config: config])
    }
}
