# Library Management System

A production-grade, object-oriented Library Management System built in Java. The application provides end-to-end management for library books, members, issue and return loan transactions, automated overdue tracking with daily fines, multi-attribute searching and sorting, and reliable CSV file-based persistence.

---

## 📋 Table of Contents
- [Project Overview](#project-overview)
- [Key Features](#key-features)
- [System Architecture](#system-architecture)
- [Class & Package Structure](#class--package-structure)
- [Data Model & Persistence](#data-model--persistence)
- [Exception Handling](#exception-handling)
- [Prerequisites & Build Instructions](#prerequisites--build-instructions)
- [Running the Application](#running-the-application)
- [Running the Automated Tests](#running-the-automated-tests)
- [Interactive CLI Guide](#interactive-cli-guide)

---

## 🎯 Project Overview
This project fulfills the Day 1 of 4 Project requirements for an internship/training curriculum:
- **Core Objective:** Implement practical object-oriented design, collections framework, robust file handling, custom exception handling, searching, sorting algorithms, and transaction-based business logic.
- **Tools & Technologies:** Java (JDK 17+ or 21+), Java Collections (`Map`, `List`, `LinkedHashMap`, `ArrayList`), Java Time API (`LocalDate`), File I/O (`BufferedReader`, `BufferedWriter`, CSV persistence), Git/GitHub.

---

## ✨ Key Features

1. **Book Management Module**:
   - Register new books with ID, ISBN, Title, Author, and Category.
   - Real-time availability tracking (`AVAILABLE` vs. `ISSUED`).
   - Remove books (with safety guards preventing removal of currently issued books).

2. **Member Management Module**:
   - Register library patrons with Member ID, Full Name, Email, and Phone Number.
   - Track each member's active list of borrowed books.
   - Inspect member profiles and active loan statuses.

3. **Transaction-based Book Issue & Return**:
   - Issue books with dynamic `LocalDate` timestamps and automated 14-day loan duration.
   - Return books with automatic status update, borrower record clearance, and overdue fine detection.

4. **Overdue Tracking & Fine Calculation**:
   - Automatically detects loans past their 14-day maturity period.
   - Accurately calculates overdue days using `ChronoUnit.DAYS`.
   - Computes overdue penalty at `$1.50` per day past due.

5. **Multi-Attribute Search & Sorting**:
   - Search by **Title**, **Author**, **Category**, **ISBN**, or **Global Keyword**.
   - Sort catalog by **Title** (A-Z / Z-A), **Author** (A-Z / Z-A), **ID** (Asc / Desc), or **Category**.

6. **File-Based Persistence**:
   - All books, members, and loan history persist to clean CSV files in the `data/` directory (`books.csv`, `members.csv`, `loans.csv`).
   - Automatic background loading on startup and saving on state changes or exit.
   - Built-in demo data generator for rapid testing.

7. **Clean Separation of Concerns**:
   - Business rules, collections management, and file storage are decoupled into `Library.java`.
   - Console presentation and user interactions are isolated in `Main.java`.

---

## 🏗️ System Architecture

```
+-----------------------------------------------------------------------+
|                             Console UI                                |
|                              Main.java                                |
+-----------------------------------+-----------------------------------+
                                    | Calls API / catches exceptions
                                    v
+-----------------------------------------------------------------------+
|                           Business Logic                              |
|                            Library.java                               |
+------------------+----------------+-----------------+-----------------+
                   |                |                 |
                   v                v                 v
           +--------------+  +--------------+  +--------------+
           |  Book.java   |  | Member.java  |  |  Loan.java   |
           +--------------+  +--------------+  +--------------+
                   |                |                 |
                   +----------------+-----------------+
                                    |
                                    v
                     +------------------------------+
                     |       File Persistence       |
                     | data/{books,members,loans}.csv|
                     +------------------------------+
```

---

## 📦 Class Breakdown

| Class | Description |
|---|---|
| `Book.java` | Encapsulates book entity (ID, ISBN, Title, Author, Category, Status, DueDate) with CSV serialization. |
| `Member.java` | Encapsulates library member profile, contact info, and active borrowed book collection. |
| `Loan.java` | Represents a loan transaction record with `loanId`, `bookId`, `memberId`, `issueDate`, `dueDate`, `returnDate`, and `fineAmount`. |
| `Library.java` | Central business engine managing in-memory collections, transactions, search/sort, and disk persistence. |
| `BookNotFoundException.java` | Checked exception thrown when an invalid or nonexistent Book ID is queried. |
| `MemberNotFoundException.java` | Checked exception thrown when an invalid or nonexistent Member ID is queried. |
| `BookUnavailableException.java` | Checked exception thrown when attempting to issue a book that is already checked out. |
| `LibraryException.java` | General checked exception for business constraint violations (duplicate IDs, removing issued books). |
| `Main.java` | Interactive command-line interface with formatted ASCII tables, input validation, and menus. |
| `LibraryTest.java` | Standalone automated test suite validating all 38 test specifications across 7 modules. |

---

## 💾 Data Model & Persistence

The application maintains persistent records under the `data/` folder:

- **`data/books.csv`**:
  `id,isbn,title,author,category,isIssued,dueDate`
- **`data/members.csv`**:
  `memberId,name,email,phone,borrowedBookIds`
- **`data/loans.csv`**:
  `loanId,bookId,memberId,issueDate,dueDate,returnDate,fineAmount,isReturned`

---

## 🛠️ Prerequisites & Build Instructions

- **Java Development Kit (JDK):** Version 17, 21, or newer.
- **Git**

### Compilation:
From the `LibraryManagementSystem` folder, run:
```bash
javac *.java
```

---

## 🚀 Running the Application

Launch the interactive console application:
```bash
java Main
```

Sample Main Menu:
```text
=================================================
       WELCOME TO LIBRARY MANAGEMENT SYSTEM      
=================================================

================ MAIN MENU ================
1. Book Management (Add, View, Remove)
2. Member Management (Register, View, Details)
3. Issue Book (Loan Transaction)
4. Return Book (Calculate Overdue & Fines)
5. Overdue Records & Fine Tracking
6. Search and Sort Books
7. View All Transaction / Loan Records
8. File Persistence (Save / Reload)
9. Exit Application
Enter choice (1-9):
```

---

## 🧪 Running the Automated Tests

To run the complete automated test suite verifying all 7 deliverable modules:
```bash
java LibraryTest
```

Expected output:
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
