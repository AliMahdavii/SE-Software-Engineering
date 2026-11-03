import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Abstraction over the persistence layer. Both {@link FileHandler}
 * (file-based) and {@link DatabaseHandler} (SQLite-based) implement it,
 * so {@link Library} can use either backend without any change.
 */
public interface DataStore {
    ArrayList<Book> loadBooks();

    ArrayList<Student> loadStudents();

    ArrayList<BorrowBook> loadBorrowRecords();

    void saveBooks(ArrayList<Book> books) throws IOException;

    void saveStudents(ArrayList<Student> students) throws IOException;

    void saveBorrowRecords(ArrayList<BorrowBook> borrowRecords) throws IOException;
}
