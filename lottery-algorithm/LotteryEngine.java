import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class LotteryEngine {

    /**
     * Runs the lottery for all courses.
     *
     * @param studentList  list of all students
     * @param courseList   list of all classes (courses)
     * @param requestList  list of all class requests
     * @return map from courseSectionId -> list of enrolled students
     */
    public Map<String, List<students>> runLottery(List<students> studentList,
                                                  List<classes> courseList,
                                                  List<ClassRequest> requestList) {

        // Fast lookup by studentId
        Map<String, students> studentsById = new HashMap<>();
        for (students s : studentList) {
            studentsById.put(s.studentId, s);
        }

        // Group requests by courseId
        Map<String, List<ClassRequest>> requestsByCourse = new HashMap<>();
        for (ClassRequest req : requestList) {
            requestsByCourse
                    .computeIfAbsent(req.courseId, k -> new ArrayList<>())
                    .add(req);
        }

        Map<String, List<students>> enrolledByCourse = new HashMap<>();
        Random rand = new Random();

        // Run lottery per course
        for (classes course : courseList) {
            List<ClassRequest> courseRequests =
                    requestsByCourse.getOrDefault(course.courseSectionId, new ArrayList<>());

            List<students> winners = runCourseLottery(course, courseRequests, studentsById, rand);
            enrolledByCourse.put(course.courseSectionId, winners);

            // update enrollment count for that course
            course.currentEnrollment += winners.size();
        }

        return enrolledByCourse;
    }

    /**
     * Runs the lottery for a single course.
     */
    private List<students> runCourseLottery(classes course,
                                            List<ClassRequest> requests,
                                            Map<String, students> studentsById,
                                            Random rand) {

        List<students> enrolled = new ArrayList<>();

        // If course already full, nobody gets in
        int seatsLeft = course.capacity - course.currentEnrollment;
        if (seatsLeft <= 0) {
            return enrolled;
        }

        // Build ticket pool: each student appears 'weight' times
        List<students> ticketPool = new ArrayList<>();

        for (ClassRequest req : requests) {
            students s = studentsById.get(req.studentId);
            if (s == null) continue; // bad student id in data

            int weight = LotteryWeightCalculator.computeWeight(s, req, course);
            if (weight <= 0) continue;

            for (int i = 0; i < weight; i++) {
                ticketPool.add(s);
            }
        }

        if (ticketPool.isEmpty()) {
            return enrolled; // no valid requests
        }

        Set<String> alreadyChosen = new HashSet<>();

        while (seatsLeft > 0 && !ticketPool.isEmpty()) {
            int idx = rand.nextInt(ticketPool.size());
            students chosen = ticketPool.get(idx);

            // avoid giving the same student 2 seats in the same course
            if (!alreadyChosen.contains(chosen.studentId)) {
                enrolled.add(chosen);
                alreadyChosen.add(chosen.studentId);
                seatsLeft--;
            }

            // remove all tickets for chosen student
            String idToRemove = chosen.studentId;
            ticketPool.removeIf(s -> s.studentId.equals(idToRemove));
        }

        return enrolled;
    }
}
