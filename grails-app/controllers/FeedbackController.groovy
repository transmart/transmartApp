import org.transmart.searchapp.Feedback

class FeedbackController {

    def index = { redirect(action: "list", params: params) }

    // the delete, save and update actions only accept POST requests
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def list = {
        if (!params.max) params.max = 10
        [feedbackList: Feedback.list(params)]
    }

    def show = {
        def feedback = Feedback.get(params.id)

        if (!feedback) {
            flash.message = "Feedback not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [feedback: feedback]
        }
    }

    def delete = {
        def feedback = Feedback.get(params.id)
        if (feedback) {
            feedback.delete()
            flash.message = "Feedback ${params.id} deleted"
            redirect(action: "list")
        } else {
            flash.message = "Feedback not found with id ${params.id}"
            redirect(action: "list")
        }
    }

    def edit = {
        def feedback = Feedback.get(params.id)

        if (!feedback) {
            flash.message = "Feedback not found with id ${params.id}"
            redirect(action: "list")
        } else {
            return [feedback: feedback]
        }
    }

    def update = {
        def feedback = Feedback.get(params.id)
        if (feedback) {
            feedback.properties = params
            if (!feedback.hasErrors() && feedback.save()) {
                flash.message = "Feedback ${params.id} updated"
                redirect(action: "show", id: feedback.id)
            } else {
                render(view: 'edit', model: [feedback: feedback])
            }
        } else {
            flash.message = "Feedback not found with id ${params.id}"
            redirect(action: "edit", id: params.id)
        }
    }

    def create = {
        def feedback = new Feedback()
        feedback.properties = params
        return ['feedback': feedback]
    }

    def save = {
        def feedback = new Feedback(params)
        if (!feedback.hasErrors() && feedback.save()) {
            flash.message = "Feedback ${feedback.id} created"
            redirect(action: "show", id: feedback.id)
        } else {
            render(view: 'create', model: [feedback: feedback])
        }
    }

    def saveFeedback = {
        //	println("Save feedback called with: "+params)
        def fb = new Feedback()
        //fb.appUser="demo"
        fb.searchUserId = 1
        fb.createDate = new java.util.Date()
        fb.appVersion = "prototype"
        fb.feedbackText = params.feedbacktext
        fb.save()
        render(template: "emptyTemplate")
    }
}
