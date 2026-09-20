package io.pillopl.library;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

import io.pillopl.library.catalogue.BookId;
import io.pillopl.library.catalogue.BookType;
import io.pillopl.library.commons.aggregates.Version;
import io.pillopl.library.lending.book.model.Book;
import io.pillopl.library.lending.book.model.BookInformation;
import io.pillopl.library.lending.librarybranch.model.LibraryBranchId;

class ModernDomainModelingTest {

  @Test
  void convertedValueObjectsKeepValueSemantics() {
    UUID bookIdValue = UUID.randomUUID();
    UUID libraryBranchIdValue = UUID.randomUUID();

    BookId bookId = new BookId(bookIdValue);
    BookId sameBookId = new BookId(bookIdValue);
    LibraryBranchId libraryBranchId = new LibraryBranchId(libraryBranchIdValue);
    LibraryBranchId sameLibraryBranchId = new LibraryBranchId(libraryBranchIdValue);
    Version version = new Version(3);
    Version sameVersion = new Version(3);
    BookInformation bookInformation = new BookInformation(bookId, BookType.Circulating);
    BookInformation sameBookInformation = new BookInformation(sameBookId, BookType.Circulating);

    assertEquals(bookId, sameBookId);
    assertEquals(bookId.hashCode(), sameBookId.hashCode());
    assertEquals(bookIdValue, bookId.getBookId());

    assertEquals(libraryBranchId, sameLibraryBranchId);
    assertEquals(libraryBranchId.hashCode(), sameLibraryBranchId.hashCode());
    assertEquals(libraryBranchIdValue, libraryBranchId.getLibraryBranchId());

    assertEquals(version, sameVersion);
    assertEquals(version.hashCode(), sameVersion.hashCode());
    assertEquals(3, version.getVersion());

    assertEquals(bookInformation, sameBookInformation);
    assertEquals(bookInformation.hashCode(), sameBookInformation.hashCode());
    assertEquals(bookId, bookInformation.getBookId());
    assertEquals(BookType.Circulating, bookInformation.getBookType());
  }

  @Test
  void convertedValueObjectsKeepNullInvariants() {
    assertThrows(NullPointerException.class, () -> new BookId(null));
    assertThrows(NullPointerException.class, () -> new LibraryBranchId(null));
    assertThrows(NullPointerException.class, () -> new BookInformation(null, BookType.Circulating));
    assertThrows(
        NullPointerException.class, () -> new BookInformation(new BookId(UUID.randomUUID()), null));
  }

  @Test
  void bookStateHierarchyIsClosed() {
    assertTrue(Book.class.isSealed());
  }
}
