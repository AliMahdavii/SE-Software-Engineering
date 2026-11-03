import java.time.LocalDate;

public class BorrowBook {
    private final int studentId;
    private final int bookId;
    private final LocalDate borrowDate;
    private boolean returned;

    public BorrowBook(int studentId, int bookId, LocalDate borrowDate, boolean returned) {
        this.studentId = studentId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.returned = returned;
    }

    public int getStudentId() {
        return studentId;
    }

    public int getBookId() {
        return bookId;
    }

    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    public boolean isReturned() {
        return returned;
    }

    public void markReturned() {
        returned = true;
    }

    @Override
    public String toString() {
        return "Student ID: " + studentId + " | Book ID: " + bookId + " | Borrowed on: " + borrowDate
                + " | " + (returned ? "Returned" : "Not returned");
    }
}
