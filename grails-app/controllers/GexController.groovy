import com.recomdata.gex.GexDao;

class GexController {

    //This is an example, Will remove this in production.
    def getData =
            {
                def dataRetrieve = new GexDao();

                dataRetrieve.getData("C:\\Users\\mmcduffie\\Desktop\\test.gex", ["subset1": "13278"], false, "", "", "941390");

                render "HELLO WURLD"
            }
}
