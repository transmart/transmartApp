package org.transmartproject.audit

import groovy.util.logging.Slf4j
import org.transmartproject.core.users.User;

/**
 * Logs to level TRACE in a specific `metric' format.
 * Configuration of output done in (out-of-tree) Config.groovy.
 *
 * @author gijs@thehyve.nl
 */
@Slf4j
class AuditLogService {

    User currentUserBean
    def grailsApplication

    def report(String controller, String action, Map params) {
        def result_instance_id1 = params.result_instance_id1
        def result_instance_id2 = params.result_instance_id2
        def concept_key = params.concept_key

        def task = controller
        def act = action
        switch(controller) {
            case 'chart':
                task = "Summary Statistics (${action})"
                act = "${concept_key}|${result_instance_id1}|${result_instance_id2}"
                break
        }
        def message = "${task}\t${act}\t${currentUserBean.username}\t${grailsApplication.metadata['app.version']}\t"
        log.trace message
    }
}
