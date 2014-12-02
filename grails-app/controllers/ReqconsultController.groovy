class ReqconsultController {

    def defaultAction = "newrequest"
    def newrequest = {}

    def saverequest = {

        render(view: 'saverequest', model: ['reqtext': params.consulttext])

    }
}
