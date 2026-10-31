import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Library {
    public static final int DAYS_ALLOWED = 14;
    public static final double FINE_PER_DAY = 1.50;

    private final Map<String, Book> books;
    private final Map<String, Member> members;
    private final List<Loan> loans;
    private final String dataDirectory;
    private int nextLoanIdCounter = 1001;

    public Library() {
        this("data");
    }

    public Library(String dataDirectory) {
        this.books = new LinkedHashMap<>();
        this.members = new LinkedHashMap<>();
        this.loans = new ArrayList<>();
        this.dataDirectory = dataDirectory;

        // Auto-load if data files exist
        File dir = new File(dataDirectory);
        if (dir.exists()) {
            try {
                loadFromFile();
            } catch (IOException e) {
                System.err.println("Note: Could not automatically load existing data: " + e.getMessage());
            }
        }
    }

    // ==========================================
    // Book Management
    // ==========================================

    public synchronized void addBook(Book book) throws LibraryException {
        if (book == null) {
            throw new LibraryException("Book details cannot be null.");
        }
        if (book.getId() == null || book.getId().trim().isEmpty()) {
            throw new LibraryException("Book ID cannot be empty.");
        }
        if (books.containsKey(book.getId())) {
            throw new LibraryException("A book with ID '" + book.getId() + "' already exists.");
        }
        books.put(book.getId(), book);
    }

    public synchronized void removeBook(String bookId) throws BookNotFoundException, LibraryException {
        Book book = getBook(bookId);
        if (book.isIssued()) {
            throw new LibraryException("Cannot remove book '" + bookId + "' because it is currently issued.");
        }
        books.remove(bookId);
    }

    public Book getBook(String bookId) throws BookNotFoundException {
        if (bookId == null || !books.containsKey(bookId.trim())) {
            throw new BookNotFoundException("Book with ID '" + bookId + "' was not found.");
        }
        return books.get(bookId.trim());
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(books.values());
    }

    public List<Book> getAvailableBooks() {
        List<Book> available = new ArrayList<>();
        for (Book b : books.values()) {
            if (!b.isIssued()) {
                available.add(b);
            }
        }
        return available;
    }

    // ==========================================
    // Member Management
    // ==========================================

    public synchronized void addMember(Member member) throws LibraryException {
        if (member == null) {
            throw new LibraryException("Member details cannot be null.");
        }
        if (member.getMemberId() == null || member.getMemberId().trim().isEmpty()) {
            throw new LibraryException("Member ID cannot be empty.");
        }
        if (members.containsKey(member.getMemberId())) {
            throw new LibraryException("A member with ID '" + member.getMemberId() + "' already exists.");
        }
        members.put(member.getMemberId(), member);
    }

    public Member getMember(String memberId) throws MemberNotFoundException {
        if (memberId == null || !members.containsKey(memberId.trim())) {
            throw new MemberNotFoundException("Member with ID '" + memberId + "' was not found.");
        }
        return members.get(memberId.trim());
    }

    public List<Member> getAllMembers() {
        return new ArrayList<>(members.values());
    }

    public synchronized void removeMember(String memberId) throws MemberNotFoundException, LibraryException {
        Member member = getMember(memberId);
        if (!member.getBorrowedBookIds().isEmpty()) {
            throw new LibraryException("Cannot remove member '" + memberId + "' because they have unreturned books.");
        }
        members.remove(memberId);
    }

    // ==========================================
    // Issue and Return Functionality
    // ==========================================

    public synchronized Loan issueBook(String bookId, String memberId)
            throws BookNotFoundException, MemberNotFoundException, BookUnavailableException {
        return issueBook(bookId, memberId, LocalDate.now());
    }

    public synchronized Loan issueBook(String bookId, String memberId, LocalDate issueDate)
            throws BookNotFoundException, MemberNotFoundException, BookUnavailableException {
        Book book = getBook(bookId);
        Member member = getMember(memberId);

        if (book.isIssued()) {
            throw new BookUnavailableException("Book '" + book.getTitle() + "' (ID: " + bookId + ") is already issued.");
        }

        LocalDate issue = issueDate != null ? issueDate : LocalDate.now();
        LocalDate due = issue.plusDays(DAYS_ALLOWED);

        String loanId = "L" + (nextLoanIdCounter++);
        while (findLoanById(loanId) != null) {
            loanId = "L" + (nextLoanIdCounter++);
        }

        Loan loan = new Loan(loanId, book.getId(), member.getMemberId(), issue, due);
        loans.add(loan);

        book.setIssued(true);
        book.setDueDate(due);
        member.borrowBook(book.getId());

        return loan;
    }

    public synchronized Loan returnBook(String bookId, String memberId)
            throws BookNotFoundException, MemberNotFoundException, LibraryException {
        return returnBook(bookId, memberId, LocalDate.now());
    }

    public synchronized Loan returnBook(String bookId, String memberId, LocalDate returnDate)
            throws BookNotFoundException, MemberNotFoundException, LibraryException {
        Book book = getBook(bookId);
        Member member = getMember(memberId);

        if (!book.isIssued()) {
            throw new LibraryException("Book '" + book.getTitle() + "' is not marked as issued.");
        }
        if (!member.getBorrowedBookIds().contains(book.getId())) {
            throw new LibraryException("Book '" + book.getId() + "' is not currently borrowed by member '" + member.getName() + "'.");
        }

        Loan activeLoan = null;
        for (int i = loans.size() - 1; i >= 0; i--) {
            Loan l = loans.get(i);
            if (!l.isReturned() && l.getBookId().equals(book.getId()) && l.getMemberId().equals(member.getMemberId())) {
                activeLoan = l;
                break;
            }
        }

        if (activeLoan == null) {
            throw new LibraryException("No active loan record found for Book ID '" + bookId + "' and Member ID '" + memberId + "'.");
        }

        LocalDate retDate = returnDate != null ? returnDate : LocalDate.now();
        activeLoan.setReturnDate(retDate);
        activeLoan.setReturned(true);

        double fine = activeLoan.calculateFine(retDate, FINE_PER_DAY);
        activeLoan.setFineAmount(fine);

        book.setIssued(false);
        book.setDueDate(null);
        member.returnBook(book.getId());

        return activeLoan;
    }

    // ==========================================
    // Overdue Tracking & Loan Inquiries
    // ==========================================

    public List<Loan> getActiveLoans() {
        List<Loan> active = new ArrayList<>();
        for (Loan l : loans) {
            if (!l.isReturned()) {
                active.add(l);
            }
        }
        return active;
    }

    public List<Loan> getAllLoans() {
        return new ArrayList<>(loans);
    }

    public Loan findLoanById(String loanId) {
        if (loanId == null) return null;
        for (Loan l : loans) {
            if (l.getLoanId().equalsIgnoreCase(loanId.trim())) {
                return l;
            }
        }
        return null;
    }

    public List<Loan> getOverdueLoans() {
        return getOverdueLoans(LocalDate.now());
    }

    public List<Loan> getOverdueLoans(LocalDate asOfDate) {
        List<Loan> overdue = new ArrayList<>();
        for (Loan l : loans) {
            if (!l.isReturned() && l.isOverdue(asOfDate)) {
                overdue.add(l);
            }
        }
        return overdue;
    }

    // ==========================================
    // Book Search and Sorting
    // ==========================================

    public List<Book> searchByTitle(String query) {
        if (query == null || query.trim().isEmpty()) return getAllBooks();
        String q = query.trim().toLowerCase();
        List<Book> result = new ArrayList<>();
        for (Book b : books.values()) {
            if (b.getTitle().toLowerCase().contains(q)) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Book> searchByAuthor(String query) {
        if (query == null || query.trim().isEmpty()) return getAllBooks();
        String q = query.trim().toLowerCase();
        List<Book> result = new ArrayList<>();
        for (Book b : books.values()) {
            if (b.getAuthor().toLowerCase().contains(q)) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Book> searchByCategory(String query) {
        if (query == null || query.trim().isEmpty()) return getAllBooks();
        String q = query.trim().toLowerCase();
        List<Book> result = new ArrayList<>();
        for (Book b : books.values()) {
            if (b.getCategory().toLowerCase().contains(q)) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Book> searchByIsbn(String query) {
        if (query == null || query.trim().isEmpty()) return getAllBooks();
        String q = query.trim().toLowerCase();
        List<Book> result = new ArrayList<>();
        for (Book b : books.values()) {
            if (b.getIsbn().toLowerCase().contains(q)) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Book> searchBooks(String query) {
        if (query == null || query.trim().isEmpty()) return getAllBooks();
        String q = query.trim().toLowerCase();
        List<Book> result = new ArrayList<>();
        for (Book b : books.values()) {
            if (b.getTitle().toLowerCase().contains(q) ||
                b.getAuthor().toLowerCase().contains(q) ||
                b.getCategory().toLowerCase().contains(q) ||
                b.getIsbn().toLowerCase().contains(q) ||
                b.getId().toLowerCase().contains(q)) {
                result.add(b);
            }
        }
        return result;
    }

    public List<Book> sortBooksByTitle(boolean ascending) {
        List<Book> list = getAllBooks();
        list.sort(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER));
        if (!ascending) Collections.reverse(list);
        return list;
    }

    public List<Book> sortBooksByAuthor(boolean ascending) {
        List<Book> list = getAllBooks();
        list.sort(Comparator.comparing(Book::getAuthor, String.CASE_INSENSITIVE_ORDER));
        if (!ascending) Collections.reverse(list);
        return list;
    }

    public List<Book> sortBooksById(boolean ascending) {
        List<Book> list = getAllBooks();
        list.sort(Comparator.comparing(Book::getId, String.CASE_INSENSITIVE_ORDER));
        if (!ascending) Collections.reverse(list);
        return list;
    }

    public List<Book> sortBooksByCategory(boolean ascending) {
        List<Book> list = getAllBooks();
        list.sort(Comparator.comparing(Book::getCategory, String.CASE_INSENSITIVE_ORDER));
        if (!ascending) Collections.reverse(list);
        return list;
    }

    // ==========================================
    // File-based Persistence
    // ==========================================

    public synchronized void saveToFile() throws IOException {
        File dir = new File(dataDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // Save books
        File booksFile = new File(dir, "books.csv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(booksFile))) {
            bw.write("id,isbn,title,author,category,isIssued,dueDate\n");
            for (Book book : books.values()) {
                bw.write(book.toCsv());
                bw.newLine();
            }
        }

        // Save members
        File membersFile = new File(dir, "members.csv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(membersFile))) {
            bw.write("memberId,name,email,phone,borrowedBookIds\n");
            for (Member member : members.values()) {
                bw.write(member.toCsv());
                bw.newLine();
            }
        }

        // Save loans
        File loansFile = new File(dir, "loans.csv");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(loansFile))) {
            bw.write("loanId,bookId,memberId,issueDate,dueDate,returnDate,fineAmount,isReturned\n");
            for (Loan loan : loans) {
                bw.write(loan.toCsv());
                bw.newLine();
            }
        }
    }

    public synchronized void loadFromFile() throws IOException {
        File dir = new File(dataDirectory);
        if (!dir.exists()) {
            return;
        }

        // Load books
        File booksFile = new File(dir, "books.csv");
        if (booksFile.exists()) {
            books.clear();
            try (BufferedReader br = new BufferedReader(new FileReader(booksFile))) {
                String line = br.readLine(); // Header
                while ((line = br.readLine()) != null) {
                    Book b = Book.fromCsv(line);
                    if (b != null) {
                        books.put(b.getId(), b);
                    }
                }
            }
        }

        // Load members
        File membersFile = new File(dir, "members.csv");
        if (membersFile.exists()) {
            members.clear();
            try (BufferedReader br = new BufferedReader(new FileReader(membersFile))) {
                String line = br.readLine(); // Header
                while ((line = br.readLine()) != null) {
                    Member m = Member.fromCsv(line);
                    if (m != null) {
                        members.put(m.getMemberId(), m);
                    }
                }
            }
        }

        // Load loans
        File loansFile = new File(dir, "loans.csv");
        if (loansFile.exists()) {
            loans.clear();
            int maxId = 1000;
            try (BufferedReader br = new BufferedReader(new FileReader(loansFile))) {
                String line = br.readLine(); // Header
                while ((line = br.readLine()) != null) {
                    Loan l = Loan.fromCsv(line);
                    if (l != null) {
                        loans.add(l);
                        if (l.getLoanId().startsWith("L")) {
                            try {
                                int idNum = Integer.parseInt(l.getLoanId().substring(1));
                                if (idNum > maxId) {
                                    maxId = idNum;
                                }
                            } catch (NumberFormatException ignored) {
                            }
                        }
                    }
                }
            }
            nextLoanIdCounter = maxId + 1;
        }
    }

    public synchronized void populateSampleData() {
        try {
            if (books.isEmpty()) {
                addBook(new Book("B001", "978-0134685991", "Effective Java", "Joshua Bloch", "Computer Science"));
                addBook(new Book("B002", "978-0596009205", "Head First Design Patterns", "Eric Freeman", "Software Engineering"));
                addBook(new Book("B003", "978-0132350884", "Clean Code", "Robert C. Martin", "Software Engineering"));
                addBook(new Book("B004", "978-0451524935", "1984", "George Orwell", "Fiction"));
                addBook(new Book("B005", "978-0061120084", "To Kill a Mockingbird", "Harper Lee", "Classic Literature"));
                addBook(new Book("B006", "978-0321356680", "Effective C++", "Scott Meyers", "Computer Science"));
            }
            if (members.isEmpty()) {
                addMember(new Member("M001", "Alice Johnson", "alice@example.com", "+1-555-0101"));
                addMember(new Member("M002", "Bob Smith", "bob@example.com", "+1-555-0102"));
                addMember(new Member("M003", "Charlie Davis", "charlie@example.com", "+1-555-0103"));
            }
            if (loans.isEmpty()) {
                // Add a sample active loan that is currently overdue for demo purposes
                LocalDate oldIssue = LocalDate.now().minusDays(20);
                issueBook("B001", "M001", oldIssue);
            }
        } catch (Exception e) {
            System.err.println("Note during sample data setup: " + e.getMessage());
        }
    }

    // Display helper methods (for backward compatibility)
    public void displayBooks() {
        if (books.isEmpty()) {
            System.out.println("No books in library.");
            return;
        }
        for (Book book : books.values()) {
            System.out.println(book);
        }
    }

    public void displayMembers() {
        if (members.isEmpty()) {
            System.out.println("No members in library.");
            return;
        }
        for (Member member : members.values()) {
            System.out.println(member);
        }
    }
}
