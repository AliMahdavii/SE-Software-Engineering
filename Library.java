import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;

// Holds all data and the business rules. Invalid operations throw IllegalArgumentException
// with a message that can be shown directly to the user.
public class Library {
    private final ArrayList<Book> books = new ArrayList<>();
    private final ArrayList<Student> students = new ArrayList<>();
    private final ArrayList<BorrowBook> borrowRecords = new ArrayList<>();
    private final DataStore store;

    public Library(DataStore store) {
        this.store = store;
        loadData();
    }

    private void loadData() {
        for (Book book : store.loadBooks()) {
            if (findBook(book.getId()) == null) {
                books.add(book);
            }
        }
        for (Student student : store.loadStudents()) {
            if (findStudent(student.getId()) == null && findStudentByNumber(student.getStudentNumber()) == null) {
                students.add(student);
            }
        }
        for (BorrowBook record : store.loadBorrowRecords()) {
            Book book = findBook(record.getBookId());
            if (book == null || findStudent(record.getStudentId()) == null) {
                continue;
            }
            borrowRecords.add(record);
            if (!record.isReturned()) {
                book.setAvailable(false);
            }
        }
    }

    private void save() {
        try {
            store.saveBooks(books);
            store.saveStudents(students);
            store.saveBorrowRecords(borrowRecords);
        } catch (IOException e) {
            System.out.println("Warning: could not save data: " + e.getMessage());
        }
    }

    public void addBook(int id, String title, String author, int year) {
        if (id <= 0) {
            throw new IllegalArgumentException("Book ID must be a positive number.");
        }
        if (findBook(id) != null) {
            throw new IllegalArgumentException("A book with this ID already exists.");
        }
        if (year < 1000 || year > LocalDate.now().getYear()) {
            throw new IllegalArgumentException("Publication year must be between 1000 and "
                    + LocalDate.now().getYear() + ".");
        }
        books.add(new Book(id, title, author, year));
        save();
    }

    public void removeBook(int id) {
        Book book = findBook(id);
        if (book == null) {
            throw new IllegalArgumentException("Book not found.");
        }
        if (!book.isAvailable()) {
            throw new IllegalArgumentException("This book is currently borrowed and cannot be removed.");
        }
        books.remove(book);
        borrowRecords.removeIf(record -> record.getBookId() == id);
        save();
    }

    public ArrayList<Book> searchBooks(String keyword) {
        ArrayList<Book> result = new ArrayList<>();
        String text = keyword.toLowerCase();
        for (Book book : books) {
            if (book.getTitle().toLowerCase().contains(text) || book.getAuthor().toLowerCase().contains(text)) {
                result.add(book);
            }
        }
        return result;
    }

    public void registerStudent(int id, String name, String studentNumber) {
        if (id <= 0) {
            throw new IllegalArgumentException("Student ID must be a positive number.");
        }
        if (findStudent(id) != null) {
            throw new IllegalArgumentException("A student with this ID already exists.");
        }
        if (findStudentByNumber(studentNumber) != null) {
            throw new IllegalArgumentException("A student with this student number already exists.");
        }
        students.add(new Student(id, name, studentNumber));
        save();
    }

    // Searches by ID first, then by student number.
    public Student searchStudent(String text) {
        try {
            Student byId = findStudent(Integer.parseInt(text));
            if (byId != null) {
                return byId;
            }
        } catch (NumberFormatException e) {
            // not a number, so it can only be a student number
        }
        return findStudentByNumber(text);
    }

    public void borrowBook(int studentId, int bookId) {
        if (findStudent(studentId) == null) {
            throw new IllegalArgumentException("Student is not registered.");
        }
        Book book = findBook(bookId);
        if (book == null) {
            throw new IllegalArgumentException("Book not found.");
        }
        if (!book.isAvailable()) {
            throw new IllegalArgumentException("This book is already borrowed.");
        }
        borrowRecords.add(new BorrowBook(studentId, bookId, LocalDate.now(), false));
        book.setAvailable(false);
        save();
    }

    public void returnBook(int studentId, int bookId) {
        BorrowBook record = findActiveBorrow(studentId, bookId);
        if (record == null) {
            throw new IllegalArgumentException("This student has not borrowed this book.");
        }
        record.markReturned();
        findBook(bookId).setAvailable(true);
        save();
    }

    public ArrayList<Book> getBooks() {
        return books;
    }

    public ArrayList<Student> getStudents() {
        return students;
    }

    public ArrayList<BorrowBook> getBorrowRecords() {
        return borrowRecords;
    }

    private Book findBook(int id) {
        for (Book book : books) {
            if (book.getId() == id) {
                return book;
            }
        }
        return null;
    }

    private Student findStudent(int id) {
        for (Student student : students) {
            if (student.getId() == id) {
                return student;
            }
        }
        return null;
    }

    private Student findStudentByNumber(String studentNumber) {
        for (Student student : students) {
            if (student.getStudentNumber().equalsIgnoreCase(studentNumber)) {
                return student;
            }
        }
        return null;
    }

    private BorrowBook findActiveBorrow(int studentId, int bookId) {
        for (BorrowBook record : borrowRecords) {
            if (record.getStudentId() == studentId && record.getBookId() == bookId && !record.isReturned()) {
                return record;
            }
        }
        return null;
    }
}
