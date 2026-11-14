package com.library.controller;

import com.library.exception.BusinessRuleException;
import com.library.model.Book;
import com.library.service.BookService;
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

@Controller
@RequestMapping("/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String q, Model model) {
        model.addAttribute("books", bookService.search(q));
        model.addAttribute("q", q);
        return "books/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("book", new Book());
        return "books/form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("book") Book book,
                         BindingResult result,
                         RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "books/form";
        }
        try {
            Book saved = bookService.create(book);
            redirect.addFlashAttribute("success", "Added \"" + saved.getTitle() + "\" to the catalogue");
            return "redirect:/books";
        } catch (BusinessRuleException e) {
            result.reject("business", e.getMessage());
            return "books/form";
        }
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirect) {
        return bookService.findById(id)
                .map(book -> {
                    model.addAttribute("book", book);
                    return "books/form";
                })
                .orElseGet(() -> {
                    redirect.addFlashAttribute("error", "Book not found");
                    return "redirect:/books";
                });
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("book") Book book,
                         BindingResult result,
                         RedirectAttributes redirect) {
        book.setId(id);
        if (result.hasErrors()) {
            return "books/form";
        }
        try {
            Book saved = bookService.update(id, book);
            redirect.addFlashAttribute("success", "Updated \"" + saved.getTitle() + "\"");
            return "redirect:/books";
        } catch (BusinessRuleException e) {
            result.reject("business", e.getMessage());
            return "books/form";
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            bookService.delete(id);
            redirect.addFlashAttribute("success", "Book deleted");
        } catch (BusinessRuleException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/books";
    }
}
