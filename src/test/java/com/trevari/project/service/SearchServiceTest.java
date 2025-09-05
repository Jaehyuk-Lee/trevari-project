package com.trevari.project.service;

import com.trevari.project.api.dto.SearchDTOs;
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
    @DisplayName("getSearchDTO() - Repository를 올바른 인자로 1회 호출하고, DTO 응답 생성")
    void getSearchResponse_callsRepositoryOnce_andBuildsDTO() {
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
        SearchDTOs.Response response = searchService.getSearchDTO(query, pageable);

        // then: DTO 변환 확인
        assertThat(response).isNotNull();
        assertThat(response.books()).hasSize(1);
        assertThat(response.searchQuery()).isEqualTo(query.query());
        assertThat(response.searchMetadata().strategy()).isEqualTo(query.strategy());

        verify(bookRepository, Mockito.times(1))
                .findAll(ArgumentMatchers.<Specification<Book>>any(), pageableCaptor.capture());
        verifyNoMoreInteractions(bookRepository);

        Pageable used = pageableCaptor.getValue();
        assertThat(used.getPageNumber()).isEqualTo(0);
        assertThat(used.getPageSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("getSearchDTO() - 빈 결과도 빈 DTO 리스트로 반환")
    void getSearchResponse_returnsEmptyList() {
        // given
        var query = new SearchQuery("ti", "ti", null, SearchStrategy.SIMPLE);
        var pageable = PageRequest.of(0, 10);
        when(bookRepository.findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class)))
                .thenReturn(Page.empty(pageable));

        // when
        SearchDTOs.Response response = searchService.getSearchDTO(query, pageable);

        // then
        assertThat(response).isNotNull();
        assertThat(response.books()).isEmpty();
        verify(bookRepository).findAll(ArgumentMatchers.<Specification<Book>>any(), any(Pageable.class));
        verifyNoMoreInteractions(bookRepository);
    }
}
