import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class RegistrationSystem
{
    // Stores all students - O(1) lookup by studentId instead of ArrayList with O(n) search
    private HashMap<String, students> students;
    // Stores all courses - O(1) lookup by courseId
    private HashMap<String, Course> courses;
    // O(1) lookup to find all sections of a course (e.g., all "CSCI004" sections)
    private HashMap<String, Set<String>> coursesByCode;
    // Lottery pool (temporarily used during course assignment)
    private LinkedList<students> lottery;
    
    /**
     * Constructor
     * Initializes all HashMaps and Lists
     */
    public RegistrationSystem()
    {
        this.students = new HashMap<>();
        this.courses = new HashMap<>();
        this.coursesByCode = new HashMap<>();
        this.lottery = new LinkedList<>();
    }
    
    /**
     * Add a student to the system
     */
    public void addStudent(students student)
    {
        if (student != null && student.getStudentId() != null)
        {
            students.put(student.getStudentId(), student);
        }
    }
    
    /**
     * Add a course to the system
     */
    public void addCourse(Course course)
    {
        if (course != null && course.getCourseSectionId() != null)
        {
            courses.put(course.getCourseSectionId(), course);
            
            String courseCode = course.getCourseCode();
            if (courseCode != null)
            {
                if (!coursesByCode.containsKey(courseCode))
                {
                    coursesByCode.put(courseCode, new HashSet<>());
                }
                coursesByCode.get(courseCode).add(course.getCourseSectionId());
            }
        }
    }
    
    /**
     * Validate if a student can register for a course
     * Time Complexity: O(p) where p = number of prerequisites
     */
    public PrerequisiteValidationResult validateRegistration(String studentId, String courseId)
    {
        students student = getStudent(studentId);
        Course course = getCourse(courseId);
        
        if (student == null)
        {
            return new PrerequisiteValidationResult(false, new HashSet<>(), "Student not found");
        }
        
        if (course == null)
        {
            return new PrerequisiteValidationResult(false, new HashSet<>(), "Course not found");
        }
        
        if (!course.hasAvailableSeats())
        {
            return new PrerequisiteValidationResult(false, new HashSet<>(), "Course is full");
        }
        
        Set<String> missingPrereqs = PrerequisiteChecker.getMissingPrerequisites(student, course);
        
        if (missingPrereqs.isEmpty())
        {
            return new PrerequisiteValidationResult(true, missingPrereqs, "Eligible for registration");
        }
        else
        {
            String message = "Missing prerequisites: " + String.join(", ", missingPrereqs);
            return new PrerequisiteValidationResult(false, missingPrereqs, message);
        }
    }
    
    /**
     * Get a student by ID - O(1) lookup
     */
    public students getStudent(String studentId)
    {
        return students.get(studentId);
    }
    
    /**
     * Get a course by ID - O(1) lookup
     */
    public Course getCourse(String courseId)
    {
        return courses.get(courseId);
    }
    
    /**
     * Load student data from file - Builds HashMap during load
     * CSV format: student_id,name,past_classes,requested_classes,grad_year,major_status
     * past_classes format: [CS140,CS51,CS62] (bracketed list)
     */
    public void loadStudentData(String filepath)
    {
        try (BufferedReader br = new BufferedReader(new FileReader(filepath)))
        {
            String line = br.readLine();
            
            while ((line = br.readLine()) != null)
            {
                int firstComma = line.indexOf(',');
                int secondComma = line.indexOf(',', firstComma + 1);
                int firstBracket = line.indexOf('[');
                int firstCloseBracket = line.indexOf(']');
                int secondBracket = line.indexOf('[', firstCloseBracket + 1);
                int secondCloseBracket = line.indexOf(']', secondBracket + 1);
                
                if (firstComma == -1 || secondComma == -1)
                {
                    continue;
                }
                
                String studentId = line.substring(0, firstComma).trim();
                String name = line.substring(firstComma + 1, secondComma).trim();
                
                String pastClassesStr = "";
                String requestedClassesStr = "";
                
                if (firstBracket != -1 && firstCloseBracket != -1)
                {
                    pastClassesStr = line.substring(firstBracket, firstCloseBracket + 1).trim();
                }
                
                if (secondBracket != -1 && secondCloseBracket != -1)
                {
                    requestedClassesStr = line.substring(secondBracket, secondCloseBracket + 1).trim();
                }
                
                String remaining = line.substring(secondCloseBracket + 2);
                String[] lastParts = remaining.split(",");
                
                if (lastParts.length < 2)
                {
                    continue;
                }
                
                int gradYear = Integer.parseInt(lastParts[0].trim());
                String majorStatus = lastParts[1].trim();
                
                students student = new students(studentId, name, gradYear, majorStatus);
                
                if (!pastClassesStr.equals("") && !pastClassesStr.equals("[]"))
                {
                    String cleaned = pastClassesStr.replace("[", "").replace("]", "");
                    String[] courses = cleaned.split(",");
                    for (String course : courses)
                    {
                        student.addPastClass(course.trim());
                    }
                }
                
                addStudent(student);
            }
        }
        catch (IOException e)
        {
            System.err.println("Error loading student data: " + e.getMessage());
        }
        catch (Exception e)
        {
            System.err.println("Error parsing student data: " + e.getMessage());
        }
    }
    
    /**
     * Load course data from file - Builds HashMap during load
     * Note: This is a placeholder for CSV format. For JSON, use a JSON library.
     * CSV format expected: courseSectionId,courseCode,capacity,currentEnrollment
     */
    public void loadCourseData(String filepath)
    {
        // This method would need a JSON parser library for the actual JSON file
        // For now, it's a placeholder that shows the structure
        System.out.println("Course data loading requires JSON parsing library");
    }
}
