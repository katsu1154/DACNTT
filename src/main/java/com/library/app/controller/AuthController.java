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

    // --- DASHBOARD (Đã thêm currentPage cho Sidebar) ---
    @GetMapping("/index")
    public String showIndex(Model model, @RequestParam(required = false) String keyword) {
        model.addAttribute("books", libraryService.searchBooks(keyword));
        // Gửi danh sách Category và User sang để hiện Dropdown
        model.addAttribute("categories", libraryService.getAllCategories());
        model.addAttribute("users", libraryService.getAllUsers());
        
        model.addAttribute("currentPage", "books"); // Active menu Sách
        return "index";
    }

    // --- TRANG QUẢN LÝ MƯỢN TRẢ (Mới) ---
    @GetMapping("/borrows")
    public String showBorrowHistory(Model model) {
        model.addAttribute("borrows", borrowRepo.findAll());
        model.addAttribute("currentPage", "borrows"); // Active menu Mượn trả
        return "borrows";
    }

    // --- ACTIONS ---

    // 1. SỬA: Nhận categoryId (Long) thay vì String name
    @PostMapping("/book/add")
    public String add(@RequestParam String title, @RequestParam String author,
                      @RequestParam(required=false) String isbn, 
                      @RequestParam Long categoryId, // <--- SỬA TẠI ĐÂY
                      @RequestParam(required=false) String publisher, @RequestParam(required=false) Integer year,
                      @RequestParam(required=false) String image, @RequestParam Integer totalQuantity) {
        
        // Gọi hàm service mới nhận ID
    	libraryService.addBook(title, author, isbn, categoryId, totalQuantity, image, publisher, year);
        return "redirect:/index";
    }

    // 2. SỬA: Nhận categoryId (Long)
    @PostMapping("/book/edit")
    public String edit(@RequestParam Long id, @RequestParam String title, @RequestParam String author,
                       @RequestParam String isbn, 
                       @RequestParam(required=false) Long categoryId, // <--- SỬA TẠI ĐÂY (để required=false phòng khi null)
                       @RequestParam String publisher,
                       @RequestParam Integer year, @RequestParam String image) {
        
        // Lưu ý: Bạn cần update hàm editBook trong Service để nhận categoryId nhé
        // Nếu chưa update service thì tạm thời logic edit category sẽ chưa chạy
        // libraryService.editBook(id, title, author, isbn, categoryId, publisher, year, image);
        
        return "redirect:/index";
    }

    @GetMapping("/book/delete")
    public String delete(@RequestParam Long id) {
        libraryService.deleteBook(id);
        return "redirect:/index";
    }

 
    // 3. SỬA: Nhận userId (Long) thay vì String name
    @PostMapping("/book/borrow")
    public String borrow(@RequestParam Long id, @RequestParam Long userId) { // <--- SỬA TẠI ĐÂY
        libraryService.borrowBook(id, userId);
        return "redirect:/index";
    }

    @GetMapping("/book/return")
    public String returnBook(@RequestParam Long id) { 
        libraryService.returnBook(id);
        return "redirect:/borrows"; // Trả xong thì về trang lịch sử mượn hay hơn
    }

    // --- AJAX API ---
    @GetMapping("/api/borrows")
    @ResponseBody
    public List<BorrowerDTO> getBorrowers(@RequestParam Long bookId) {
        List<BorrowRequest> list = borrowRepo.findByBookIdAndStatus(bookId, "BORROWING");
        List<BorrowerDTO> dtos = new ArrayList<>();
        for (BorrowRequest r : list) {
            // 4. SỬA: Lấy tên từ User Object
            String name = (r.getUser() != null) ? r.getUser().getFullName() : "Unknown";
            dtos.add(new BorrowerDTO(r.getId(), name, r.getBorrowDate().toString()));
        }
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