package com.library.controller;

import com.library.service.BookService;
import com.library.service.LoanService;
import com.library.service.MemberService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final BookService bookService;
    private final MemberService memberService;
    private final LoanService loanService;

    public DashboardController(BookService bookService, MemberService memberService, LoanService loanService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.loanService = loanService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("titles", bookService.countTitles());
        model.addAttribute("availableCopies", bookService.countAvailableCopies());
        model.addAttribute("members", memberService.count());
        model.addAttribute("activeLoans", loanService.countActive());
        model.addAttribute("overdueLoans", loanService.countOverdue());
        model.addAttribute("overdueList", loanService.findOverdue());
        return "index";
    }
}
