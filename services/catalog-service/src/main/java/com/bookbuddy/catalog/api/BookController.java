package com.bookbuddy.catalog.api;

import com.bookbuddy.catalog.api.dto.BookResponse;
import com.bookbuddy.catalog.service.BookService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read endpoints for the book catalog: search (R6) and the top rankings (R9/R10).
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    /** Search books by title. (R6) */
    @GetMapping("/search")
    public List<BookResponse> search(@RequestParam @NotBlank String q,
                                     @RequestParam(defaultValue = "20") int limit) {
        return bookService.searchByTitle(q, limit).stream()
                .map(BookResponse::from)
                .toList();
    }

    /**
     * Top books, ranked by either rating (R9) or wishlist count (R10). The Figma
     * home page exposes exactly this choice via the "Top N books by Rating /
     * Wishlist count" control.
     *
     * @param by  either {@code rating} or {@code wishlist}
     * @param n   number of books to return
     */
    @GetMapping("/top")
    public List<BookResponse> top(@RequestParam(defaultValue = "rating") String by,
                                  @RequestParam(defaultValue = "5") int n) {
        return switch (by.toLowerCase()) {
            case "wishlist", "wishlistcount" -> bookService.topByWishlistCount(n).stream()
                    .map(BookResponse::from).toList();
            default -> bookService.topByRating(n).stream()
                    .map(BookResponse::from).toList();
        };
    }

    @GetMapping("/{id}")
    public BookResponse getById(@PathVariable String id) {
        return BookResponse.from(bookService.getById(id));
    }
}
