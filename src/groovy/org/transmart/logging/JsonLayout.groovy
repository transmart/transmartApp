package org.transmart.logging

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.apache.log4j.EnhancedPatternLayout
import org.apache.log4j.spi.LoggingEvent

@CompileStatic
class JsonLayout extends EnhancedPatternLayout {

    boolean singleLine = false
    String dateFormat = "yyyy-MM-dd HH:mm:ss.SSSX"
    boolean printNulls = true

    @Lazy volatile Gson gson = {
        GsonBuilder builder = new GsonBuilder().setDateFormat(dateFormat)
        if (!singleLine) builder.setPrettyPrinting()
        if (printNulls) builder.serializeNulls()
        builder.create()
    }()

    /*
     * We need to serialize the json structure to a string and then call the parent layout methods on that.
     * Unfortunately LoggingEvent.message is not accessible without reflection/CompileDynamic magic.
     *
     * Some alternatives are: trying to create a modified event, but LoggingEvent is not cloneable and also not easily
     * inheritable due to several private / final members. Another option is to override the pattern processing
     * machinery in EnhancedPatternLayout, but that code is not easily extendable for our use case without copying
     * large parts of it and/or some ugly hacks: the super constructor already parses the pattern and creates the
     * processor, which we then need to recreate after initializing a Gson and preparing a modified PatternConverter.
     * So this simple hack looks like the best option.
     */
    @CompileDynamic
    @Override
    String format(LoggingEvent event) {
        Object originalMessage = event.@message
        String origRenderedMessage = event.@renderedMessage
        try {
            event.@message = gson.toJson(originalMessage)
            return super.format(event)
        } finally {
            event.@message = originalMessage
            event.@renderedMessage = origRenderedMessage
        }
    }
}
