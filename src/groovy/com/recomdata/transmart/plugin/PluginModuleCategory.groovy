package com.recomdata.transmart.plugin

enum PluginModuleCategory {
    DEFAULT("Default"), HEATMAP("Heatmap")
    final String value

    PluginModuleCategory(String value) { this.value = value }

    String toString() { value }

    String getKey() { name() }
}
