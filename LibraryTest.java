import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class LibraryTest {
    private static int totalTests = 0;
    private static int passedTests = 0;

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      RUNNING LIBRARY SYSTEM AUTOMATED TESTS     ");
        System.out.println("=================================================");

        // Clean test directory
        File testDir = new File("test_data");
        deleteDirectory(testDir);

        Library lib = new Library("test_data");

        testBookManagement(lib);
        testMemberManagement(lib);
        testSearchAndSort(lib);
        testIssueAndReturn(lib);
        testExceptionHandling(lib);
        testOverdueTracking(lib);
        testFilePersistence();

        // Clean test directory after tests
        deleteDirectory(testDir);

        System.out.println("\n=================================================");
        System.out.printf("TEST SUMMARY: %d / %d PASSED (%s)%n",
                passedTests, totalTests, (passedTests == totalTests ? "100% SUCCESS" : "FAILURES DETECTED"));
        System.out.println("=================================================");

        if (passedTests != totalTests) {
            System.exit(1);
        }
    }

    private static void assertTrue(String testName, boolean condition) {
        totalTests++;
        if (condition) {
            passedTests++;
            System.out.println("  [PASS] " + testName);
        } else {
            System.err.println("  [FAIL] " + testName);
        }
    }

    private static void testBookManagement(Library lib) {
        System.out.println("\n[1] Testing Book Management Module:");
        try {
            Book b1 = new Book("B101", "978-0134685991", "Effective Java", "Joshua Bloch", "Computer Science");
            Book b2 = new Book("B102", "978-0596009205", "Head First Design Patterns", "Eric Freeman", "Software Engineering");
            Book b3 = new Book("B103", "978-0132350884", "Clean Code", "Robert C. Martin", "Software Engineering");

            lib.addBook(b1);
            lib.addBook(b2);
            lib.addBook(b3);

            assertTrue("Add books and verify total count is 3", lib.getAllBooks().size() == 3);
            assertTrue("Find book by ID 'B101'", lib.getBook("B101").getTitle().equals("Effective Java"));
            assertTrue("Initial availability status is true", !lib.getBook("B101").isIssued());

            // Duplicate book ID should throw LibraryException
            boolean threwDuplicate = false;
            try {
                lib.addBook(new Book("B101", "123", "Duplicate Book", "Author", "Cat"));
            } catch (LibraryException e) {
                threwDuplicate = true;
            }
            assertTrue("Duplicate Book ID throws LibraryException", threwDuplicate);

        } catch (Exception e) {
            assertTrue("Book management exception: " + e.getMessage(), false);
        }
    }

    private static void testMemberManagement(Library lib) {
        System.out.println("\n[2] Testing Member Management Module:");
        try {
            Member m1 = new Member("M101", "Alice Johnson", "alice@example.com", "555-0101");
            Member m2 = new Member("M102", "Bob Smith", "bob@example.com", "555-0102");

            lib.addMember(m1);
            lib.addMember(m2);

            assertTrue("Add members and verify total count is 2", lib.getAllMembers().size() == 2);
            assertTrue("Find member by ID 'M101'", lib.getMember("M101").getName().equals("Alice Johnson"));

            // Duplicate member ID check
            boolean threwDuplicate = false;
            try {
                lib.addMember(new Member("M101", "Duplicate Alice", "dup@example.com", "123"));
            } catch (LibraryException e) {
                threwDuplicate = true;
            }
            assertTrue("Duplicate Member ID throws LibraryException", threwDuplicate);

        } catch (Exception e) {
            assertTrue("Member management exception: " + e.getMessage(), false);
        }
    }

    private static void testSearchAndSort(Library lib) {
        System.out.println("\n[3] Testing Book Search and Sorting:");
        List<Book> searchTitle = lib.searchByTitle("Design");
        assertTrue("Search by title contains 'Design'", searchTitle.size() == 1 && searchTitle.get(0).getId().equals("B102"));

        List<Book> searchAuthor = lib.searchByAuthor("Bloch");
        assertTrue("Search by author contains 'Bloch'", searchAuthor.size() == 1 && searchAuthor.get(0).getId().equals("B101"));

        List<Book> searchCategory = lib.searchByCategory("Software");
        assertTrue("Search by category 'Software'", searchCategory.size() == 2);

        List<Book> searchIsbn = lib.searchByIsbn("978-0132350884");
        assertTrue("Search by ISBN exact match", searchIsbn.size() == 1 && searchIsbn.get(0).getId().equals("B103"));

        List<Book> globalSearch = lib.searchBooks("Robert");
        assertTrue("Global search by author name in keyword", globalSearch.size() == 1 && globalSearch.get(0).getId().equals("B103"));

        List<Book> sortedTitleAsc = lib.sortBooksByTitle(true);
        assertTrue("Sort books by title ascending ('Clean Code' first)", sortedTitleAsc.get(0).getTitle().equals("Clean Code"));

        List<Book> sortedTitleDesc = lib.sortBooksByTitle(false);
        assertTrue("Sort books by title descending ('Head First...' first)", sortedTitleDesc.get(0).getTitle().equals("Head First Design Patterns"));
    }

    private static void testIssueAndReturn(Library lib) {
        System.out.println("\n[4] Testing Book Issue and Return Functionality:");
        try {
            LocalDate issueDate = LocalDate.now();
            Loan loan = lib.issueBook("B101", "M101", issueDate);

            assertTrue("Loan record created with ID " + loan.getLoanId(), loan.getLoanId() != null);
            assertTrue("Book status is now ISSUED", lib.getBook("B101").isIssued());
            assertTrue("Book due date is issueDate + 14 days", lib.getBook("B101").getDueDate().equals(issueDate.plusDays(14)));
            assertTrue("Member's borrowed list contains B101", lib.getMember("M101").getBorrowedBookIds().contains("B101"));

            // On-time return
            Loan returnedLoan = lib.returnBook("B101", "M101", issueDate.plusDays(7));
            assertTrue("Book returned on time, status is returned", returnedLoan.isReturned());
            assertTrue("Book status reverted to AVAILABLE", !lib.getBook("B101").isIssued());
            assertTrue("Fine is $0.00 for on-time return", returnedLoan.getFineAmount() == 0.0);
            assertTrue("Member borrowed list no longer contains B101", !lib.getMember("M101").getBorrowedBookIds().contains("B101"));

            // Late return test (Overdue fine calculation)
            LocalDate pastIssue = LocalDate.now().minusDays(20); // Due was 6 days ago (20 - 14 = 6 days overdue)
            lib.issueBook("B102", "M102", pastIssue);
            Loan lateLoan = lib.returnBook("B102", "M102", LocalDate.now());
            assertTrue("Late return fine calculated (6 days * $1.50 = $9.00)", Math.abs(lateLoan.getFineAmount() - 9.0) < 0.001);

        } catch (Exception e) {
            assertTrue("Issue/Return exception: " + e.getMessage(), false);
        }
    }

    private static void testExceptionHandling(Library lib) {
        System.out.println("\n[5] Testing Exception Handling (Invalid IDs & Unavailable Books):");
        // Invalid Book ID
        boolean caughtInvalidBook = false;
        try {
            lib.issueBook("INVALID_BOOK", "M101");
        } catch (BookNotFoundException e) {
            caughtInvalidBook = true;
        } catch (Exception ignored) {
        }
        assertTrue("Throws BookNotFoundException for non-existent book ID", caughtInvalidBook);

        // Invalid Member ID
        boolean caughtInvalidMember = false;
        try {
            lib.issueBook("B101", "INVALID_MEMBER");
        } catch (MemberNotFoundException e) {
            caughtInvalidMember = true;
        } catch (Exception ignored) {
        }
        assertTrue("Throws MemberNotFoundException for non-existent member ID", caughtInvalidMember);

        // Book Unavailable Exception
        boolean caughtUnavailable = false;
        try {
            lib.issueBook("B103", "M101");
            // Try issuing same book again to M102
            lib.issueBook("B103", "M102");
        } catch (BookUnavailableException e) {
            caughtUnavailable = true;
        } catch (Exception ignored) {
        }
        assertTrue("Throws BookUnavailableException when book is already issued", caughtUnavailable);
    }

    private static void testOverdueTracking(Library lib) {
        System.out.println("\n[6] Testing Overdue Tracking Module:");
        try {
            // Add a book and issue it with an overdue date
            Book bOverdue = new Book("B999", "978-0000000000", "Overdue Test Book", "Author X", "Testing");
            lib.addBook(bOverdue);

            LocalDate issueDate = LocalDate.now().minusDays(25); // Due 11 days ago
            Loan loan = lib.issueBook("B999", "M101", issueDate);

            List<Loan> overdueLoans = lib.getOverdueLoans();
            assertTrue("Overdue loans list includes loan " + loan.getLoanId(), overdueLoans.stream().anyMatch(l -> l.getLoanId().equals(loan.getLoanId())));
            assertTrue("Overdue days is 11", loan.getOverdueDays(LocalDate.now()) == 11);
            assertTrue("Calculated overdue fine is $16.50", Math.abs(loan.calculateFine(LocalDate.now(), Library.FINE_PER_DAY) - 16.50) < 0.001);

        } catch (Exception e) {
            assertTrue("Overdue tracking exception: " + e.getMessage(), false);
        }
    }

    private static void testFilePersistence() {
        System.out.println("\n[7] Testing File-based Persistence (Save and Reload):");
        try {
            File persistDir = new File("test_persistence");
            deleteDirectory(persistDir);

            Library lib1 = new Library("test_persistence");
            lib1.addBook(new Book("BP1", "ISBN-111", "Persistence Title 1", "Author 1", "Tech"));
            lib1.addBook(new Book("BP2", "ISBN-222", "Persistence Title 2", "Author 2", "Science"));
            lib1.addMember(new Member("MP1", "Persist User", "user@persist.org", "1234567890"));
            lib1.issueBook("BP1", "MP1");

            lib1.saveToFile();

            // Check files created
            assertTrue("books.csv created", new File(persistDir, "books.csv").exists());
            assertTrue("members.csv created", new File(persistDir, "members.csv").exists());
            assertTrue("loans.csv created", new File(persistDir, "loans.csv").exists());

            // Reload in a completely new Library instance
            Library lib2 = new Library("test_persistence");
            lib2.loadFromFile();

            assertTrue("Reloaded books count is 2", lib2.getAllBooks().size() == 2);
            assertTrue("Reloaded members count is 1", lib2.getAllMembers().size() == 1);
            assertTrue("Reloaded loans count is 1", lib2.getAllLoans().size() == 1);
            assertTrue("Book BP1 is still marked as issued", lib2.getBook("BP1").isIssued());
            assertTrue("Member MP1 still holds borrowed book BP1", lib2.getMember("MP1").getBorrowedBookIds().contains("BP1"));

            deleteDirectory(persistDir);
            assertTrue("Persistence verification completed successfully", true);

        } catch (Exception e) {
            assertTrue("Persistence test failed with exception: " + e.getMessage(), false);
        }
    }

    private static void deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory()) {
                        deleteDirectory(f);
                    } else {
                        f.delete();
                    }
                }
            }
            dir.delete();
        }
    }
}

