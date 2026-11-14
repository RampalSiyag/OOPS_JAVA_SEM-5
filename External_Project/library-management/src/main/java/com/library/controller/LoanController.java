package com.library.controller;

import com.library.exception.BusinessRuleException;
import com.library.model.Loan;
import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;
    private final BookService bookService;
    private final MemberService memberService;

    public LoanController(LoanService loanService, BookService bookService, MemberService memberService) {
        this.loanService = loanService;
        this.bookService = bookService;
        this.memberService = memberService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "active") String filter, Model model) {
        List<Loan> loans = switch (filter) {
            case "all" -> loanService.findAll();
            case "overdue" -> loanService.findOverdue();
            default -> {
                filter = "active";
                yield loanService.findActive();
            }
        };
        model.addAttribute("loans", loans);
        model.addAttribute("filter", filter);
        return "loans/list";
    }

    @GetMapping("/issue")
    public String issueForm(@RequestParam(required = false) Long bookId, Model model) {
        model.addAttribute("books", bookService.findAvailable());
        model.addAttribute("members", memberService.findAll());
        model.addAttribute("selectedBookId", bookId);
        model.addAttribute("loanDays", Loan.LOAN_DAYS);
        model.addAttribute("finePerDay", Loan.FINE_PER_DAY);
        model.addAttribute("maxLoans", LoanService.MAX_ACTIVE_LOANS);
        return "loans/issue";
    }

    @PostMapping("/issue")
    public String issue(@RequestParam(required = false) Long bookId,
                        @RequestParam(required = false) Long memberId,
                        RedirectAttributes redirect) {
        try {
            Loan loan = loanService.issue(bookId, memberId);
            redirect.addFlashAttribute("success", "Issued \"" + loan.getBook().getTitle() + "\" to "
                    + loan.getMember().getName() + ". Due back on " + loan.getDueDate() + ".");
            return "redirect:/loans";
        } catch (BusinessRuleException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/loans/issue" + (bookId != null ? "?bookId=" + bookId : "");
        }
    }

    @PostMapping("/{id}/return")
    public String returnBook(@PathVariable Long id,
                             @RequestParam(defaultValue = "active") String filter,
                             RedirectAttributes redirect) {
        try {
            Loan loan = loanService.returnBook(id);
            String message = "\"" + loan.getBook().getTitle() + "\" returned by " + loan.getMember().getName() + ".";
            if (loan.getFine() > 0) {
                message += " Late fine to collect: ₹" + loan.getFine() + ".";
            }
            redirect.addFlashAttribute("success", message);
        } catch (BusinessRuleException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/loans?filter=" + ("all".equals(filter) || "overdue".equals(filter) ? filter : "active");
    }
}
