import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

// Each record is one line. Fields are separated by a tab character.
// Backslash, tab and line breaks inside a field are escaped so they cannot break the format.
public class FileHandler implements DataStore {
    private static final String BOOKS_FILE = "books.txt";
    private static final String STUDENTS_FILE = "students.txt";
    private static final String BORROWS_FILE = "borrows.txt";

    private final Path folder;

    public FileHandler(String folderName) {
        this.folder = Paths.get(folderName);
    }

    public ArrayList<Book> loadBooks() {
        ArrayList<Book> books = new ArrayList<>();
        for (String[] f : readRecords(BOOKS_FILE, 4)) {
            try {
                books.add(new Book(Integer.parseInt(f[0]), f[1], f[2], Integer.parseInt(f[3])));
            } catch (NumberFormatException e) {
                System.out.println("Skipping invalid book record in " + BOOKS_FILE);
            }
        }
        return books;
    }

    public ArrayList<Student> loadStudents() {
        ArrayList<Student> students = new ArrayList<>();
        for (String[] f : readRecords(STUDENTS_FILE, 3)) {
            try {
                students.add(new Student(Integer.parseInt(f[0]), f[1], f[2]));
            } catch (NumberFormatException e) {
                System.out.println("Skipping invalid student record in " + STUDENTS_FILE);
            }
        }
        return students;
    }

    public ArrayList<BorrowBook> loadBorrowRecords() {
        ArrayList<BorrowBook> records = new ArrayList<>();
        for (String[] f : readRecords(BORROWS_FILE, 4)) {
            try {
                if (!f[3].equals("true") && !f[3].equals("false")) {
                    throw new NumberFormatException();
                }
                records.add(new BorrowBook(Integer.parseInt(f[0]), Integer.parseInt(f[1]),
                        LocalDate.parse(f[2]), Boolean.parseBoolean(f[3])));
            } catch (NumberFormatException | DateTimeParseException e) {
                System.out.println("Skipping invalid borrowing record in " + BORROWS_FILE);
            }
        }
        return records;
    }

    public void saveBooks(ArrayList<Book> books) throws IOException {
        ArrayList<String[]> records = new ArrayList<>();
        for (Book b : books) {
            records.add(new String[]{String.valueOf(b.getId()), b.getTitle(), b.getAuthor(),
                    String.valueOf(b.getYear())});
        }
        writeRecords(BOOKS_FILE, records);
    }

    public void saveStudents(ArrayList<Student> students) throws IOException {
        ArrayList<String[]> records = new ArrayList<>();
        for (Student s : students) {
            records.add(new String[]{String.valueOf(s.getId()), s.getName(), s.getStudentNumber()});
        }
        writeRecords(STUDENTS_FILE, records);
    }

    public void saveBorrowRecords(ArrayList<BorrowBook> borrowRecords) throws IOException {
        ArrayList<String[]> records = new ArrayList<>();
        for (BorrowBook r : borrowRecords) {
            records.add(new String[]{String.valueOf(r.getStudentId()), String.valueOf(r.getBookId()),
                    r.getBorrowDate().toString(), String.valueOf(r.isReturned())});
        }
        writeRecords(BORROWS_FILE, records);
    }

    // Returns an empty list if the file is missing or cannot be read. Bad lines are skipped.
    private ArrayList<String[]> readRecords(String fileName, int fieldCount) {
        ArrayList<String[]> records = new ArrayList<>();
        Path file = folder.resolve(fileName);
        if (!Files.exists(file)) {
            return records;
        }
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.trim().isEmpty()) {
                    continue;
                }
                String[] fields = line.split("\t", -1);
                if (fields.length != fieldCount) {
                    System.out.println("Skipping malformed line in " + fileName + ": " + line);
                    continue;
                }
                for (int i = 0; i < fields.length; i++) {
                    fields[i] = unescape(fields[i]);
                }
                records.add(fields);
            }
        } catch (IOException e) {
            System.out.println("Could not read " + fileName + ", starting empty: " + e.getMessage());
            records.clear();
        }
        return records;
    }

    private void writeRecords(String fileName, ArrayList<String[]> records) throws IOException {
        Files.createDirectories(folder);
        ArrayList<String> lines = new ArrayList<>();
        for (String[] fields : records) {
            StringBuilder line = new StringBuilder();
            for (int i = 0; i < fields.length; i++) {
                if (i > 0) {
                    line.append('\t');
                }
                line.append(escape(fields[i]));
            }
            lines.add(line.toString());
        }
        Files.write(folder.resolve(fileName), lines, StandardCharsets.UTF_8);
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\t", "\\t").replace("\n", "\\n").replace("\r", "\\r");
    }

    private static String unescape(String text) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '\\' && i + 1 < text.length()) {
                i++;
                char next = text.charAt(i);
                if (next == 't') {
                    result.append('\t');
                } else if (next == 'n') {
                    result.append('\n');
                } else if (next == 'r') {
                    result.append('\r');
                } else {
                    result.append(next);
                }
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
