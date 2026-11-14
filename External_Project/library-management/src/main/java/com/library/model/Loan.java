package com.library.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "loans")
public class Loan {

    /** How many days a member may keep a book. */
    public static final int LOAN_DAYS = 14;

    /** Fine charged (in rupees) for every day a book is returned late. */
    public static final int FINE_PER_DAY = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne(optional = false)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;

    /** Final fine, fixed when the book is returned. */
    private int fine;

    public boolean isReturned() {
        return returnDate != null;
    }

    public boolean isOverdue() {
        return returnDate == null && LocalDate.now().isAfter(dueDate);
    }

    public long getOverdueDays() {
        LocalDate end = returnDate != null ? returnDate : LocalDate.now();
        return Math.max(0, ChronoUnit.DAYS.between(dueDate, end));
    }

    /** Fine owed right now (running total for open loans, final amount for returned ones). */
    public int getCurrentFine() {
        return isReturned() ? fine : (int) (getOverdueDays() * FINE_PER_DAY);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public Member getMember() { return member; }
    public void setMember(Member member) { this.member = member; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }

    public int getFine() { return fine; }
    public void setFine(int fine) { this.fine = fine; }
}
