package com.trevari.project.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "isbn")
@Entity
@Table(
        name = "books",
        indexes = {
                @Index(name = "idx_books_title", columnList = "title"),
                @Index(name = "idx_books_author", columnList = "author"),
                @Index(name = "idx_books_publisher", columnList = "publisher")
        }
)
public class Book {

    @Id
    @NotNull @Column(length = 20, nullable = false)
    private String isbn; // 예: "9781617291609"

    @NotNull
    @Column(length = 255, nullable = false)
    private String title;

    @Column(length = 255)
    private String subtitle;

    @NotNull @Column(length = 255, nullable = false)
    private String author;

    @Column(length = 255)
    private String publisher;

    // API 응답: "published"
    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(length = 512)
    private String image;

    @Builder
    public Book(@NonNull String isbn,
                @NonNull String title,
                String subtitle,
                @NonNull String author,
                String publisher,
                LocalDate publishedDate,
                String image) {
        this.isbn = isbn;
        this.title = title;
        this.subtitle = subtitle;
        this.author = author;
        this.publisher = publisher;
        this.publishedDate = publishedDate;
        this.image = image;
    }
}
