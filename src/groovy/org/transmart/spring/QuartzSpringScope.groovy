package org.transmart.spring

import groovy.util.logging.Log4j
import org.springframework.beans.factory.ObjectFactory
import org.springframework.beans.factory.config.Scope
import org.springframework.core.NamedThreadLocal

@Log4j
class QuartzSpringScope implements Scope {

    private static ThreadLocal<Map<String, Object>> SCOPE_MAP =
            new NamedThreadLocal<Map<String, Object>>("JobScope") {
                @Override
                protected Map<String, Object> initialValue() {
                    new HashMap<String, Object>()
                }
            };

    void setProperty(String name, Object value) {
        SCOPE_MAP.get().put name, value
    }

    void clear() {
        SCOPE_MAP.remove()
    }

    @Override
    Object get(String name, ObjectFactory<?> objectFactory) {
        /* the only way beans are added to this scope is via
         * setProperty. We refuse to create beans */
        def ret
        if ((ret = SCOPE_MAP.get().get(name))) {
            return ret
        }

        throw new IllegalStateException("No bean named '$name' " +
                "has bean submitted to this scope. This scope does " +
                "not create beans")
    }

    @Override
    Object remove(String name) {
        throw new UnsupportedOperationException("Scope doesn't support removal")
    }

    @Override
    void registerDestructionCallback(String name, Runnable callback) {
        log.warn "Destruction callbacks are not supported; tried to add one " +
                "for $name"
    }

    @Override
    Object resolveContextualObject(String key) {
        null
    }

    @Override
    String getConversationId() {
        null
    }
}
