package com.trevari.project.search;

import com.trevari.project.domain.Book;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class BookSpecifications {

    private BookSpecifications() {} // 순수 유틸 클래스: 인스턴스화 방지

    private static final List<String> FIELDS =
            List.of("isbn", "title", "subtitle", "author", "publisher");

    // 공통: 필드들 중 하나라도 like 매칭되면 true (NULL-safe, case-insensitive)
    private static Predicate anyFieldLike(Root<Book> root, CriteriaBuilder cb, String like) {
        List<Predicate> preds = new ArrayList<>(FIELDS.size());
        for (String f : FIELDS) {
            preds.add(cb.like(cb.lower(cb.coalesce(root.get(f).as(String.class), "")), like));
        }
        return cb.or(preds.toArray(new Predicate[0]));
    }

    private static Specification<Book> contains(String kw) {
        if (kw == null || kw.isBlank()) {
            // 항상 거짓: SIMPLE=0건, OR에서는 무시 효과, NOT에서 right가 빈 경우는 파서에서 400 처리됨
            return (root, q, cb) -> cb.disjunction();
        }
        String like = "%" + kw.toLowerCase(java.util.Locale.ROOT) + "%";
        return (root, q, cb) -> anyFieldLike(root, cb, like);
    }

    public static Specification<Book> forQuery(SearchQuery sq) {
        return switch (sq.strategy()) {
            case SIMPLE -> contains(sq.left());
            case OR_OPERATION -> Specification.anyOf(contains(sq.left()), contains(sq.right()));
            case NOT_OPERATION -> Specification.allOf(contains(sq.left()), Specification.not(contains(sq.right())));
        };
    }
}
