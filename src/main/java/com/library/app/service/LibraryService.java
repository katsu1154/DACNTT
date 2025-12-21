package com.library.app.service;

import com.library.app.domain.Book;
import com.library.app.domain.BookBuilder;
import com.library.app.domain.BorrowRequest;
import com.library.app.infrastructure.XmlBookAdapter;
import com.library.app.repository.BookRepository;
import com.library.app.repository.BorrowRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class LibraryService {
    @Autowired private BookRepository bookRepository;
    @Autowired private BorrowRequestRepository borrowRequestRepository;
    @Autowired private XmlBookAdapter xmlBookAdapter;

    public List<Book> searchBooks(String keyword) {
        if (keyword != null && !keyword.isEmpty()) {
            return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(keyword, keyword);
        }
        return bookRepository.findAll();
    }

    public List<Book> getAllBooks() { return bookRepository.findAll(); }

    public void addBook(String title, String author, String isbn, String cat, String pub, Integer year, String img, Integer qty) {
        Book newBook = new BookBuilder(title, author)
                .setIsbn(isbn).setCategory(cat).setPublisher(pub)
                .setPublishYear(year).setImage(img).setTotalQuantity(qty)
                .build();
        bookRepository.save(newBook);
    }

    public void editBook(Long id, String title, String author, String isbn, String cat, String pub, Integer year, String img) {
        Book book = bookRepository.findById(id).orElse(null);
        if (book != null) {
            book.setTitle(title); book.setAuthor(author); book.setIsbn(isbn);
            book.setCategory(cat); book.setPublisher(pub); book.setPublishYear(year); book.setImage(img);
            bookRepository.save(book);
        }
    }

    public void deleteBook(Long id) { bookRepository.deleteById(id); }

    public void importFromLegacy(String data) {
        List<Book> books = xmlBookAdapter.importBooks(data);
        bookRepository.saveAll(books);
    }

    // --- LOGIC MƯỢN TRẢ ---
    public void borrowBook(Long bookId, String borrowerName) {
        Book book = bookRepository.findById(bookId).orElse(null);
        if (book != null && book.getAvailableQuantity() > 0) {
            book.setAvailableQuantity(book.getAvailableQuantity() - 1);
            bookRepository.save(book);

            BorrowRequest req = new BorrowRequest();
            req.setBook(book);
            req.setBorrowerName(borrowerName);
            req.setBorrowDate(LocalDate.now());
            req.setStatus("BORROWING");
            borrowRequestRepository.save(req);
        }
    }

    public void returnBook(Long requestId) {
        BorrowRequest req = borrowRequestRepository.findById(requestId).orElse(null);
        if (req != null && "BORROWING".equals(req.getStatus())) {
            req.setStatus("RETURNED");
            req.setReturnDate(LocalDate.now());
            borrowRequestRepository.save(req);

            Book book = req.getBook();
            book.setAvailableQuantity(book.getAvailableQuantity() + 1);
            bookRepository.save(book);
        }
    }
}