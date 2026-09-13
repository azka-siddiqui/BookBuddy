package com.bookbuddy.discovery.api;

import com.bookbuddy.discovery.ingest.IngestionResult;
import com.bookbuddy.discovery.ingest.IngestionService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * Triggers ingestion of books from the Open Library source API.
 * Exposed under {@code /api/discovery}.
 */
@RestController
@RequestMapping("/api/discovery")
public class IngestionController {

    private final IngestionService ingestionService;

    public IngestionController(IngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    /**
     * Scrapes Open Library for the given query and stores the results.
     *
     * @param request the ingestion request containing the search query
     * @return a summary of the ingestion run
     */
    @PostMapping("/ingest")
    public ResponseEntity<IngestionResult> ingest(@Valid @RequestBody IngestRequest request) {
        IngestionResult result = ingestionService.ingest(request.query());
        return ResponseEntity.ok(result);
    }

    public record IngestRequest(@NotBlank String query) {
    }
}
