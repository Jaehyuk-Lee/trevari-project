package com.trevari.project.search;

import com.trevari.project.domain.Book;
import org.springframework.data.jpa.domain.Specification;

public class BookSpecifications {
    private static Specification<Book> contains(String kw) {
        if (kw == null || kw.isBlank()) {
            // 키워드가 비어있는 경우에는 항상 false인 Predicate를 반환합니다.
            return (root, q, cb) -> cb.disjunction();
        }
        String normalized = kw.toLowerCase();
        String like = "%" + normalized + "%";
        return (root, q, cb) -> cb.or(
            cb.like(cb.lower(root.get("title")), like),
            cb.like(cb.lower(root.get("subtitle")), like),
            cb.like(cb.lower(root.get("author")), like),
            cb.like(cb.lower(root.get("publisher")), like),
            cb.like(cb.lower(root.get("isbn")), like)
        );
    }

    public static Specification<Book> forQuery(SearchQuery sq) {
        return switch (sq.strategy()) {
            case SIMPLE -> contains(sq.left());
            case OR_OPERATION -> Specification.anyOf(contains(sq.left()), contains(sq.right()));
            case NOT_OPERATION -> Specification.allOf(contains(sq.left()), Specification.not(contains(sq.right())));
        };
    }
}
