package com.bookbuddy.discovery.ingest;

/**
 * Summary of an ingestion run.
 *
 * @param query    the search query that was ingested
 * @param fetched  number of docs returned by the source API
 * @param created  number of new books inserted
 * @param updated  number of existing books refreshed
 * @param skipped  number of docs that could not be mapped
 */
public record IngestionResult(String query, int fetched, int created, int updated, int skipped) {
}
