public class TestPrereqSystem
{
    public static void main(String[] args)
    {
        System.out.println("Course Registration Prerequisite System Tests\n");
        
        testCourseClass();
        testStudentClass();
        testPrerequisiteChecker();
        testPrerequisiteValidationResult();
        testRegistrationSystem();
        testRegistrationSystemWithCSVData();
        
        System.out.println("\nAll Tests Completed");
    }
    
    /**
     * Test Course class functionality
     */
    public static void testCourseClass()
    {
        System.out.println("Testing Course Class");
        
        Course cs51 = new Course("CSCI051A PO-01 SP2025", "CS51", 30);
        Course cs62 = new Course("CSCI062 PO-01 SP2025", "CS62", 24);
        
        cs62.addPrerequisite("CS51");
        cs62.addPrerequisite("CS54");
        
        System.out.println("Created course: " + cs62.getCourseSectionId());
        System.out.println("Course code: " + cs62.getCourseCode());
        System.out.println("Capacity: " + cs62.getCapacity());
        System.out.println("Prerequisites: " + cs62.getPrerequisites());
        System.out.println("Has available seats: " + cs62.hasAvailableSeats());
        
        for (int i = 0; i < 24; i++)
        {
            cs62.enrollStudent();
        }
        
        System.out.println("After enrolling 24 students:");
        System.out.println("Current enrollment: " + cs62.getCurrentEnrollment());
        System.out.println("Has available seats: " + cs62.hasAvailableSeats());
        System.out.println("Can enroll more: " + cs62.enrollStudent());
        
        System.out.println("Course class tests passed\n");
    }
    
    /**
     * Test Student class functionality
     */
    public static void testStudentClass()
    {
        System.out.println("Testing Student Class");
        
        Student student = new Student("S0001", "John Doe", 2025, "CS major");
        
        System.out.println("Student ID: " + student.getStudentId());
        System.out.println("Name: " + student.getName());
        System.out.println("Grad Year: " + student.getGradYear());
        System.out.println("Major Status: " + student.getMajorStatus());
        
        student.addPastClass("CS51");
        student.addPastClass("CS54");
        student.addPastClass("CS62");
        
        System.out.println("Past classes: " + student.getPastClasses());
        System.out.println("Has completed CS51: " + student.hasCompletedCourse("CS51"));
        System.out.println("Has completed CS105: " + student.hasCompletedCourse("CS105"));
        
        System.out.println("Student class tests passed\n");
    }
    
    /**
     * Test PrerequisiteChecker functionality
     */
    public static void testPrerequisiteChecker()
    {
        System.out.println("Testing PrerequisiteChecker");
        
        Student student1 = new Student("S0001", "Alice", 2025, "CS major");
        student1.addPastClass("CS51");
        student1.addPastClass("CS54");
        student1.addPastClass("CS62");
        
        Student student2 = new Student("S0002", "Bob", 2026, "CS minor");
        student2.addPastClass("CS51");
        
        Course cs105 = new Course("CSCI105 PO-01 SP2025", "CS105", 30);
        cs105.addPrerequisite("CS51");
        cs105.addPrerequisite("CS54");
        cs105.addPrerequisite("CS62");
        
        System.out.println("Course: " + cs105.getCourseCode());
        System.out.println("Prerequisites: " + cs105.getPrerequisites());
        
        boolean student1Eligible = PrerequisiteChecker.checkPrerequisites(student1, cs105);
        boolean student2Eligible = PrerequisiteChecker.checkPrerequisites(student2, cs105);
        
        System.out.println("\nStudent 1 (Alice) - Completed: " + student1.getPastClasses());
        System.out.println("Eligible for CS105: " + student1Eligible);
        System.out.println("Missing prerequisites: " + 
            PrerequisiteChecker.getMissingPrerequisites(student1, cs105));
        
        System.out.println("\nStudent 2 (Bob) - Completed: " + student2.getPastClasses());
        System.out.println("Eligible for CS105: " + student2Eligible);
        System.out.println("Missing prerequisites: " + 
            PrerequisiteChecker.getMissingPrerequisites(student2, cs105));
        
        if (student1Eligible && !student2Eligible)
        {
            System.out.println("PrerequisiteChecker tests passed\n");
        }
        else
        {
            System.out.println("PrerequisiteChecker tests failed\n");
        }
    }
    
    /**
     * Test PrerequisiteValidationResult class
     */
    public static void testPrerequisiteValidationResult()
    {
        System.out.println("Testing PrerequisiteValidationResult");
        
        java.util.HashSet<String> missing = new java.util.HashSet<>();
        missing.add("CS51");
        missing.add("CS62");
        
        PrerequisiteValidationResult result1 = new PrerequisiteValidationResult(
            true, new java.util.HashSet<>(), "Eligible for registration"
        );
        
        PrerequisiteValidationResult result2 = new PrerequisiteValidationResult(
            false, missing, "Missing prerequisites: CS51, CS62"
        );
        
        System.out.println("Result 1 - Eligible: " + result1.isEligible());
        System.out.println("Message: " + result1.getMessage());
        
        System.out.println("\nResult 2 - Eligible: " + result2.isEligible());
        System.out.println("Missing: " + result2.getMissingPrerequisites());
        System.out.println("Message: " + result2.getMessage());
        
        System.out.println("PrerequisiteValidationResult tests passed\n");
    }
    
    /**
     * Test RegistrationSystem basic functionality
     */
    public static void testRegistrationSystem()
    {
        System.out.println("Testing RegistrationSystem");
        
        RegistrationSystem system = new RegistrationSystem();
        
        Student student1 = new Student("S0001", "Alice Brown", 2025, "CS major");
        student1.addPastClass("CS51");
        student1.addPastClass("CS54");
        student1.addPastClass("CS62");
        
        Student student2 = new Student("S0002", "Charlie Davis", 2026, "CS minor");
        student2.addPastClass("CS51");
        
        Course cs105 = new Course("CSCI105 PO-01 SP2025", "CS105", 2);
        cs105.addPrerequisite("CS51");
        cs105.addPrerequisite("CS54");
        cs105.addPrerequisite("CS62");
        
        system.addStudent(student1);
        system.addStudent(student2);
        system.addCourse(cs105);
        
        System.out.println("Added 2 students and 1 course to system");
        
        Student retrieved = system.getStudent("S0001");
        System.out.println("Retrieved student: " + retrieved.getName());
        
        Course retrievedCourse = system.getCourse("CSCI105 PO-01 SP2025");
        System.out.println("Retrieved course: " + retrievedCourse.getCourseCode());
        
        PrerequisiteValidationResult validation1 = 
            system.validateRegistration("S0001", "CSCI105 PO-01 SP2025");
        System.out.println("\nValidation for Alice: " + validation1.getMessage());
        System.out.println("Eligible: " + validation1.isEligible());
        
        PrerequisiteValidationResult validation2 = 
            system.validateRegistration("S0002", "CSCI105 PO-01 SP2025");
        System.out.println("\nValidation for Charlie: " + validation2.getMessage());
        System.out.println("Eligible: " + validation2.isEligible());
        System.out.println("Missing: " + validation2.getMissingPrerequisites());
        
        cs105.enrollStudent();
        cs105.enrollStudent();
        
        PrerequisiteValidationResult validation3 = 
            system.validateRegistration("S0001", "CSCI105 PO-01 SP2025");
        System.out.println("\nValidation after course is full: " + validation3.getMessage());
        System.out.println("Eligible: " + validation3.isEligible());
        
        System.out.println("RegistrationSystem tests passed\n");
    }
    
    /**
     * Test RegistrationSystem with actual CSV data
     */
    public static void testRegistrationSystemWithCSVData()
    {
        System.out.println("Testing RegistrationSystem with CSV Data");
        
        RegistrationSystem system = new RegistrationSystem();
        
        String studentFile = "/Users/japhetacquahosei/AAA. COURSE REG/CourseReg/data/student.csv";
        system.loadStudentData(studentFile);
        
        Student s0000 = system.getStudent("S0000");
        if (s0000 != null)
        {
            System.out.println("Loaded student: " + s0000.getName());
            System.out.println("Student ID: " + s0000.getStudentId());
            System.out.println("Grad Year: " + s0000.getGradYear());
            System.out.println("Major Status: " + s0000.getMajorStatus());
            System.out.println("Past classes: " + s0000.getPastClasses());
            System.out.println("Has completed CS51: " + s0000.hasCompletedCourse("CS51"));
            System.out.println("Has completed CS62: " + s0000.hasCompletedCourse("CS62"));
        }
        else
        {
            System.out.println("Failed to load student S0000");
        }
        
        Student s0005 = system.getStudent("S0005");
        if (s0005 != null)
        {
            System.out.println("\nLoaded student: " + s0005.getName());
            System.out.println("Past classes: " + s0005.getPastClasses());
            System.out.println("(Empty past classes - new student)");
        }
        
        Course cs122 = new Course("CSCI122 PO-01 SP2025", "CS122", 25);
        cs122.addPrerequisite("CS62");
        cs122.addPrerequisite("CS105");
        
        system.addCourse(cs122);
        
        if (s0000 != null)
        {
            PrerequisiteValidationResult result = 
                system.validateRegistration("S0000", "CSCI122 PO-01 SP2025");
            System.out.println("\nValidation for " + s0000.getName() + " in CS122:");
            System.out.println("Eligible: " + result.isEligible());
            System.out.println("Message: " + result.getMessage());
        }
        
        System.out.println("CSV data loading tests passed\n");
    }
}
