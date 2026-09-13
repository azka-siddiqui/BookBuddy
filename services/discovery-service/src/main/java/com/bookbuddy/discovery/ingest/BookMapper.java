package com.bookbuddy.discovery.ingest;

import com.bookbuddy.discovery.client.dto.OpenLibraryDoc;
import com.bookbuddy.discovery.domain.Book;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Normalizes a raw {@link OpenLibraryDoc} from the source API into BookBuddy's
 * internal {@link Book} representation. Centralizing this mapping keeps all of the
 * source-specific quirks (key prefixes, cover-id URLs, subject capping) in one place.
 */
@Component
public class BookMapper {

    private static final String COVER_URL_TEMPLATE = "https://covers.openlibrary.org/b/id/%d-M.jpg";

    /** Open Library works can carry hundreds of subjects; cap the tags we keep. */
    private static final int MAX_TAGS = 6;

    /**
     * Builds a new {@link Book} from an Open Library doc. The caller is responsible
     * for assigning an internal id and for deciding whether to insert or update.
     *
     * @return the mapped book, or {@code null} if the doc is unusable (no key/title)
     */
    public Book toBook(OpenLibraryDoc doc) {
        String openLibraryId = extractWorkId(doc.key());
        if (openLibraryId == null || doc.title() == null || doc.title().isBlank()) {
            return null;
        }

        Book book = new Book();
        book.setOpenLibraryId(openLibraryId);
        book.setTitle(doc.title().trim());
        book.setAuthors(List.copyOf(doc.authorNames()));
        book.setPageCount(doc.medianPages() == null ? 0 : doc.medianPages());
        book.setTags(cappedSubjects(doc.subjects()));
        book.setCoverUrl(coverUrl(doc.coverId()));
        book.setDescription(firstSentence(doc.firstSentence()));
        book.setRatingAvg(0.0);
        book.setRatingCount(0);
        book.setWishlistCount(0);
        book.setCreatedAt(Instant.now());
        return book;
    }

    /**
     * Copies the mutable, source-derived fields from a freshly mapped book onto an
     * existing persisted book, preserving BookBuddy-owned fields (id, ratings,
     * wishlist count, createdAt).
     */
    public void copySourceFields(Book from, Book target) {
        target.setTitle(from.getTitle());
        target.setAuthors(from.getAuthors());
        target.setPageCount(from.getPageCount());
        target.setTags(from.getTags());
        target.setCoverUrl(from.getCoverUrl());
        target.setDescription(from.getDescription());
    }

    private String extractWorkId(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }
        // key looks like "/works/OL82563W"
        int lastSlash = key.lastIndexOf('/');
        String id = lastSlash >= 0 ? key.substring(lastSlash + 1) : key;
        return id.isBlank() ? null : id;
    }

    private List<String> cappedSubjects(List<String> subjects) {
        return subjects.stream()
                .filter(s -> s != null && !s.isBlank())
                .limit(MAX_TAGS)
                .toList();
    }

    private String coverUrl(Long coverId) {
        return coverId == null ? null : String.format(COVER_URL_TEMPLATE, coverId);
    }

    private String firstSentence(List<String> firstSentence) {
        if (firstSentence == null || firstSentence.isEmpty()) {
            return null;
        }
        return firstSentence.get(0);
    }
}
