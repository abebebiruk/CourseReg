import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * PrerequisiteGraph - DAG (Directed Acyclic Graph) for managing course prerequisites
 * Uses adjacency list representation for efficient prerequisite lookups
 */
public class PrerequisiteGraph
{
    // Adjacency list: course -> set of direct prerequisites
    private HashMap<String, Set<String>> graph;
    
    /**
     * Constructor - initializes the graph with all CS course prerequisites
     */
    public PrerequisiteGraph()
    {
        this.graph = new HashMap<>();
        initializeCSPrerequisites();
    }
    
    /**
     * Initialize all CS course prerequisites as specified
     */
    private void initializeCSPrerequisites()
    {
        // CS35 (no prerequisites) - no entry needed, but we'll add it for completeness
        addCourse("CS35");
        
        // CS51 (no prerequisites)
        addCourse("CS51");
        
        // CS54 requires CS51
        addPrerequisite("CS54", "CS51");
        
        // CS62 requires CS51
        addPrerequisite("CS62", "CS51");
        
        // CS101 requires CS54 and CS62
        addPrerequisite("CS101", "CS54");
        addPrerequisite("CS101", "CS62");
        
        // CS105 requires CS54 and CS62
        addPrerequisite("CS105", "CS54");
        addPrerequisite("CS105", "CS62");
        
        // CS140 requires CS54 and CS62
        addPrerequisite("CS140", "CS54");
        addPrerequisite("CS140", "CS62");
        
        // CS122 requires CS62
        addPrerequisite("CS122", "CS62");
        
        // CS124 requires CS51
        addPrerequisite("CS124", "CS51");
        
        // CS131 requires CS62
        addPrerequisite("CS131", "CS62");
        
        // CS132 requires CS105 and CS101
        addPrerequisite("CS132", "CS105");
        addPrerequisite("CS132", "CS101");
        
        // CS133 requires CS62
        addPrerequisite("CS133", "CS62");
        
        // CS138 requires CS105
        addPrerequisite("CS138", "CS105");
        
        // CS143 requires CS62
        addPrerequisite("CS143", "CS62");
        
        // CS145 requires CS140
        addPrerequisite("CS145", "CS140");
        
        // CS151 requires CS62
        addPrerequisite("CS151", "CS62");
        
        // CS152 requires CS62
        addPrerequisite("CS152", "CS62");
        
        // CS153 requires CS62
        addPrerequisite("CS153", "CS62");
        
        // CS158 requires CS62
        addPrerequisite("CS158", "CS62");
        
        // CS159 requires CS62
        addPrerequisite("CS159", "CS62");
        
        // CS181AA requires CS140
        addPrerequisite("CS181AA", "CS140");
        
        // CS181CA requires CS105
        addPrerequisite("CS181CA", "CS105");
        
        // CS181DA requires CS62
        addPrerequisite("CS181DA", "CS62");
        
        // CS181AV requires CS62
        addPrerequisite("CS181AV", "CS62");
    }
    
    /**
     * Add a course to the graph (if it doesn't exist)
     */
    public void addCourse(String courseCode)
    {
        if (!graph.containsKey(courseCode))
        {
            graph.put(courseCode, new HashSet<>());
        }
    }
    
    /**
     * Add a prerequisite relationship: course requires prerequisite
     * @param courseCode The course that requires the prerequisite
     * @param prerequisite The prerequisite course
     */
    public void addPrerequisite(String courseCode, String prerequisite)
    {
        addCourse(courseCode);
        addCourse(prerequisite);
        graph.get(courseCode).add(prerequisite);
    }
    
    /**
     * Get direct prerequisites for a course
     * @param courseCode The course code
     * @return Set of direct prerequisites (empty set if course has no prerequisites)
     */
    public Set<String> getDirectPrerequisites(String courseCode)
    {
        Set<String> prereqs = graph.get(courseCode);
        return (prereqs != null) ? new HashSet<>(prereqs) : new HashSet<>();
    }
    
    /**
     * Get all prerequisites (transitive closure) for a course using DFS
     * This includes direct prerequisites and all their prerequisites recursively
     * @param courseCode The course code
     * @return Set of all prerequisites (direct and indirect)
     */
    public Set<String> getAllPrerequisites(String courseCode)
    {
        Set<String> allPrereqs = new HashSet<>();
        Set<String> visited = new HashSet<>();
        dfsPrerequisites(courseCode, allPrereqs, visited);
        return allPrereqs;
    }
    
    /**
     * DFS helper to find all prerequisites (transitive closure)
     */
    private void dfsPrerequisites(String courseCode, Set<String> allPrereqs, Set<String> visited)
    {
        if (visited.contains(courseCode))
        {
            return; // Already processed
        }
        
        visited.add(courseCode);
        Set<String> directPrereqs = graph.get(courseCode);
        
        if (directPrereqs != null)
        {
            for (String prereq : directPrereqs)
            {
                allPrereqs.add(prereq);
                dfsPrerequisites(prereq, allPrereqs, visited);
            }
        }
    }
    
    /**
     * Check if a course has prerequisites
     * @param courseCode The course code
     * @return true if the course has at least one prerequisite
     */
    public boolean hasPrerequisites(String courseCode)
    {
        Set<String> prereqs = graph.get(courseCode);
        return prereqs != null && !prereqs.isEmpty();
    }
    
    /**
     * Check if the graph contains a cycle (should not happen in valid prerequisites. We added this to ensure the graph is a DAG)
     * Uses DFS to detect back edges
     * @return true if a cycle is detected
     */
    public boolean hasCycle()
    {
        Set<String> visited = new HashSet<>();
        Set<String> recursionStack = new HashSet<>();
        
        for (String course : graph.keySet())
        {
            if (!visited.contains(course))
            {
                if (hasCycleDFS(course, visited, recursionStack))
                {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * DFS helper for cycle detection
     */
    private boolean hasCycleDFS(String course, Set<String> visited, Set<String> recursionStack)
    {
        visited.add(course);
        recursionStack.add(course);
        
        Set<String> prereqs = graph.get(course);
        if (prereqs != null)
        {
            for (String prereq : prereqs)
            {
                if (!visited.contains(prereq))
                {
                    if (hasCycleDFS(prereq, visited, recursionStack))
                    {
                        return true;
                    }
                }
                else if (recursionStack.contains(prereq))
                {
                    // Back edge found - cycle detected
                    return true;
                }
            }
        }
        
        recursionStack.remove(course);
        return false;
    }
}

