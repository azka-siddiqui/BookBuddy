package com.bookbuddy.social.book;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

/**
 * Read-only projection of the shared {@code books} collection. social-service does
 * not own books (catalog/discovery do), but reads them to resolve titles, authors
 * and tags for the profile, recommendation and club features.
 */
@Document(collection = "books")
public class BookView {

    @Id
    private String id;
    private String title;
    private List<String> authors;
    private int pageCount;
    private List<String> tags;
    private String coverUrl;

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public int getPageCount() {
        return pageCount;
    }

    public List<String> getTags() {
        return tags == null ? List.of() : tags;
    }

    public String getCoverUrl() {
        return coverUrl;
    }
}
