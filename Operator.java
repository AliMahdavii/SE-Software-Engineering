// Handles the daily desk work: borrowing and returning books.
public class Operator {
    private final Library library;

    public Operator(Library library) {
        this.library = library;
    }

    public String borrowBook(int studentId, int bookId) {
        library.borrowBook(studentId, bookId);
        return "Book borrowed successfully.";
    }

    public String returnBook(int studentId, int bookId) {
        library.returnBook(studentId, bookId);
        return "Book returned successfully.";
    }
}
