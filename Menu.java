import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Menu {
    private final Scanner scanner = new Scanner(System.in, "UTF-8");
    private final Library library;
    private final Manager manager;
    private final Operator operator;

    public Menu() {
        // SQLite-backed storage. The database file (data/library.db) is
        // created automatically on first run. To switch back to the
        // file-based backend, replace DatabaseHandler with FileHandler.
        library = new Library(new DatabaseHandler("data"));
        manager = new Manager(library);
        operator = new Operator(library);
    }

    public static void main(String[] args) {
        new Menu().run();
    }

    public void run() {
        try {
            boolean running = true;
            while (running) {
                System.out.println();
                System.out.println("===== Library Management System =====");
                System.out.println("1. Book management");
                System.out.println("2. Student management");
                System.out.println("3. Borrow a book");
                System.out.println("4. Return a book");
                System.out.println("5. Show borrowing records");
                System.out.println("0. Exit");
                int choice = readInt("Choose an option: ");
                try {
                    switch (choice) {
                        case 1:
                            bookMenu();
                            break;
                        case 2:
                            studentMenu();
                            break;
                        case 3:
                            System.out.println(operator.borrowBook(readInt("Student ID: "), readInt("Book ID: ")));
                            break;
                        case 4:
                            System.out.println(operator.returnBook(readInt("Student ID: "), readInt("Book ID: ")));
                            break;
                        case 5:
                            printAll(library.getBorrowRecords(), "No borrowing records yet.");
                            break;
                        case 0:
                            running = false;
                            break;
                        default:
                            System.out.println("Invalid choice. Please try again.");
                    }
                } catch (IllegalArgumentException e) {
                    System.out.println("Error: " + e.getMessage());
                }
            }
        } catch (NoSuchElementException e) {
            System.out.println();
            System.out.println("Input closed.");
        }
        System.out.println("Goodbye!");
    }

    private void bookMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- Book management ---");
            System.out.println("1. Add a book");
            System.out.println("2. Display all books");
            System.out.println("3. Search books by title or author");
            System.out.println("4. Remove a book");
            System.out.println("0. Back");
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1:
                        int id = readInt("Book ID: ");
                        String title = readText("Title: ");
                        String author = readText("Author: ");
                        int year = readInt("Publication year: ");
                        System.out.println(manager.addBook(id, title, author, year));
                        break;
                    case 2:
                        printAll(library.getBooks(), "No books in the library.");
                        break;
                    case 3:
                        printAll(library.searchBooks(readText("Search text: ")), "No matching books found.");
                        break;
                    case 4:
                        System.out.println(manager.removeBook(readInt("Book ID: ")));
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void studentMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("--- Student management ---");
            System.out.println("1. Register a student");
            System.out.println("2. Display all students");
            System.out.println("3. Search student by ID or student number");
            System.out.println("0. Back");
            int choice = readInt("Choose an option: ");
            try {
                switch (choice) {
                    case 1:
                        int id = readInt("Student ID: ");
                        String name = readText("Name: ");
                        String number = readText("Student number: ");
                        System.out.println(manager.registerStudent(id, name, number));
                        break;
                    case 2:
                        printAll(library.getStudents(), "No students registered.");
                        break;
                    case 3:
                        Student student = library.searchStudent(readText("ID or student number: "));
                        System.out.println(student == null ? "No student found." : student);
                        break;
                    case 0:
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private void printAll(ArrayList<?> items, String emptyMessage) {
        if (items.isEmpty()) {
            System.out.println(emptyMessage);
            return;
        }
        for (Object item : items) {
            System.out.println(item);
        }
    }

    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            try {
                return Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine().trim();
            if (!line.isEmpty()) {
                return line;
            }
            System.out.println("This field cannot be empty.");
        }
    }
}
