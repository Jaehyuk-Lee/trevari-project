package com.trevari.project.api;

import com.trevari.project.api.dto.SearchDTOs;
import com.trevari.project.api.dto.SearchKeyword;
import com.trevari.project.search.SearchQuery;
import com.trevari.project.search.SearchStrategy;
import com.trevari.project.service.BookService;
import com.trevari.project.service.SearchAggregateService;
import com.trevari.project.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    private MockMvc mockMvc;

    @Mock
    private SearchService searchService;

    @Mock
    private BookService bookService;

    @Mock
    private SearchAggregateService searchAggregateService;

    @InjectMocks
    private BookController bookController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(bookController).build();
    }

    @Test
    @DisplayName("GET /api/books/{ID}: ID로 도서 반환")
    void get_book_by_id_returns_book() throws Exception {
        SearchDTOs.Book book = new SearchDTOs.Book(
            "9786247377209",
            "테스트 도서",
            "",
            "",
            "저자",
            "isbn-1",
            java.time.LocalDate.now()
        );

        Mockito.when(bookService.getBookDTO(eq("9786247377209"))).thenReturn(book);
        mockMvc.perform(get("/api/books/9786247377209").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("9786247377209"))
            .andExpect(jsonPath("$.title").value("테스트 도서"));
    }

    @Test
    @DisplayName("GET /api/books: SIMPLE 모드로 페이지 결과 반환")
    void browse_simple_keyword_returns_page() throws Exception {

        SearchDTOs.Book book = new SearchDTOs.Book(
            "9786247193209",
            "타이틀",
            "",
            "",
            "저자1",
            "isbn-b1",
            java.time.LocalDate.now()
        );

        SearchDTOs.PageInfo pageInfo = new SearchDTOs.PageInfo(1, 20, 1, 1L);
        SearchDTOs.Metadata metadata = new SearchDTOs.Metadata(5L, SearchStrategy.SIMPLE);
        SearchDTOs.Response resp = new SearchDTOs.Response("keyword", pageInfo, Collections.singletonList(book), metadata);

    Mockito.when(searchService.getSearchDTO(Mockito.<SearchQuery>any(), Mockito.<Pageable>any())).thenReturn(resp);

        mockMvc.perform(get("/api/books").param("keyword", "keyword").param("page", "1").param("size", "20")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.searchQuery").value("keyword"))
            .andExpect(jsonPath("$.books[0].id").value("9786247193209"));
    }

    @Test
    @DisplayName("GET /api/search/books: q 파라미터로 파싱 후 결과 반환")
    void search_with_operators_returns_result() throws Exception {
        SearchDTOs.Book book = new SearchDTOs.Book(
            "9786242859209",
            "테스트 도서 제목",
            "",
            "",
            "저자2",
            "isbn-b2",
            java.time.LocalDate.now()
        );

        SearchDTOs.PageInfo pageInfo2 = new SearchDTOs.PageInfo(1, 10, 1, 1L);
        SearchDTOs.Metadata metadata2 = new SearchDTOs.Metadata(7L, SearchStrategy.OR_OPERATION);
        SearchDTOs.Response resp = new SearchDTOs.Response("term-other", pageInfo2, Collections.singletonList(book), metadata2);

    Mockito.when(searchService.getSearchDTO(Mockito.<SearchQuery>any(), Mockito.<Pageable>any())).thenReturn(resp);
        mockMvc.perform(get("/api/search/books").param("q", "term-Other").param("page", "1").param("size", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.searchQuery").value("term-other"))
            .andExpect(jsonPath("$.books[0].id").value("9786242859209"));
    }

    @Test
    @DisplayName("GET /api/analytics/search/top10: 인기 검색어 TOP10 조회")
    void get_top10_keywords_returns_list() throws Exception {
        var kw1 = new SearchKeyword("spring boot", 10L);
        var kw2 = new SearchKeyword("tdd", 7L);

        List<SearchKeyword> list = List.of(kw1, kw2);

        Mockito.when(searchAggregateService.getTop10Keywords()).thenReturn(list);

        mockMvc.perform(get("/api/analytics/search/top10").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].keyword").value("spring boot"))
            .andExpect(jsonPath("$[0].count").value(10))
            .andExpect(jsonPath("$[1].keyword").value("tdd"))
            .andExpect(jsonPath("$[1].count").value(7));
    }
}
