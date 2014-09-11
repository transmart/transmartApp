package org.transmart.authorization

/**
 * Created by glopes on 9/5/14.
 */
class SpringSecurityProxyMetaClassInterceptor implements PropertyAccessInterceptor {
    String username

    Object beforeInvoke(Object object, String methodName, Object[] arguments) {}

    Object afterInvoke(Object object,
                       String methodName,
                       Object[] arguments,
                       Object result) {
        if (methodName == 'isLoggedIn') {
            return true
        }
        if (methodName == 'getPrincipal') {
            return beforeGet(object, 'principal')
        }
        throw new UnsupportedOperationException(
                "Unexpected method call: $methodName($arguments)")
    }

    boolean doInvoke() {
        false
    }

    Object beforeGet(Object object, String property) {
        if (property == 'principal') {
            return [username: username]
        }
        throw new UnsupportedOperationException(
                "Unexpected property requested: $property")
    }

    void beforeSet(Object object, String property, Object newValue) {
        throw new UnsupportedOperationException(
                "Unexpected property setting requested: $property, " +
                        "value $newValue")
    }
}
