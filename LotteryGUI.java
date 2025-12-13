import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Map;
import java.util.Set;


public class LotteryGUI extends JFrame {

    private JTextArea outputArea;
    private JButton runSampleButton;
    private JButton runCSVButton;

    public LotteryGUI() {
        super("Course Lottery System");

        // ----- basic window setup -----
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null); // center on screen

        // ----- UI components -----
        runSampleButton = new JButton("Run Sample Lottery");
        runCSVButton = new JButton("Run Lottery from CSV");
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));

        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(runSampleButton);
        buttonPanel.add(runCSVButton);

        // Layout: buttons at top, text area in center
        setLayout(new BorderLayout());
        add(buttonPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // Button actions
        runSampleButton.addActionListener((ActionEvent e) -> runSampleLottery());
        runCSVButton.addActionListener((ActionEvent e) -> runLotteryFromCSV());
    }

    /**
     * Runs lottery with sample data.
     */
    private void runSampleLottery() {
        outputArea.setText(""); // clear old output

        // 1. Build some example data
        java.util.List<students> studentList = buildSampleStudents();
        java.util.List<classes> courseList = buildSampleCourses();
        java.util.List<ClassRequest> requestList = buildSampleRequests();
        
        runLotteryAndDisplay(studentList, courseList, requestList, "Sample Lottery");
    }
    
    /**
     * Runs lottery with data loaded from CSV file.
     */
    private void runLotteryFromCSV() {
        outputArea.setText(""); // clear old output
        
        // Find CSV file
        String csvPath = findCSVFile();
        if (csvPath == null) {
            outputArea.append("Error: Could not find student.csv file.\n");
            outputArea.append("Please ensure data/student.csv exists in the project directory.\n");
            return;
        }
        
        outputArea.append("Loading students from: " + csvPath + "\n");
        outputArea.append("Please wait...\n\n");
        outputArea.repaint(); // Update UI
        
        // Load students from CSV
        java.util.List<students> studentList = loadStudentsFromCSV(csvPath);
        if (studentList.isEmpty()) {
            outputArea.append("Error: No students loaded from CSV file.\n");
            return;
        }
        
        outputArea.append("Loaded " + studentList.size() + " students.\n\n");
        
        // Create class requests from student data
        java.util.List<ClassRequest> requestList = new ArrayList<>();
        Set<String> uniqueCourseIds = new HashSet<>();
        
        for (students s : studentList) {
            int rank = 1;
            for (String courseId : s.requestedClasses) {
                if (rank <= 4) { // ClassRequest only allows ranks 1-4
                    requestList.add(new ClassRequest(s.studentId, courseId, rank));
                    uniqueCourseIds.add(courseId);
                    rank++;
                }
            }
        }
        
        // Create classes for all requested courses (default capacity: 30)
        java.util.List<classes> courseList = new ArrayList<>();
        for (String courseId : uniqueCourseIds) {
            courseList.add(new classes(courseId, "01", 30, 0, 1.0));
        }
        
        runLotteryAndDisplay(studentList, courseList, requestList, "CSV Lottery");
    }
    
    /**
     * Common method to run lottery and display results.
     */
    private void runLotteryAndDisplay(java.util.List<students> studentList,
                                      java.util.List<classes> courseList,
                                      java.util.List<ClassRequest> requestList,
                                      String title) {
        outputArea.setText(""); // clear old output

        // 2. Show basic info about the setup
        outputArea.append("=== " + title + " ===\n\n");
        outputArea.append("Students: " + studentList.size() + "\n");
        if (studentList.size() <= 20) {
            for (students s : studentList) {
                outputArea.append("  " + s.studentId + " - " + s.name + 
                        " (Year: " + s.gradYear + ", Major: " + s.majorStatus + ")\n");
            }
        } else {
            outputArea.append("  (Showing first 10 students)\n");
            for (int i = 0; i < Math.min(10, studentList.size()); i++) {
                students s = studentList.get(i);
                outputArea.append("  " + s.studentId + " - " + s.name + "\n");
            }
            outputArea.append("  ... and " + (studentList.size() - 10) + " more\n");
        }
        outputArea.append("\nCourses: " + courseList.size() + "\n");
        for (classes c : courseList) {
            outputArea.append("  " + c.courseSectionId +
                    " (cap=" + c.capacity + ", enrolled=" + c.currentEnrollment + ")\n");
        }
        outputArea.append("\nRequests: " + requestList.size() + "\n");
        if (requestList.size() <= 50) {
            for (ClassRequest r : requestList) {
                outputArea.append("  student " + r.studentId +
                        " -> course " + r.courseId +
                        " (rank " + r.preferenceRank + ")\n");
            }
        } else {
            outputArea.append("  (Too many requests to display individually)\n");
        }
        outputArea.append("\n=== RUNNING LOTTERY (Backend Process) ===\n\n");
        outputArea.repaint(); // Update UI

        // 3. Run detailed lottery with backend process visible
        Map<String, java.util.List<students>> results =
                runDetailedLottery(studentList, courseList, requestList);

        // 4. Display results course by course
        for (Map.Entry<String, java.util.List<students>> entry : results.entrySet()) {
            String courseId = entry.getKey();
            java.util.List<students> enrolled = entry.getValue();

            outputArea.append("Course " + courseId + " enrollments:\n");
            if (enrolled.isEmpty()) {
                outputArea.append("  (no students enrolled)\n");
            } else {
                for (students s : enrolled) {
                    outputArea.append("  - " + s.name + " (" + s.studentId + ")\n");
                }
            }
            outputArea.append("\n");
        }

        // Print summary statistics
        outputArea.append("\n=== SUMMARY ===\n");
        int totalEnrolled = 0;
        for (java.util.List<students> enrolled : results.values()) {
            totalEnrolled += enrolled.size();
        }
        outputArea.append("Total students enrolled: " + totalEnrolled + "\n");
        outputArea.append("Total course sections: " + results.size() + "\n");
        outputArea.append("\n=== End of Lottery ===\n");
    }
    
    /**
     * Runs lottery with detailed backend process visible.
     */
    private Map<String, java.util.List<students>> runDetailedLottery(
            java.util.List<students> studentList,
            java.util.List<classes> courseList,
            java.util.List<ClassRequest> requestList) {
        
        // Fast lookup by studentId
        Map<String, students> studentsById = new HashMap<>();
        for (students s : studentList) {
            studentsById.put(s.studentId, s);
        }

        // Group requests by courseId
        Map<String, java.util.List<ClassRequest>> requestsByCourse = new HashMap<>();
        for (ClassRequest req : requestList) {
            requestsByCourse
                    .computeIfAbsent(req.courseId, k -> new ArrayList<>())
                    .add(req);
        }

        Map<String, java.util.List<students>> enrolledByCourse = new HashMap<>();
        java.util.Random rand = new java.util.Random();

        // Run lottery per course
        for (classes course : courseList) {
            outputArea.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n");
            outputArea.append("COURSE: " + course.courseSectionId + 
                    " (Capacity: " + course.capacity + 
                    ", Currently Enrolled: " + course.currentEnrollment + ")\n");
            outputArea.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");
            
            java.util.List<ClassRequest> courseRequests =
                    requestsByCourse.getOrDefault(course.courseSectionId, new ArrayList<>());

            if (courseRequests.isEmpty()) {
                outputArea.append("No requests for this course.\n\n");
                enrolledByCourse.put(course.courseSectionId, new ArrayList<>());
                continue;
            }

            outputArea.append("STEP 1: Calculating weights for each student request...\n");
            outputArea.append("─────────────────────────────────────────────────────\n");
            
            // Build ticket pool: each student appears 'weight' times
            java.util.List<students> ticketPool = new ArrayList<>();
            Map<String, Integer> studentWeights = new HashMap<>();
            
            int seatsLeft = course.capacity - course.currentEnrollment;
            if (seatsLeft <= 0) {
                outputArea.append("Course is already full. No new enrollments.\n\n");
                enrolledByCourse.put(course.courseSectionId, new ArrayList<>());
                continue;
            }
            
            outputArea.append("Available seats: " + seatsLeft + "\n\n");
            
            for (ClassRequest req : courseRequests) {
                students s = studentsById.get(req.studentId);
                if (s == null) {
                    outputArea.append("  ⚠ Warning: Student " + req.studentId + " not found, skipping.\n");
                    continue;
                }

                int weight = LotteryWeightCalculator.computeWeight(s, req, course);
                if (weight <= 0) {
                    outputArea.append("  ⚠ Warning: Invalid weight for " + s.studentId + ", skipping.\n");
                    continue;
                }
                
                studentWeights.put(s.studentId, weight);
                
                // Show weight breakdown
                int currentYear = java.time.Year.now().getValue();
                StudentYear year = LotteryWeightCalculator.getStudentYear(s.gradYear, currentYear);
                
                outputArea.append("  Student: " + s.name + " (" + s.studentId + ")\n");
                outputArea.append("    - Preference Rank: " + req.preferenceRank + 
                        " (bonus: " + getRankBonus(req.preferenceRank) + ")\n");
                outputArea.append("    - Major Status: " + s.majorStatus + 
                        " (bonus: " + getMajorBonus(s.majorStatus) + ")\n");
                outputArea.append("    - Academic Year: " + year + 
                        " (bonus: " + getYearBonus(year) + ")\n");
                outputArea.append("    - Base Weight: 10\n");
                outputArea.append("    → TOTAL WEIGHT: " + weight + " tickets\n\n");

                for (int i = 0; i < weight; i++) {
                    ticketPool.add(s);
                }
            }

            if (ticketPool.isEmpty()) {
                outputArea.append("No valid requests. No enrollments.\n\n");
                enrolledByCourse.put(course.courseSectionId, new ArrayList<>());
                continue;
            }

            outputArea.append("STEP 2: Building ticket pool...\n");
            outputArea.append("─────────────────────────────────────────────────────\n");
            outputArea.append("Total tickets in pool: " + ticketPool.size() + "\n");
            
            // Count tickets per student
            Map<String, Integer> ticketCounts = new HashMap<>();
            for (students s : ticketPool) {
                ticketCounts.put(s.studentId, ticketCounts.getOrDefault(s.studentId, 0) + 1);
            }
            
            outputArea.append("Ticket distribution:\n");
            for (Map.Entry<String, Integer> entry : ticketCounts.entrySet()) {
                students s = studentsById.get(entry.getKey());
                outputArea.append("  " + s.name + " (" + entry.getKey() + "): " + 
                        entry.getValue() + " tickets\n");
            }
            outputArea.append("\n");

            outputArea.append("STEP 3: Running random selection...\n");
            outputArea.append("─────────────────────────────────────────────────────\n");
            
            java.util.List<students> enrolled = new ArrayList<>();
            Set<String> alreadyChosen = new HashSet<>();
            int selectionRound = 1;

            while (seatsLeft > 0 && !ticketPool.isEmpty()) {
                int idx = rand.nextInt(ticketPool.size());
                students chosen = ticketPool.get(idx);

                // avoid giving the same student 2 seats in the same course
                if (!alreadyChosen.contains(chosen.studentId)) {
                    enrolled.add(chosen);
                    alreadyChosen.add(chosen.studentId);
                    seatsLeft--;
                    
                    outputArea.append("  Round " + selectionRound + ": Selected ticket #" + (idx + 1) + 
                            " → " + chosen.name + " (" + chosen.studentId + ")\n");
                    outputArea.append("    Remaining seats: " + seatsLeft + 
                            ", Remaining tickets: " + ticketPool.size() + "\n");
                    selectionRound++;
                } else {
                    outputArea.append("  Round " + selectionRound + ": Selected " + chosen.name + 
                            " but already enrolled, redrawing...\n");
                }

                // remove all tickets for chosen student
                String idToRemove = chosen.studentId;
                int removedCount = 0;
                java.util.Iterator<students> it = ticketPool.iterator();
                while (it.hasNext()) {
                    if (it.next().studentId.equals(idToRemove)) {
                        it.remove();
                        removedCount++;
                    }
                }
                
                if (removedCount > 0 && !alreadyChosen.contains(idToRemove)) {
                    outputArea.append("    Removed " + removedCount + " tickets for " + chosen.name + "\n");
                }
            }

            enrolledByCourse.put(course.courseSectionId, enrolled);
            course.currentEnrollment += enrolled.size();

            outputArea.append("\nSTEP 4: Final Results\n");
            outputArea.append("─────────────────────────────────────────────────────\n");
            if (enrolled.isEmpty()) {
                outputArea.append("No students enrolled.\n");
            } else {
                outputArea.append("Enrolled students (" + enrolled.size() + "):\n");
                for (students s : enrolled) {
                    outputArea.append("  ✓ " + s.name + " (" + s.studentId + ")\n");
                }
            }
            outputArea.append("\n");
        }

        return enrolledByCourse;
    }
    
    private int getRankBonus(int rank) {
        switch (rank) {
            case 1: return 4;
            case 2: return 3;
            case 3: return 2;
            case 4: return 1;
            default: return 0;
        }
    }
    
    private int getMajorBonus(students.MajorStatus majorStatus) {
        switch (majorStatus) {
            case CS_MAJOR: return 4;
            case CS_MINOR: return 2;
            case NON_MAJOR: return 0;
            default: return 0;
        }
    }
    
    private int getYearBonus(StudentYear year) {
        switch (year) {
            case SENIOR: return 4;
            case JUNIOR: return 3;
            case SOPHOMORE: return 2;
            case FRESHMAN: return 1;
            default: return 0;
        }
    }

    // ---------------- SAMPLE DATA ----------------

    private java.util.List<students> buildSampleStudents() {
        java.util.List<students> list = new ArrayList<>();

        // Correct constructor: students(String studentId, String name,
        //                               List<String> pastClasses, List<String> requestedClasses,
        //                               int gradYear, MajorStatus majorStatus)
        students s1 = new students("S1", "Alice",
                new ArrayList<>(Arrays.asList("CS51", "CS62")),
                new ArrayList<>(Arrays.asList("CS105", "CS140")),
                2025, students.MajorStatus.CS_MAJOR);
        
        students s2 = new students("S2", "Bob",
                new ArrayList<>(Arrays.asList("CS51")),
                new ArrayList<>(Arrays.asList("CS62", "CS105")),
                2026, students.MajorStatus.CS_MINOR);
        
        students s3 = new students("S3", "Chloe",
                new ArrayList<>(),
                new ArrayList<>(Arrays.asList("CS51", "CS62")),
                2027, students.MajorStatus.NON_MAJOR);
        
        students s4 = new students("S4", "David",
                new ArrayList<>(Arrays.asList("CS51", "CS62", "CS105")),
                new ArrayList<>(Arrays.asList("CS140")),
                2025, students.MajorStatus.CS_MAJOR);

        list.add(s1);
        list.add(s2);
        list.add(s3);
        list.add(s4);

        return list;
    }

    private java.util.List<classes> buildSampleCourses() {
        java.util.List<classes> list = new ArrayList<>();

        // Correct constructor: classes(String id, String number, int capacity, 
        //                              int currentEnrollment, double creditHours)
        classes c1 = new classes("CS62-01", "01", 2, 0, 1.0);
        classes c2 = new classes("CS105-01", "01", 2, 0, 1.0);
        classes c3 = new classes("CS140-01", "01", 1, 0, 1.0);
        classes c4 = new classes("CS51-01", "01", 3, 0, 1.0);

        list.add(c1);
        list.add(c2);
        list.add(c3);
        list.add(c4);

        return list;
    }

    private java.util.List<ClassRequest> buildSampleRequests() {
        java.util.List<ClassRequest> list = new ArrayList<>();

        // Everyone wants CS62-01, with different ranks
        list.add(new ClassRequest("S1", "CS62-01", 1));
        list.add(new ClassRequest("S2", "CS62-01", 2));
        list.add(new ClassRequest("S3", "CS62-01", 3));
        list.add(new ClassRequest("S4", "CS62-01", 1));

        // Some also want CS105-01
        list.add(new ClassRequest("S1", "CS105-01", 2));
        list.add(new ClassRequest("S2", "CS105-01", 1));
        list.add(new ClassRequest("S3", "CS105-01", 4));

        return list;
    }

    // ---------------- CSV LOADING (from Main.java) ----------------

    /**
     * Finds the CSV file in common locations.
     */
    private String findCSVFile() {
        String[] possiblePaths = {"data/student.csv", "../data/student.csv"};
        for (String path : possiblePaths) {
            if (new File(path).exists()) {
                return path;
            }
        }
        return null;
    }

    /**
     * Loads students from CSV file.
     * CSV format: student_id,name,past_classes,requested_classes,grad_year,major_status
     */
    private java.util.List<students> loadStudentsFromCSV(String filepath) {
        java.util.List<students> studentList = new ArrayList<>();
        
        try (BufferedReader br = new BufferedReader(new FileReader(filepath))) {
            String line = br.readLine(); // Skip header
            
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                
                try {
                    int firstComma = line.indexOf(',');
                    int secondComma = line.indexOf(',', firstComma + 1);
                    int firstBracket = line.indexOf('[');
                    int firstCloseBracket = line.indexOf(']');
                    int secondBracket = (firstCloseBracket != -1) ? line.indexOf('[', firstCloseBracket + 1) : -1;
                    int secondCloseBracket = (secondBracket != -1) ? line.indexOf(']', secondBracket + 1) : -1;
                    
                    if (firstComma == -1 || secondComma == -1) {
                        continue;
                    }
                    
                    String studentId = line.substring(0, firstComma).trim();
                    String name = line.substring(firstComma + 1, secondComma).trim();
                    
                    // Parse past classes
                    java.util.List<String> pastClasses = new ArrayList<>();
                    if (firstBracket != -1 && firstCloseBracket != -1) {
                        String pastClassesStr = line.substring(firstBracket + 1, firstCloseBracket).trim();
                        if (!pastClassesStr.isEmpty()) {
                            String[] courses = pastClassesStr.split(",");
                            for (String course : courses) {
                                pastClasses.add(course.trim());
                            }
                        }
                    }
                    
                    // Parse requested classes
                    java.util.List<String> requestedClasses = new ArrayList<>();
                    if (secondBracket != -1 && secondCloseBracket != -1) {
                        String requestedClassesStr = line.substring(secondBracket + 1, secondCloseBracket).trim();
                        if (!requestedClassesStr.isEmpty()) {
                            String[] courses = requestedClassesStr.split(",");
                            for (String course : courses) {
                                requestedClasses.add(course.trim());
                            }
                        }
                    }
                    
                    // Parse remaining fields (grad_year, major_status)
                    int lastBracketClose = (secondCloseBracket != -1) ? secondCloseBracket : firstCloseBracket;
                    
                    String remaining;
                    if (lastBracketClose != -1 && lastBracketClose < line.length()) {
                        int commaPos = line.indexOf(',', lastBracketClose);
                        if (commaPos != -1 && commaPos < line.length() - 1) {
                            remaining = line.substring(commaPos + 1);
                        } else {
                            if (lastBracketClose + 1 < line.length()) {
                                remaining = line.substring(lastBracketClose + 1).trim();
                                if (remaining.startsWith(",")) {
                                    remaining = remaining.substring(1).trim();
                                }
                            } else {
                                continue;
                            }
                        }
                    } else {
                        if (secondComma + 1 < line.length()) {
                            remaining = line.substring(secondComma + 1);
                        } else {
                            continue;
                        }
                    }
                    
                    if (remaining.isEmpty()) {
                        continue;
                    }
                    
                    String[] lastParts = remaining.split(",", 2);
                    
                    if (lastParts.length < 2) {
                        continue;
                    }
                    
                    int gradYear;
                    try {
                        gradYear = Integer.parseInt(lastParts[0].trim());
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    String majorStatusStr = lastParts[1].trim();
                    
                    // Convert major status string to enum
                    students.MajorStatus majorStatus;
                    if (majorStatusStr.equalsIgnoreCase("CS major")) {
                        majorStatus = students.MajorStatus.CS_MAJOR;
                    } else if (majorStatusStr.equalsIgnoreCase("CS minor")) {
                        majorStatus = students.MajorStatus.CS_MINOR;
                    } else {
                        majorStatus = students.MajorStatus.NON_MAJOR;
                    }
                    
                    students student = new students(
                            studentId, name, pastClasses, requestedClasses, gradYear, majorStatus
                    );
                    studentList.add(student);
                    
                } catch (Exception e) {
                    // Skip malformed lines
                    continue;
                }
            }
        } catch (IOException e) {
            outputArea.append("Error reading CSV file: " + e.getMessage() + "\n");
        }
        
        return studentList;
    }

    // ---------------------- MAIN: start the GUI ----------------------

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LotteryGUI gui = new LotteryGUI();
            gui.setVisible(true);
            // Run lottery from CSV automatically after GUI is shown
            gui.runLotteryFromCSV();
   });
   }
}
