package com.library.app.infrastructure;

import com.library.app.domain.Book;
import com.library.app.domain.BookBuilder;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class XmlBookAdapter {
    public List<Book> importBooks(String rawData) {
        List<Book> books = new ArrayList<>();
        // Giả lập parse chuỗi: "Title;Author;ISBN|Title2;Author2;ISBN2"
        String[] items = rawData.split("\\|");
        for (String item : items) {
            String[] parts = item.split(";");
            if (parts.length >= 2) {
                // Adapter gọi Builder để tạo Book
                Book book = new BookBuilder(parts[0], parts[1])
                            .setIsbn(parts.length > 2 ? parts[2] : null)
                            .setTotalQuantity(5) // Mặc định import 5 cuốn
                            .build();
                books.add(book);
            }
        }
        return books;
    }
}