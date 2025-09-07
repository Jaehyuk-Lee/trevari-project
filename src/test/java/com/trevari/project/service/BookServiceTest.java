package com.trevari.project.service;

import com.trevari.project.api.dto.BookDetailDTO;
import com.trevari.project.domain.Book;
import com.trevari.project.exception.NotFoundException;
import com.trevari.project.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock private BookRepository bookRepository;
    @InjectMocks private BookService bookService;

    @Test
    @DisplayName("getBookDetailDTO() - 존재하는 도서면 DTO 반환")
    void getBookDetailDTOById_returnsDetailDto_whenFound() {
        var book = Book.builder()
                .isbn("9781617291609")
                .title("MongoDB in Action")
                .author("Kyle Banker")
                .build();

        when(bookRepository.findById("9781617291609")).thenReturn(Optional.of(book));

        BookDetailDTO dto = bookService.getBookDetailDTO("9781617291609");

        assertThat(dto).isNotNull();
        assertThat(dto.title()).isEqualTo("MongoDB in Action");
    }

    @Test
    @DisplayName("getBookDetailDTO() - 없는 도서면 NotFoundException 발생")
    void getBookDetailDTOById_throwsNotFound_whenMissing() {
        when(bookRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookService.getBookDetailDTO("missing"))
                .isInstanceOf(NotFoundException.class);
    }
}
