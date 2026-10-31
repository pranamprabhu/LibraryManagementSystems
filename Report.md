# Comprehensive Technical Report: Library Management System

**Author / Candidate:** Pranam Prabhu  
**Project:** Library Management System (Day 1 of 4 - Project)  
**Language & Platform:** Java 21 LTS (Temurin), Java Collections, File I/O, Java Time API  
**Status:** Completed & Empirically Verified (38 / 38 Tests Passing)

---

## 1. Executive Summary & Objectives

Modern library administration requires consistent tracking of literary inventory, registered patrons, lending transactions, and financial penalties for overdue items. This project delivers a production-grade, console-based Library Management System constructed in Java adhering strictly to Object-Oriented Design (OOD) standards.

### Key Learning Objectives Fulfilled:
- **Object-Oriented Design:** Strong separation of concerns, high cohesion, low coupling, and data encapsulation.
- **Java Collections Framework:** Efficient key-based lookups using `LinkedHashMap` and dynamic transaction tracking using `ArrayList`.
- **Transaction Processing:** Immutable transaction state tracking via dedicated `Loan` records linking books, patrons, and time intervals.
- **Date Handling with Modern Java Time API:** Explicit date arithmetic using `java.time.LocalDate` and `java.time.temporal.ChronoUnit`.
- **Custom Checked Exceptions:** Clean recovery paths avoiding raw `null` returns or silent business logic failures.
- **Search & Sorting Algorithms:** Multi-field predicate filtering (title, author, category, ISBN) and comparative sorting using `Comparator`.
- **File-based Persistence:** Custom CSV streaming and parser allowing complete state recovery across session boundaries without external database dependencies.

---

## 2. System Architecture & Component Design

The system implements a layered architectural design separating presentation, domain models, business logic services, and persistence:

```
+-------------------------------------------------------------+
|                      Presentation Layer                     |
|                 Main.java (CLI & Menu Driver)               |
+------------------------------+------------------------------+
                               |
                               v
+-------------------------------------------------------------+
|                       Service Layer                         |
|            Library.java (Core Transaction Engine)           |
+------------------------------+------------------------------+
                               |
        +----------------------+----------------------+
        |                      |                      |
        v                      v                      v
+----------------+     +----------------+     +----------------+
|   Book.java    |     |  Member.java   |     |   Loan.java    |
| (Domain Model) |     | (Domain Model) |     | (Domain Model) |
+----------------+     +----------------+     +----------------+
        |                      |                      |
        +----------------------+----------------------+
                               |
                               v
+-------------------------------------------------------------+
|                      Persistence Layer                      |
|            data/books.csv, members.csv, loans.csv           |
+-------------------------------------------------------------+
```

### Component Roles:
1. **`Book`:** Models physical or digital library assets. Contains properties: `id`, `isbn`, `title`, `author`, `category`, `isIssued`, and `dueDate`.
2. **`Member`:** Models library patrons. Contains: `memberId`, `name`, `email`, `phone`, and `borrowedBookIds` list.
3. **`Loan`:** Models an individual lending contract. Tracks `loanId`, `bookId`, `memberId`, `issueDate`, `dueDate`, `returnDate`, `fineAmount`, and `isReturned`.
4. **`Library`:** Serves as the central domain controller maintaining collections, executing issue/return transactions, applying fine policy, filtering search queries, sorting catalogs, and synchronizing state to disk.
5. **`Main`:** Handles terminal user interactions, menu navigation, input formatting, and maps thrown exceptions into clear feedback.
6. **`LibraryTest`:** Automated regression test suite validating each module with empirical assertions.

---

## 3. Object-Oriented Design (OOD) Principles Applied

### A. Encapsulation
- All state variables in `Book`, `Member`, and `Loan` are declared `private`.
- Mutable collections (such as `borrowedBookIds` in `Member`) return defensive copies (`new ArrayList<>(borrowedBookIds)`) preventing external tampering.
- Direct mutations are guarded through validated setters and business methods (`borrowBook()`, `returnBook()`).

### B. Separation of Concerns
- `Library.java` does not contain terminal printing routines (`System.out.println`) within core transaction methods. Instead, it yields domain results or throws descriptive exceptions, leaving UI formatting strictly to `Main.java`.

### C. Cohesion & Single Responsibility
- Domain entities only manage their internal properties and serialization logic (`toCsv()`, `fromCsv()`).
- Transaction business rules (fine calculation rates, loan duration limits, duplicate detection) are consolidated within `Library.java`.

---

## 4. Collections & Data Structures Analysis

| Collection | Class Used | Time Complexity | Justification |
|---|---|---|---|
| Books Registry | `LinkedHashMap<String, Book>` | $O(1)$ Search / Insert | Preserves insertion order while providing instant constant-time lookup by unique `bookId`. |
| Members Registry | `LinkedHashMap<String, Member>` | $O(1)$ Search / Insert | Provides $O(1)$ patron retrieval during loan and return operations. |
| Loan History | `ArrayList<Loan>` | $O(1)$ Append, $O(n)$ Scan | Preserves sequential timeline of transactions; supports iteration for overdue scanning. |
| Member's Active Books | `ArrayList<String>` | $O(k)$ Operations ($k \le 5$) | Lightweight list tracking IDs currently checked out by a specific patron. |

---

## 5. Key Functional Modules

### 5.1 Book & Member Management
- Prevents collisions by verifying uniqueness of `bookId` and `memberId`.
- Prevents deletion of books that are currently checked out.
- Prevents deregistering members who possess unreturned items.

### 5.2 Transaction-based Issue & Return Logic
1. **Issuance:**
   - Verifies target book exists and is currently available (`!isIssued`).
   - Verifies patron is registered.
   - Generates sequential loan identifier `L1001`, `L1002`, etc.
   - Sets issue date (`LocalDate.now()`) and due date (`issueDate.plusDays(14)`).
   - Marks book as issued and adds book ID to member's borrowed list.
2. **Return:**
   - Verifies book is currently checked out by the submitting member.
   - Locates active loan record.
   - Sets `returnDate = LocalDate.now()`.
   - Reverts book status to `AVAILABLE` and clears due date.
   - Removes book ID from member's active list.
   - Calculates overdue penalties if `returnDate.isAfter(dueDate)`.

### 5.3 Overdue Tracking & Fine Computation
- Mathematical Formula for Fine:
  $$\text{Fine} = \max(0, \text{DaysBetween}(\text{dueDate}, \text{asOfDate})) \times \text{DAILY\_RATE}$$
- Default Parameters:
  - Allowed Borrowing Duration: `14 days`
  - Daily Fine Rate: `$1.50 per day`
- Active scanning allows librarians to inspect real-time liabilities before books are returned.

### 5.4 Search & Sorting Engine
- **Search Capabilities:**
  - Case-insensitive substring matching across `Title`, `Author`, and `Category`.
  - Normalized matching for `ISBN` and `Book ID`.
  - Combined global query scanning all fields simultaneously.
- **Sorting Capabilities:**
  - `sortBooksByTitle(ascending/descending)` using `Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER)`.
  - `sortBooksByAuthor()`, `sortBooksById()`, and `sortBooksByCategory()`.

### 5.5 File-based Persistence Engine
- CSV-based flat files stored in `data/`:
  - Handles string sanitization, quotes, and commas gracefully.
  - Automatically loads on application startup.
  - Flushes state to disk after each critical transaction.

---

## 6. Custom Exception Hierarchy

| Exception | Base Class | Trigger Scenario |
|---|---|---|
| `BookNotFoundException` | `Exception` | Lookups for unindexed or nonexistent book identifiers. |
| `MemberNotFoundException` | `Exception` | Lookups for unregistered member identifiers. |
| `BookUnavailableException` | `Exception` | Attempting to issue a book whose `isIssued` flag is `true`. |
| `LibraryException` | `Exception` | Domain constraint violations (duplicate IDs, removing issued book, mismatched borrower return). |

---

## 7. Verification & Empirical Proof

A comprehensive test harness (`LibraryTest.java`) was executed to prove complete functionality:

```text
=================================================
      RUNNING LIBRARY SYSTEM AUTOMATED TESTS     
=================================================

[1] Testing Book Management Module:
  [PASS] Add books and verify total count is 3
  [PASS] Find book by ID 'B101'
  [PASS] Initial availability status is true
  [PASS] Duplicate Book ID throws LibraryException

[2] Testing Member Management Module:
  [PASS] Add members and verify total count is 2
  [PASS] Find member by ID 'M101'
  [PASS] Duplicate Member ID throws LibraryException

[3] Testing Book Search and Sorting:
  [PASS] Search by title contains 'Design'
  [PASS] Search by author contains 'Bloch'
  [PASS] Search by category 'Software'
  [PASS] Search by ISBN exact match
  [PASS] Global search by author name in keyword
  [PASS] Sort books by title ascending ('Clean Code' first)
  [PASS] Sort books by title descending ('Head First...' first)

[4] Testing Book Issue and Return Functionality:
  [PASS] Loan record created with ID L1001
  [PASS] Book status is now ISSUED
  [PASS] Book due date is issueDate + 14 days
  [PASS] Member's borrowed list contains B101
  [PASS] Book returned on time, status is returned
  [PASS] Book status reverted to AVAILABLE
  [PASS] Fine is $0.00 for on-time return
  [PASS] Member borrowed list no longer contains B101
  [PASS] Late return fine calculated (6 days * $1.50 = $9.00)

[5] Testing Exception Handling (Invalid IDs & Unavailable Books):
  [PASS] Throws BookNotFoundException for non-existent book ID
  [PASS] Throws MemberNotFoundException for non-existent member ID
  [PASS] Throws BookUnavailableException when book is already issued

[6] Testing Overdue Tracking Module:
  [PASS] Overdue loans list includes loan L1004
  [PASS] Overdue days is 11
  [PASS] Calculated overdue fine is $16.50

[7] Testing File-based Persistence (Save and Reload):
  [PASS] books.csv created
  [PASS] members.csv created
  [PASS] loans.csv created
  [PASS] Reloaded books count is 2
  [PASS] Reloaded members count is 1
  [PASS] Reloaded loans count is 1
  [PASS] Book BP1 is still marked as issued
  [PASS] Member MP1 still holds borrowed book BP1
  [PASS] Persistence verification completed successfully

=================================================
TEST SUMMARY: 38 / 38 PASSED (100% SUCCESS)
=================================================
```

---

## 8. Conclusion

All 7 required deliverables and mini-guide specifications outlined in the project brief have been developed, decoupled, documented, and verified. The codebase is modular, clean, and ready for submission and evaluation.
