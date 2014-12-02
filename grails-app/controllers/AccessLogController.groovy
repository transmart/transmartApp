import com.recomdata.util.ExcelGenerator
import com.recomdata.util.ExcelSheet
import org.transmart.AccessLogFilter
import org.transmart.searchapp.AccessLog

import java.text.SimpleDateFormat;

class AccessLogController {

    def searchService

    def index = { redirect(action: "list", params: params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def list = {

        def filter = session.accesslogFilter
        if (filter == null) {
            filter = new AccessLogFilter()
            session.accesslogFilter = filter
        }

        SimpleDateFormat df1 = new SimpleDateFormat("dd/MM/yyyy");
        GregorianCalendar calendar = new GregorianCalendar()

        try {
            if (filter.startdate == null || params.startdate != null)
                filter.startdate = df1.parse(params.startdate)
        } catch (e) {
            calendar.setTime(new Date())
            calendar.add(Calendar.WEEK_OF_MONTH, -1)
            filter.startdate = calendar.getTime()
        }

        try {
            if (filter.enddate == null || params.enddate != null) {
                calendar.setTime(df1.parse(params.enddate))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                filter.enddate = calendar.getTime()
            }
        } catch (e) {
            filter.enddate = new Date()
        }

        def pageMap = searchService.createPagingParamMap(params, grailsApplication.config.com.recomdata.admin.paginate.max, 0)
        pageMap['sort'] = 'accesstime'
        pageMap['order'] = 'desc'

        def result = AccessLog.createCriteria().list(
                max: pageMap['max'],
                offset: pageMap['offset'],
                sort: pageMap['sort'],
                order: pageMap['order']) {
            between "accesstime", filter.startdate, filter.enddate
        }

        render(view: 'list', model: [accessLogInstanceList: result, startdate: df1.format(filter.startdate), enddate: df1.format(filter.enddate), totalcount: result.totalCount])
    }

    def export = {

        def filter = session.accesslogFilter
        if (filter == null) {
            filter = new AccessLogFilter()
            session.accesslogFilter = filter
        }

        SimpleDateFormat df1 = new SimpleDateFormat("dd/MM/yyyy");
        GregorianCalendar calendar = new GregorianCalendar()

        try {
            if (filter.startdate == null || params.startdate != null)
                filter.startdate = df1.parse(params.startdate)
        } catch (e) {
            calendar.setTime(new Date())
            calendar.add(Calendar.WEEK_OF_MONTH, -1)
            filter.startdate = calendar.getTime()
        }

        try {
            if (filter.enddate == null || params.enddate != null) {
                calendar.setTime(df1.parse(params.enddate))
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                filter.enddate = calendar.getTime()
            }
        } catch (e) {
            filter.enddate = new Date()
        }

        def pageMap = searchService.createPagingParamMap(params, grailsApplication.config.com.recomdata.search.paginate.max, 0)
        pageMap['sort'] = 'accesstime'
        pageMap['order'] = 'desc'

        def results = AccessLog.createCriteria().list(
                sort: pageMap['sort'],
                order: pageMap['order']) {
            between "accesstime", filter.startdate, filter.enddate
        }
        def headers = ["Access Time", "User", "Event", "Event Message"]
        def values = []

        results.each {
            values.add([it.accesstime, it.username, it.event, it.eventmessage])
        }

        def sheet = new ExcelSheet("sheet1", headers, values);
        def gen = new ExcelGenerator()

        response.setHeader("Content-Type", "application/vnd.ms-excel; charset=utf-8")
        response.setHeader("Content-Disposition", "attachment; filename=\"pre_clinical.xls\"")
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0")
        response.setHeader("Pragma", "public");
        response.setHeader("Expires", "0");

        response.outputStream << gen.generateExcel([sheet]);
    }

    def show = {
        def accessLogInstance = AccessLog.get(params.id)

        if (!accessLogInstance) {
            flash.message = "AccessLog not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [accessLogInstance: accessLogInstance]
        }
    }

    def delete = {
        def accessLogInstance = AccessLog.get(params.id)
        if (accessLogInstance) {
            accessLogInstance.delete()
            flash.message = "AccessLog ${params.id} deleted"
            redirect(action: "list")
        } else {
            flash.message = "AccessLog not found with id ${params.id}"
            redirect(action: "list")
        }
    }

    def edit = {
        def accessLogInstance = AccessLog.get(params.id)

        if (!accessLogInstance) {
            flash.message = "AccessLog not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [accessLogInstance: accessLogInstance]
        }
    }

    def update = {
        def accessLogInstance = AccessLog.get(params.id)
        if (accessLogInstance) {
            accessLogInstance.properties = params
            if (!accessLogInstance.hasErrors() && accessLogInstance.save()) {
                flash.message = "AccessLog ${params.id} updated"
                redirect(action: "show", id: accessLogInstance.id)
            } else {
                render(view: 'edit', model: [accessLogInstance: accessLogInstance])
            }
        } else {
            flash.message = "AccessLog not found with id ${params.id}"
            redirect(action: "edit", id: params.id)
        }
    }

    def create = {
        def accessLogInstance = new AccessLog()
        accessLogInstance.properties = params
        return ['accessLogInstance': accessLogInstance]
    }

    def save = {
        def accessLogInstance = new AccessLog(params)
        if (!accessLogInstance.hasErrors() && accessLogInstance.save()) {
            flash.message = "AccessLog ${accessLogInstance.id} created"
            redirect(action: "show", id: accessLogInstance.id)
        } else {
            render(view: 'create', model: [accessLogInstance: accessLogInstance])
        }
    }
}
