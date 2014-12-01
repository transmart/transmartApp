/**
 * @author JIsikoff
 *
 */
package command

import grails.validation.Validateable

@Validateable
public class SecureObjectAccessCommand {
    String[] sobjectstoadd
    String[] sobjectstoremove
    String[] groupstoadd
    String[] groupstoremove
    String accesslevelid
    String searchtext
}
