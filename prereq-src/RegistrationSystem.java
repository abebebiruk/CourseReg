import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;

public class RegistrationSystem
{
    // Stores all students - O(1) lookup by studentId instead of ArrayList with O(n) search
    private HashMap<String, Student> students;
    // Stores all courses - O(1) lookup by courseId
    private HashMap<String, Course> courses;
    // O(1) lookup to find all sections of a course (e.g., all "CSCI004" sections)
    private HashMap<String, Set<String>> coursesByCode;
    // Lottery pool (temporarily used during course assignment)
    private LinkedList<Student> lottery;
    
    /**
     * Constructor
     * Initializes all HashMaps and Lists
     */
    public RegistrationSystem()
    {
    }
    
    /**
     * Add a student to the system
     */
    public void addStudent(Student student)
    {
    }
    
    /**
     * Add a course to the system
     */
    public void addCourse(Course course)
    {
    }
    
    /**
     * Validate if a student can register for a course
     * Time Complexity: O(p) where p = number of prerequisites
     */
    public PrerequisiteValidationResult validateRegistration(String studentId, String courseId)
    {
        return null;
    }
    
    /**
     * Get a student by ID - O(1) lookup
     */
    public Student getStudent(String studentId)
    {
        return null;
    }
    
    /**
     * Get a course by ID - O(1) lookup
     */
    public Course getCourse(String courseId)
    {
        return null;
    }
    
    /**
     * Load student data from file - Builds HashMap during load
     */
    public void loadStudentData(String filepath)
    {
    }
    
    /**
     * Load course data from file - Builds HashMap during load
     */
    public void loadCourseData(String filepath)
    {
    }
}
