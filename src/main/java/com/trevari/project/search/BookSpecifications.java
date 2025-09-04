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

    private static <T> Specification<T> negateIfPresent(Specification<T> spec) {
        return (spec == null) ? null : Specification.not(spec);
    }

    // 공통: 필드들 중 하나라도 like 매칭되면 true (NULL-safe, case-insensitive)
    private static Predicate anyFieldLike(Root<Book> root, CriteriaBuilder cb, String like) {
        List<Predicate> preds = new ArrayList<>(FIELDS.size());
        for (String f : FIELDS) {
            preds.add(cb.like(cb.lower(cb.coalesce(root.get(f), "")), like));
        }
        return cb.or(preds.toArray(new Predicate[0]));
    }

    private static Specification<Book> contains(String kw) {
        if (kw == null || kw.isBlank()) {
            // 제약 없음 (호출부에서 and/or 결합 시 무시되도록 null 반환)
            return null;
        }
        String like = "%" + kw.toLowerCase() + "%";
        return (root, q, cb) -> anyFieldLike(root, cb, like);
    }

    public static Specification<Book> forQuery(SearchQuery sq) {
        return switch (sq.strategy()) {
            case SIMPLE -> {
                String left = sq.left();
                if (left == null || left.isBlank()) {
                    // 빈 키워드: 항상 false -> 결과 0건
                    yield (root, q, cb) -> cb.disjunction();
                }
                yield contains(left);
            }
            case OR_OPERATION -> Specification.anyOf(contains(sq.left()), contains(sq.right()));
            case NOT_OPERATION -> Specification.allOf(contains(sq.left()), negateIfPresent(contains(sq.right())));
        };
    }
}
