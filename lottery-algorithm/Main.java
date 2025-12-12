import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        // ----- create some sample students -----
        students s1 = new students(
                "S1", "Alice",
                new ArrayList<>(),
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
                new ArrayList<>(),
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

        // ----- run lottery -----
        LotteryEngine engine = new LotteryEngine();
        Map<String, List<students>> result =
                engine.runLottery(studentList, courseList, requests);

        // ----- print results -----
        for (Map.Entry<String, List<students>> entry : result.entrySet()) {
            String courseId = entry.getKey();
            List<students> enrolled = entry.getValue();

            System.out.println("Course " + courseId + " enrollments:");
            for (students s : enrolled) {
                System.out.println("  - " + s.name + " (" + s.studentId + ")");
            }
            System.out.println();
        }
    }
}
