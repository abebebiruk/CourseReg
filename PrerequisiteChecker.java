import java.util.HashSet;
import java.util.Set;

public class PrerequisiteChecker
{
    /**
     * Check if student meets all prerequisites for a course
     * Time Complexity: O(p) where p = number of prerequisites
     */
    public static boolean checkPrerequisites(Student student, Course course)
    {
        return false;
    }
    
    /**
     * Get the set of missing prerequisites for a student
     * Time Complexity: O(p) where p = number of prerequisites
     * Returns Set instead of List for efficiency
     */
    public static Set<String> getMissingPrerequisites(Student student, Course course)
    {
        return null;
    }
    
    /**
     * Check if all required prerequisites are in completed courses
     * Time Complexity: O(p) where p = number of prerequisites
     * Uses Set.containsAll() for optimal checking
     */
    public static boolean hasCompletedAllPrerequisites(Set<String> completedCourses, Set<String> requiredPrerequisites)
    {
        return false;
    }
}
