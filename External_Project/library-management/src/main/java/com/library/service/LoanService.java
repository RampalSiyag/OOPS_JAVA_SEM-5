package com.library.service;

import com.library.exception.BusinessRuleException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Transactional
public class LoanService {

    /** A member can hold at most this many books at once. */
    public static final int MAX_ACTIVE_LOANS = 3;

    private final LoanRepository loanRepository;
    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;

    public LoanService(LoanRepository loanRepository,
                       BookRepository bookRepository,
                       MemberRepository memberRepository) {
        this.loanRepository = loanRepository;
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<Loan> findAll() {
        return loanRepository.findAllByOrderByIssueDateDescIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Loan> findActive() {
        return loanRepository.findByReturnDateIsNullOrderByDueDateAsc();
    }

    @Transactional(readOnly = true)
    public List<Loan> findOverdue() {
        return loanRepository.findByReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public long countActive() {
        return loanRepository.countByReturnDateIsNull();
    }

    @Transactional(readOnly = true)
    public long countOverdue() {
        return loanRepository.countByReturnDateIsNullAndDueDateBefore(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public long countActiveForMember(Member member) {
        return loanRepository.countByMemberAndReturnDateIsNull(member);
    }

    public Loan issue(Long bookId, Long memberId) {
        if (bookId == null || memberId == null) {
            throw new BusinessRuleException("Please choose both a book and a member");
        }
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessRuleException("Book not found"));
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessRuleException("Member not found"));

        if (book.getAvailableCopies() <= 0) {
            throw new BusinessRuleException("No copies of \"" + book.getTitle() + "\" are available right now");
        }
        if (loanRepository.existsByBookAndMemberAndReturnDateIsNull(book, member)) {
            throw new BusinessRuleException(member.getName() + " already has a copy of \"" + book.getTitle() + "\"");
        }
        LocalDate today = LocalDate.now();
        if (loanRepository.existsByMemberAndReturnDateIsNullAndDueDateBefore(member, today)) {
            throw new BusinessRuleException(member.getName() + " has overdue books and must return them first");
        }
        if (loanRepository.countByMemberAndReturnDateIsNull(member) >= MAX_ACTIVE_LOANS) {
            throw new BusinessRuleException(member.getName() + " already has " + MAX_ACTIVE_LOANS
                    + " books on loan (the maximum)");
        }

        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setIssueDate(today);
        loan.setDueDate(today.plusDays(Loan.LOAN_DAYS));
        book.setAvailableCopies(book.getAvailableCopies() - 1);
        return loanRepository.save(loan);
    }

    public Loan returnBook(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new BusinessRuleException("Loan not found"));
        if (loan.isReturned()) {
            throw new BusinessRuleException("This book has already been returned");
        }
        LocalDate today = LocalDate.now();
        long lateDays = Math.max(0, ChronoUnit.DAYS.between(loan.getDueDate(), today));

        loan.setReturnDate(today);
        loan.setFine((int) (lateDays * Loan.FINE_PER_DAY));

        Book book = loan.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + 1);
        return loan;
    }
}
