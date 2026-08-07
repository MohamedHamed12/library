package io.pillopl.library.lending.patron.model

import spock.lang.Specification
import spock.lang.Unroll

class EmailAddressTest extends Specification {

    def 'should construct a valid email address'() {
        expect:
            EmailAddress.of("patron@example.test").value() == "patron@example.test"
    }

    def 'should trim leading and trailing whitespace before storing normalized value'() {
        expect:
            EmailAddress.of("  patron@example.test  ").value() == "patron@example.test"
            EmailAddress.of("\tpatron@example.test\n").value() == "patron@example.test"
    }

    def 'should normalize to lowercase for whole-value comparison'() {
        expect:
            EmailAddress.of("Patron@Example.Test").value() == "patron@example.test"
    }

    def 'should have value-based equality and hash code'() {
        given:
            EmailAddress upperCase = EmailAddress.of("Patron@Example.Test")
            EmailAddress lowerCase = EmailAddress.of("patron@example.test")
        expect:
            upperCase == lowerCase
            upperCase.hashCode() == lowerCase.hashCode()
            upperCase != EmailAddress.of("other@example.test")
    }

    @Unroll
    def 'should reject invalid email "#email"'() {
        when:
            EmailAddress.of(email)
        then:
            thrown(InvalidEmailAddress)

        where:
            email                   | _
            null                    | _
            ""                      | _
            "   "                   | _
            "patron"                | _
            "patron@"               | _
            "@example.test"         | _
            "patron@example"        | _
            "patron@-example.test"  | _
            "patron.example.test"   | _
            "pa tron@example.test"  | _
    }

    def 'should reject email longer than 254 characters'() {
        given:
            String localPart = "a" * 200
            String domain = ("b" * 60) + ".test"
            String longEmail = localPart + "@" + domain
        expect:
            longEmail.length() > 254
        when:
            EmailAddress.of(longEmail)
        then:
            thrown(InvalidEmailAddress)
    }

    def 'should reject email that only exceeds length after trimming whitespace'() {
        given:
            String localPart = "a" * 200
            String domain = ("b" * 60) + ".test"
            String longEmail = "  " + localPart + "@" + domain + "  "
        expect:
            longEmail.trim().length() > 254
        when:
            EmailAddress.of(longEmail)
        then:
            thrown(InvalidEmailAddress)
    }
}
