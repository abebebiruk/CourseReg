import java.util.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Main interactive interface for student course registration system
 * Handles student registration with validation and course selection
 */
public class RegistrationInterface
{
    private RegistrationSystem registrationSystem;
    private Scanner scanner;
    private String studentCsvPath;
    
    public RegistrationInterface(String studentCsvPath)
    {
        this.registrationSystem = new RegistrationSystem();
        this.scanner = new Scanner(System.in);
        this.studentCsvPath = studentCsvPath;
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
            System.out.println("1. Register New Student");
            System.out.println("2. Exit");
            System.out.print("\nEnter your choice (1-2): ");
            
            String choice = scanner.nextLine().trim();
            
            switch (choice)
            {
                case "1":
                    registerNewStudent();
                    break;
                case "2":
                    System.out.println("Thank you for using the Course Registration System!");
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1 or 2.");
            }
        }
    }
    
    /**
     * Load existing student and course data
     */
    private void loadExistingData()
    {
        try
        {
            // Load student data
            if (new File(studentCsvPath).exists())
            {
                registrationSystem.loadStudentData(studentCsvPath);
                System.out.println("Loaded existing student data.");
            }
        }
        catch (Exception e)
        {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }
    
    
    /**
     * Normalize course code input from user
     * Students enter just the number/identifier (e.g., "101", "181DV")
     * This method adds "CS" prefix automatically
     * Also converts CSCI to CS (e.g., "CSCI51" -> "CS51")
     */
    private String normalizeCourseCodeInput(String courseCode)
    {
        if (courseCode == null) return "";
        courseCode = courseCode.toUpperCase().replaceAll("\\s+", "");
        
        // Convert CSCI prefix to CS first (before handling CS)
        // This ensures CSCI51 and CS51 both become CS51
        if (courseCode.startsWith("CSCI"))
        {
            courseCode = courseCode.substring(4); // Remove "CSCI"
            // Remove leading zeros
            if (courseCode.matches("^0+\\d+.*"))
            {
                courseCode = courseCode.replaceFirst("^0+", "");
            }
        }
        // If already starts with CS, remove it (we'll add it back)
        else if (courseCode.startsWith("CS"))
        {
            courseCode = courseCode.substring(2); // Remove "CS"
        }
        
        // Remove leading zeros from numbers (e.g., "051" -> "51", "004" -> "4")
        if (courseCode.matches("^0+\\d+.*"))
        {
            courseCode = courseCode.replaceFirst("^0+", "");
        }
        
        // Add CS prefix - ensures consistent format
        return "CS" + courseCode;
    }
    
    /**
     * Validate that a course code is valid (after normalization)
     * Since normalization always adds "CS" prefix, we just need to check:
     * - Not empty
     * - Has content after "CS" (e.g., "CS51", "CS181DV" are valid; "CS" alone is invalid)
     */
    private boolean isValidCourseCode(String courseCode)
    {
        if (courseCode == null || courseCode.isEmpty())
        {
            return false;
        }
        
        // After normalization, it should always start with "CS" and have something after it
        // Must have something after "CS" (not just "CS")
        if (courseCode.length() <= 2)
        {
            return false;
        }
        
        // After "CS", must have at least one character (digit or letter)
        String afterCS = courseCode.substring(2);
        if (afterCS.isEmpty() || afterCS.trim().isEmpty())
        {
            return false;
        }
        
        return true;
    }
    
    /**
     * Get the next available student ID from CSV
     */
    private String getNextAvailableStudentId()
    {
        try
        {
            File file = new File(studentCsvPath);
            if (!file.exists())
            {
                return "S0000";
            }
            
            int maxId = -1;
            try (BufferedReader br = new BufferedReader(new FileReader(file)))
            {
                String line = br.readLine(); // Skip header
                
                while ((line = br.readLine()) != null)
                {
                    if (line.trim().isEmpty()) continue;
                    
                    int firstComma = line.indexOf(',');
                    if (firstComma == -1) continue;
                    
                    String studentId = line.substring(0, firstComma).trim();
                    
                    // Extract number from ID (e.g., "S1499" -> 1499)
                    if (studentId.startsWith("S") && studentId.length() > 1)
                    {
                        try
                        {
                            int idNum = Integer.parseInt(studentId.substring(1));
                            if (idNum > maxId)
                            {
                                maxId = idNum;
                            }
                        }
                        catch (NumberFormatException e)
                        {
                            // Skip invalid IDs
                        }
                    }
                }
            }
            
            // Return next ID
            return "S" + String.format("%04d", maxId + 1);
        }
        catch (IOException e)
        {
            System.err.println("Error reading student CSV: " + e.getMessage());
            return "S0000";
        }
    }
    
    /**
     * Register a new student interactively
     */
    private void registerNewStudent()
    {
        System.out.println("\n=== Register New Student ===");
        System.out.println();
        
        // Generate student ID
        String studentId = getNextAvailableStudentId();
        System.out.println("Your assigned Student ID: " + studentId);
        System.out.println();
        
        // Get name - cannot be empty
        String name;
        while (true)
        {
            System.out.print("Enter Student Name: ");
            name = scanner.nextLine().trim();
            
            if (name.isEmpty())
            {
                System.out.println("Name cannot be empty. Please try again.");
                continue;
            }
            
            break;
        }
        
        // Get graduation year - must be between 2025 and 2029
        int gradYear = 0;
        while (true)
        {
            System.out.print("Enter Graduation Year (2025-2029): ");
            String yearStr = scanner.nextLine().trim();
            
            if (yearStr.isEmpty())
            {
                System.out.println("Graduation year cannot be empty. Please try again.");
                continue;
            }
            
            try
            {
                gradYear = Integer.parseInt(yearStr);
                if (gradYear >= 2025 && gradYear <= 2029)
                {
                    break;
                }
                else
                {
                    System.out.println("Please enter a valid year between 2025 and 2029.");
                }
            }
            catch (NumberFormatException e)
            {
                System.out.println("Please enter a valid number.");
            }
        }
        
        // Get major status
        System.out.println("\nMajor Status Options:");
        System.out.println("1. CS major");
        System.out.println("2. CS minor");
        System.out.println("3. Non-major");
        System.out.print("Select major status (1-3): ");
        String majorChoice = scanner.nextLine().trim();
        String majorStatus;
        switch (majorChoice)
        {
            case "1":
                majorStatus = "CS major";
                break;
            case "2":
                majorStatus = "CS minor";
                break;
            case "3":
                majorStatus = "Non-major";
                break;
            default:
                System.out.println("Invalid choice. Defaulting to Non-major.");
                majorStatus = "Non-major";
        }
        
        // Get past classes - can be empty
        System.out.println("\nEnter CS classes you have completed.");
        System.out.println("Enter just the number/identifier (e.g., 101, 51, 181DV, 140) - 'CS' prefix will be added automatically.");
        System.out.println("Enter one course per line, or press Enter with empty line to finish:");
        List<String> pastClasses = new ArrayList<>();
        while (true)
        {
            System.out.print("Past class (or Enter to finish): ");
            String course = scanner.nextLine().trim();
            if (course.isEmpty())
            {
                break;
            }
            
            // Normalize: converts CSCI to CS, adds CS prefix, handles leading zeros
            // This ensures CS51 and CSCI51 both become CS51 and are detected as duplicates
            String normalizedCourse = normalizeCourseCodeInput(course);
            
            // Validate: must have valid format after normalization
            if (!isValidCourseCode(normalizedCourse))
            {
                System.out.println("Invalid course code. Please enter a valid course number/identifier (e.g., 101, 51, 181DV, 140).");
                continue;
            }
            
            // Check for duplicates (after normalization, so CS51 and CSCI51 are treated as same)
            if (pastClasses.contains(normalizedCourse))
            {
                System.out.println("Course code '" + normalizedCourse + "' has already been added.");
                System.out.println("(Note: CS and CSCI are treated as the same - e.g., CS51 and CSCI51 are duplicates)");
                continue;
            }
            
            course = normalizedCourse;
            
            if (!course.isEmpty())
            {
                pastClasses.add(course);
                System.out.println("Added: " + course);
            }
        }
        
        // Follows same logic as past classes
        // Get requested classes
        System.out.println("\nEnter CS classes you want to take.");
        System.out.println("Enter just the number/identifier (e.g., 101, 51, 181DV, 140) - 'CS' prefix will be added automatically.");
        System.out.println("Enter one course per line, or press Enter with empty line to finish:");
        List<String> requestedClasses = new ArrayList<>();
        while (true)
        {
            System.out.print("Requested class (or Enter to finish): ");
            String course = scanner.nextLine().trim();
            if (course.isEmpty())
            {
                break;
            }
            
            // Normalize: converts CSCI to CS, adds CS prefix, handles leading zeros
            // This ensures CS51 and CSCI51 both become CS51 and are detected as duplicates
            String normalizedCourse = normalizeCourseCodeInput(course);
            
            // Validate: must have valid format after normalization
            if (!isValidCourseCode(normalizedCourse))
            {
                System.out.println("Invalid course code. Please enter a valid course number/identifier (e.g., 101, 51, 181DV, 140).");
                continue;
            }
            
            // Check for duplicates (after normalization, so CS51 and CSCI51 are treated as same)
            if (requestedClasses.contains(normalizedCourse))
            {
                System.out.println("Course code '" + normalizedCourse + "' has already been added.");
                System.out.println("(Note: CS and CSCI are treated as the same - e.g., CS51 and CSCI51 are duplicates)");
                continue;
            }
            
            course = normalizedCourse;
            
            if (!course.isEmpty())
            {
                requestedClasses.add(course);
                System.out.println("Added: " + course);
            }
        }
        
        // Create student object
        Student student = new Student(studentId, name, gradYear, majorStatus);
        for (String course : pastClasses)
        {
            student.addPastClass(course);
        }
        
        // Add to registration system
        registrationSystem.addStudent(student);
        
        // Write to CSV
        boolean writeSuccess = writeStudentToCsv(studentId, name, pastClasses, requestedClasses, gradYear, majorStatus);
        
        if (writeSuccess)
        {
            System.out.println("\nStudent " + name + " (ID: " + studentId + ") has been registered successfully!");
        }
        else
        {
            System.out.println("\nERROR: Failed to write student data to CSV file.");
        }
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }
    
    /**
     * Write student data to CSV file
     */
    private boolean writeStudentToCsv(String studentId, String name, List<String> pastClasses, 
                                   List<String> requestedClasses, int gradYear, String majorStatus)
    {
        try
        {
            File file = new File(studentCsvPath);
            boolean fileExists = file.exists();
            
            // Create header if file doesn't exist
            if (!fileExists)
            {
                String header = "student_id,name,past_classes,requested_classes,grad_year,major_status\n";
                Files.write(Paths.get(studentCsvPath), header.getBytes(), StandardOpenOption.CREATE);
            }
            
            // Format past classes
            String pastClassesStr = formatClassList(pastClasses);
            String requestedClassesStr = formatClassList(requestedClasses);
            
            // Create CSV line
            String csvLine = String.format("%s,%s,%s,%s,%d,%s%n",
                studentId, name, pastClassesStr, requestedClassesStr, gradYear, majorStatus);
            
            // Append to file
            Files.write(Paths.get(studentCsvPath), csvLine.getBytes(), StandardOpenOption.APPEND);
            
            return true;
        }
        catch (IOException e)
        {
            System.err.println("Error writing to CSV file: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Format a list of classes as CSV format: [CS140,CS51,CS62]
     */
    private String formatClassList(List<String> classes)
    {
        if (classes.isEmpty())
        {
            return "[]";
        }
        return "[" + String.join(",", classes) + "]";
    }
    
    /**
     * Close resources
     */
    public void close()
    {
        if (scanner != null)
        {
            scanner.close();
        }
    }
    
    /**
     * Main method to run the interface
     */
    public static void main(String[] args)
    {
        String studentCsvPath = "data/student.csv";
        
        if (args.length >= 1)
        {
            studentCsvPath = args[0];
        }
        
        RegistrationInterface regInterface = new RegistrationInterface(studentCsvPath);
        try
        {
            regInterface.run();
        }
        finally
        {
            regInterface.close();
        }
    }
}

