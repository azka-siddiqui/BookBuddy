package com.bookbuddy.catalog.api;

import com.bookbuddy.catalog.service.WishlistService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Wishlist actions (R7). The authenticated user id is supplied by the gateway
 * via the {@code X-User-Id} header after it validates the JWT.
 */
@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/{bookId}")
    public ResponseEntity<Map<String, Object>> add(@RequestHeader("X-User-Id") String userId,
                                                    @PathVariable String bookId) {
        boolean created = wishlistService.add(userId, bookId);
        return ResponseEntity
                .status(created ? HttpStatus.CREATED : HttpStatus.OK)
                .body(Map.of("bookId", bookId, "added", created));
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Map<String, Object>> remove(@RequestHeader("X-User-Id") String userId,
                                                       @PathVariable String bookId) {
        boolean removed = wishlistService.remove(userId, bookId);
        return ResponseEntity.ok(Map.of("bookId", bookId, "removed", removed));
    }
}
