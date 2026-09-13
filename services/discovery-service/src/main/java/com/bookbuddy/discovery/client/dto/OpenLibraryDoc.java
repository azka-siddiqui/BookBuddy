package com.bookbuddy.discovery.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A single work returned by the Open Library search API.
 *
 * <p>Field names follow Open Library's schema:
 * <ul>
 *   <li>{@code key} is the work key, e.g. {@code /works/OL82563W}</li>
 *   <li>{@code author_name} is a list of author display names</li>
 *   <li>{@code subject} is a list of subjects we treat as tags/genres</li>
 *   <li>{@code cover_i} is the numeric cover id used to build a cover URL</li>
 *   <li>{@code number_of_pages_median} is the best available page count</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenLibraryDoc(
        @JsonProperty("key") String key,
        @JsonProperty("title") String title,
        @JsonProperty("author_name") List<String> authorNames,
        @JsonProperty("subject") List<String> subjects,
        @JsonProperty("cover_i") Long coverId,
        @JsonProperty("number_of_pages_median") Integer medianPages,
        @JsonProperty("first_sentence") List<String> firstSentence
) {
    public List<String> authorNames() {
        return authorNames == null ? List.of() : authorNames;
    }

    public List<String> subjects() {
        return subjects == null ? List.of() : subjects;
    }
}
