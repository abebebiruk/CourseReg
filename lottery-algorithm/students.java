import java.util.ArrayList;
import java.util.List;

public class students {
    public enum MajorStatus { CS_MAJOR, CS_MINOR, NON_MAJOR }
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
        if (studentId == null || name == null)
            throw new IllegalArgumentException("Student ID and name cannot be null.");
        this.studentId = studentId;
        this.name = name;
        this.pastClasses = (pastClasses != null) ? pastClasses : new ArrayList<>();
        this.requestedClasses = (requestedClasses != null) ? requestedClasses : new ArrayList<>();
        if (gradYear < 1900 || gradYear > 2100)
            throw new IllegalArgumentException("Invalid graduation year: " + gradYear);
        this.gradYear = gradYear;
        this.majorStatus = (majorStatus != null) ? majorStatus : MajorStatus.NON_MAJOR;
    }
    
    /**
     * Checks whether the student has previously completed a specific class.
     * @param classId  the ID of the class to look for
     * @return true if the class appears in the student's list of completed courses, false otherwise
     */
    public boolean hasTaken(String classId) {
        return pastClasses.contains(classId);
    }

    /**
     * Checks whether the student is currently requesting enrollment in a specific class.
     * @param classId  the ID of the class being checked
     * @return true if the class appears in the student's requestedClasses list, false otherwise
     */
    public boolean isRequesting(String classId) {
        return requestedClasses.contains(classId);
    }
}
