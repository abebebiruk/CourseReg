import java.util.Set;

public class PrerequisiteValidationResult
{
    private boolean isEligible;
    private Set<String> missingPrerequisites; // Set instead of List (no duplicates needed)
    private String message;
    
    /**
     * Constructor
     */
    public PrerequisiteValidationResult(boolean isEligible, Set<String> missingPrerequisites, String message)
    {
        this.isEligible = isEligible;
        this.missingPrerequisites = missingPrerequisites;
        this.message = message;
    }
    
    // Getters
    public boolean isEligible()
    {
        return isEligible;
    }
    
    public Set<String> getMissingPrerequisites()
    {
        return missingPrerequisites;
    }
    
    public String getMessage()
    {
        return message;
    }
}
