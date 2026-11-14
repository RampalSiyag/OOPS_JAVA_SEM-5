package com.library.repository;

import com.library.model.Book;
import com.library.model.Loan;
import com.library.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findAllByOrderByIssueDateDescIdDesc();

    List<Loan> findByReturnDateIsNullOrderByDueDateAsc();

    List<Loan> findByReturnDateIsNullAndDueDateBeforeOrderByDueDateAsc(LocalDate date);

    long countByReturnDateIsNull();

    long countByReturnDateIsNullAndDueDateBefore(LocalDate date);

    long countByMemberAndReturnDateIsNull(Member member);

    boolean existsByMemberAndReturnDateIsNullAndDueDateBefore(Member member, LocalDate date);

    boolean existsByBookAndMemberAndReturnDateIsNull(Book book, Member member);

    boolean existsByBook(Book book);

    boolean existsByMember(Member member);
}
