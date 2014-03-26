package org.transmart.marshallers

import org.transmartproject.core.querytool.QueryResult

class QueryResultMarshaller extends SuperTypePropertiesMarshallerSupport {
    static targetType = QueryResult

    def convert(QueryResult queryResult) {
        getPropertySubsetForSuperType(queryResult, QueryResult)
    }
}
