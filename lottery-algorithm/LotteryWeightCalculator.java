public class LotteryWeightCalculator {

    public static int computeWeight(students s, ClassRequest req, classes course) {
        int currentYear = java.time.Year.now().getValue();
        return computeWeight(s, req, course, currentYear);
    }

    public static int computeWeight(students s, ClassRequest req, classes course, int currentYear) {

        if (s == null || req == null || course == null) {
            throw new IllegalArgumentException("Student, request, and course cannot be null.");
        }

        // --- Course-level rules ---
        if (course.status == classes.Status.CLOSED ||
            course.currentEnrollment >= course.capacity) {
            return 0;
        }

        int weight = 10;

        // --- Preference rank ---
        int rank = req.preferenceRank;
        if (rank < 1 || rank > 4) rank = 4;

        switch (rank) {
            case 1: weight += 4; break;
            case 2: weight += 3; break;
            case 3: weight += 2; break;
            case 4: weight += 1; break;
        }

        // --- Major status ---
        students.MajorStatus majorStatus =
                (s.majorStatus == null) ? students.MajorStatus.NON_MAJOR : s.majorStatus;

        switch (majorStatus) {
            case CS_MAJOR: weight += 4; break;
            case CS_MINOR: weight += 2; break;
            case NON_MAJOR: break;
        }

        // --- Student Year ---
        StudentYear year = getStudentYear(s.gradYear, currentYear);

        switch (year) {
            case SENIOR: weight += 4; break;
            case JUNIOR: weight += 3; break;
            case SOPHOMORE: weight += 2; break;
            case FRESHMAN: weight += 1; break;
        }

        // --- Seats left bonus ---
        int seatsLeft = course.capacity - course.currentEnrollment;
        if (seatsLeft >= 10)      weight += 2;
        else if (seatsLeft >= 5) weight += 1;

        // --- Restricted course penalty ---
        if (course.status == classes.Status.RESTRICTED &&
            majorStatus != students.MajorStatus.CS_MAJOR) {
            weight -= 2;
        }

        if (weight < 1) weight = 1;

        return weight;
    }

    public static StudentYear getStudentYear(int gradYear, int currentYear) {
        int yearsLeft = gradYear - currentYear;

        if (yearsLeft <= 0) return StudentYear.SENIOR;
        if (yearsLeft == 1) return StudentYear.JUNIOR;
        if (yearsLeft == 2) return StudentYear.SOPHOMORE;
        return StudentYear.FRESHMAN;
    }
}
