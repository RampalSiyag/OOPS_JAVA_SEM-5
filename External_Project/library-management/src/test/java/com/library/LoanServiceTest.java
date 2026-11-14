package com.library;

import com.library.exception.BusinessRuleException;
import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import com.library.service.LoanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
class LoanServiceTest {

    @Autowired LoanService loanService;
    @Autowired LoanRepository loanRepository;
    @Autowired BookRepository bookRepository;
    @Autowired MemberRepository memberRepository;

    private Book book(String isbn, int copies) {
        Book book = new Book();
        book.setTitle("Book " + isbn);
        book.setAuthor("Author");
        book.setIsbn(isbn);
        book.setTotalCopies(copies);
        book.setAvailableCopies(copies);
        return bookRepository.save(book);
    }

    private Member member(String email) {
        Member member = new Member();
        member.setName("Member " + email);
        member.setEmail(email);
        return memberRepository.save(member);
    }

    @Test
    void issuingReducesAvailableCopiesAndSetsDueDate() {
        Book book = book("T-1", 2);
        Member member = member("a@test.com");

        Loan loan = loanService.issue(book.getId(), member.getId());

        assertEquals(1, bookRepository.findById(book.getId()).orElseThrow().getAvailableCopies());
        assertEquals(LocalDate.now().plusDays(Loan.LOAN_DAYS), loan.getDueDate());
    }

    @Test
    void cannotIssueWhenNoCopiesAreLeft() {
        Book book = book("T-2", 1);
        loanService.issue(book.getId(), member("b@test.com").getId());

        Member other = member("c@test.com");
        assertThrows(BusinessRuleException.class, () -> loanService.issue(book.getId(), other.getId()));
    }

    @Test
    void memberCannotHoldMoreThanThreeBooks() {
        Member member = member("d@test.com");
        for (int i = 0; i < LoanService.MAX_ACTIVE_LOANS; i++) {
            loanService.issue(book("T-3-" + i, 1).getId(), member.getId());
        }
        Book fourth = book("T-3-extra", 1);
        assertThrows(BusinessRuleException.class, () -> loanService.issue(fourth.getId(), member.getId()));
    }

    @Test
    void memberWithOverdueBookCannotBorrow() {
        Book old = book("T-4", 1);
        Member member = member("e@test.com");
        Loan overdue = new Loan();
        overdue.setBook(old);
        overdue.setMember(member);
        overdue.setIssueDate(LocalDate.now().minusDays(30));
        overdue.setDueDate(LocalDate.now().minusDays(16));
        loanRepository.save(overdue);

        Book another = book("T-4b", 1);
        assertThrows(BusinessRuleException.class, () -> loanService.issue(another.getId(), member.getId()));
    }

    @Test
    void returningOnTimeHasNoFineAndRestoresCopy() {
        Book book = book("T-5", 1);
        Loan loan = loanService.issue(book.getId(), member("f@test.com").getId());

        Loan returned = loanService.returnBook(loan.getId());

        assertEquals(0, returned.getFine());
        assertEquals(1, bookRepository.findById(book.getId()).orElseThrow().getAvailableCopies());
    }

    @Test
    void returningLateChargesFivePerDay() {
        Book book = book("T-6", 1);
        book.setAvailableCopies(0);
        Member member = member("g@test.com");
        Loan loan = new Loan();
        loan.setBook(book);
        loan.setMember(member);
        loan.setIssueDate(LocalDate.now().minusDays(20));
        loan.setDueDate(LocalDate.now().minusDays(6));
        loan = loanRepository.save(loan);

        Loan returned = loanService.returnBook(loan.getId());

        assertEquals(6 * Loan.FINE_PER_DAY, returned.getFine());
        assertTrue(returned.isReturned());
    }

    @Test
    void cannotReturnTheSameLoanTwice() {
        Book book = book("T-7", 1);
        Loan loan = loanService.issue(book.getId(), member("h@test.com").getId());
        loanService.returnBook(loan.getId());

        Long id = loan.getId();
        assertThrows(BusinessRuleException.class, () -> loanService.returnBook(id));
    }
}
