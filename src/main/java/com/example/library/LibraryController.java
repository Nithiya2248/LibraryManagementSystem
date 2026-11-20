package com.example.library;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api")
public class LibraryController {
    private final Map<String, Account> accounts = new HashMap<>();
    private final List<Book> books = new ArrayList<>();
    private final List<Loan> loans = new ArrayList<>();
    private final AtomicInteger nextBookId = new AtomicInteger(4);
    private final AtomicInteger nextLoanId = new AtomicInteger(1);

    public LibraryController() {
        books.add(new Book(1, "The Alchemist", "Paulo Coelho", "Fiction", "images/alchemist.svg", true));
        books.add(new Book(2, "Wings of Fire", "A. P. J. Abdul Kalam", "Biography", "images/wings.svg", true));
        books.add(new Book(3, "Atomic Habits", "James Clear", "Self-help", "images/atomic.svg", true));
        // Staff demo accounts are pre-registered for the assignment demonstration.
        accounts.put("librarian", new Account("librarian", "1234", "Library Staff", "librarian"));
        accounts.put("admin", new Account("admin", "1234", "Administrator", "admin"));
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody RegisterRequest request) {
        String username = clean(request.username());
        String name = clean(request.name());
        String password = request.password() == null ? "" : request.password();
        if (name.length() < 2 || !username.matches("[A-Za-z0-9_]{4,20}") || password.length() < 6) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Enter a name (at least 2 characters), a username (4–20 letters/numbers/underscore), and a password with at least 6 characters.");
        }
        if (accounts.containsKey(username.toLowerCase())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "That username is already registered. Choose another one.");
        }
        accounts.put(username.toLowerCase(Locale.ROOT), new Account(username, password, name, "member"));
        return Map.of("message", "Registration successful. Please log in with your new account.", "username", username.toLowerCase(Locale.ROOT));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {
        String username = request.username() == null ? "" : request.username().trim().toLowerCase(Locale.ROOT);
        Account account = accounts.get(username);
        if (account == null || !account.password.equals(request.password())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password. Please register first.");
        }
        return Map.of("username", username, "name", account.name, "role", account.role);
    }

    @GetMapping("/books")
    public List<Book> getBooks() { return books; }

    @PostMapping("/books")
    public Book addBook(@RequestBody BookRequest request) {
        Book book = new Book(nextBookId.getAndIncrement(), clean(request.title()), clean(request.author()),
                clean(request.category()), "images/default-book.svg", true);
        books.add(book);
        return book;
    }

    @DeleteMapping("/books/{id}")
    public Map<String, String> deleteBook(@PathVariable int id) {
        books.removeIf(book -> book.id() == id);
        return Map.of("message", "Book removed.");
    }

    @PostMapping("/borrow/{id}")
    public Map<String, String> borrow(@PathVariable int id, @RequestParam String username) {
        Book book = books.stream().filter(b -> b.id() == id).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found."));
        if (!book.available()) throw new ResponseStatusException(HttpStatus.CONFLICT, "This book is already borrowed.");
        replaceBookAvailability(id, false);
        loans.add(new Loan(nextLoanId.getAndIncrement(), username, book.title(), "Borrowed"));
        return Map.of("message", "Book borrowed successfully.");
    }

    @GetMapping("/loans")
    public List<Loan> getLoans(@RequestParam(required = false) String username) {
        if (username == null || username.isBlank()) return loans;
        return loans.stream().filter(l -> l.username().equalsIgnoreCase(username)).toList();
    }

    @PostMapping("/return/{id}")
    public Map<String, String> returnBook(@PathVariable int id, @RequestParam String username) {
        Loan loan = loans.stream().filter(l -> l.id() == id && l.username().equalsIgnoreCase(username)
                && l.status().equals("Borrowed")).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Active loan not found."));
        loans.set(loans.indexOf(loan), new Loan(loan.id(), loan.username(), loan.bookTitle(), "Returned"));
        books.stream().filter(b -> b.title().equals(loan.bookTitle())).findFirst()
                .ifPresent(b -> replaceBookAvailability(b.id(), true));
        return Map.of("message", "Book returned successfully.");
    }

    private void replaceBookAvailability(int id, boolean available) {
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            if (b.id() == id) {
                books.set(i, new Book(b.id(), b.title(), b.author(), b.category(), b.image(), available));
                return;
            }
        }
    }

    private String clean(String value) { return value == null ? "" : value.trim(); }

    public record RegisterRequest(String name, String username, String password) {}
    public record LoginRequest(String username, String password) {}
    public record BookRequest(String title, String author, String category) {}
    public record Book(int id, String title, String author, String category, String image, boolean available) {}
    public record Loan(int id, String username, String bookTitle, String status) {}
    private static class Account {
        final String username, password, name, role;
        Account(String username, String password, String name, String role) {
            this.username = username; this.password = password; this.name = name; this.role = role;
        }
    }
}
