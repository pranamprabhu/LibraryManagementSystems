import java.time.LocalDate;
import java.util.Objects;

public class Book {
    private String id;
    private String isbn;
    private String title;
    private String author;
    private String category;
    private boolean isIssued;
    private LocalDate dueDate;

    public Book(String id, String title, String author) {
        this(id, "N/A", title, author, "General", false, null);
    }

    public Book(String id, String isbn, String title, String author, String category) {
        this(id, isbn, title, author, category, false, null);
    }

    public Book(String id, String isbn, String title, String author, String category, boolean isIssued, LocalDate dueDate) {
        this.id = id != null ? id.trim() : "";
        this.isbn = isbn != null ? isbn.trim() : "N/A";
        this.title = title != null ? title.trim() : "";
        this.author = author != null ? author.trim() : "";
        this.category = category != null ? category.trim() : "General";
        this.isIssued = isIssued;
        this.dueDate = dueDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isIssued() {
        return isIssued;
    }

    public void setIssued(boolean issued) {
        this.isIssued = issued;
        if (!issued) {
            this.dueDate = null;
        }
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String toCsv() {
        String dueStr = (dueDate != null) ? dueDate.toString() : "NONE";
        return String.join(",",
                escapeCsv(id),
                escapeCsv(isbn),
                escapeCsv(title),
                escapeCsv(author),
                escapeCsv(category),
                Boolean.toString(isIssued),
                dueStr
        );
    }

    public static Book fromCsv(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return null;
        }
        String[] parts = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (parts.length < 5) {
            return null;
        }
        String id = unescapeCsv(parts[0].trim());
        String isbn = unescapeCsv(parts[1].trim());
        String title = unescapeCsv(parts[2].trim());
        String author = unescapeCsv(parts[3].trim());
        String category = unescapeCsv(parts[4].trim());
        boolean isIssued = parts.length > 5 && Boolean.parseBoolean(parts[5].trim());
        LocalDate dueDate = null;
        if (parts.length > 6 && !parts[6].trim().equalsIgnoreCase("NONE") && !parts[6].trim().isEmpty()) {
            try {
                dueDate = LocalDate.parse(parts[6].trim());
            } catch (Exception ignored) {
            }
        }
        return new Book(id, isbn, title, author, category, isIssued, dueDate);
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
        if (!(o instanceof Book)) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("[%s] \"%s\" by %s | ISBN: %s | Category: %s | Status: %s%s",
                id,
                title,
                author,
                isbn,
                category,
                isIssued ? "ISSUED" : "AVAILABLE",
                (isIssued && dueDate != null) ? " (Due: " + dueDate + ")" : ""
        );
    }
}
