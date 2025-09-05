package com.trevari.project.repository;

import com.trevari.project.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface BookRepository extends JpaRepository<Book, String>, JpaSpecificationExecutor<Book> {

}
