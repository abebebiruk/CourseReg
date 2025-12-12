import java.util.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * MainInterface - Main interactive interface for course registration system
 * Provides the main menu with:
 * - Student Registration - Add course requests for existing students
 * - Run Lottery - Process all requests
 * - View Courses and Prerequisites - View courses and their prerequisites
 * - Exit
 */
public class MainInterface
{
    private RegistrationSystem registrationSystem;
    private Scanner scanner;
    private String studentCsvPath;
    private String courseJsonPath;
    private List<classes> allCourses;
    private List<ClassRequest> allRequests; // Store all course requests
    
    
    // Main Entry Point
    
    public MainInterface(String studentCsvPath, String courseJsonPath)
    {
        this.registrationSystem = new RegistrationSystem();
        this.scanner = new Scanner(System.in);
        this.studentCsvPath = studentCsvPath;
        this.courseJsonPath = courseJsonPath;
        this.allCourses = new ArrayList<>();
        this.allRequests = new ArrayList<>();
    }
    
    /**
     * Main method to run the interface
     */
    public static void main(String[] args)
    {
        String studentCsvPath = "data/student.csv";
        String courseJsonPath = "data/cs-courses.json";
        
        if (args.length >= 1)
        {
            studentCsvPath = args[0];
        }
        if (args.length >= 2)
        {
            courseJsonPath = args[1];
        }
        
        MainInterface mainInterface = new MainInterface(studentCsvPath, courseJsonPath);
        try
        {
            mainInterface.run();
        }
        finally
        {
            mainInterface.close();
        }
    }

    /**
     * Main menu loop
     */
    public void run()
    {
        System.out.println("=== Course Registration System ===");
        System.out.println();
        
        // Load existing data
        loadExistingData();
        
        while (true)
        {
            System.out.println("\n--- Main Menu ---");
            System.out.println("1. Student Registration - Add course requests");
            System.out.println("2. Run Lottery - Process all requests");
            System.out.println("3. View Courses and Prerequisites");
            System.out.println("4. Exit");
            System.out.print("\nEnter your choice (1-4): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice)
            {
                case "1":
                    studentRegistration();
                    break;
                case "2":
                    // runLottery();
                    break;
                case "3":
                    // viewCoursesAndPrerequisites();
                    break;
                case "4":
                    System.out.println("Thank you for using the Course Registration System!");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2, 3, or 4.");
            }
        }
    }
    

    // Load Data
    
    /**
     * Load existing student and course data
     */
    private void loadExistingData()
    {
        try
        {
            // Load student data
            if (new java.io.File(studentCsvPath).exists())
            {
                registrationSystem.loadStudentData(studentCsvPath);
                System.out.println("Loaded existing student data.");
            }
            
            // Load course data from JSON
            if (new java.io.File(courseJsonPath).exists())
            {
                allCourses = CourseJsonLoader.loadCoursesFromJson(courseJsonPath);
                System.out.println("Loaded " + allCourses.size() + " courses.");
                
                // Add courses to registration system for prerequisite checking
                for (classes c : allCourses)
                {
                    String courseCode = extractCourseCode(c.courseSectionId);
                    Course course = new Course(c.courseSectionId, courseCode, c.capacity);
                    registrationSystem.addCourse(course);
                }
            }
            else
            {
                System.out.println("Warning: Course data file not found at " + courseJsonPath);
            }
            
            // Load requested classes from CSV and convert to ClassRequest objects
            loadRequestedClassesFromCsv();
        }
        catch (Exception e)
        {
            System.err.println("Error loading data: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Load requested classes from CSV file and convert to ClassRequest objects
     */
    private void loadRequestedClassesFromCsv()
    {
        try (BufferedReader br = new BufferedReader(new FileReader(studentCsvPath)))
        {
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null)
            {
                if (line.trim().isEmpty()) continue;
                
                int firstComma = line.indexOf(',');
                int secondComma = line.indexOf(',', firstComma + 1);
                int firstCloseBracket = line.indexOf(']');
                int secondBracket = line.indexOf('[', firstCloseBracket + 1);
                int secondCloseBracket = (secondBracket != -1) ? line.indexOf(']', secondBracket + 1) : -1;
                
                if (firstComma == -1 || secondComma == -1) continue;
                
                String studentId = line.substring(0, firstComma).trim();
                String requestedClassesStr = "";
                
                if (secondBracket != -1 && secondCloseBracket != -1)
                {
                    requestedClassesStr = line.substring(secondBracket, secondCloseBracket + 1).trim();
                }
                
                if (requestedClassesStr.equals("") || requestedClassesStr.equals("[]"))
                {
                    continue; // No requested classes
                }
                
                // Parse requested classes
                String cleaned = requestedClassesStr.replace("[", "").replace("]", "");
                String[] courseCodes = cleaned.split(",");
                
                int rank = 1;
                for (String courseCode : courseCodes)
                {
                    courseCode = courseCode.trim();
                    if (courseCode.isEmpty()) continue;
                    
                    // Normalize course code (ensure CS prefix)
                    String normalizedCode = normalizeCourseCodeInput(courseCode);
                    
                    // Find matching course sections
                    List<classes> matchingCourses = findCoursesByCode(normalizedCode);
                    
                    if (!matchingCourses.isEmpty())
                    {
                        // Use the first available section (or first section if all are full)
                        // In a real system, you might want to pick the one with most available seats
                        classes selectedCourse = matchingCourses.get(0);
                        
                        // Create ClassRequest with preference rank
                        ClassRequest request = new ClassRequest(studentId, selectedCourse.courseSectionId, rank);
                        allRequests.add(request);
                    }
                    
                    rank++;
                    if (rank > 4) break; // Maximum 4 requests
                }
            }
            
            if (!allRequests.isEmpty())
            {
                System.out.println("Loaded " + allRequests.size() + " course requests from CSV.");
            }
        }
        catch (IOException e)
        {
            System.err.println("Error loading requested classes from CSV: " + e.getMessage());
        }
        catch (Exception e)
        {
            System.err.println("Error parsing requested classes: " + e.getMessage());
        }
    }
    
    // Registration of students and adding course requests
    
    /**
     * Student Registration - Register new students or add course requests for existing students
     */
    private void studentRegistration()
    {
        System.out.println("\n=== Student Registration ===");
        System.out.println("1. Register as New Student");
        System.out.println("2. Add Course Requests (Existing Student)");
        System.out.print("Enter your choice (1-2): ");
        
        String choice = scanner.nextLine().trim();
        
        switch (choice)
        {
            case "1":
                // Register new student using RegistrationInterface
                RegistrationInterface regInterface = new RegistrationInterface(studentCsvPath);
                regInterface.registerNewStudent();
                // Reload student data and requested classes after registration
                registrationSystem.loadStudentData(studentCsvPath);
                loadRequestedClassesFromCsv();
                break;
            case "2":
                // Add course requests for existing student
                addCourseRequestsForExistingStudent();
                break;
            default:
                System.out.println("Invalid choice. Please enter 1 or 2.");
        }
    }
    
    /**
     * Add course requests for an existing student
     */
    private void addCourseRequestsForExistingStudent()
    {
        System.out.print("\nEnter your name: ");
        String name = scanner.nextLine().trim();
        
        if (name.isEmpty())
        {
            System.out.println("Name cannot be empty.");
            return;
        }
        
        // Find student by name
        Student student = findStudentByName(name);
        if (student == null)
        {
            System.out.println("Student not found. Please register as a new student first (option 1).");
            return;
        }
        
        // Display student profile
        displayStudentProfile(student);
        
        // Add course requests
        addCourseRequests(student);
    }
    
    /**
     * Find a student by name (case-insensitive)
     */
    private Student findStudentByName(String name)
    {
        Map<String, Student> allStudents = registrationSystem.getAllStudents();
        for (Student s : allStudents.values())
        {
            if (s.getName().equalsIgnoreCase(name))
            {
                return s;
            }
        }
        return null;
    }
    
    /**
     * Display student profile
     */
    private void displayStudentProfile(Student student)
    {
        System.out.println("\n--- Your Profile ---");
        System.out.println("Name: " + student.getName());
        System.out.println("Student ID: " + student.getStudentId());
        System.out.println("Graduation Year: " + student.getGradYear());
        System.out.println("Major Status: " + student.getMajorStatus());
        
        System.out.print("Past Courses Completed: ");
        Set<String> pastClasses = student.getPastClasses();
        if (pastClasses.isEmpty())
        {
            System.out.println("None");
        }
        else
        {
            List<String> sorted = new ArrayList<>(pastClasses);
            Collections.sort(sorted);
            System.out.println(String.join(", ", sorted));
        }
        System.out.println();
    }
    
    // Checking course code validity
    
    /**
     * Normalize course code input from user
     */
    private String normalizeCourseCodeInput(String courseCode)
    {
        if (courseCode == null) return "";
        courseCode = courseCode.toUpperCase().replaceAll("\\s+", "");
        
        if (courseCode.startsWith("CSCI"))
        {
            courseCode = courseCode.substring(4);
            if (courseCode.matches("^0+\\d+.*"))
            {
                courseCode = courseCode.replaceFirst("^0+", "");
            }
        }
        else if (courseCode.startsWith("CS"))
        {
            courseCode = courseCode.substring(2);
        }
        
        if (courseCode.matches("^0+\\d+.*"))
        {
            courseCode = courseCode.replaceFirst("^0+", "");
        }
        
        return "CS" + courseCode;
    }
    
    /**
     * Extract course code from course section ID
     * Handles formats like "CSCI140  HM-01 SP2025", "CSCI181DVPO-01 SP2025", or "CS62-01"
     * Converts CSCI prefix to CS and removes location codes (like HM, PO)
     */
    private String extractCourseCode(String courseSectionId)
    {
        if (courseSectionId == null || courseSectionId.isEmpty())
        {
            return "";
        }
        
        int dashIndex = courseSectionId.indexOf('-');
        int spaceIndex = courseSectionId.indexOf(' ');
        
        String courseCode;
        if (dashIndex > 0 && (spaceIndex == -1 || dashIndex < spaceIndex))
        {
            courseCode = courseSectionId.substring(0, dashIndex).trim();
        }
        else if (spaceIndex > 0)
        {
            courseCode = courseSectionId.substring(0, spaceIndex).trim();
        }
        else
        {
            courseCode = courseSectionId.trim();
        }
        
        // Convert CSCI prefix to CS
        if (courseCode.startsWith("CSCI"))
        {
            courseCode = "CS" + courseCode.substring(4);
        }
        
        // Note: Location codes (like PO, HM) are kept in the course code
        // Matching is done using "starts with" logic in findCoursesByCode()
        
        // Remove leading zeros from numbers
        if (courseCode.length() > 2 && courseCode.startsWith("CS"))
        {
            String numberPart = courseCode.substring(2);
            if (numberPart.matches("^0+\\d+.*"))
            {
                numberPart = numberPart.replaceFirst("^0+", "");
                courseCode = "CS" + numberPart;
            }
        }
        
        return courseCode;
    }
    
    /**
     * Remove location code from course code (e.g., CS181DVPO -> CS181DV)
     */
    private String removeLocationCode(String courseCode)
    {
        if (courseCode.length() > 4 && courseCode.startsWith("CS"))
        {
            String afterCS = courseCode.substring(2);
            // Check if ends with 2 uppercase letters (likely location code)
            if (afterCS.length() >= 2)
            {
                String lastTwo = afterCS.substring(afterCS.length() - 2);
                // Common location codes (2 uppercase letters)
                Set<String> locationCodes = Set.of("PO", "HM", "CM", "PZ", "AF", "SA", "IO", "BS");
                
                if (locationCodes.contains(lastTwo))
                {
                    // Remove the location code
                    return "CS" + afterCS.substring(0, afterCS.length() - 2);
                }
            }
        }
        return courseCode;
    }
    
    /**
     * Validate course code format
     */
    private boolean isValidCourseCode(String courseCode)
    {
        if (courseCode == null || courseCode.isEmpty() || courseCode.length() <= 2)
        {
            return false;
        }
        String afterCS = courseCode.substring(2);
        return !afterCS.isEmpty() && !afterCS.trim().isEmpty();
    }
    
    /**
     * Find courses by course code (normalized)
     * Uses smart matching to handle location codes (e.g., CS181DV matches CS181DVPO)
     */
    private List<classes> findCoursesByCode(String courseCode)
    {
        List<classes> matches = new ArrayList<>();
        for (classes c : allCourses)
        {
            String code = extractCourseCode(c.courseSectionId);
            
            // Exact match
            if (code.equals(courseCode))
            {
                matches.add(c);
            }
            // Handle location codes: CS181DV should match CS181DVPO
            // Check if one is a prefix of the other and the difference is 2-3 uppercase letters
            else if (code.startsWith(courseCode))
            {
                String suffix = code.substring(courseCode.length());
                // If suffix is 2-3 uppercase letters, it's likely a location code
                if (suffix.matches("^[A-Z]{2,3}$"))
                {
                    matches.add(c);
                }
            }
            else if (courseCode.startsWith(code))
            {
                String suffix = courseCode.substring(code.length());
                // If suffix is 2-3 uppercase letters, it's likely a location code
                if (suffix.matches("^[A-Z]{2,3}$"))
                {
                    matches.add(c);
                }
            }
        }
        return matches;
    }
    
     // Managing course requests
    
    /**
     * Add course requests for a student
     */
    private void addCourseRequests(Student student)
    {
        System.out.println("\n=== Add Course Requests ===");
    }
    
    /**
     * Close Scanner
     */
    public void close()
    {
        if (scanner != null)
        {
            scanner.close();
        }
    }
}
