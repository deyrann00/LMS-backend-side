package com.example.lms.service;

import com.example.lms.model.*;
import com.example.lms.repository.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TestService {

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TestResultRepository testResultRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProgressRepository progressRepository;

    public Test createTest(Test test, List<Question> questions) {
        test = testRepository.save(test);
        for (Question q : questions) {
            q.setTest(test);
        }
        questionRepository.saveAll(questions);
        return test;
    }

    public TestResult submitTest(Long userId, Long testId, Map<Long, Integer> answers) throws JsonProcessingException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        List<Question> questions = questionRepository.findByTestId(testId);
        int score = 0;
        for (Question q : questions) {
            Integer studentAnswer = answers.get(q.getId());
            if (studentAnswer != null && studentAnswer.equals(q.getCorrectOption())) {
                score++;
            }
        }

        TestResult result = new TestResult();
        result.setUser(user);
        result.setTest(test);
        result.setScore(score);
        result.setTotalQuestions(questions.size());
        result.setCompletedAt(new Timestamp(System.currentTimeMillis()));
        result.setAnswers(new com.fasterxml.jackson.databind.ObjectMapper()
                .writeValueAsString(answers));
        result.setUserTestKey(userId + "_" + testId);
        return testResultRepository.save(result);
    }

    public List<TestResult> getTestResults(Long testId) {
        return testResultRepository.findByTestId(testId);
    }

    public TestResult getStudentResult(Long userId, Long testId) {
        return testResultRepository.findByUserIdAndTestId(userId, testId)
                .orElseThrow(() -> new RuntimeException("Result not found"));
    }

    // Check if the student has completed all modules in the course
    public boolean isCourseFullyCompleted(Long userId, Long courseId) {
        Optional<Progress> progress = progressRepository.findByUserIdAndCourseId(userId, courseId);
        return progress.isPresent() && progress.get().getCompletedCourseModules().size() == progress.get().getTotalModules();
    }

    // Check if the student has completed all modules in the course before allowing the test
    public boolean canTakeTest(Long userId, Long testId) {
        // Fetch the test by its ID
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        // Fetch the course for this test
        Course course = test.getModule().getCourse();

        // Check if the student has completed all modules in the course
        Optional<Progress> progress = progressRepository.findByUserIdAndCourseId(userId, course.getId());
        if (progress.isPresent()) {
            // Check if the student has completed all modules
            if (progress.get().getCompletedCourseModules().size() == progress.get().getTotalModules()) {
                return true;
            }
        }

        // If the student has not completed all modules, they cannot take the test
        return false;
    }

    public boolean doesTestExist(Long moduleId) {
        return testRepository.existsByModuleId(moduleId);  // Call the updated repository method
    }


    public boolean isEnrolled(Long userId, Long testId) {
        // Fetch the test by its ID
        Test test = testRepository.findById(testId)
                .orElseThrow(() -> new RuntimeException("Test not found"));

        // Fetch the course for this test
        Course course = test.getModule().getCourse();

        // Check if the user is enrolled in the course
        return course.getStudents().stream()
                .anyMatch(student -> student.getUser().getId().equals(userId));
    }
}