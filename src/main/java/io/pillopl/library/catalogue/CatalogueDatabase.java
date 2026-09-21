package io.pillopl.library.catalogue;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import io.vavr.control.Option;

class CatalogueDatabase {

    private final JdbcTemplate jdbcTemplate;

    CatalogueDatabase(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    Book saveNew(Book book) {
        jdbcTemplate.update(
                """
                INSERT INTO catalogue_book (isbn, title, author)
                VALUES (?, ?, ?)
                """,
                book.getBookIsbn().getIsbn(),
                book.getTitle().getTitle(),
                book.getAuthor().getName());
        return book;
    }

    BookInstance saveNew(BookInstance bookInstance) {
        jdbcTemplate.update(
                """
                INSERT INTO catalogue_book_instance (isbn, book_id)
                VALUES (?, ?)
                """,
                bookInstance.getBookIsbn().getIsbn(),
                bookInstance.getBookId().getBookId());
        return bookInstance;
    }

    Option<Book> findBy(ISBN isbn) {
        try {
            return Option.of(
                    jdbcTemplate.queryForObject(
                            "SELECT b.* FROM catalogue_book b WHERE b.isbn = ?",
                            (rs, rowNum) -> new Book(
                                    rs.getString("isbn"),
                                    rs.getString("author"),
                                    rs.getString("title")),
                            isbn.getIsbn()));
        } catch (EmptyResultDataAccessException e) {
            return Option.none();
        }
    }
}
