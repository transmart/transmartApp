package org.transmartproject.app.user

import grails.validation.Validateable
import org.springframework.web.servlet.support.RequestContextUtils
import org.transmart.searchapp.AuthUser

class ChangeMyPasswordController {

    static Map allowedMethods = [save: 'POST']
    static defaultAction = "show"

    def messageSource
    def springSecurityService

    def show = {}

    def save(ChangePasswordCommand command) {
        if (command.hasErrors()) {
            render(view: 'show', model: [command: command])
        } else {
            AuthUser currentUser = springSecurityService.currentUser
            currentUser.passwd = springSecurityService.encodePassword(command.newPassword)
            currentUser.changePassword = false
            currentUser.save(flush: true)

            if (currentUser.hasErrors()) {
                command.errors.reject('ChangePassword.couldNotSave')
                render(view: 'show', model: [command: command])
            } else {
                flash.message = messageSource.getMessage('ChangePassword.savedSuccessfully',
                        new Objects[0], RequestContextUtils.getLocale(request))
                redirect(action: 'show')
            }
        }
    }

}

@Validateable
class ChangePasswordCommand {

    def grailsApplication
    def springSecurityService

    String oldPassword
    String newPassword
    String newPasswordRepeated

    static constraints = {

        oldPassword(blank: false, validator: { oldPsw, thisCmd ->
            if (!thisCmd.springSecurityService.passwordEncoder
                    .isPasswordValid(thisCmd.springSecurityService.currentUser.getPersistentValue('passwd'), oldPsw, null)) {
                'doesNotMatch'
            }
        })

        newPassword(blank: false,
                validator: { newPsw, thisCmd ->
            if (newPsw == thisCmd.oldPassword) {
                'hasToBeChanged'
            } else if (thisCmd.grailsApplication.config.user.password.strength.regex.with { it && !(newPsw ==~ it)}) {
                'lowPasswordStrength'
            }
        })

        newPasswordRepeated(blank: false, validator: { newPsw2, thisCmd ->
            if (newPsw2 != thisCmd.newPassword) {
                'doesNotEqual'
            }
        })

    }

}
