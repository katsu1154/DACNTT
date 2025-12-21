package com.library.app.controller;

import com.library.app.domain.BorrowRequest;
import com.library.app.repository.BorrowRequestRepository;
import com.library.app.service.LibraryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Controller
public class AuthController {
    @Autowired private LibraryService libraryService;
    @Autowired private BorrowRequestRepository borrowRepo;

    // --- LOGIN ---
    @GetMapping("/")
    public String showLogin() { return "login"; }

    @PostMapping("/login")
    public String handleLogin(@RequestParam String username, @RequestParam String password) {
        if ("admin".equals(username) && "123".equals(password)) return "redirect:/index";
        return "redirect:/?error";
    }
    
    @GetMapping("/logout")
    public String logout() { return "redirect:/"; }

    // --- DASHBOARD ---
    @GetMapping("/index")
    public String showIndex(Model model, @RequestParam(required = false) String keyword) {
        model.addAttribute("books", libraryService.searchBooks(keyword));
        model.addAttribute("keyword", keyword);
        return "index";
    }

    // --- ACTIONS ---
    @PostMapping("/book/add")
    public String add(@RequestParam String title, @RequestParam String author,
                      @RequestParam(required=false) String isbn, @RequestParam(required=false) String category,
                      @RequestParam(required=false) String publisher, @RequestParam(required=false) Integer year,
                      @RequestParam(required=false) String image, @RequestParam Integer totalQuantity) {
        libraryService.addBook(title, author, isbn, category, publisher, year, image, totalQuantity);
        return "redirect:/index";
    }

    @PostMapping("/book/edit")
    public String edit(@RequestParam Long id, @RequestParam String title, @RequestParam String author,
                       @RequestParam String isbn, @RequestParam String category, @RequestParam String publisher,
                       @RequestParam Integer year, @RequestParam String image) {
        libraryService.editBook(id, title, author, isbn, category, publisher, year, image);
        return "redirect:/index";
    }

    @GetMapping("/book/delete")
    public String delete(@RequestParam Long id) {
        libraryService.deleteBook(id);
        return "redirect:/index";
    }

    @PostMapping("/book/import")
    public String importBook(@RequestParam("legacyData") String data) {
        libraryService.importFromLegacy(data);
        return "redirect:/index";
    }

    @PostMapping("/book/borrow")
    public String borrow(@RequestParam Long id, @RequestParam String borrowerName) {
        libraryService.borrowBook(id, borrowerName);
        return "redirect:/index";
    }

    @GetMapping("/book/return")
    public String returnBook(@RequestParam Long id) { // id ở đây là requestId
        libraryService.returnBook(id);
        return "redirect:/index";
    }

    // --- AJAX API ---
    @GetMapping("/api/borrows")
    @ResponseBody
    public List<BorrowerDTO> getBorrowers(@RequestParam Long bookId) {
        List<BorrowRequest> list = borrowRepo.findByBookIdAndStatus(bookId, "BORROWING");
        List<BorrowerDTO> dtos = new ArrayList<>();
        for (BorrowRequest r : list) dtos.add(new BorrowerDTO(r.getId(), r.getBorrowerName(), r.getBorrowDate().toString()));
        return dtos;
    }

    // DTO Class
    static class BorrowerDTO {
        public Long requestId;
        public String name;
        public String date;
        public BorrowerDTO(Long r, String n, String d) { this.requestId = r; this.name = n; this.date = d; }
    }
}