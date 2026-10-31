import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Library library = new Library("data");

    public static void main(String[] args) {
        // Automatically populate demo data if empty
        if (library.getAllBooks().isEmpty() && library.getAllMembers().isEmpty()) {
            library.populateSampleData();
            try {
                library.saveToFile();
            } catch (IOException ignored) {
            }
        }

        boolean running = true;
        System.out.println("=================================================");
        System.out.println("       WELCOME TO LIBRARY MANAGEMENT SYSTEM      ");
        System.out.println("=================================================");

        while (running) {
            printMainMenu();
            String input = scanner.nextLine().trim();

            switch (input) {
                case "1":
                    handleBookManagement();
                    break;
                case "2":
                    handleMemberManagement();
                    break;
                case "3":
                    handleIssueBook();
                    break;
                case "4":
                    handleReturnBook();
                    break;
                case "5":
                    handleOverdueTracking();
                    break;
                case "6":
                    handleSearchAndSort();
                    break;
                case "7":
                    handleLoanHistory();
                    break;
                case "8":
                    handlePersistence();
                    break;
                case "9":
                    running = false;
                    try {
                        library.saveToFile();
                        System.out.println("-> All data automatically saved to 'data/' directory.");
                    } catch (IOException e) {
                        System.err.println("Failed to save data: " + e.getMessage());
                    }
                    System.out.println("\nThank you for using the Library Management System. Goodbye!");
                    break;
                default:
                    System.out.println("[!] Invalid choice. Please enter a number between 1 and 9.");
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n================ MAIN MENU ================");
        System.out.println("1. Book Management (Add, View, Remove)");
        System.out.println("2. Member Management (Register, View, Details)");
        System.out.println("3. Issue Book (Loan Transaction)");
        System.out.println("4. Return Book (Calculate Overdue & Fines)");
        System.out.println("5. Overdue Records & Fine Tracking");
        System.out.println("6. Search and Sort Books");
        System.out.println("7. View All Transaction / Loan Records");
        System.out.println("8. File Persistence (Save / Reload)");
        System.out.println("9. Exit Application");
        System.out.print("Enter choice (1-9): ");
    }

    // ==========================================
    // Book Management UI
    // ==========================================

    private static void handleBookManagement() {
        System.out.println("\n--- BOOK MANAGEMENT ---");
        System.out.println("1. Add New Book");
        System.out.println("2. View All Books");
        System.out.println("3. View Available Books Only");
        System.out.println("4. Remove Book");
        System.out.println("5. Return to Main Menu");
        System.out.print("Choose option (1-5): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                System.out.print("Enter Book ID (e.g., B101): ");
                String id = scanner.nextLine().trim();
                System.out.print("Enter ISBN: ");
                String isbn = scanner.nextLine().trim();
                System.out.print("Enter Title: ");
                String title = scanner.nextLine().trim();
                System.out.print("Enter Author: ");
                String author = scanner.nextLine().trim();
                System.out.print("Enter Category: ");
                String category = scanner.nextLine().trim();

                try {
                    library.addBook(new Book(id, isbn, title, author, category));
                    library.saveToFile();
                    System.out.println("[SUCCESS] Book added successfully!");
                } catch (LibraryException | IOException e) {
                    System.out.println("[ERROR] " + e.getMessage());
                }
                break;

            case "2":
                displayBookTable(library.getAllBooks(), "ALL BOOKS IN LIBRARY");
                break;

            case "3":
                displayBookTable(library.getAvailableBooks(), "AVAILABLE BOOKS");
                break;

            case "4":
                System.out.print("Enter Book ID to remove: ");
                String removeId = scanner.nextLine().trim();
                try {
                    library.removeBook(removeId);
                    library.saveToFile();
                    System.out.println("[SUCCESS] Book removed successfully.");
                } catch (BookNotFoundException | LibraryException | IOException e) {
                    System.out.println("[ERROR] " + e.getMessage());
                }
                break;

            case "5":
                return;

            default:
                System.out.println("[!] Invalid option.");
        }
    }

    // ==========================================
    // Member Management UI
    // ==========================================

    private static void handleMemberManagement() {
        System.out.println("\n--- MEMBER MANAGEMENT ---");
        System.out.println("1. Register New Member");
        System.out.println("2. View All Members");
        System.out.println("3. View Member Details");
        System.out.println("4. Return to Main Menu");
        System.out.print("Choose option (1-4): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                System.out.print("Enter Member ID (e.g., M101): ");
                String id = scanner.nextLine().trim();
                System.out.print("Enter Name: ");
                String name = scanner.nextLine().trim();
                System.out.print("Enter Email: ");
                String email = scanner.nextLine().trim();
                System.out.print("Enter Phone: ");
                String phone = scanner.nextLine().trim();

                try {
                    library.addMember(new Member(id, name, email, phone));
                    library.saveToFile();
                    System.out.println("[SUCCESS] Member registered successfully!");
                } catch (LibraryException | IOException e) {
                    System.out.println("[ERROR] " + e.getMessage());
                }
                break;

            case "2":
                displayMemberTable(library.getAllMembers());
                break;

            case "3":
                System.out.print("Enter Member ID: ");
                String mId = scanner.nextLine().trim();
                try {
                    Member member = library.getMember(mId);
                    System.out.println("\nMember Details:");
                    System.out.println("------------------------------------------");
                    System.out.println("ID:       " + member.getMemberId());
                    System.out.println("Name:     " + member.getName());
                    System.out.println("Email:    " + member.getEmail());
                    System.out.println("Phone:    " + member.getPhone());
                    System.out.println("Borrowed Books (" + member.getBorrowedBookIds().size() + "):");
                    if (member.getBorrowedBookIds().isEmpty()) {
                        System.out.println("  (None)");
                    } else {
                        for (String bId : member.getBorrowedBookIds()) {
                            try {
                                Book b = library.getBook(bId);
                                System.out.println("  - [" + b.getId() + "] " + b.getTitle() + " (Due: " + b.getDueDate() + ")");
                            } catch (BookNotFoundException e) {
                                System.out.println("  - [" + bId + "] (Book record not found)");
                            }
                        }
                    }
                    System.out.println("------------------------------------------");
                } catch (MemberNotFoundException e) {
                    System.out.println("[ERROR] " + e.getMessage());
                }
                break;

            case "4":
                return;

            default:
                System.out.println("[!] Invalid option.");
        }
    }

    // ==========================================
    // Book Issue and Return UI
    // ==========================================

    private static void handleIssueBook() {
        System.out.println("\n--- ISSUE BOOK (LOAN TRANSACTION) ---");
        System.out.print("Enter Book ID to issue: ");
        String bookId = scanner.nextLine().trim();
        System.out.print("Enter Member ID borrowing the book: ");
        String memberId = scanner.nextLine().trim();

        try {
            Loan loan = library.issueBook(bookId, memberId);
            library.saveToFile();
            System.out.println("\n[SUCCESS] Book issued successfully!");
            System.out.println("------------------------------------------");
            System.out.println("Transaction Loan ID: " + loan.getLoanId());
            System.out.println("Book ID:             " + loan.getBookId());
            System.out.println("Member ID:           " + loan.getMemberId());
            System.out.println("Issue Date:          " + loan.getIssueDate());
            System.out.println("Due Date:            " + loan.getDueDate() + " (" + Library.DAYS_ALLOWED + " days allowed)");
            System.out.println("------------------------------------------");
        } catch (BookNotFoundException | MemberNotFoundException | BookUnavailableException | IOException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void handleReturnBook() {
        System.out.println("\n--- RETURN BOOK TRANSACTION ---");
        System.out.print("Enter Book ID to return: ");
        String bookId = scanner.nextLine().trim();
        System.out.print("Enter Member ID returning the book: ");
        String memberId = scanner.nextLine().trim();

        try {
            Loan loan = library.returnBook(bookId, memberId);
            library.saveToFile();
            System.out.println("\n[SUCCESS] Book returned successfully!");
            System.out.println("------------------------------------------");
            System.out.println("Loan ID:      " + loan.getLoanId());
            System.out.println("Book ID:      " + loan.getBookId());
            System.out.println("Member ID:    " + loan.getMemberId());
            System.out.println("Due Date:     " + loan.getDueDate());
            System.out.println("Return Date:  " + loan.getReturnDate());
            if (loan.getFineAmount() > 0) {
                long overdueDays = loan.getOverdueDays(loan.getReturnDate());
                System.out.printf("[ALERT] Returned late! Overdue by %d days. Fine charged: $%.2f%n",
                        overdueDays, loan.getFineAmount());
            } else {
                System.out.println("[INFO] Returned on time. No overdue fines.");
            }
            System.out.println("------------------------------------------");
        } catch (BookNotFoundException | MemberNotFoundException | LibraryException | IOException e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    // ==========================================
    // Overdue Tracking UI
    // ==========================================

    private static void handleOverdueTracking() {
        System.out.println("\n--- OVERDUE RECORDS & FINE TRACKING ---");
        List<Loan> overdueLoans = library.getOverdueLoans();

        if (overdueLoans.isEmpty()) {
            System.out.println("[INFO] No overdue books at this time. All active loans are within their due dates!");
            return;
        }

        System.out.printf("%-10s %-10s %-12s %-12s %-10s %-10s%n",
                "Loan ID", "Book ID", "Member ID", "Due Date", "Days Late", "Est. Fine");
        System.out.println("----------------------------------------------------------------------");
        double totalFines = 0.0;
        for (Loan l : overdueLoans) {
            long daysLate = l.getOverdueDays(LocalDate.now());
            double estFine = l.calculateFine(LocalDate.now(), Library.FINE_PER_DAY);
            totalFines += estFine;
            System.out.printf("%-10s %-10s %-12s %-12s %-10d $%-9.2f%n",
                    l.getLoanId(), l.getBookId(), l.getMemberId(), l.getDueDate(), daysLate, estFine);
        }
        System.out.println("----------------------------------------------------------------------");
        System.out.printf("Total Outstanding Overdue Fines: $%.2f%n", totalFines);
    }

    // ==========================================
    // Search and Sort UI
    // ==========================================

    private static void handleSearchAndSort() {
        System.out.println("\n--- SEARCH AND SORT BOOKS ---");
        System.out.println("1. Search Books by Title");
        System.out.println("2. Search Books by Author");
        System.out.println("3. Search Books by Category");
        System.out.println("4. Search Books by ISBN");
        System.out.println("5. Global Keyword Search");
        System.out.println("6. Sort Books by Title");
        System.out.println("7. Sort Books by Author");
        System.out.println("8. Sort Books by ID");
        System.out.println("9. Sort Books by Category");
        System.out.println("10. Return to Main Menu");
        System.out.print("Choose option (1-10): ");

        String choice = scanner.nextLine().trim();
        List<Book> result;
        switch (choice) {
            case "1":
                System.out.print("Enter Title keyword: ");
                result = library.searchByTitle(scanner.nextLine());
                displayBookTable(result, "SEARCH RESULTS BY TITLE");
                break;
            case "2":
                System.out.print("Enter Author keyword: ");
                result = library.searchByAuthor(scanner.nextLine());
                displayBookTable(result, "SEARCH RESULTS BY AUTHOR");
                break;
            case "3":
                System.out.print("Enter Category keyword: ");
                result = library.searchByCategory(scanner.nextLine());
                displayBookTable(result, "SEARCH RESULTS BY CATEGORY");
                break;
            case "4":
                System.out.print("Enter ISBN: ");
                result = library.searchByIsbn(scanner.nextLine());
                displayBookTable(result, "SEARCH RESULTS BY ISBN");
                break;
            case "5":
                System.out.print("Enter general search query: ");
                result = library.searchBooks(scanner.nextLine());
                displayBookTable(result, "GLOBAL SEARCH RESULTS");
                break;
            case "6":
                System.out.print("Sort order (1 for Ascending A-Z, 2 for Descending Z-A): ");
                boolean ascTitle = !"2".equals(scanner.nextLine().trim());
                result = library.sortBooksByTitle(ascTitle);
                displayBookTable(result, "BOOKS SORTED BY TITLE (" + (ascTitle ? "A-Z" : "Z-A") + ")");
                break;
            case "7":
                System.out.print("Sort order (1 for Ascending A-Z, 2 for Descending Z-A): ");
                boolean ascAuth = !"2".equals(scanner.nextLine().trim());
                result = library.sortBooksByAuthor(ascAuth);
                displayBookTable(result, "BOOKS SORTED BY AUTHOR (" + (ascAuth ? "A-Z" : "Z-A") + ")");
                break;
            case "8":
                System.out.print("Sort order (1 for Ascending, 2 for Descending): ");
                boolean ascId = !"2".equals(scanner.nextLine().trim());
                result = library.sortBooksById(ascId);
                displayBookTable(result, "BOOKS SORTED BY ID (" + (ascId ? "Asc" : "Desc") + ")");
                break;
            case "9":
                System.out.print("Sort order (1 for Ascending A-Z, 2 for Descending Z-A): ");
                boolean ascCat = !"2".equals(scanner.nextLine().trim());
                result = library.sortBooksByCategory(ascCat);
                displayBookTable(result, "BOOKS SORTED BY CATEGORY (" + (ascCat ? "A-Z" : "Z-A") + ")");
                break;
            case "10":
                return;
            default:
                System.out.println("[!] Invalid option.");
        }
    }

    // ==========================================
    // Loan History UI
    // ==========================================

    private static void handleLoanHistory() {
        System.out.println("\n--- ALL TRANSACTION / LOAN RECORDS ---");
        List<Loan> loans = library.getAllLoans();
        if (loans.isEmpty()) {
            System.out.println("No loans on record.");
            return;
        }

        System.out.printf("%-8s %-8s %-10s %-12s %-12s %-12s %-10s %-10s%n",
                "Loan ID", "Book ID", "Member ID", "Issued", "Due", "Returned", "Status", "Fine");
        System.out.println("--------------------------------------------------------------------------------------------");
        for (Loan l : loans) {
            String retDateStr = l.getReturnDate() != null ? l.getReturnDate().toString() : "-";
            String status = l.isReturned() ? "RETURNED" : (l.isOverdue(LocalDate.now()) ? "OVERDUE" : "ACTIVE");
            System.out.printf("%-8s %-8s %-10s %-12s %-12s %-12s %-10s $%-9.2f%n",
                    l.getLoanId(), l.getBookId(), l.getMemberId(), l.getIssueDate(),
                    l.getDueDate(), retDateStr, status, l.getFineAmount());
        }
        System.out.println("--------------------------------------------------------------------------------------------");
    }

    // ==========================================
    // File Persistence UI
    // ==========================================

    private static void handlePersistence() {
        System.out.println("\n--- FILE PERSISTENCE ---");
        System.out.println("1. Save Data to Disk Now");
        System.out.println("2. Reload Data from Disk");
        System.out.println("3. Populate Demo Data (Reset)");
        System.out.println("4. Return to Main Menu");
        System.out.print("Choose option (1-4): ");

        String choice = scanner.nextLine().trim();
        switch (choice) {
            case "1":
                try {
                    library.saveToFile();
                    System.out.println("[SUCCESS] Successfully saved books, members, and loans to CSV files in 'data/'.");
                } catch (IOException e) {
                    System.out.println("[ERROR] Failed to save data: " + e.getMessage());
                }
                break;
            case "2":
                try {
                    library.loadFromFile();
                    System.out.println("[SUCCESS] Successfully reloaded data from disk!");
                    System.out.printf("Loaded %d books, %d members, %d loans.%n",
                            library.getAllBooks().size(), library.getAllMembers().size(), library.getAllLoans().size());
                } catch (IOException e) {
                    System.out.println("[ERROR] Failed to load data: " + e.getMessage());
                }
                break;
            case "3":
                library.populateSampleData();
                try {
                    library.saveToFile();
                    System.out.println("[SUCCESS] Populated sample books, members, and demo loans.");
                } catch (IOException e) {
                    System.out.println("[ERROR] " + e.getMessage());
                }
                break;
            case "4":
                return;
            default:
                System.out.println("[!] Invalid option.");
        }
    }

    // ==========================================
    // Table Formatters
    // ==========================================

    private static void displayBookTable(List<Book> bookList, String headerTitle) {
        System.out.println("\n--- " + headerTitle + " (" + bookList.size() + ") ---");
        if (bookList.isEmpty()) {
            System.out.println("No books found matching the criteria.");
            return;
        }

        System.out.printf("%-8s %-16s %-30s %-22s %-20s %-12s%n",
                "ID", "ISBN", "Title", "Author", "Category", "Status");
        System.out.println("----------------------------------------------------------------------------------------------------------------");
        for (Book b : bookList) {
            String title = truncate(b.getTitle(), 28);
            String author = truncate(b.getAuthor(), 20);
            String category = truncate(b.getCategory(), 18);
            String status = b.isIssued() ? ("ISSUED (" + b.getDueDate() + ")") : "AVAILABLE";

            System.out.printf("%-8s %-16s %-30s %-22s %-20s %-12s%n",
                    b.getId(), b.getIsbn(), title, author, category, status);
        }
        System.out.println("----------------------------------------------------------------------------------------------------------------");
    }

    private static void displayMemberTable(List<Member> memberList) {
        System.out.println("\n--- REGISTERED MEMBERS (" + memberList.size() + ") ---");
        if (memberList.isEmpty()) {
            System.out.println("No members registered.");
            return;
        }

        System.out.printf("%-8s %-20s %-25s %-16s %-14s%n",
                "ID", "Name", "Email", "Phone", "Books Held");
        System.out.println("-----------------------------------------------------------------------------------------");
        for (Member m : memberList) {
            System.out.printf("%-8s %-20s %-25s %-16s %-14d%n",
                    m.getMemberId(), truncate(m.getName(), 18), truncate(m.getEmail(), 23),
                    m.getPhone(), m.getBorrowedBookIds().size());
        }
        System.out.println("-----------------------------------------------------------------------------------------");
    }

    private static String truncate(String text, int maxLen) {
        if (text == null) return "";
        if (text.length() <= maxLen) return text;
        return text.substring(0, maxLen - 3) + "...";
    }
}
