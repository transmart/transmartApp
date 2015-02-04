public class RequiredUploadField {

    Long id
    String type;
    String field;

    static mapping = {
        table 'CZ_REQUIRED_UPLOAD_FIELD'
        version false
        id column: 'REQUIRED_UPLOAD_FIELD_ID'
        type column: 'TYPE'
        field column: 'FIELD'
    }

    static constraints = {
        type(nullable: false)
        field(nullable: false)
    }

}
