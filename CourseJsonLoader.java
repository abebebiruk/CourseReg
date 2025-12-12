import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple JSON loader for course data from cs-courses.json
 * Parses the JSON format and creates classes objects
 */
public class CourseJsonLoader {
    
    /**
     * Load courses from JSON file
     * @param filepath Path to the JSON file
     * @return List of classes objects
     */
    public static List<classes> loadCoursesFromJson(String filepath) {
        List<classes> courses = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            StringBuilder jsonContent = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                jsonContent.append(line).append("\n");
            }
            
            String json = jsonContent.toString().trim();
            
            // Remove outer brackets if present
            if (json.startsWith("[")) {
                json = json.substring(1);
            }
            if (json.endsWith("]")) {
                json = json.substring(0, json.length() - 1);
            }
            
            // Split by objects (simple approach - look for closing braces)
            String[] objects = splitJsonObjects(json);
            
            for (String obj : objects) {
                classes course = parseCourseObject(obj);
                if (course != null) {
                    courses.add(course);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading course data: " + e.getMessage());
        }
        
        return courses;
    }
    
    /**
     * Split JSON string into individual object strings
     */
    private static String[] splitJsonObjects(String json) {
        List<String> objects = new ArrayList<>();
        int braceCount = 0;
        int start = 0;
        
        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '{') {
                if (braceCount == 0) {
                    start = i;
                }
                braceCount++;
            } else if (c == '}') {
                braceCount--;
                if (braceCount == 0) {
                    objects.add(json.substring(start, i + 1));
                }
            }
        }
        
        return objects.toArray(new String[0]);
    }
    
    /**
     * Parse a single course object from JSON string
     */
    private static classes parseCourseObject(String objJson) {
        try {
            String courseSectionId = extractStringValue(objJson, "courseSectionId");
            String courseSectionNumber = extractStringValue(objJson, "courseSectionNumber");
            int capacity = extractIntValue(objJson, "capacity");
            int currentEnrollment = extractIntValue(objJson, "currentEnrollment");
            double creditHours = extractDoubleValue(objJson, "creditHours");
            
            if (courseSectionId == null || courseSectionNumber == null) {
                return null;
            }
            
            return new classes(courseSectionId, courseSectionNumber, capacity, currentEnrollment, creditHours);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Extract string value from JSON object
     */
    private static String extractStringValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return null;
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return null;
        
        // Find the opening quote
        int quoteStart = json.indexOf("\"", colonIndex);
        if (quoteStart == -1) return null;
        
        // Find the closing quote
        int quoteEnd = json.indexOf("\"", quoteStart + 1);
        if (quoteEnd == -1) return null;
        
        return json.substring(quoteStart + 1, quoteEnd);
    }
    
    /**
     * Extract integer value from JSON object
     */
    private static int extractIntValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return 0;
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return 0;
        
        // Skip whitespace
        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        
        // Find the end of the number (comma, brace, or bracket)
        int valueEnd = valueStart;
        while (valueEnd < json.length() && 
               (Character.isDigit(json.charAt(valueEnd)) || json.charAt(valueEnd) == '"')) {
            valueEnd++;
        }
        
        String valueStr = json.substring(valueStart, valueEnd).replace("\"", "").trim();
        try {
            return Integer.parseInt(valueStr);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Extract double value from JSON object
     */
    private static double extractDoubleValue(String json, String key) {
        String searchKey = "\"" + key + "\"";
        int keyIndex = json.indexOf(searchKey);
        if (keyIndex == -1) return 0.0;
        
        int colonIndex = json.indexOf(":", keyIndex);
        if (colonIndex == -1) return 0.0;
        
        // Skip whitespace
        int valueStart = colonIndex + 1;
        while (valueStart < json.length() && Character.isWhitespace(json.charAt(valueStart))) {
            valueStart++;
        }
        
        // Find the end of the number
        int valueEnd = valueStart;
        while (valueEnd < json.length() && 
               (Character.isDigit(json.charAt(valueEnd)) || 
                json.charAt(valueEnd) == '.' || 
                json.charAt(valueEnd) == '"')) {
            valueEnd++;
        }
        
        String valueStr = json.substring(valueStart, valueEnd).replace("\"", "").trim();
        try {
            return Double.parseDouble(valueStr);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
}

