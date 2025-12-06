public class Course {

    public enum Status {
        OPEN,      // O
        CLOSED,    // C
        RESTRICTED // R
    }

    public String courseSectionId;
    public String courseSectionNumber;
    public int capacity;
    public int currentEnrollment;
    public Status status;
    public double creditHours;

    public Course(String id, String number, int capacity, int currentEnrollment,
                  Status status, double creditHours) {

        if (capacity < 0) {
            throw new IllegalArgumentException("capacity cannot be negative");
        }
        if (currentEnrollment < 0 || currentEnrollment > capacity) {
            throw new IllegalArgumentException("currentEnrollment must be between 0 and capacity");
        }

        this.courseSectionId = id;
        this.courseSectionNumber = number;
        this.capacity = capacity;
        this.currentEnrollment = currentEnrollment;
        this.status = status;
        this.creditHours = creditHours;
    }
}
