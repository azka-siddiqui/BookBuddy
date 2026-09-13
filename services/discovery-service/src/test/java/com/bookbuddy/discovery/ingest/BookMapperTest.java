package com.bookbuddy.discovery.ingest;

import com.bookbuddy.discovery.client.dto.OpenLibraryDoc;
import com.bookbuddy.discovery.domain.Book;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookMapperTest {

    private final BookMapper mapper = new BookMapper();

    @Test
    void mapsWorkKeyToOpenLibraryId() {
        OpenLibraryDoc doc = new OpenLibraryDoc(
                "/works/OL82563W", "Harry Potter", List.of("J.K. Rowling"),
                List.of("Fantasy"), 10521270L, 652, List.of("The boy who lived."));

        Book book = mapper.toBook(doc);

        assertThat(book).isNotNull();
        assertThat(book.getOpenLibraryId()).isEqualTo("OL82563W");
        assertThat(book.getTitle()).isEqualTo("Harry Potter");
        assertThat(book.getAuthors()).containsExactly("J.K. Rowling");
        assertThat(book.getPageCount()).isEqualTo(652);
        assertThat(book.getCoverUrl()).isEqualTo("https://covers.openlibrary.org/b/id/10521270-M.jpg");
        assertThat(book.getDescription()).isEqualTo("The boy who lived.");
    }

    @Test
    void returnsNullWhenKeyMissing() {
        OpenLibraryDoc doc = new OpenLibraryDoc(
                null, "No Key", List.of(), List.of(), null, null, null);

        assertThat(mapper.toBook(doc)).isNull();
    }

    @Test
    void returnsNullWhenTitleBlank() {
        OpenLibraryDoc doc = new OpenLibraryDoc(
                "/works/OL1W", "  ", List.of(), List.of(), null, null, null);

        assertThat(mapper.toBook(doc)).isNull();
    }

    @Test
    void defaultsPageCountToZeroAndNullCoverWhenMissing() {
        OpenLibraryDoc doc = new OpenLibraryDoc(
                "/works/OL2W", "Untitled Pages", List.of(), List.of(), null, null, null);

        Book book = mapper.toBook(doc);

        assertThat(book).isNotNull();
        assertThat(book.getPageCount()).isZero();
        assertThat(book.getCoverUrl()).isNull();
        assertThat(book.getDescription()).isNull();
    }

    @Test
    void capsSubjectsToMaximum() {
        List<String> manySubjects = List.of("a", "b", "c", "d", "e", "f", "g", "h");
        OpenLibraryDoc doc = new OpenLibraryDoc(
                "/works/OL3W", "Tagged", List.of(), manySubjects, null, 100, null);

        Book book = mapper.toBook(doc);

        assertThat(book.getTags()).hasSize(6).containsExactly("a", "b", "c", "d", "e", "f");
    }

    @Test
    void copySourceFieldsPreservesOwnedFields() {
        Book existing = new Book();
        existing.setId("internal-1");
        existing.setOpenLibraryId("OL9W");
        existing.setRatingAvg(4.5);
        existing.setRatingCount(100);
        existing.setWishlistCount(20);
        existing.setTitle("Old Title");

        Book fresh = new Book();
        fresh.setTitle("New Title");
        fresh.setAuthors(List.of("New Author"));
        fresh.setPageCount(321);
        fresh.setTags(List.of("Sci-Fi"));

        mapper.copySourceFields(fresh, existing);

        // Source fields refreshed
        assertThat(existing.getTitle()).isEqualTo("New Title");
        assertThat(existing.getAuthors()).containsExactly("New Author");
        assertThat(existing.getPageCount()).isEqualTo(321);
        // Owned fields preserved
        assertThat(existing.getId()).isEqualTo("internal-1");
        assertThat(existing.getRatingAvg()).isEqualTo(4.5);
        assertThat(existing.getRatingCount()).isEqualTo(100);
        assertThat(existing.getWishlistCount()).isEqualTo(20);
    }
}
