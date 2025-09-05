package com.trevari.project.service;

import com.trevari.project.domain.Book;
import com.trevari.project.repository.BookRepository;
import com.trevari.project.search.SearchQuery;
import com.trevari.project.search.SearchStrategy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {

    @Mock private BookRepository bookRepository;
    @InjectMocks private SearchService searchService;

    @Captor ArgumentCaptor<Pageable> pageableCaptor;

    @Test
    @DisplayName("search() - Repository를 올바른 인자로 1회 호출하고, 반환을 그대로 전달한다")
    void search_callsRepositoryOnce_withPolicyPageable_andReturnsAsIs() {
        // given
        var query = new SearchQuery("ti", "ti", null, SearchStrategy.SIMPLE);
        var pageable = PageRequest.of(0, 10);

        var book = Book.builder()
                .isbn("9874151387415")
                .title("kw_title")
                .author("author")
                .build();
        Page<Book> expected = new PageImpl<>(Collections.singletonList(book), pageable, 1);

        when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
                .thenReturn(expected);

        // when
        Page<Book> result = searchService.search(query, pageable);

        // then
        assertThat(result).isSameAs(expected);

        verify(bookRepository, Mockito.times(1))
                .findAll(ArgumentMatchers.<Specification<Book>>any(), pageableCaptor.capture());
        verifyNoMoreInteractions(bookRepository);

        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("search() - 빈 결과도 그대로 반환한다")
    void search_returnsEmptyPage() {
        // given
        var query = new SearchQuery("ti", "ti", null, SearchStrategy.SIMPLE);
        var pageable = PageRequest.of(0, 10);
        when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        // when
        Page<Book> result = searchService.search(query, pageable);

        // then
        assertThat(result).isNotNull().isEmpty();
        verify(bookRepository).findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class));
        verifyNoMoreInteractions(bookRepository);
    }
}
