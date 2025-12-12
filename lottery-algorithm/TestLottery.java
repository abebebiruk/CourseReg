import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestLottery {
    public static void main(String[] args) {
        // ----- create some sample students -----
        // CS62 requires CS51, so students need CS51 in their past classes
        List<String> pastClasses = new ArrayList<>();
        pastClasses.add("CS51"); // Required prerequisite for CS62
        
        students s1 = new students(
                "S1", "Alice",
                new ArrayList<>(pastClasses), // Has CS51
                new ArrayList<>(),
                2025,
                students.MajorStatus.CS_MAJOR
        );

        students s2 = new students(
                "S2", "Bob",
                new ArrayList<>(),
                new ArrayList<>(),
                2026,
                students.MajorStatus.CS_MINOR
        );

        students s3 = new students(
                "S3", "Charlie",
                new ArrayList<>(pastClasses), // Has CS51
                new ArrayList<>(),
                2027,
                students.MajorStatus.NON_MAJOR
        );

        List<students> studentList = Arrays.asList(s1, s2, s3);

        // ----- create some sample classes -----
        classes c1 = new classes("CS62-01", "01", 2, 0, 1.0);
        classes c2 = new classes("CS62-02", "02", 1, 0, 1.0);

        List<classes> courseList = Arrays.asList(c1, c2);

        // ----- create sample class requests -----
        List<ClassRequest> requests = new ArrayList<>();

        // everyone wants CS62-01
        requests.add(new ClassRequest("S1", "CS62-01", 1));
        requests.add(new ClassRequest("S2", "CS62-01", 2));
        requests.add(new ClassRequest("S3", "CS62-01", 3));

        // Alice and Bob also want CS62-02
        requests.add(new ClassRequest("S1", "CS62-02", 1));
        requests.add(new ClassRequest("S2", "CS62-02", 1));

        // Create maps for quick lookup
        Map<String, classes> courseMap = new HashMap<>();
        for (classes c : courseList) {
            courseMap.put(c.courseSectionId, c);
        }
        
        Map<String, Map<String, ClassRequest>> requestMap = new HashMap<>();
        for (ClassRequest req : requests) {
            requestMap.computeIfAbsent(req.studentId, k -> new HashMap<>())
                      .put(req.courseId, req);
        }
        
        int currentYear = java.time.Year.now().getValue();

        // ----- check for students with missing prerequisites (weight = 0) -----
        List<String> rejectedStudents = new ArrayList<>();
        Map<String, PrerequisiteValidationResult> validationResults = new HashMap<>();
        
        for (ClassRequest req : requests) {
            students s = studentList.stream()
                    .filter(st -> st.studentId.equals(req.studentId))
                    .findFirst()
                    .orElse(null);
            if (s == null) continue;
            
            classes course = courseMap.get(req.courseId);
            if (course == null) continue;
            
            int weight = LotteryWeightCalculator.computeWeight(s, req, course, currentYear);
            if (weight == 0) {
                // Student rejected due to missing prerequisites
                String key = req.studentId + ":" + req.courseId;
                if (!rejectedStudents.contains(key)) {
                    rejectedStudents.add(key);
                    PrerequisiteValidationResult result = LotteryWeightCalculator.validatePrerequisites(s, course);
                    validationResults.put(key, result);
                }
            }
        }

        // ----- run lottery -----
        LotteryEngine engine = new LotteryEngine();
        Map<String, List<students>> result =
                engine.runLottery(studentList, courseList, requests);

        // ----- print enrolled students with weights -----
        for (Map.Entry<String, List<students>> entry : result.entrySet()) {
            String courseId = entry.getKey();
            List<students> enrolled = entry.getValue();
            classes course = courseMap.get(courseId);

            System.out.println("Course " + courseId + " enrollments:");
            for (students s : enrolled) {
                ClassRequest req = requestMap.get(s.studentId).get(courseId);
                int weight = LotteryWeightCalculator.computeWeight(s, req, course, currentYear);
                System.out.println("  - " + s.name + " (" + s.studentId + ") - Weight: " + weight);
            }
            System.out.println();
        }
        
        // ----- print rejected students (missing prerequisites) -----
        if (!rejectedStudents.isEmpty()) {
            System.out.println("=== STUDENTS REJECTED (Missing Prerequisites) ===");
            for (String key : rejectedStudents) {
                String[] parts = key.split(":");
                String studentId = parts[0];
                String courseId = parts[1];
                
                students s = studentList.stream()
                        .filter(st -> st.studentId.equals(studentId))
                        .findFirst()
                        .orElse(null);
                if (s == null) continue;
                
                PrerequisiteValidationResult validationResult = validationResults.get(key);
                System.out.println("  - " + s.name + " (" + s.studentId + ") - Course: " + courseId);
                System.out.println("    " + validationResult.getMessage());
                System.out.println("    Weight: 0 (prerequisites not met)");
                System.out.println();
            }
        }
    }
    
}
