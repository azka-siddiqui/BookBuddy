package com.bookbuddy.discovery.ingest;

import com.bookbuddy.discovery.client.OpenLibraryClient;
import com.bookbuddy.discovery.client.dto.OpenLibraryDoc;
import com.bookbuddy.discovery.domain.Book;
import com.bookbuddy.discovery.domain.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestrates the scrape-and-store flow: fetch works from Open Library, normalize
 * them, and upsert into the {@code books} collection.
 *
 * <p>Ingestion is idempotent: books are keyed by {@code openLibraryId}, so re-running
 * a query refreshes source fields on existing books rather than creating duplicates.
 * BookBuddy-owned fields (ratings, wishlist counts) are preserved on update.
 */
@Service
public class IngestionService {

    private static final Logger log = LoggerFactory.getLogger(IngestionService.class);

    private final OpenLibraryClient client;
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;

    public IngestionService(OpenLibraryClient client,
                            BookRepository bookRepository,
                            BookMapper bookMapper) {
        this.client = client;
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    /**
     * Fetches works matching the query from Open Library and stores them.
     *
     * @param query free-text search term
     * @return a summary of how many books were fetched, created, updated and skipped
     */
    public IngestionResult ingest(String query) {
        List<OpenLibraryDoc> docs = client.search(query);

        int created = 0;
        int updated = 0;
        int skipped = 0;

        for (OpenLibraryDoc doc : docs) {
            Book mapped = bookMapper.toBook(doc);
            if (mapped == null) {
                skipped++;
                continue;
            }

            Optional<Book> existing = bookRepository.findByOpenLibraryId(mapped.getOpenLibraryId());
            if (existing.isPresent()) {
                Book target = existing.get();
                bookMapper.copySourceFields(mapped, target);
                bookRepository.save(target);
                updated++;
            } else {
                mapped.setId(UUID.randomUUID().toString());
                bookRepository.save(mapped);
                created++;
            }
        }

        IngestionResult result = new IngestionResult(query, docs.size(), created, updated, skipped);
        log.info("Ingestion complete: {}", result);
        return result;
    }
}
