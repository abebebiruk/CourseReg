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
}
