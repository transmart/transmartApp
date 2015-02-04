class NewsUpdateController {

    def listDetailed =
            {
                //Render the screen of detailed information about this update.
                render(template: "listDetailed", model: [thisUpdate: NewsUpdate.get(params.id)]);
            }

}
