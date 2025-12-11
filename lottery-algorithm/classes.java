public class classes {

    public final String courseSectionId;
    public final String courseSectionNumber;
    public final int capacity;
    public int currentEnrollment;
    public final double creditHours;

    public classes(String id, String number, int capacity, int currentEnrollment, double creditHours) {
        if (capacity < 0)
            throw new IllegalArgumentException("capacity cannot be negative");
        if (currentEnrollment < 0 || currentEnrollment > capacity)
            throw new IllegalArgumentException("currentEnrollment must be between 0 and capacity");

        this.courseSectionId = id;
        this.courseSectionNumber = number;
        this.capacity = capacity;
        this.currentEnrollment = currentEnrollment;
        this.creditHours = creditHours;
    }
}
