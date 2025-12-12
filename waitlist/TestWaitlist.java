import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Full integration test for waitlist feature with lottery.
 * Tests the complete flow: lottery execution + waitlist analysis.
 */
public class TestWaitlist {
    public static void main(String[] args) {
        try {
            System.out.println("=== WAITLIST TEST ===\n");
            
            List<String> pastClassesWithPrereqs = new ArrayList<>();
            pastClassesWithPrereqs.add("CS51");
            pastClassesWithPrereqs.add("CS54");
            pastClassesWithPrereqs.add("CS62");
            
            List<String> pastClassesWithoutPrereqs = new ArrayList<>();
            pastClassesWithoutPrereqs.add("CS51");
            // Missing CS54 and CS62
            
            // Student 1: CS Major, Senior, Rank 1, Has prerequisites
            students s1 = new students("S1", "Alice", pastClassesWithPrereqs, new ArrayList<>(), 
                                      2025, students.MajorStatus.CS_MAJOR);
            
            // Student 2: CS Minor, Junior, Rank 2, Has prerequisites
            students s2 = new students("S2", "Bob", pastClassesWithPrereqs, new ArrayList<>(), 
                                      2026, students.MajorStatus.CS_MINOR);
            
            // Student 3: Non-major, Sophomore, Rank 1, Missing prerequisites
            students s3 = new students("S3", "Charlie", pastClassesWithoutPrereqs, new ArrayList<>(), 
                                      2027, students.MajorStatus.NON_MAJOR);
            
            // Student 4: CS Major, Senior, Rank 3, Has prerequisites
            students s4 = new students("S4", "Diana", pastClassesWithPrereqs, new ArrayList<>(), 
                                      2025, students.MajorStatus.CS_MAJOR);
            
            // Student 5: CS Minor, Freshman, Rank 4, Has prerequisites
            students s5 = new students("S5", "Eve", pastClassesWithPrereqs, new ArrayList<>(), 
                                      2028, students.MajorStatus.CS_MINOR);
            
            // Student 6: Non-major, Junior, Rank 2, Has prerequisites
            students s6 = new students("S6", "Frank", pastClassesWithPrereqs, new ArrayList<>(), 
                                      2026, students.MajorStatus.NON_MAJOR);
            
            List<students> studentList = Arrays.asList(s1, s2, s3, s4, s5, s6);
            
            // Create a course with limited capacity (CS140 requires CS54 and CS62)
            classes cs140 = new classes("CSCI140 HM-01 SP2025", "01", 3, 0, 1.0);
            List<classes> courseList = Arrays.asList(cs140);
            
            System.out.println("Course: " + cs140.courseSectionId);
            System.out.println("Capacity: " + cs140.capacity);
            System.out.println("Current Enrollment: " + cs140.currentEnrollment);
            System.out.println("Available Seats: " + (cs140.capacity - cs140.currentEnrollment) + "\n");
            
            // Create requests with different preference ranks
            List<ClassRequest> requests = new ArrayList<>();
            requests.add(new ClassRequest("S1", cs140.courseSectionId, 1)); // Alice: CS Major, Senior, Rank 1
            requests.add(new ClassRequest("S2", cs140.courseSectionId, 2)); // Bob: CS Minor, Junior, Rank 2
            requests.add(new ClassRequest("S3", cs140.courseSectionId, 1)); // Charlie: Missing prerequisites
            requests.add(new ClassRequest("S4", cs140.courseSectionId, 3)); // Diana: CS Major, Senior, Rank 3
            requests.add(new ClassRequest("S5", cs140.courseSectionId, 4)); // Eve: CS Minor, Freshman, Rank 4
            requests.add(new ClassRequest("S6", cs140.courseSectionId, 2)); // Frank: Non-major, Junior, Rank 2
            
            System.out.println("Created " + requests.size() + " class requests\n");
            
            // Run lottery with waitlist
            LotteryEngine engine = new LotteryEngine();
            LotteryEngine.LotteryResult result = engine.runLotteryWithWaitlist(
                    studentList, courseList, requests);
            
            // Display results
            System.out.println("=== LOTTERY RESULTS ===\n");
            
            for (Map.Entry<String, List<students>> entry : result.enrolledByCourse.entrySet()) {
                String courseId = entry.getKey();
                List<students> enrolled = entry.getValue();
                
                System.out.println("Course: " + courseId);
                System.out.println("Enrolled Students (" + enrolled.size() + "):");
                for (students s : enrolled) {
                    WaitlistResult waitlistResult = result.getResult(s.studentId, courseId);
                    int weight = waitlistResult != null ? waitlistResult.studentWeight : 0;
                    System.out.println(" " + s.name + " (" + s.studentId + ") - " + 
                        getMajorStatusString(s.majorStatus) + ", Grad: " + s.gradYear + ", Weight: " + weight);
                }
                System.out.println();
            }
            
            // Count and display all results
            int enrolledCount = 0;
            int waitlistedCount = 0;
            int rejectedCount = 0;
            
            List<WaitlistResult> waitlistedExamples = new ArrayList<>();
            List<WaitlistResult> rejectedExamples = new ArrayList<>();
            
            for (ClassRequest req : requests) {
                WaitlistResult waitlistResult = result.getResult(req.studentId, req.courseId);
                if (waitlistResult != null) {
                    switch (waitlistResult.status) {
                        case ENROLLED:
                            enrolledCount++;
                            break;
                        case WAITLISTED:
                            waitlistedCount++;
                            if (waitlistedExamples.size() < 3) {
                                waitlistedExamples.add(waitlistResult);
                            }
                            break;
                        case REJECTED:
                            rejectedCount++;
                            if (rejectedExamples.size() < 3) {
                                rejectedExamples.add(waitlistResult);
                            }
                            break;
                    }
                }
            }
            
            // Show waitlisted examples
            if (!waitlistedExamples.isEmpty()) {
                System.out.println("\n=== WAITLISTED STUDENTS (Sample of " + 
                                 Math.min(3, waitlistedCount) + ") ===\n");
                for (WaitlistResult waitlistResult : waitlistedExamples) {
                    students student = studentList.stream()
                            .filter(s -> s.studentId.equals(waitlistResult.studentId))
                            .findFirst()
                            .orElse(null);
                    
                    if (student != null) {
                        System.out.print(waitlistResult.formatDetailedDisplay(
                            student.name, 
                            getMajorStatusString(student.majorStatus), 
                            student.gradYear));
                        System.out.println();
                    }
                }
            }
            
            // Show rejected examples
            if (!rejectedExamples.isEmpty()) {
                System.out.println("\n=== REJECTED STUDENTS (Sample of " + 
                                 Math.min(3, rejectedCount) + ") ===\n");
                for (WaitlistResult waitlistResult : rejectedExamples) {
                    students student = studentList.stream()
                            .filter(s -> s.studentId.equals(waitlistResult.studentId))
                            .findFirst()
                            .orElse(null);
                    
                    if (student != null) {
                        System.out.print(waitlistResult.formatDetailedDisplay(
                            student.name, 
                            getMajorStatusString(student.majorStatus), 
                            student.gradYear));
                        System.out.println();
                    }
                }
            }
            
            // Summary
            System.out.println("\n=== SUMMARY ===");
            System.out.println("Enrolled: " + enrolledCount);
            System.out.println("Waitlisted: " + waitlistedCount);
            System.out.println("Rejected: " + rejectedCount);
            System.out.println("Total: " + requests.size());
            
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static String getMajorStatusString(students.MajorStatus status) {
        if (status == null) return "Non-major";
        switch (status) {
            case CS_MAJOR: return "CS Major";
            case CS_MINOR: return "CS Minor";
            case NON_MAJOR: return "Non-major";
            default: return "Unknown";
        }
    }
}

