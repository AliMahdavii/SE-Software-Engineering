public class Book {
    private final int id;
    private final String title;
    private final String author;
    private final int year;
    private boolean available = true;

    public Book(int id, String title, String author, int year) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getAuthor() {
        return author;
    }

    public int getYear() {
        return year;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    @Override
    public String toString() {
        return "ID: " + id + " | " + title + " by " + author + " (" + year + ") | "
                + (available ? "Available" : "Borrowed");
    }
}
