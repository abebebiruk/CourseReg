import java.util.HashSet;
import java.util.Set;

public class Course
{
    private String courseSectionId;
    private String courseCode; // Base code (e.g., "CSCI004")
    private HashSet<String> prerequisites; // O(1) lookup for prerequisite checking
    private int capacity;
    private int currentEnrollment;
    
    /**
     * Constructor
     */
    public Course(String courseSectionId, String courseCode, int capacity)
    {
    }
    
    /**
     * Add a prerequisite to this course
     */
    public void addPrerequisite(String prereqId)
    {
    }
    
    /**
     * Enroll a student (increment enrollment count)
     */
    public boolean enrollStudent()
    {
        return false;
    }
    
    /**
     * Check if course has available seats
     */
    public boolean hasAvailableSeats()
    {
        return false;
    }
    
    // Getters
    public String getCourseSectionId()
    {
        return courseSectionId;
    }
    
    public String getCourseCode()
    {
        return courseCode;
    }
    
    public Set<String> getPrerequisites()
    {
        return prerequisites;
    }
    
    public int getCapacity()
    {
        return capacity;
    }
    
    public int getCurrentEnrollment()
    {
        return currentEnrollment;
    }
}
