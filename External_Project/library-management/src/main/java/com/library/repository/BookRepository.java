package com.library.repository;

import com.library.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrderByTitleAsc(
            String title, String author, String isbn, String category);

    List<Book> findByAvailableCopiesGreaterThanOrderByTitleAsc(int copies);

    @Query("select coalesce(sum(b.availableCopies), 0) from Book b")
    long sumAvailableCopies();
}
