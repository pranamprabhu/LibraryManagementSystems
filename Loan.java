import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Loan {
    private String loanId;
    private String bookId;
    private String memberId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private double fineAmount;
    private boolean isReturned;

    public Loan(String loanId, String bookId, String memberId, LocalDate issueDate, LocalDate dueDate) {
        this(loanId, bookId, memberId, issueDate, dueDate, null, 0.0, false);
    }

    public Loan(String loanId, String bookId, String memberId, LocalDate issueDate, LocalDate dueDate,
                LocalDate returnDate, double fineAmount, boolean isReturned) {
        this.loanId = loanId != null ? loanId.trim() : "";
        this.bookId = bookId != null ? bookId.trim() : "";
        this.memberId = memberId != null ? memberId.trim() : "";
        this.issueDate = issueDate != null ? issueDate : LocalDate.now();
        this.dueDate = dueDate != null ? dueDate : this.issueDate.plusDays(14);
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
        this.isReturned = isReturned;
    }

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public double getFineAmount() {
        return fineAmount;
    }

    public void setFineAmount(double fineAmount) {
        this.fineAmount = fineAmount;
    }

    public boolean isReturned() {
        return isReturned;
    }

    public void setReturned(boolean returned) {
        isReturned = returned;
    }

    public boolean isOverdue(LocalDate asOfDate) {
        if (asOfDate == null) {
            asOfDate = LocalDate.now();
        }
        if (isReturned) {
            return returnDate != null && returnDate.isAfter(dueDate);
        }
        return asOfDate.isAfter(dueDate);
    }

    public long getOverdueDays(LocalDate asOfDate) {
        if (asOfDate == null) {
            asOfDate = LocalDate.now();
        }
        LocalDate compareDate = isReturned ? (returnDate != null ? returnDate : dueDate) : asOfDate;
        if (compareDate.isAfter(dueDate)) {
            return ChronoUnit.DAYS.between(dueDate, compareDate);
        }
        return 0;
    }

    public double calculateFine(LocalDate asOfDate, double ratePerDay) {
        long overdueDays = getOverdueDays(asOfDate);
        return overdueDays * ratePerDay;
    }

    public String toCsv() {
        String retStr = (returnDate != null) ? returnDate.toString() : "NONE";
        return String.join(",",
                escapeCsv(loanId),
                escapeCsv(bookId),
                escapeCsv(memberId),
                issueDate.toString(),
                dueDate.toString(),
                retStr,
                Double.toString(fineAmount),
                Boolean.toString(isReturned)
        );
    }

    public static Loan fromCsv(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return null;
        }
        String[] parts = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (parts.length < 8) {
            return null;
        }
        String loanId = unescapeCsv(parts[0].trim());
        String bookId = unescapeCsv(parts[1].trim());
        String memberId = unescapeCsv(parts[2].trim());
        LocalDate issueDate = LocalDate.parse(parts[3].trim());
        LocalDate dueDate = LocalDate.parse(parts[4].trim());

        LocalDate returnDate = null;
        if (!parts[5].trim().equalsIgnoreCase("NONE") && !parts[5].trim().isEmpty()) {
            try {
                returnDate = LocalDate.parse(parts[5].trim());
            } catch (Exception ignored) {
            }
        }
        double fine = 0.0;
        try {
            fine = Double.parseDouble(parts[6].trim());
        } catch (NumberFormatException ignored) {
        }
        boolean returned = Boolean.parseBoolean(parts[7].trim());

        return new Loan(loanId, bookId, memberId, issueDate, dueDate, returnDate, fine, returned);
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String unescapeCsv(String value) {
        if (value.startsWith("\"") && value.endsWith("\"") && value.length() >= 2) {
            value = value.substring(1, value.length() - 1);
            return value.replace("\"\"", "\"");
        }
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Loan)) return false;
        Loan loan = (Loan) o;
        return Objects.equals(loanId, loan.loanId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(loanId);
    }

    @Override
    public String toString() {
        String status = isReturned ? "RETURNED on " + returnDate : "ACTIVE (Due: " + dueDate + ")";
        String fineStr = fineAmount > 0 ? String.format(" | Fine: $%.2f", fineAmount) : "";
        return String.format("[Loan ID: %s] Book ID: %s | Member ID: %s | Issued: %s | %s%s",
                loanId, bookId, memberId, issueDate, status, fineStr);
    }
}
