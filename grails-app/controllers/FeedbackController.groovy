/*************************************************************************
 * tranSMART - translational medicine data mart
 *
 * Copyright 2008-2012 Janssen Research & Development, LLC.
 *
 * This product includes software developed at Janssen Research & Development, LLC.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License 
 * as published by the Free Software  * Foundation, either version 3 of the License, or (at your option) any later version, along with the following terms:
 * 1.	You may convey a work based on this program in accordance with section 5, provided that you retain the above notices.
 * 2.	You may convey verbatim copies of this program code as you receive it, in any medium, provided that you retain the above notices.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS    * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *
 ******************************************************************/


import org.transmart.searchapp.Feedback

class FeedbackController {

    def index = { redirect(action: list, params: params) }

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
            redirect(action: list)
        } else {
            return [feedback: feedback]
        }
    }

    def delete = {
        def feedback = Feedback.get(params.id)
        if (feedback) {
            feedback.delete()
            flash.message = "Feedback ${params.id} deleted"
            redirect(action: list)
        } else {
            flash.message = "Feedback not found with id ${params.id}"
            redirect(action: list)
        }
    }

    def edit = {
        def feedback = Feedback.get(params.id)

        if (!feedback) {
            flash.message = "Feedback not found with id ${params.id}"
            redirect(action: list)
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
                redirect(action: show, id: feedback.id)
            } else {
                render(view: 'edit', model: [feedback: feedback])
            }
        } else {
            flash.message = "Feedback not found with id ${params.id}"
            redirect(action: edit, id: params.id)
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
            redirect(action: show, id: feedback.id)
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
