// Handles changes to the book and student lists and returns a message for the user.
public class Manager {
    private final Library library;

    public Manager(Library library) {
        this.library = library;
    }

    public String addBook(int id, String title, String author, int year) {
        library.addBook(id, title, author, year);
        return "Book added: " + title;
    }

    public String removeBook(int id) {
        library.removeBook(id);
        return "Book removed.";
    }

    public String registerStudent(int id, String name, String studentNumber) {
        library.registerStudent(id, name, studentNumber);
        return "Student registered: " + name;
    }
}
