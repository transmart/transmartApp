/**
 * @author JIsikoff
 *
 */
package command

import grails.validation.Validateable

@Validateable
public class UserGroupCommand {
    String[] userstoadd
    String[] userstoremove
    String[] groupstoadd
    String[] groupstoremove
    String searchtext
}
