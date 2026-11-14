package com.library.service;

import com.library.exception.BusinessRuleException;
import com.library.model.Book;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final LoanRepository loanRepository;

    public BookService(BookRepository bookRepository, LoanRepository loanRepository) {
        this.bookRepository = bookRepository;
        this.loanRepository = loanRepository;
    }

    @Transactional(readOnly = true)
    public List<Book> search(String query) {
        if (query == null || query.isBlank()) {
            return bookRepository.findAll().stream()
                    .sorted((a, b) -> a.getTitle().compareToIgnoreCase(b.getTitle()))
                    .toList();
        }
        String q = query.trim();
        return bookRepository
                .findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrderByTitleAsc(
                        q, q, q, q);
    }

    @Transactional(readOnly = true)
    public List<Book> findAvailable() {
        return bookRepository.findByAvailableCopiesGreaterThanOrderByTitleAsc(0);
    }

    @Transactional(readOnly = true)
    public Optional<Book> findById(Long id) {
        return bookRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public long countTitles() {
        return bookRepository.count();
    }

    @Transactional(readOnly = true)
    public long countAvailableCopies() {
        return bookRepository.sumAvailableCopies();
    }

    public Book create(Book form) {
        form.setIsbn(form.getIsbn().trim());
        ensureIsbnIsFree(form.getIsbn(), null);
        Book book = new Book();
        copyFields(form, book);
        book.setAvailableCopies(form.getTotalCopies());
        return bookRepository.save(book);
    }

    public Book update(Long id, Book form) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Book not found"));
        form.setIsbn(form.getIsbn().trim());
        ensureIsbnIsFree(form.getIsbn(), id);

        int issued = book.getTotalCopies() - book.getAvailableCopies();
        if (form.getTotalCopies() < issued) {
            throw new BusinessRuleException("Total copies cannot be less than the " + issued
                    + " copies currently on loan");
        }
        copyFields(form, book);
        book.setAvailableCopies(form.getTotalCopies() - issued);
        return bookRepository.save(book);
    }

    public void delete(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new BusinessRuleException("Book not found"));
        if (loanRepository.existsByBook(book)) {
            throw new BusinessRuleException("\"" + book.getTitle()
                    + "\" has loan history and cannot be deleted. Set its copies to match what is on loan instead.");
        }
        bookRepository.delete(book);
    }

    private void ensureIsbnIsFree(String isbn, Long currentId) {
        bookRepository.findByIsbn(isbn).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new BusinessRuleException("A book with ISBN " + isbn + " already exists");
            }
        });
    }

    private void copyFields(Book from, Book to) {
        to.setTitle(from.getTitle().trim());
        to.setAuthor(from.getAuthor().trim());
        to.setIsbn(from.getIsbn());
        to.setCategory(from.getCategory() == null ? null : from.getCategory().trim());
        to.setTotalCopies(from.getTotalCopies());
    }
}
