package org.transmart.marshallers

import org.transmartproject.core.querytool.QueryResult

/**
 * This class is not a marshaller because there is already a marshaller for QueryResult in the rest api. The
 * conversion here is needed for the transmartApp GUI. Call this conversion method explicitly when you need it.
 */
class QueryResultConverter {

    static def convert(QueryResult queryResult) {
        getPropertySubsetForSuperType(queryResult, QueryResult, ['patients', 'username'])
    }

    /**
     * Support for exposing only the properties of some object that are
     * defined on a subtype of that object.
     * This method used to have its own class but now this is the only place where it is still used.
     */
    private static getPropertySubsetForSuperType(Object o,
                                                   Class superType,
                                                   List<String> excludes = []) {
        if (!superType.isAssignableFrom(o.getClass())) {
            throw new IllegalArgumentException("Object '$o' is not of type " +
                    "$superType")
        }

        superType.metaClass.properties.findAll {
            !(it.name in excludes)
        }.collectEntries { MetaBeanProperty prop ->
            [prop.name, prop.getProperty(o)]
        }
    }

}
