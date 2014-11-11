/**
 * Central help  controller
 * $Id: HelpController.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class HelpController {
    def index = {
        render(view: 'guide');
    }
}
