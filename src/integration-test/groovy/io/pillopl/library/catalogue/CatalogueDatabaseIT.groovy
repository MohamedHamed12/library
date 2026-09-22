package io.pillopl.library.catalogue

import io.pillopl.library.database.PostgreSQLTestConfiguration
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import spock.lang.Specification

import java.time.Clock
import java.util.Optional

import static io.pillopl.library.catalogue.BookFixture.DDD
import static io.pillopl.library.catalogue.BookFixture.NON_PRESENT_ISBN
import static io.pillopl.library.catalogue.BookInstance.instanceOf
import static io.pillopl.library.catalogue.BookType.Restricted

@Import(PostgreSQLTestConfiguration)
@SpringBootTest(classes = [CatalogueConfiguration, ClockConfiguration])
class CatalogueDatabaseIT extends Specification {

    @Autowired
    CatalogueDatabase catalogueDatabase

    @Autowired
    EntityManagerFactory entityManagerFactory

    def 'uses JPA persistence and Flyway-managed catalogue schema'() {
        expect:
            entityManagerFactory != null
    }

    def 'should be able to save and load new book'() {
        given:
            Book book = DDD
        when:
            catalogueDatabase.saveNew(book)
        and:
            Optional<Book> ddd = catalogueDatabase.findBy(book.bookIsbn)
        then:
            ddd.isPresent()
            ddd.get() == book
    }

    def 'should not load not present book'() {
        when:
            Optional<Book> ddd = catalogueDatabase.findBy(NON_PRESENT_ISBN)
        then:
            ddd.isEmpty()
    }

    def 'should save book instance'() {
        when:
            catalogueDatabase.saveNew(instanceOf(DDD, Restricted))
        then:
            noExceptionThrown()
    }

    @TestConfiguration
    static class ClockConfiguration {

        @Bean
        Clock clock() {
            Clock.systemUTC()
        }
    }

}
