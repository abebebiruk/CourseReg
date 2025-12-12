import java.util.HashSet;
import java.util.Set;

public class LotteryWeightCalculator {

    /**
     * Computes weight using the current system year automatically.
     * @param s       The student requesting the course
     * @param req     The request details (studentId, courseId, rank)
     * @param course  The course being requested
     * @return        Calculated weight (minimum 1)
     */
    public static int computeWeight(students s, ClassRequest req, classes course) {
        int currentYear = java.time.Year.now().getValue();
        return computeWeight(s, req, course, currentYear);
    }

    /**
     * Computes the lottery weight using an explicitly provided current year.
     *
     * @param s            The student requesting the course (cannot be null)
     * @param req          The class request (contains preference rank)
     * @param course       The target course (capacity, status, enrollment)
     * @param currentYear  The academic year used to determine student standing
     * @return             The computed weight (minimum value of 1)
     * @throws IllegalArgumentException if any argument is null
     */
    public static int computeWeight(students s, ClassRequest req, classes course, int currentYear) {
        if (s == null || req == null || course == null) {
            throw new IllegalArgumentException("Student, request, and course cannot be null.");
        }

        // Check prerequisites first - if not met, return 0
        if (!checkPrerequisites(s, course)) {
            return 0;
        }

        int weight = 10; // Base weight
        
        // Preference rank bonus
        int rank = req.preferenceRank;
        if (rank < 1 || rank > 4) rank = 4;
        switch (rank) {
            case 1: weight += 4; break;
            case 2: weight += 3; break;
            case 3: weight += 2; break;
            case 4: weight += 1; break;
        }

        // Major status bonus
        students.MajorStatus majorStatus =
                (s.majorStatus == null) ? students.MajorStatus.NON_MAJOR : s.majorStatus;
        switch (majorStatus) {
            case CS_MAJOR: weight += 4; break;
            case CS_MINOR: weight += 2; break;
            case NON_MAJOR: break;
        }

        // Student academic year bonus
        StudentYear year = getStudentYear(s.gradYear, currentYear);
        switch (year) {
            case SENIOR: weight += 4; break;
            case JUNIOR: weight += 3; break;
            case SOPHOMORE: weight += 2; break;
            case FRESHMAN: weight += 1; break;
        }

        return Math.max(weight, 1); // Weight cannot drop below 1
    }

    /**
     * Converts graduation year into student standing (Freshman → Senior).
     *
     * @param gradYear     Student's expected graduation year
     * @param currentYear  The current academic year
     * @return             The StudentYear enum value based on years left
     */
    public static StudentYear getStudentYear(int gradYear, int currentYear) {
        int yearsLeft = gradYear - currentYear;
        if (yearsLeft <= 0) return StudentYear.SENIOR;
        if (yearsLeft == 1) return StudentYear.JUNIOR;
        if (yearsLeft == 2) return StudentYear.SOPHOMORE;
        return StudentYear.FRESHMAN;
    }

    /**
     * Validates prerequisites for a student and course, returning a validation result.
     * Uses the PrerequisiteGraph from the prereq-src directory.
     * @param student The student requesting the course
     * @param course  The course being requested
     * @return PrerequisiteValidationResult with eligibility status and missing prerequisites
     */
    public static PrerequisiteValidationResult validatePrerequisites(students student, classes course) {
        String courseCode = extractCourseCode(course.courseSectionId);
        if (courseCode.isEmpty()) {
            return new PrerequisiteValidationResult(true, new HashSet<>(), "No course code to validate");
        }

        try {
            PrerequisiteGraph graph = PrerequisiteChecker.getPrerequisiteGraph();
            Set<String> allPrerequisites = graph.getAllPrerequisites(courseCode);

            // If no prerequisites, student is eligible
            if (allPrerequisites.isEmpty()) {
                return new PrerequisiteValidationResult(true, new HashSet<>(), "No prerequisites required");
            }

            // Find missing prerequisites
            Set<String> missing = new HashSet<>();
            for (String prereq : allPrerequisites) {
                if (!student.hasTaken(prereq)) {
                    missing.add(prereq);
                }
            }

            if (missing.isEmpty()) {
                return new PrerequisiteValidationResult(true, new HashSet<>(), "All prerequisites met");
            } else {
                String message = "Missing prerequisites: " + String.join(", ", missing);
                return new PrerequisiteValidationResult(false, missing, message);
            }
        } catch (Exception e) {
            // Log error and fail closed for prerequisite checking (safer)
            System.err.println("Error checking prerequisites for courseCode='" + courseCode + 
                             "' (courseSectionId='" + course.courseSectionId + "'): " + e.getMessage());
            e.printStackTrace();
            // Fail closed - reject if we can't verify prerequisites
            Set<String> errorSet = new HashSet<>();
            errorSet.add("Error validating prerequisites");
            return new PrerequisiteValidationResult(false, errorSet, 
                "Error validating prerequisites: " + e.getMessage());
        }
    }

    /**
     * Checks if a student meets all prerequisites for a course.
     * Uses the PrerequisiteGraph from the prereq-src directory.
     * @param student The student requesting the course
     * @param course  The course being requested
     * @return true if student meets all prerequisites, false otherwise
     */
    private static boolean checkPrerequisites(students student, classes course) {
        PrerequisiteValidationResult result = validatePrerequisites(student, course);
        return result.isEligible();
    }

    /**
     * Extracts the course code from a course section ID.
     * Handles formats like "CSCI140  HM-01 SP2025" or "CS62-01"
     * Converts CSCI prefix to CS and normalizes the code.
     * @param courseSectionId The full course section ID
     * @return The course code (e.g., "CS140" or "CS62")
     */
    private static String extractCourseCode(String courseSectionId) {
        if (courseSectionId == null || courseSectionId.isEmpty()) {
            return "";
        }
        
        // Find the dash or space that separates course code from section
        int dashIndex = courseSectionId.indexOf('-');
        int spaceIndex = courseSectionId.indexOf(' ');
        
        String courseCode;
        if (dashIndex > 0 && (spaceIndex == -1 || dashIndex < spaceIndex)) {
            // Dash comes first (e.g., "CS62-01")
            courseCode = courseSectionId.substring(0, dashIndex).trim();
        } else if (spaceIndex > 0) {
            // Space comes first (e.g., "CSCI140  HM-01 SP2025")
            courseCode = courseSectionId.substring(0, spaceIndex).trim();
        } else {
            courseCode = courseSectionId.trim();
        }
        
        // Convert CSCI prefix to CS (e.g., "CSCI140" -> "CS140")
        if (courseCode.startsWith("CSCI")) {
            courseCode = "CS" + courseCode.substring(4);
        }
        
        // Remove leading zeros from numbers (e.g., "CS004" -> "CS4")
        if (courseCode.length() > 2 && courseCode.startsWith("CS")) {
            String numberPart = courseCode.substring(2);
            if (numberPart.matches("^0+\\d+.*")) {
                numberPart = numberPart.replaceFirst("^0+", "");
                courseCode = "CS" + numberPart;
            }
        }
        
        return courseCode;
    }
}
