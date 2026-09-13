package com.bookbuddy.social.club;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

/**
 * A book club. Membership is capacity-limited; the {@link Version} field enables
 * optimistic locking so concurrent joins for the last seat cannot both succeed (R14).
 */
@Document(collection = "book_clubs")
public class BookClub {

    @Id
    private String id;

    private String name;

    @Indexed
    private List<String> genres;

    private int capacity;
    private int memberCount;

    /** Optimistic-locking version, managed by Spring Data on save. */
    @Version
    private Long version;

    private Instant createdAt;

    public BookClub() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getGenres() {
        return genres == null ? List.of() : genres;
    }

    public void setGenres(List<String> genres) {
        this.genres = genres;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isFull() {
        return memberCount >= capacity;
    }
}
