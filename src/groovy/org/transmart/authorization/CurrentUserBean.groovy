package org.transmart.authorization

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.users.ProtectedOperation
import org.transmartproject.core.users.ProtectedResource
import org.transmartproject.core.users.User
import org.transmartproject.core.users.UsersResource

/**
 * Bean that can be inject to get the current {@link User}.
 *
 * Should be request scoped. Wrap it in a proxy so it can be injected in
 * singletons.
 */
class CurrentUserBean implements User {

    @Autowired
    SpringSecurityService springSecurityService

    @Autowired
    UsersResource usersResource

    @Lazy
    User user = {
        if (springSecurityService == null) {
            throw new IllegalStateException('springSecurityService not injected')
        }

        if (!SpringSecurityUtils.securityConfig.active) {
            throw new IllegalStateException('springSecurityService not active')
        }

        if (!springSecurityService.isLoggedIn()) {
            throw new IllegalStateException('User is not logged in')
        }

        def username = springSecurityService.principal.username
        usersResource.getUserFromUsername(username)
    }()

    @Override
    Long getId() {
        user.id
    }

    @Override
    String getUsername() {
        user.username
    }

    @Override
    String getRealName() {
        user.username
    }

    @Override
    boolean canPerform(ProtectedOperation operation, ProtectedResource protectedResource) {
        user.canPerform(operation, protectedResource)
    }

}
