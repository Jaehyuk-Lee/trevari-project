package com.trevari.project.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(of = "isbn")
@ToString
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
    @Column(length = 20, nullable = false)
    private String isbn; // 예: "9781617291609"

    @Column(length = 255, nullable = false)
    private String title;

    @Column(length = 255)
    private String subtitle;

    @Column(length = 255, nullable = false)
    private String author;

    @Column(length = 255)
    private String publisher;

    // API 응답: "published"
    @Column(name = "published_date")
    private LocalDate publishedDate;

    @Column(length = 512)
    private String image;
}
