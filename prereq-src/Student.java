import java.util.HashSet;
import java.util.Set;

public class Student
{
    private String studentId;
    private String name;
    private HashSet<String> pastClasses; // O(1) lookup instead of List which is O(n)
    private int gradYear;
    private String majorStatus;
    
    /**
     * Constructor
     */
    public Student(String studentId, String name, int gradYear, String majorStatus)
    {
    }
    
    /**
     * Check if student has completed a specific course - O(1) lookup
     */
    public boolean hasCompletedCourse(String courseId)
    {
        return false;
    }
    
    /**
     * Add a completed course to student's history
     */
    public void addPastClass(String courseId)
    {
    }
    
    // Getters
    public String getStudentId()
    {
        return studentId;
    }
    
    public String getName()
    {
        return name;
    }
    
    public Set<String> getPastClasses()
    {
        return pastClasses;
    }
    
    public int getGradYear()
    {
        return gradYear;
    }
    
    public String getMajorStatus()
    {
        return majorStatus;
    }
}
