package org.transmart.authorization

interface AuthorizationDecorator<T> {

    void setDelegate(T delegate)

}
