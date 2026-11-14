package com.library.config;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import com.library.repository.BookRepository;
import com.library.repository.LoanRepository;
import com.library.repository.MemberRepository;
import com.library.service.LoanService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/** Fills an empty database with sample data so the app has something to show on first run. */
@Component
@ConditionalOnProperty(name = "app.seed-data", havingValue = "true", matchIfMissing = true)
public class DataLoader implements CommandLineRunner {

    private final BookRepository bookRepository;
    private final MemberRepository memberRepository;
    private final LoanRepository loanRepository;
    private final LoanService loanService;

    public DataLoader(BookRepository bookRepository,
                      MemberRepository memberRepository,
                      LoanRepository loanRepository,
                      LoanService loanService) {
        this.bookRepository = bookRepository;
        this.memberRepository = memberRepository;
        this.loanRepository = loanRepository;
        this.loanService = loanService;
    }

    @Override
    public void run(String... args) {
        if (bookRepository.count() > 0) {
            return;
        }

        Book wings = book("Wings of Fire", "A. P. J. Abdul Kalam", "9788173711466", "Biography", 3);
        book("The Alchemist", "Paulo Coelho", "9780062315007", "Fiction", 2);
        Book cleanCode = book("Clean Code", "Robert C. Martin", "9780132350884", "Technology", 2);
        Book java = book("Effective Java", "Joshua Bloch", "9780134685991", "Technology", 1);
        Book sapiens = book("Sapiens", "Yuval Noah Harari", "9780062316097", "History", 2);
        Book gitanjali = book("Gitanjali", "Rabindranath Tagore", "9780486280202", "Poetry", 2);
        book("Malgudi Days", "R. K. Narayan", "9780143039655", "Fiction", 3);
        book("Atomic Habits", "James Clear", "9780735211292", "Self-help", 4);

        Member asha = member("Asha Verma", "asha.verma@example.com", "9810011223");
        Member rohan = member("Rohan Mehta", "rohan.mehta@example.com", "9899044556");
        Member priya = member("Priya Nair", "priya.nair@example.com", "9911177889");
        member("Karan Singh", "karan.singh@example.com", "9654332110");

        // Regular loans issued today
        loanService.issue(wings.getId(), asha.getId());
        loanService.issue(cleanCode.getId(), asha.getId());
        loanService.issue(sapiens.getId(), priya.getId());

        // An overdue loan (issued 20 days ago, due 6 days ago)
        Loan overdue = new Loan();
        overdue.setBook(java);
        overdue.setMember(rohan);
        overdue.setIssueDate(LocalDate.now().minusDays(20));
        overdue.setDueDate(LocalDate.now().minusDays(6));
        loanRepository.save(overdue);
        java.setAvailableCopies(java.getAvailableCopies() - 1);
        bookRepository.save(java);

        // A returned loan that was 6 days late (fine = 6 x 5 = 30)
        Loan returned = new Loan();
        returned.setBook(gitanjali);
        returned.setMember(priya);
        returned.setIssueDate(LocalDate.now().minusDays(30));
        returned.setDueDate(LocalDate.now().minusDays(16));
        returned.setReturnDate(LocalDate.now().minusDays(10));
        returned.setFine(6 * Loan.FINE_PER_DAY);
        loanRepository.save(returned);
    }

    private Book book(String title, String author, String isbn, String category, int copies) {
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setCategory(category);
        book.setTotalCopies(copies);
        book.setAvailableCopies(copies);
        return bookRepository.save(book);
    }

    private Member member(String name, String email, String phone) {
        Member member = new Member();
        member.setName(name);
        member.setEmail(email);
        member.setPhone(phone);
        return memberRepository.save(member);
    }
}
