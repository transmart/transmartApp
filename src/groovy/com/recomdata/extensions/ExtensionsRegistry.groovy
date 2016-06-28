package com.recomdata.extensions

import groovy.transform.CompileStatic

/**
 * Date: 14-Jan-16
 * Time: 18:08
 */
class ExtensionsRegistry {
    def analysisTabExtensions = []
    List<Map<String, Object>> tabs = new LinkedList<>()

    ExtensionsRegistry() {
    }

    void registerAnalysisTabExtension(Map<String, Object> config = [:], String extensionId, String resourcesUrl, String bootstrapFunction) {
        analysisTabExtensions.add([extensionId: extensionId, resourcesUrl: resourcesUrl, bootstrapFunction: bootstrapFunction, config: config])
    }

    void registerTab(Map<String, Object> config = [:], String id, String title, String controller) {
        def tab = [id: id, title: title, controller: controller, insertBefore: config.insertBefore]
        findAndInsertBefore(tabs, tab) { Map<String, Object> it -> tab.id in it.insertBefore }
    }

    @CompileStatic
    private static <T> void findAndInsertBefore(List<T> items, T item, Closure<Boolean> closure) {
        for (def it = items.listIterator(); it.hasNext();) {
            if (closure(it.next())) {
                // Step back to be before current tab and insert the tab before that
                it.previous()
                it.add(item)
                return
            }
        }
        items.add(item)
    }

    /**
     * incorporateTab in existing tab list. If insertBefore option specified, then tries to found tab with specified id
     * and insert new tab before that
     */
    @CompileStatic
    private static void incorporateTab(LinkedList<Map<String, Object>> tabs, Map<String, Object> tab) {
        def insertBefore = tab.insertBefore
        if (insertBefore) {
            findAndInsertBefore(tabs, tab) { Map<String, Object> it -> it.id in insertBefore }
        } else {
            tabs << tab
        }
    }

    List<Map<String, Object>> getTabs(List<Map<String, Object>> defaultTabs = []) {
        List<Map<String, Object>> result = new LinkedList<>()
        result.addAll(defaultTabs)
        for (def tab : tabs) {
            incorporateTab(result, tab)
        }
        result
    }
}
