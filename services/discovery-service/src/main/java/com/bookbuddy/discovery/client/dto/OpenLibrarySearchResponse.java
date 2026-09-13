package com.bookbuddy.discovery.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Subset of the Open Library {@code /search.json} response that we care about.
 * Unknown fields are ignored so the mapping is resilient to the source API
 * returning additional properties.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibrarySearchResponse(
        @JsonProperty("numFound") int numFound,
        @JsonProperty("docs") List<OpenLibraryDoc> docs
) {
    public List<OpenLibraryDoc> docs() {
        return docs == null ? List.of() : docs;
    }
}
