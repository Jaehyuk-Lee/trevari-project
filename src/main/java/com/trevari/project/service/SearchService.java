package com.trevari.project.service;

import com.trevari.project.domain.Book;
import com.trevari.project.repository.BookRepository;
import com.trevari.project.search.BookSpecifications;
import com.trevari.project.search.SearchQuery;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@AllArgsConstructor
@Service
public class SearchService {
    private final BookRepository bookRepository;

    @Transactional(readOnly = true)
    public Page<Book> search(SearchQuery searchQuery, Pageable pageable) {
        return bookRepository.findAll(BookSpecifications.forQuery(searchQuery), pageable);
    }
}
