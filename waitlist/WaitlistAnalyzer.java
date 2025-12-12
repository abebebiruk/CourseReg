import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyzes lottery results and generates waitlist reasons based on demographics
 * of enrolled students and comparison with waitlisted students.
 */
public class WaitlistAnalyzer {

    /**
     * Analyzes lottery results and generates waitlist results for all requests.
     * 
     * @param enrolledByCourse Map of courseId -> list of enrolled students
     * @param allRequests All class requests (including enrolled and waitlisted)
     * @param studentList All students
     * @param courseList All courses
     * @param requestWeights Map of (studentId, courseId) -> weight for each request
     * @param currentYear Current academic year
     * @return Map of (studentId, courseId) -> WaitlistResult
     */
    public Map<String, WaitlistResult> analyzeWaitlist(
            Map<String, List<students>> enrolledByCourse,
            List<ClassRequest> allRequests,
            List<students> studentList,
            List<classes> courseList,
            Map<String, Integer> requestWeights,
            int currentYear) {

        Map<String, WaitlistResult> results = new HashMap<>();
        Map<String, students> studentsById = new HashMap<>();
        Map<String, classes> coursesById = new HashMap<>();
        Map<String, ClassRequest> requestsByKey = new HashMap<>();

        // Build lookup maps
        for (students s : studentList) {
            studentsById.put(s.studentId, s);
        }
        for (classes c : courseList) {
            coursesById.put(c.courseSectionId, c);
        }
        for (ClassRequest req : allRequests) {
            String key = req.studentId + ":" + req.courseId;
            requestsByKey.put(key, req);
        }

        // Process each request
        for (ClassRequest req : allRequests) {
            String key = req.studentId + ":" + req.courseId;
            students student = studentsById.get(req.studentId);
            classes course = coursesById.get(req.courseId);

            if (student == null || course == null) {
                continue;
            }

            // Check if student is enrolled
            List<students> enrolled = enrolledByCourse.getOrDefault(req.courseId, new ArrayList<>());
            boolean isEnrolled = enrolled.stream()
                    .anyMatch(s -> s.studentId.equals(req.studentId));

            // Get student's weight from lottery results
            // Weight 0 means prerequisites not met (handled by LotteryWeightCalculator)
            int studentWeight = requestWeights.getOrDefault(key, 0);
            
            if (isEnrolled) {
                results.put(key, WaitlistResult.enrolled(req.courseId, req.studentId, studentWeight));
                continue;
            }

            // If weight is 0, prerequisites are not met (lottery engine already checked)
            // Get missing prerequisites using PrerequisiteValidationResult
            if (studentWeight == 0) {
                PrerequisiteValidationResult validationResult = 
                    LotteryWeightCalculator.validatePrerequisites(student, course);
                if (!validationResult.isEligible()) {
                    String missingPrereqs = String.join(", ", validationResult.getMissingPrerequisites());
                    results.put(key, WaitlistResult.rejected(req.courseId, req.studentId, missingPrereqs));
                    continue;
                } else {
                    // Edge case: weight is 0 but prerequisites are met (shouldn't happen, but handle gracefully)
                    results.put(key, WaitlistResult.rejected(req.courseId, req.studentId, 
                        "Prerequisites not met (unable to determine specific missing courses)"));
                    continue;
                }
            }

            // Student is waitlisted - generate reason based on demographics
            WaitlistResult.ClassDemographics demographics = 
                    calculateDemographics(enrolled, allRequests, studentsById, currentYear, 
                                         req.courseId, requestWeights);
            String reason = generateWaitlistReason(student, req, course, enrolled, 
                                                   demographics, studentWeight, currentYear);
            results.put(key, WaitlistResult.waitlisted(req.courseId, req.studentId, reason, 
                                                      studentWeight, demographics));
        }

        return results;
    }

    /**
     * Generates a detailed waitlist reason by comparing the student with enrolled demographics.
     */
    private String generateWaitlistReason(students student, ClassRequest request, classes course,
                                         List<students> enrolled, 
                                         WaitlistResult.ClassDemographics demographics,
                                         int studentWeight,
                                         int currentYear) {
        List<String> reasons = new ArrayList<>();

        // Get student's characteristics
        StudentYear studentYear = LotteryWeightCalculator.getStudentYear(student.gradYear, currentYear);
        
        // Compare with enrolled students
        if (demographics.totalEnrolled == 0) {
            return "Course is full. No seats available.";
        }
        
        // Start with lottery weight information
        StringBuilder sb = new StringBuilder();
        sb.append("Your lottery weight: ").append(studentWeight);
        
        // Explain weight components
        sb.append(" (Base: 10");
        // Preference rank contribution
        int rankBonus = 0;
        switch (request.preferenceRank) {
            case 1: rankBonus = 4; break;
            case 2: rankBonus = 3; break;
            case 3: rankBonus = 2; break;
            case 4: rankBonus = 1; break;
        }
        if (rankBonus > 0) {
            sb.append(" + Preference Rank ").append(request.preferenceRank).append(": +").append(rankBonus);
        }
        
        // Major status contribution
        int majorBonus = 0;
        if (student.majorStatus == students.MajorStatus.CS_MAJOR) {
            majorBonus = 4;
            sb.append(" + CS Major: +").append(majorBonus);
        } else if (student.majorStatus == students.MajorStatus.CS_MINOR) {
            majorBonus = 2;
            sb.append(" + CS Minor: +").append(majorBonus);
        }
        
        // Year contribution
        int yearBonus = 0;
        switch (studentYear) {
            case SENIOR: yearBonus = 4; sb.append(" + Senior: +").append(yearBonus); break;
            case JUNIOR: yearBonus = 3; sb.append(" + Junior: +").append(yearBonus); break;
            case SOPHOMORE: yearBonus = 2; sb.append(" + Sophomore: +").append(yearBonus); break;
            case FRESHMAN: yearBonus = 1; sb.append(" + Freshman: +").append(yearBonus); break;
        }
        sb.append(")");
        
        // Compare to average weight
        if (demographics.avgWeight > 0) {
            sb.append(". Average weight of enrolled students: ").append(String.format("%.2f", demographics.avgWeight));
            if (studentWeight < demographics.avgWeight) {
                double difference = demographics.avgWeight - studentWeight;
                sb.append(" (you were ").append(String.format("%.2f", difference))
                  .append(" points below average)");
                reasons.add("Your lottery weight was below the average of enrolled students");
            } else if (studentWeight > demographics.avgWeight) {
                sb.append(" (you were ").append(String.format("%.2f", studentWeight - demographics.avgWeight))
                  .append(" points above average, but course was full)");
            }
        }
        sb.append(". ");

        // Check graduation year factor
        if (studentYear == StudentYear.FRESHMAN && demographics.freshmen == 0) {
            reasons.add("All enrolled students are upperclassmen (Sophomores, Juniors, or Seniors)");
        } else if (studentYear == StudentYear.SOPHOMORE && 
                   demographics.seniors + demographics.juniors > demographics.sophomores) {
            reasons.add("Priority given to upperclassmen (Juniors and Seniors)");
        } else if (studentYear == StudentYear.JUNIOR && 
                   demographics.seniors > demographics.juniors) {
            reasons.add("Priority given to Seniors");
        }

        // Check major status factor
        if (student.majorStatus == students.MajorStatus.NON_MAJOR && 
            demographics.csMajors + demographics.csMinors > demographics.nonMajors) {
            reasons.add("Priority given to CS Majors and Minors");
        } else if (student.majorStatus == students.MajorStatus.CS_MINOR && 
                   demographics.csMajors > demographics.csMinors) {
            reasons.add("Priority given to CS Majors");
        }

        // Check preference rank factor
        if (request.preferenceRank > 1) {
            int higherRankCount = 0;
            if (request.preferenceRank >= 2) higherRankCount += demographics.rank1Preferences;
            if (request.preferenceRank >= 3) higherRankCount += demographics.rank2Preferences;
            if (request.preferenceRank == 4) higherRankCount += demographics.rank3Preferences;
            
            if (higherRankCount > 0) {
                reasons.add("Students with higher preference ranks (more preferred) were prioritized");
            }
        }

        // Capacity issue
        int seatsLeft = course.capacity - course.currentEnrollment;
        if (seatsLeft <= 0) {
            reasons.add("Course is at capacity (" + course.capacity + " seats)");
        }

        // Build final reason message
        if (reasons.isEmpty()) {
            sb.append("Waitlisted due to: Course is full. Limited seats available and lottery selection favored other students.");
        } else {
            sb.append("Waitlisted due to: ");
            sb.append(String.join("; ", reasons));
        }
        
        sb.append(". Class demographics: ").append(demographics.totalEnrolled)
          .append(" students enrolled (");
        sb.append(demographics.seniors).append(" Seniors, ")
          .append(demographics.juniors).append(" Juniors, ")
          .append(demographics.sophomores).append(" Sophomores, ")
          .append(demographics.freshmen).append(" Freshmen).");

        return sb.toString();
    }

    /**
     * Calculates demographic information about enrolled students for a specific course.
     */
    private WaitlistResult.ClassDemographics calculateDemographics(
            List<students> enrolled,
            List<ClassRequest> allRequests,
            Map<String, students> studentsById,
            int currentYear,
            String courseId,
            Map<String, Integer> requestWeights) {

        int seniors = 0, juniors = 0, sophomores = 0, freshmen = 0;
        int csMajors = 0, csMinors = 0, nonMajors = 0;
        int rank1 = 0, rank2 = 0, rank3 = 0, rank4 = 0;
        double totalWeight = 0.0;
        int weightCount = 0;

        // Create map of requests by student and course
        Map<String, ClassRequest> requestsByStudentCourse = new HashMap<>();
        for (ClassRequest req : allRequests) {
            String key = req.studentId + ":" + req.courseId;
            requestsByStudentCourse.put(key, req);
        }

        for (students s : enrolled) {
            // Count by year
            StudentYear year = LotteryWeightCalculator.getStudentYear(s.gradYear, currentYear);
            switch (year) {
                case SENIOR: seniors++; break;
                case JUNIOR: juniors++; break;
                case SOPHOMORE: sophomores++; break;
                case FRESHMAN: freshmen++; break;
            }

            // Count by major
            students.MajorStatus major = (s.majorStatus == null) ? 
                    students.MajorStatus.NON_MAJOR : s.majorStatus;
            switch (major) {
                case CS_MAJOR: csMajors++; break;
                case CS_MINOR: csMinors++; break;
                case NON_MAJOR: nonMajors++; break;
            }

            // Count by preference rank and weight (if courseId is provided)
            if (courseId != null) {
                String key = s.studentId + ":" + courseId;
                ClassRequest req = requestsByStudentCourse.get(key);
                if (req != null) {
                    switch (req.preferenceRank) {
                        case 1: rank1++; break;
                        case 2: rank2++; break;
                        case 3: rank3++; break;
                        case 4: rank4++; break;
                    }
                    
                    // Track weight if available
                    if (requestWeights != null) {
                        Integer weight = requestWeights.get(key);
                        if (weight != null && weight > 0) {
                            totalWeight += weight;
                            weightCount++;
                        }
                    }
                }
            }
        }

        double avgWeight = weightCount > 0 ? totalWeight / weightCount : 0.0;

        return new WaitlistResult.ClassDemographics(
                enrolled.size(), seniors, juniors, sophomores, freshmen,
                csMajors, csMinors, nonMajors,
                rank1, rank2, rank3, rank4, avgWeight);
    }

}