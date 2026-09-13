package com.bookbuddy.discovery.client;

import com.bookbuddy.discovery.client.dto.OpenLibraryDoc;
import com.bookbuddy.discovery.client.dto.OpenLibrarySearchResponse;
import com.bookbuddy.discovery.config.OpenLibraryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

/**
 * Thin client over the Open Library search API. Encapsulates the HTTP call and
 * translates transport-level failures into a domain-specific exception so callers
 * do not need to know how the data is fetched.
 */
@Component
public class OpenLibraryClient {

    private static final Logger log = LoggerFactory.getLogger(OpenLibraryClient.class);

    /**
     * Fields we ask Open Library to return. Restricting fields keeps the payload
     * small and the ingestion fast.
     */
    private static final String FIELDS =
            "key,title,author_name,subject,cover_i,number_of_pages_median,first_sentence";

    private final RestClient restClient;
    private final OpenLibraryProperties properties;

    public OpenLibraryClient(RestClient openLibraryRestClient, OpenLibraryProperties properties) {
        this.restClient = openLibraryRestClient;
        this.properties = properties;
    }

    /**
     * Searches Open Library for works matching the given free-text query.
     *
     * @param query free-text search (e.g. a title or author)
     * @return the matching works, capped at the configured maximum
     * @throws OpenLibraryException if the source API cannot be reached or returns an error
     */
    public List<OpenLibraryDoc> search(String query) {
        int limit = properties.maxResults();
        log.info("Searching Open Library for query='{}' (limit={})", query, limit);
        try {
            OpenLibrarySearchResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/search.json")
                            .queryParam("q", query)
                            .queryParam("fields", FIELDS)
                            .queryParam("limit", limit)
                            .build())
                    .retrieve()
                    .body(OpenLibrarySearchResponse.class);

            if (response == null) {
                log.warn("Open Library returned an empty body for query='{}'", query);
                return List.of();
            }
            log.info("Open Library returned {} docs (numFound={}) for query='{}'",
                    response.docs().size(), response.numFound(), query);
            return response.docs();
        } catch (RestClientException ex) {
            throw new OpenLibraryException(
                    "Failed to fetch results from Open Library for query='" + query + "'", ex);
        }
    }
}
