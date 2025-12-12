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
                    runLottery();
                    break;
                case "3":
                    viewCoursesAndPrerequisites();
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
        System.out.println("=== Add Course Requests ===");
        
        // Get existing requests from CSV
        List<String> existingCourseCodesFromCsv = getRequestedClassesFromCsv(student.getStudentId());
        
        // Display existing requests
        if (!existingCourseCodesFromCsv.isEmpty())
        {
            System.out.println("\nYour Current Requests:");
            for (int i = 0; i < existingCourseCodesFromCsv.size(); i++)
            {
                String code = removeLocationCode(existingCourseCodesFromCsv.get(i));
                System.out.println("  " + (i + 1) + ". " + code);
            }
            int remainingSlots = 4 - existingCourseCodesFromCsv.size();
            System.out.println("\nYou can add up to " + remainingSlots + " more course request(s).");
        }
        else
        {
            System.out.println("\nYou currently have no course requests.");
            System.out.println("You can request up to 4 courses, ranked by preference (1 = highest preference).");
        }
        
        System.out.println("\nEnter course codes (e.g., 140, 181DV, 51) - 'CS' prefix will be added automatically.");
        System.out.println("Press Enter with empty line to finish.\n");
        
        // Remove existing requests for this student from allRequests (we'll rebuild them)
        allRequests.removeIf(req -> req.studentId.equals(student.getStudentId()));
        
        // Start with existing requests
        List<String> requestedCourseCodes = new ArrayList<>(existingCourseCodesFromCsv);
        int rank = existingCourseCodesFromCsv.size() + 1;
        
        // Find course sections for existing requests and add them to allRequests
        for (int i = 0; i < existingCourseCodesFromCsv.size(); i++)
        {
            String courseCode = existingCourseCodesFromCsv.get(i);
            String normalizedCode = normalizeCourseCodeInput(courseCode);
            
            // Find matching course sections
            List<classes> matchingCourses = findCoursesByCode(normalizedCode);
            if (!matchingCourses.isEmpty())
            {
                // Use the first available section
                classes selectedCourse = matchingCourses.get(0);
                ClassRequest request = new ClassRequest(student.getStudentId(), 
                                                       selectedCourse.courseSectionId, i + 1);
                allRequests.add(request);
            }
        }
        
        // Allow user to add more requests (up to 4 total)
        while (rank <= 4)
        {
            System.out.print("Course " + rank + " (or Enter to finish): ");
            String input = scanner.nextLine().trim();
            
            if (input.isEmpty())
            {
                break;
            }
            
            // Normalize course code
            String normalizedCode = normalizeCourseCodeInput(input);
            
            if (!isValidCourseCode(normalizedCode))
            {
                System.out.println("Invalid course code. Please try again.");
                continue;
            }
            
            // Check if already requested
            if (requestedCourseCodes.contains(normalizedCode))
            {
                System.out.println("You have already requested this course. Please choose a different one.");
                continue;
            }
            
            // Find matching course sections
            List<classes> matchingCourses = findCoursesByCode(normalizedCode);
            
            if (matchingCourses.isEmpty())
            {
                System.out.println("No courses found matching: " + normalizedCode);
                System.out.println("Please check the course code and try again.");
                continue;
            }
            
            // If multiple sections, let user choose
            classes selectedCourse = null;
            if (matchingCourses.size() == 1)
            {
                selectedCourse = matchingCourses.get(0);
            }
            else
            {
                System.out.println("\nMultiple sections found for " + normalizedCode + ":");
                for (int i = 0; i < matchingCourses.size(); i++)
                {
                    classes c = matchingCourses.get(i);
                    int availableSeats = c.capacity - c.currentEnrollment;
                    System.out.println((i + 1) + ". " + c.courseSectionId + 
                                     " - Available seats: " + availableSeats + 
                                     " / Capacity: " + c.capacity);
                }
                System.out.print("Select section (1-" + matchingCourses.size() + "): ");
                String sectionChoice = scanner.nextLine().trim();
                try
                {
                    int idx = Integer.parseInt(sectionChoice) - 1;
                    if (idx >= 0 && idx < matchingCourses.size())
                    {
                        selectedCourse = matchingCourses.get(idx);
                    }
                    else
                    {
                        System.out.println("Invalid selection.");
                        continue;
                    }
                }
                catch (NumberFormatException e)
                {
                    System.out.println("Invalid selection.");
                    continue;
                }
            }
            
            // Check available seats first
            int availableSeats = selectedCourse.capacity - selectedCourse.currentEnrollment;
            System.out.println("Available seats: " + availableSeats + " / Capacity: " + selectedCourse.capacity);
            
            if (availableSeats <= 0)
            {
                System.out.println("Cannot register: Course is full.");
                continue;
            }
            
            // Validate prerequisites
            Course courseForValidation = registrationSystem.getCourse(selectedCourse.courseSectionId);
            if (courseForValidation == null)
            {
                // Create course object for validation if it doesn't exist
                String courseCode = extractCourseCode(selectedCourse.courseSectionId);
                courseForValidation = new Course(selectedCourse.courseSectionId, courseCode, selectedCourse.capacity);
                registrationSystem.addCourse(courseForValidation);
            }
            
            PrerequisiteValidationResult validation = registrationSystem.validateRegistration(
                student.getStudentId(), selectedCourse.courseSectionId);
            
            if (!validation.isEligible())
            {
                System.out.println("Cannot register: " + validation.getMessage());
                if (!validation.getMissingPrerequisites().isEmpty())
                {
                    System.out.println("Missing prerequisites: " + 
                        String.join(", ", validation.getMissingPrerequisites()));
                }
                continue;
            }
            
            // Create request
            ClassRequest request = new ClassRequest(student.getStudentId(), 
                                                   selectedCourse.courseSectionId, rank);
            allRequests.add(request);
            requestedCourseCodes.add(normalizedCode);
            
            System.out.println("Added: " + selectedCourse.courseSectionId + " (Preference rank: " + rank + ")");
            rank++;
        }
        
        // Update CSV with merged list
        updateRequestedClassesInCsv(student.getStudentId(), requestedCourseCodes);
        
        if (rank > existingCourseCodesFromCsv.size() + 1)
        {
            int newRequests = rank - existingCourseCodesFromCsv.size() - 1;
            System.out.println("\nSuccessfully added " + newRequests + " new course request(s).");
            System.out.println("Total requests: " + requestedCourseCodes.size());
        }
        else if (existingCourseCodesFromCsv.isEmpty())
        {
            System.out.println("\nNo course requests added.");
        }
        else
        {
            System.out.println("\nYour existing requests have been preserved.");
        }
        
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    // Extract/update requested classes in CSV
    
    /**
     * Get requested classes from CSV for a specific student
     */
    private List<String> getRequestedClassesFromCsv(String studentId)
    {
        List<String> requestedClasses = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(studentCsvPath)))
        {
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null)
            {
                if (line.trim().isEmpty()) continue;
                
                int firstComma = line.indexOf(',');
                if (firstComma == -1) continue;
                
                String csvStudentId = line.substring(0, firstComma).trim();
                if (!csvStudentId.equals(studentId)) continue;
                
                int firstCloseBracket = line.indexOf(']');
                int secondBracket = line.indexOf('[', firstCloseBracket + 1);
                int secondCloseBracket = (secondBracket != -1) ? line.indexOf(']', secondBracket + 1) : -1;
                
                if (secondBracket != -1 && secondCloseBracket != -1)
                {
                    String requestedClassesStr = line.substring(secondBracket, secondCloseBracket + 1).trim();
                    
                    if (!requestedClassesStr.equals("") && !requestedClassesStr.equals("[]"))
                    {
                        String cleaned = requestedClassesStr.replace("[", "").replace("]", "");
                        String[] courseCodes = cleaned.split(",");
                        
                        for (String courseCode : courseCodes)
                        {
                            courseCode = courseCode.trim();
                            if (!courseCode.isEmpty())
                            {
                                requestedClasses.add(courseCode);
                            }
                        }
                    }
                }
                break; // Found the student, no need to continue
            }
        }
        catch (IOException e)
        {
            // Silently fail
        }
        
        return requestedClasses;
    }
    
    /**
     * Update requested classes in CSV for a student
     */
    private void updateRequestedClassesInCsv(String studentId, List<String> requestedCourseCodes)
    {
        try
        {
            // Read all lines
            List<String> lines = new ArrayList<>();
            try (BufferedReader br = new BufferedReader(new FileReader(studentCsvPath)))
            {
                String line;
                while ((line = br.readLine()) != null)
                {
                    lines.add(line);
                }
            }
            
            // Update the student's line
            for (int i = 0; i < lines.size(); i++)
            {
                String line = lines.get(i);
                if (i == 0) continue; // Skip header
                
                int firstComma = line.indexOf(',');
                if (firstComma == -1) continue;
                
                String csvStudentId = line.substring(0, firstComma).trim();
                if (!csvStudentId.equals(studentId)) continue;
                
                // Found the student - update the line
                String updatedLine = updateStudentRequestedClasses(line, requestedCourseCodes);
                lines.set(i, updatedLine);
                break;
            }
            
            // Write all lines back
            try (FileWriter writer = new FileWriter(studentCsvPath))
            {
                for (String line : lines)
                {
                    writer.write(line + "\n");
                }
            }
        }
        catch (IOException e)
        {
            System.err.println("Error updating requested classes in CSV: " + e.getMessage());
        }
    }
    
    /**
     * Update the requested_classes field in a CSV line
     */
    private String updateStudentRequestedClasses(String csvLine, List<String> requestedCourseCodes)
    {
        // Parse the line manually to handle brackets correctly
        int firstComma = csvLine.indexOf(',');
        if (firstComma == -1) return csvLine;
        
        String studentId = csvLine.substring(0, firstComma);
        String remaining = csvLine.substring(firstComma + 1);
        
        // Find name (ends at next comma)
        int nameEnd = remaining.indexOf(',');
        if (nameEnd == -1) return csvLine;
        String name = remaining.substring(0, nameEnd);
        remaining = remaining.substring(nameEnd + 1);
        
        // Find past_classes (bracketed)
        int pastClassesStart = remaining.indexOf('[');
        int pastClassesEnd = remaining.indexOf(']');
        if (pastClassesStart == -1 || pastClassesEnd == -1) return csvLine;
        String pastClasses = remaining.substring(pastClassesStart, pastClassesEnd + 1);
        remaining = remaining.substring(pastClassesEnd + 2); // Skip ] and comma
        
        // Find requested_classes (bracketed) - but we'll replace it
        int requestedClassesStart = remaining.indexOf('[');
        int requestedClassesEnd = remaining.indexOf(']');
        if (requestedClassesStart == -1 || requestedClassesEnd == -1) return csvLine;
        remaining = remaining.substring(requestedClassesEnd + 2); // Skip ] and comma
        
        // Format requested classes
        String requestedClassesStr;
        if (requestedCourseCodes.isEmpty())
        {
            requestedClassesStr = "[]";
        }
        else
        {
            requestedClassesStr = "[" + String.join(",", requestedCourseCodes) + "]";
        }
        
        // Reconstruct the line
        return studentId + "," + name + "," + pastClasses + "," + requestedClassesStr + "," + remaining;
    }

     /**
     * Run Lottery - Process requests for specific courses
     */
     private void runLottery()
     {
        System.out.println("\n=== Lottery ===");

        if (allRequests.isEmpty())
            {
                System.out.println("No course requests found. Please add requests first.");
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
                return;
            }
            
            // Get unique courses that have requests
            Map<String, String> courseCodeToSectionId = new LinkedHashMap<>(); // Preserves order
            Set<String> seenCourseCodes = new HashSet<>();
            
            for (ClassRequest req : allRequests)
            {
                String courseCode = removeLocationCode(extractCourseCode(req.courseId));
                if (!seenCourseCodes.contains(courseCode))
                {
                    seenCourseCodes.add(courseCode);
                    courseCodeToSectionId.put(courseCode, req.courseId);
                }
            }
            
            if (courseCodeToSectionId.isEmpty())
            {
                System.out.println("No courses found with requests.");
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
                return;
            }
            
            // Display available courses
            System.out.println("\nAvailable Courses with Requests:");
            List<String> courseCodesList = new ArrayList<>(courseCodeToSectionId.keySet());
            for (int i = 0; i < courseCodesList.size(); i++)
            {
                String courseCode = courseCodesList.get(i);
                // Count requests for this course
                long requestCount = allRequests.stream()
                    .filter(req -> removeLocationCode(extractCourseCode(req.courseId)).equals(courseCode))
                    .count();
                System.out.println((i + 1) + ". " + courseCode + " (" + requestCount + " requests)");
            }
            System.out.println((courseCodesList.size() + 1) + ". All Courses");
            
            System.out.print("\nSelect course(s) to run lottery for (enter course code like CS105, or number like 2, or 'all' for all courses): ");
            String selection = scanner.nextLine().trim();
            
            Set<String> selectedCourseCodes = new HashSet<>();
            Set<String> selectedCourseSectionIds = new HashSet<>();
            
            // Check if "all" or the number for "All Courses" was selected
            if (selection.equalsIgnoreCase("all") || selection.equals(String.valueOf(courseCodesList.size() + 1)))
            {
                // All courses selected
                selectedCourseCodes.addAll(courseCodesList);
                for (String courseCode : courseCodesList)
                {
                    // Get all section IDs for this course code
                    for (ClassRequest req : allRequests)
                    {
                        String code = removeLocationCode(extractCourseCode(req.courseId));
                        if (code.equals(courseCode))
                        {
                            selectedCourseSectionIds.add(req.courseId);
                        }
                    }
                }
            }
            else
            {
                // Parse selection - can be numbers or course codes
                String[] selections = selection.split(",");
                for (String sel : selections)
                {
                    sel = sel.trim();
                    
                    // Try to parse as number first
                    try
                    {
                        int index = Integer.parseInt(sel) - 1;
                        if (index >= 0 && index < courseCodesList.size())
                        {
                            String courseCode = courseCodesList.get(index);
                            selectedCourseCodes.add(courseCode);
                            // Get all section IDs for this course code
                            for (ClassRequest req : allRequests)
                            {
                                String code = removeLocationCode(extractCourseCode(req.courseId));
                                if (code.equals(courseCode))
                                {
                                    selectedCourseSectionIds.add(req.courseId);
                                }
                            }
                        }
                        continue;
                    }
                    catch (NumberFormatException e)
                    {
                        // Not a number, try as course code
                    }
                    
                    // Try as course code
                    String normalizedCode = normalizeCourseCodeInput(sel);
                    
                    // Check if this course code exists in the list
                    boolean found = false;
                    for (String courseCode : courseCodesList)
                    {
                        if (courseCode.equalsIgnoreCase(normalizedCode) || 
                            courseCode.equalsIgnoreCase(sel) ||
                            normalizedCode.equalsIgnoreCase(courseCode))
                        {
                            selectedCourseCodes.add(courseCode);
                            found = true;
                            // Get all section IDs for this course code
                            for (ClassRequest req : allRequests)
                            {
                                String code = removeLocationCode(extractCourseCode(req.courseId));
                                if (code.equals(courseCode))
                                {
                                    selectedCourseSectionIds.add(req.courseId);
                                }
                            }
                            break;
                        }
                    }
                    
                    if (!found)
                    {
                        System.out.println("Course not found: " + sel + " (available courses are listed above)");
                    }
                }
            }
            
            if (selectedCourseCodes.isEmpty())
            {
                System.out.println("No valid courses selected.");
                System.out.println("Press Enter to continue...");
                scanner.nextLine();
                return;
            }
            
            // Filter requests to only include selected courses
            List<ClassRequest> filteredRequests = new ArrayList<>();
            for (ClassRequest req : allRequests)
            {
                if (selectedCourseSectionIds.contains(req.courseId))
                {
                    filteredRequests.add(req);
                }
            }
            
            // Filter courses to only include selected ones
            List<classes> filteredCourses = new ArrayList<>();
            for (classes c : allCourses)
            {
                if (selectedCourseSectionIds.contains(c.courseSectionId))
                {
                    filteredCourses.add(c);
                }
            }
            
            System.out.println("\nProcessing lottery for " + selectedCourseCodes.size() + " course(s): " + 
                              String.join(", ", selectedCourseCodes));
            System.out.println("Total requests: " + filteredRequests.size());
     }

    // Prerequsite feature view
    /**
     * View Courses and Prerequisites - Display all courses with their prerequisites
     */
    private void viewCoursesAndPrerequisites()
    {
        System.out.println("\n=== Courses and Prerequisites ===");
        System.out.println("Displaying course prerequisite relationships (DAG):\n");
        
        try
        {
            // Get the prerequisite graph
            PrerequisiteGraph graph = PrerequisiteChecker.getPrerequisiteGraph();
            
            // Collect all course codes from loaded courses
            Set<String> courseCodes = new TreeSet<>();
            for (classes c : allCourses)
            {
                String courseCode = removeLocationCode(extractCourseCode(c.courseSectionId));
                courseCodes.add(courseCode);
            }
            
            // Add courses from the prerequisite graph by checking common course codes
            String[] commonCourses = {
                "CS35", "CS51", "CS54", "CS62", "CS101", "CS105", "CS140", 
                "CS122", "CS124", "CS131", "CS132", "CS133", "CS138", 
                "CS143", "CS145", "CS151", "CS152", "CS153", "CS158", 
                "CS159", "CS181AA", "CS181CA", "CS181DA", "CS181AV", "CS181DV"
            };
            
            for (String courseCode : commonCourses)
            {
                Set<String> prereqs = graph.getDirectPrerequisites(courseCode);
                if (!prereqs.isEmpty())
                {
                    courseCodes.add(courseCode);
                }
                // Add courses that are prerequisites
                courseCodes.add(courseCode);
            }
            
            // Display courses with prerequisites
            System.out.println("Courses with prerequisites:");
            boolean hasAnyPrereqs = false;
            for (String courseCode : courseCodes)
            {
                Set<String> prereqs = graph.getDirectPrerequisites(courseCode);
                if (!prereqs.isEmpty())
                {
                    hasAnyPrereqs = true;
                    List<String> prereqList = new ArrayList<>(prereqs);
                    Collections.sort(prereqList);
                    System.out.println("  " + courseCode + " -> " + String.join(", ", prereqList));
                }
            }
            
            if (!hasAnyPrereqs)
            {
                System.out.println("  (none)");
            }
        }
        catch (Exception e)
        {
            System.out.println("Error displaying prerequisites: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
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
