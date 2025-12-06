public class ClassRequest {
    public final String studentId;
    public final String courseId;
    public final int preferenceRank;

    public ClassRequest(String studentId, String courseId, int preferenceRank) {
        if (preferenceRank < 1 || preferenceRank > 4) {
            throw new IllegalArgumentException("preferenceRank must be between 1 and 4");
        }
        this.studentId = studentId;
        this.courseId = courseId;
        this.preferenceRank = preferenceRank;
    }
}
