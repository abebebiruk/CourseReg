import java.util.ArrayList;
import java.util.List;

public class students {

    public enum MajorStatus {
        CS_MAJOR,
        CS_MINOR,
        NON_MAJOR
    }

    public final String studentId;
    public final String name;
    public final List<String> pastClasses;
    public final List<String> requestedClasses;
    public final int gradYear;
    public final MajorStatus majorStatus;

    public students(String studentId, String name,
                   List<String> pastClasses,
                   List<String> requestedClasses,
                   int gradYear,
                   MajorStatus majorStatus) {

        if (studentId == null || name == null) {
            throw new IllegalArgumentException("Student ID and name cannot be null.");
        }

        this.studentId = studentId;
        this.name = name;

        // Defend against null lists
        this.pastClasses = (pastClasses != null)
                ? pastClasses
                : new ArrayList<>();

        this.requestedClasses = (requestedClasses != null)
                ? requestedClasses
                : new ArrayList<>();

        // Validate grad year 
        if (gradYear < 1900 || gradYear > 2100) {
            throw new IllegalArgumentException("Invalid graduation year: " + gradYear);
        }
        this.gradYear = gradYear;

        // Default major status
        this.majorStatus = (majorStatus != null)
                ? majorStatus
                : MajorStatus.NON_MAJOR;
    }

    // has the student already taken the class?
    public boolean hasTaken(String classId) {
        return pastClasses.contains(classId);
    }

    // is the student trying to enroll in the class?
    public boolean isRequesting(String classId) {
        return requestedClasses.contains(classId);
    }
}
