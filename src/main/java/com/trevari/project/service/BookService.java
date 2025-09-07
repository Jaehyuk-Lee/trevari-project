package com.trevari.project.service;

import com.trevari.project.api.dto.BookDetailDTO;
import com.trevari.project.domain.Book;
import com.trevari.project.exception.NotFoundException;
import com.trevari.project.repository.BookRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class BookService {
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public BookDetailDTO getBookDetailDTO(String id) {
        Book b = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book not found: " + id));
        return new BookDetailDTO(
                b.getIsbn(), b.getTitle(), b.getSubtitle(), b.getImage(),
                b.getAuthor(), b.getIsbn(), b.getPublisher(), b.getPublishedDate()
        );
    }

}
