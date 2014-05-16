package org.transmart.authorization

import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.exceptions.AccessDeniedException
import org.transmartproject.core.exceptions.InvalidRequestException
import org.transmartproject.core.exceptions.NoSuchResourceException
import org.transmartproject.core.querytool.QueriesResource
import org.transmartproject.core.querytool.QueryDefinition
import org.transmartproject.core.querytool.QueryResult

import javax.annotation.Resource

import static org.transmartproject.core.users.ProtectedOperation.WellKnownOperations.BUILD_COHORT
import static org.transmartproject.core.users.ProtectedOperation.WellKnownOperations.READ

class QueriesResourceAuthorizationDecorator
        implements QueriesResource, AuthorizationDecorator<QueriesResource> {

    @Resource(name = CurrentUserBeanProxyFactory.BEAN_BAME)
    User user

    @Autowired
    QueriesResource delegate

    @Override
    QueryResult runQuery(QueryDefinition definition) throws InvalidRequestException {
        if (!user.canPerform(BUILD_COHORT, definition)) {
            throw new AccessDeniedException("Denied ${user.username} access " +
                    "for building cohort based on $definition")
        }

        delegate.runQuery definition
    }

    @Override
    QueryResult runQuery(QueryDefinition definition, String username) throws InvalidRequestException {
        if (!user.canPerform(BUILD_COHORT, definition)) {
            throw new AccessDeniedException("Denied ${user.username} access " +
                    "for building cohort based on $definition")
        }
        if (username != user.username) {
            throw new AccessDeniedException("Denied ${user.username} access " +
                    "to building a cohort in name of ${username}")
        }

        delegate.runQuery definition, username
    }

    @Override
    QueryResult getQueryResultFromId(Long id) throws NoSuchResourceException {
        def res = delegate.getQueryResultFromId id

        if (!user.canPerform(READ, res)) {
            throw new AccessDeniedException("Defined ${user.username} access " +
                    "to query result with id $id")
        }

        res
    }

    @Override
    QueryDefinition getQueryDefinitionForResult(QueryResult result) throws NoSuchResourceException {
        /* the gatekeeping is done when fetching the query result only.
         * Odd that this method is not in QueryResult anyway */
        delegate.getQueryDefinitionForResult(result)
    }
}
