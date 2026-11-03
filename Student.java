public class Student extends User {
    private final String studentNumber;

    public Student(int id, String name, String studentNumber) {
        super(id, name);
        this.studentNumber = studentNumber;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    @Override
    public String toString() {
        return "ID: " + getId() + " | Name: " + getName() + " | Student No: " + studentNumber;
    }
}
