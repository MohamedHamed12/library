package io.pillopl.library.lending.book.application;

import java.util.UUID;

import org.springframework.context.event.EventListener;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookInstanceAddedToCatalogue;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.model.AvailableBook;
import io.pillopl.library.lending.book.model.BookRepository;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

public class CreateAvailableBookOnInstanceAddedEventHandler {

  private final BookRepository bookRepository;

  public CreateAvailableBookOnInstanceAddedEventHandler(BookRepository bookRepository) {
    this.bookRepository = bookRepository;
  }

  @EventListener
  void handle(BookInstanceAddedToCatalogue event) {
    bookRepository.save(
        new AvailableBook(
            new BookId(event.getBookId()), event.getType(), ourLibraryBranch(), Version.zero()));
  }

  private LibraryBranchId ourLibraryBranch() {
    // from properties
    return new LibraryBranchId(UUID.randomUUID());
  }
}
