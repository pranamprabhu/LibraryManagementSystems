import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Member {
    private String memberId;
    private String name;
    private String email;
    private String phone;
    private List<String> borrowedBookIds;

    public Member(String memberId, String name) {
        this(memberId, name, "N/A", "N/A", new ArrayList<>());
    }

    public Member(String memberId, String name, String email, String phone) {
        this(memberId, name, email, phone, new ArrayList<>());
    }

    public Member(String memberId, String name, String email, String phone, List<String> borrowedBookIds) {
        this.memberId = memberId != null ? memberId.trim() : "";
        this.name = name != null ? name.trim() : "";
        this.email = email != null ? email.trim() : "N/A";
        this.phone = phone != null ? phone.trim() : "N/A";
        this.borrowedBookIds = borrowedBookIds != null ? new ArrayList<>(borrowedBookIds) : new ArrayList<>();
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getBorrowedBookIds() {
        return new ArrayList<>(borrowedBookIds);
    }

    public void borrowBook(String bookId) {
        if (bookId != null && !borrowedBookIds.contains(bookId)) {
            borrowedBookIds.add(bookId);
        }
    }

    public void returnBook(String bookId) {
        if (bookId != null) {
            borrowedBookIds.remove(bookId);
        }
    }

    public String toCsv() {
        String booksStr = String.join(";", borrowedBookIds);
        return String.join(",",
                escapeCsv(memberId),
                escapeCsv(name),
                escapeCsv(email),
                escapeCsv(phone),
                escapeCsv(booksStr)
        );
    }

    public static Member fromCsv(String csvLine) {
        if (csvLine == null || csvLine.trim().isEmpty()) {
            return null;
        }
        String[] parts = csvLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        if (parts.length < 2) {
            return null;
        }
        String id = unescapeCsv(parts[0].trim());
        String name = unescapeCsv(parts[1].trim());
        String email = parts.length > 2 ? unescapeCsv(parts[2].trim()) : "N/A";
        String phone = parts.length > 3 ? unescapeCsv(parts[3].trim()) : "N/A";

        List<String> books = new ArrayList<>();
        if (parts.length > 4) {
            String booksStr = unescapeCsv(parts[4].trim());
            if (!booksStr.isEmpty()) {
                String[] bIds = booksStr.split(";");
                for (String b : bIds) {
                    if (!b.trim().isEmpty()) {
                        books.add(b.trim());
                    }
                }
            }
        }
        return new Member(id, name, email, phone, books);
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
        if (!(o instanceof Member)) return false;
        Member member = (Member) o;
        return Objects.equals(memberId, member.memberId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberId);
    }

    @Override
    public String toString() {
        return String.format("[%s] %s | Email: %s | Phone: %s | Books Borrowed: %d (%s)",
                memberId,
                name,
                email,
                phone,
                borrowedBookIds.size(),
                borrowedBookIds.isEmpty() ? "None" : String.join(", ", borrowedBookIds)
        );
    }
}
