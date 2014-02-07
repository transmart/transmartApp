/**
 * Created with IntelliJ IDEA.
 * User: sbedard
 * Date: 8/8/13
 * Time: 11:03 AM
 * To change this template use File | Settings | File Templates.
 */
class TransmartQueryItem {

    String itemKey
    String concept_cd
    String linkType

    String getFullName()
    {
        return itemKey.substring(itemKey.indexOf("\\",2), itemKey.length())
    }
}
