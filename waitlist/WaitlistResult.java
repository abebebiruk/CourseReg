/**
 * Represents the result of a course registration request.
 * Can be ENROLLED, WAITLISTED, or REJECTED
 */
public class WaitlistResult {
    public enum Status {
        ENROLLED,      
        WAITLISTED,    
        REJECTED
    }

    public final Status status;
    public final String reason;
    public final String courseId;
    public final String studentId;
    
    // Student's lottery weight (0 if not calculated or rejected)
    public final int studentWeight;
    
    // Demographic information about enrolled students (for waitlisted students)
    public final ClassDemographics enrolledDemographics;

    public WaitlistResult(Status status, String reason, String courseId, String studentId) {
        this(status, reason, courseId, studentId, 0, null);
    }

    public WaitlistResult(Status status, String reason, String courseId, String studentId, 
                         int studentWeight, ClassDemographics enrolledDemographics) {
        this.status = status;
        this.reason = reason;
        this.courseId = courseId;
        this.studentId = studentId;
        this.studentWeight = studentWeight;
        this.enrolledDemographics = enrolledDemographics;
    }

    /**
     * Method for rejected students (missing prerequisites)
     */
    public static WaitlistResult rejected(String courseId, String studentId, String missingPrereqs) {
        String reason = "Missing prerequisites: " + missingPrereqs + 
                       ". Must complete prerequisites before registering for this class.";
        return new WaitlistResult(Status.REJECTED, reason, courseId, studentId, 0, null);
    }

    /**
     * Method for enrolled students
     */
    public static WaitlistResult enrolled(String courseId, String studentId, int studentWeight) {
        return new WaitlistResult(Status.ENROLLED, "Successfully enrolled", courseId, studentId, studentWeight, null);
    }

    /**
     * Method for waitlisted students
     */
    public static WaitlistResult waitlisted(String courseId, String studentId, String reason, 
                                           int studentWeight, ClassDemographics demographics) {
        return new WaitlistResult(Status.WAITLISTED, reason, courseId, studentId, studentWeight, demographics);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Status: ").append(status).append("\n");
        sb.append("Course: ").append(courseId).append("\n");
        sb.append("Reason: ").append(reason).append("\n");
        if (enrolledDemographics != null) {
            sb.append("\nEnrolled Class Demographics:\n");
            sb.append(enrolledDemographics.toString());
        }
        return sb.toString();
    }

    /**
     * Formats a detailed display string for this result, including student information.
     * @param studentName The student's name
     * @param majorStatus The student's major status (e.g., "CS Major", "CS Minor", "Non-major")
     * @param gradYear The student's graduation year
     * @return A formatted string ready for display
     */
    public String formatDetailedDisplay(String studentName, String majorStatus, int gradYear) {
        StringBuilder sb = new StringBuilder();
        sb.append("Student: ").append(studentName).append(" (").append(studentId).append(")\n");
        sb.append("  Major: ").append(majorStatus).append("\n");
        sb.append("  Grad Year: ").append(gradYear).append("\n");
        if (studentWeight > 0) {
            sb.append("  Lottery Weight: ").append(studentWeight).append("\n");
        }
        sb.append("  Reason: ").append(reason).append("\n");
        
        if (enrolledDemographics != null) {
            sb.append("\n  Enrolled Class Demographics:\n");
            String[] demoLines = enrolledDemographics.toString().split("\n");
            for (String line : demoLines) {
                sb.append("  ").append(line).append("\n");
            }
        }
        
        return sb.toString();
    }

    /**
     * Contains demographic information about students who enrolled in a course.
     */
    public static class ClassDemographics {
        public final int totalEnrolled;
        public final int seniors;
        public final int juniors;
        public final int sophomores;
        public final int freshmen;
        public final int csMajors;
        public final int csMinors;
        public final int nonMajors;
        public final int rank1Preferences;
        public final int rank2Preferences;
        public final int rank3Preferences;
        public final int rank4Preferences;
        public final double avgWeight;

        public ClassDemographics(int totalEnrolled, int seniors, int juniors, int sophomores, 
                int freshmen, int csMajors, int csMinors, int nonMajors,
                int rank1Preferences, int rank2Preferences, int rank3Preferences, 
                int rank4Preferences, double avgWeight) {
            this.totalEnrolled = totalEnrolled;
            this.seniors = seniors;
            this.juniors = juniors;
            this.sophomores = sophomores;
            this.freshmen = freshmen;
            this.csMajors = csMajors;
            this.csMinors = csMinors;
            this.nonMajors = nonMajors;
            this.rank1Preferences = rank1Preferences;
            this.rank2Preferences = rank2Preferences;
            this.rank3Preferences = rank3Preferences;
            this.rank4Preferences = rank4Preferences;
            this.avgWeight = avgWeight;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("  Total Enrolled: ").append(totalEnrolled).append("\n");
            sb.append("  By Year: Seniors=").append(seniors)
              .append(", Juniors=").append(juniors)
              .append(", Sophomores=").append(sophomores)
              .append(", Freshmen=").append(freshmen).append("\n");
            sb.append("  By Major: CS Majors=").append(csMajors)
              .append(", CS Minors=").append(csMinors)
              .append(", Non-Majors=").append(nonMajors).append("\n");
            sb.append("  By Preference Rank: Rank 1=").append(rank1Preferences)
              .append(", Rank 2=").append(rank2Preferences)
              .append(", Rank 3=").append(rank3Preferences)
              .append(", Rank 4=").append(rank4Preferences).append("\n");
            sb.append("  Average Lottery Weight: ").append(String.format("%.2f", avgWeight)).append("\n");
            return sb.toString();
        }
    }
}