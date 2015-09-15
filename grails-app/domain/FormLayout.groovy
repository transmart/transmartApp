public class FormLayout {

    Long id
    String key;
    String column;
    String displayName;
    String dataType;
    Integer sequence;
    Boolean display = true;

    static mapping = {
        table 'SEARCH_FORM_LAYOUT'
        id generator: 'sequence', params: [sequence: 'SEQ_SEARCH_FORM_LAYOUT_ID']
        version false
        id column: 'FORM_LAYOUT_ID'
        key column: 'FORM_KEY'
        column column: 'FORM_COLUMN'
        displayName column: 'DISPLAY_NAME'
        dataType column: 'DATA_TYPE'
        sequence column: 'SEQUENCE'
    }

    static constraints = {
        key(nullable: false)
        column(nullable: false)
        displayName(nullable: true)
        dataType(nullable: true)
        sequence(nullable: true)
    }

}
