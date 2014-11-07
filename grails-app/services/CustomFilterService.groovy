import org.transmart.searchapp.CustomFilter


/*
 * $Id: CustomFilterService.groovy 9178 2011-08-24 13:50:06Z mmcduffie $
 */

/**
 * @author $Author: mmcduffie $
 * @version $Revision: 9178 $
 */
class CustomFilterService {

    def queryCustomFilters(searchUserId) {

        return CustomFilter.findAllBySearchUserId(Long.valueOf(searchUserId))

    }

    def queryCustomFilter(id) {

        return CustomFilter.findAllById(Long.valueOf(id))

    }

    def saveCustomFilter(customFilter) {

        CustomFilter.save();

    }

}
