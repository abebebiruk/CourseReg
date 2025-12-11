import java.util.HashSet;
import java.util.Set;

public class PrerequisiteChecker
{
    // Static instance of the prerequisite graph (DAG)
    private static PrerequisiteGraph prerequisiteGraph = new PrerequisiteGraph();
    
    /**
     * Get the prerequisite graph instance
     */
    public static PrerequisiteGraph getPrerequisiteGraph()
    {
        return prerequisiteGraph;
    }
    
    /**
     * Check if student meets all prerequisites for a course using DAG
     * Time Complexity: O(p) where p = number of prerequisites (including transitive)
     */
    public static boolean checkPrerequisites(Student student, Course course)
    {
        String courseCode = course.getCourseCode();
        Set<String> allPrerequisites = prerequisiteGraph.getAllPrerequisites(courseCode);
        
        if (allPrerequisites.isEmpty())
        {
            return true;
        }
        
        for (String prereq : allPrerequisites)
        {
            if (!student.hasCompletedCourse(prereq))
            {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Get the set of missing prerequisites for a student using DAG
     * Returns all missing prerequisites (direct and indirect)
     * Time Complexity: O(p) where p = number of prerequisites
     * Returns Set instead of List for efficiency
     */
    public static Set<String> getMissingPrerequisites(Student student, Course course)
    {
        Set<String> missing = new HashSet<>();
        String courseCode = course.getCourseCode();
        Set<String> allPrerequisites = prerequisiteGraph.getAllPrerequisites(courseCode);
        
        if (allPrerequisites.isEmpty())
        {
            return missing;
        }
        
        for (String prereq : allPrerequisites)
        {
            if (!student.hasCompletedCourse(prereq))
            {
                missing.add(prereq);
            }
        }
        return missing;
    }
    
    /**
     * Get direct prerequisites for a course (immediate prerequisites only)
     */
    public static Set<String> getDirectPrerequisites(Course course)
    {
        String courseCode = course.getCourseCode();
        return prerequisiteGraph.getDirectPrerequisites(courseCode);
    }
    
    /**
     * Check if all required prerequisites are in completed courses
     * Time Complexity: O(p) where p = number of prerequisites
     * Uses Set.containsAll() for optimal checking
     */
    public static boolean hasCompletedAllPrerequisites(Set<String> completedCourses, Set<String> requiredPrerequisites)
    {
        if (requiredPrerequisites == null || requiredPrerequisites.isEmpty())
        {
            return true;
        }
        if (completedCourses == null)
        {
            return false;
        }
        return completedCourses.containsAll(requiredPrerequisites);
    }
}
