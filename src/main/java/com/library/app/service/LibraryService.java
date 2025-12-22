package com.library.app.service;

import com.library.app.domain.*;
import com.library.app.infrastructure.notification.NotificationAdapter;
import com.library.app.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class LibraryService {
    @Autowired private BookRepository bookRepository;
    @Autowired private BorrowRequestRepository borrowRequestRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    
    // Adapter thông báo (Mới)
    @Autowired private NotificationAdapter notificationService;

    // --- 1. CÁC HÀM GET DỮ LIỆU ---
    public List<Book> getAllBooks() { return bookRepository.findAll(); }
    public List<Category> getAllCategories() { return categoryRepository.findAll(); }
    public List<User> getAllUsers() { return userRepository.findAll(); }

    public List<Book> searchBooks(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword);
        }
        return bookRepository.findAll();
    }

    // --- 2. HÀM THÊM SÁCH ---
    public void addBook(String title, String author, String isbn, 
                        Long categoryId, Integer qty, String img,
                        String publisher, Integer year) {
        
        Category cat = (categoryId != null) ? categoryRepository.findById(categoryId).orElse(null) : null;

        Book newBook = new BookBuilder(title, author)
                .setIsbn(isbn)
                .setCategory(cat)
                .setTotalQuantity(qty)
                .setImage(img)
                .setPublisher(publisher)
                .setPublishYear(year)
                .build();
        
        bookRepository.save(newBook);
    }

    // --- 3. HÀM SỬA SÁCH ---
    public void editBook(Long id, String title, String author, String isbn, 
                         Long categoryId, String publisher, Integer year, String img) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book != null) {
            book.setTitle(title);
            book.setAuthor(author);
            book.setIsbn(isbn);
            
            if (categoryId != null) {
                Category cat = categoryRepository.findById(categoryId).orElse(null);
                book.setCategory(cat);
            }
            
            book.setPublisher(publisher);
            book.setPublishYear(year);
            book.setImage(img);
            
            bookRepository.save(book);
        }
    }

    public void deleteBook(Long id) { bookRepository.deleteById(id); }

    // --- 4. LOGIC MƯỢN SÁCH (Phiên bản chuẩn: Có User ID + Có Thông báo) ---
    public void borrowBook(Long bookId, Long userId) {
        Book book = bookRepository.findById(bookId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);

        if (book != null && user != null && book.getAvailableQuantity() > 0) {
            // 1. Trừ kho
            book.setAvailableQuantity(book.getAvailableQuantity() - 1);
            bookRepository.save(book);

            // 2. Tạo phiếu mượn
            BorrowRequest req = new BorrowRequest();
            req.setBook(book);
            req.setUser(user);
            req.setBorrowDate(LocalDate.now());
            req.setStatus("BORROWING");
            borrowRequestRepository.save(req);
            
            // 3. Gửi thông báo
            String msg = "Xin chào " + user.getFullName() + ", bạn đã mượn thành công cuốn sách: " + book.getTitle();
            notificationService.sendNotification(msg, user.getEmail());
        }
    }

    // --- 5. LOGIC TRẢ SÁCH (Phiên bản chuẩn: Có Thông báo) ---
    public void returnBook(Long requestId) {
        BorrowRequest req = borrowRequestRepository.findById(requestId).orElse(null);
        
        if (req != null && "BORROWING".equals(req.getStatus())) {
            // 1. Đổi trạng thái
            req.setStatus("RETURNED");
            req.setReturnDate(LocalDate.now());
            borrowRequestRepository.save(req);

            // 2. Cộng lại kho
            Book book = req.getBook();
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
            bookRepository.save(book);
            
            // 3. Gửi thông báo cảm ơn
            if (req.getUser() != null) {
                String msg = "Cảm ơn bạn đã trả sách đúng hạn. Hẹn gặp lại!";
                notificationService.sendNotification(msg, req.getUser().getEmail());
            }
        }
    }
}