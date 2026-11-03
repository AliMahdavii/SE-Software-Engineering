import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

/**
 * SQLite-backed storage that is a drop-in replacement for {@link FileHandler}.
 *
 * <p>It exposes the exact same public methods as {@code FileHandler}, so
 * {@link Library} can use either backend without any change. The database
 * file is created automatically on first use inside the configured data
 * folder.</p>
 *
 * <p>Schema:
 * <pre>
 *   books(id, title, author, year)
 *   students(id, name, student_number)
 *   borrows(student_id, book_id, borrow_date, returned)
 * </pre>
 * The {@code available} flag of a {@link Book} is derived from the
 * borrows table at load time, exactly like the file-based implementation
 * does inside {@link Library#loadData()}.
 */
public class DatabaseHandler implements DataStore {
    private static final String DRIVER = "org.sqlite.JDBC";

    private final String dbUrl;

    public DatabaseHandler(String folderName) {
        this(folderName, "library.db");
    }

    public DatabaseHandler(String folderName, String dbFileName) {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "SQLite JDBC driver not found. Make sure sqlite-jdbc.jar is on the classpath.",
                    e);
        }
        java.nio.file.Path folder = java.nio.file.Paths.get(folderName);
        try {
            java.nio.file.Files.createDirectories(folder);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Could not create data folder: " + folder, e);
        }
        java.nio.file.Path dbFile = folder.resolve(dbFileName);
        this.dbUrl = "jdbc:sqlite:" + dbFile.toString();
        initSchema();
    }

    private Connection connect() throws SQLException {
        // SQLite is much faster for our use case when auto-commit is on.
        return DriverManager.getConnection(dbUrl);
    }

    private void initSchema() {
        String[] ddl = {
                "CREATE TABLE IF NOT EXISTS books (" +
                        "    id    INTEGER PRIMARY KEY," +
                        "    title    TEXT NOT NULL," +
                        "    author    TEXT NOT NULL," +
                        "    year    INTEGER NOT NULL" +
                        ")",
                "CREATE TABLE IF NOT EXISTS students (" +
                        "    id    INTEGER PRIMARY KEY," +
                        "    name    TEXT NOT NULL," +
                        "    student_number    TEXT NOT NULL UNIQUE" +
                        ")",
                "CREATE TABLE IF NOT EXISTS borrows (" +
                        "    student_id    INTEGER NOT NULL," +
                        "    book_id    INTEGER NOT NULL," +
                        "    borrow_date    TEXT NOT NULL," +
                        "    returned    INTEGER NOT NULL," +
                        "    PRIMARY KEY (student_id, book_id, borrow_date)" +
                        ")"
        };
        try (Connection conn = connect();
             Statement st = conn.createStatement()) {
            for (String sql : ddl) {
                st.executeUpdate(sql);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not initialize database schema: " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------
    // Loaders
    // ------------------------------------------------------------------

    public ArrayList<Book> loadBooks() {
        ArrayList<Book> books = new ArrayList<>();
        String sql = "SELECT id, title, author, year FROM books ORDER BY id";
        try (Connection conn = connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                try {
                    books.add(new Book(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("year")));
                } catch (SQLException e) {
                    System.out.println("Skipping invalid book row: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not load books, starting empty: " + e.getMessage());
        }
        return books;
    }

    public ArrayList<Student> loadStudents() {
        ArrayList<Student> students = new ArrayList<>();
        String sql = "SELECT id, name, student_number FROM students ORDER BY id";
        try (Connection conn = connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                try {
                    students.add(new Student(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("student_number")));
                } catch (SQLException e) {
                    System.out.println("Skipping invalid student row: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not load students, starting empty: " + e.getMessage());
        }
        return students;
    }

    public ArrayList<BorrowBook> loadBorrowRecords() {
        ArrayList<BorrowBook> records = new ArrayList<>();
        String sql = "SELECT student_id, book_id, borrow_date, returned FROM borrows ORDER BY borrow_date";
        try (Connection conn = connect();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                try {
                    records.add(new BorrowBook(
                            rs.getInt("student_id"),
                            rs.getInt("book_id"),
                            LocalDate.parse(rs.getString("borrow_date")),
                            rs.getInt("returned") == 1));
                } catch (SQLException | DateTimeParseException e) {
                    System.out.println("Skipping invalid borrow row: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.out.println("Could not load borrow records, starting empty: " + e.getMessage());
        }
        return records;
    }

    // ------------------------------------------------------------------
    // Savers
    // Each save replaces the whole table. This mirrors the existing
    // file-based behaviour where Library.save() rewrites everything.
    // ------------------------------------------------------------------

    public void saveBooks(ArrayList<Book> books) {
        String sql = "INSERT INTO books (id, title, author, year) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM books");
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Book b : books) {
                    ps.setInt(1, b.getId());
                    ps.setString(2, b.getTitle());
                    ps.setString(3, b.getAuthor());
                    ps.setInt(4, b.getYear());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            System.out.println("Warning: could not save books: " + e.getMessage());
        }
    }

    public void saveStudents(ArrayList<Student> students) {
        String sql = "INSERT INTO students (id, name, student_number) VALUES (?, ?, ?)";
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM students");
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (Student s : students) {
                    ps.setInt(1, s.getId());
                    ps.setString(2, s.getName());
                    ps.setString(3, s.getStudentNumber());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            System.out.println("Warning: could not save students: " + e.getMessage());
        }
    }

    public void saveBorrowRecords(ArrayList<BorrowBook> borrowRecords) {
        String sql = "INSERT INTO borrows (student_id, book_id, borrow_date, returned) VALUES (?, ?, ?, ?)";
        try (Connection conn = connect()) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                st.executeUpdate("DELETE FROM borrows");
            }
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                for (BorrowBook r : borrowRecords) {
                    ps.setInt(1, r.getStudentId());
                    ps.setInt(2, r.getBookId());
                    ps.setString(3, r.getBorrowDate().toString());
                    ps.setInt(4, r.isReturned() ? 1 : 0);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            System.out.println("Warning: could not save borrow records: " + e.getMessage());
        }
    }
}
