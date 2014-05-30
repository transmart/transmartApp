package org.transmart.authorization

import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.SpringSecurityUtils
import org.springframework.beans.factory.FactoryBean
import org.springframework.beans.factory.annotation.Autowired
import org.transmartproject.core.users.User
import org.transmartproject.core.users.UsersResource

import javax.annotation.PostConstruct

/**
 * Should be request scoped!
 */
class CurrentUserBeanFactoryBean implements FactoryBean<User> {

    @Autowired
    SpringSecurityService springSecurityService

    @Autowired
    UsersResource usersResource

    final boolean singleton = true

    final Class<?> objectType = User

    private User user

    @PostConstruct
    void fetchUser() {
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

        user = usersResource.getUserFromUsername(username)
    }

    @Override
    User getObject() throws Exception {
        user
    }
}
