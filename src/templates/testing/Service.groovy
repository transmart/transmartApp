@ artifact.package
@ import grails.test.mixin.TestFor
@ import grails.test.mixin.TestFor
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */
@TestFor(@artifact.testclass @ )
class @artifact.name @ extends Specification {

    def setup() {
    }

    def cleanup() {
    }

    void "test something"() {
    }
}