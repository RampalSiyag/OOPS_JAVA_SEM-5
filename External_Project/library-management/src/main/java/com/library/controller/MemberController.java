package com.library.controller;

import com.library.exception.BusinessRuleException;
import com.library.model.Member;
import com.library.service.LoanService;
import com.library.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final LoanService loanService;

    public MemberController(MemberService memberService, LoanService loanService) {
        this.memberService = memberService;
        this.loanService = loanService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        List<Member> members = memberService.search(q);
        Map<Long, Long> activeLoans = new HashMap<>();
        for (Member member : members) {
            activeLoans.put(member.getId(), loanService.countActiveForMember(member));
        }
        model.addAttribute("members", members);
        model.addAttribute("activeLoans", activeLoans);
        model.addAttribute("maxLoans", LoanService.MAX_ACTIVE_LOANS);
        model.addAttribute("q", q);
        return "members/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("member", new Member());
        return "members/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("member") Member member,
                         BindingResult result,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "members/form";
        }
        try {
            Member saved = memberService.create(member);
            redirect.addFlashAttribute("success", "Registered " + saved.getName());
            return "redirect:/members";
        } catch (BusinessRuleException e) {
            result.reject("business", e.getMessage());
            return "members/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        return memberService.findById(id)
                .map(member -> {
                    model.addAttribute("member", member);
                    return "members/form";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "Member not found");
                    return "redirect:/members";
                });
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("member") Member member,
                         BindingResult result,
                         RedirectAttributes redirect) {
        member.setId(id);
        if (result.hasErrors()) {
            return "members/form";
        }
        try {
            Member saved = memberService.update(id, member);
            redirect.addFlashAttribute("success", "Updated " + saved.getName());
            return "redirect:/members";
        } catch (BusinessRuleException e) {
            result.reject("business", e.getMessage());
            return "members/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            memberService.delete(id);
            redirect.addFlashAttribute("success", "Member removed");
        } catch (BusinessRuleException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/members";
    }
}
