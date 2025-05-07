package com.example.lms.controller;

import com.example.lms.dto.*;
import com.example.lms.model.*;
import com.example.lms.service.TestService;
import com.example.lms.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tests")
@CrossOrigin(origins = "http://localhost:3000")
public class TestController {

    @Autowired
    private TestService testService;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseModuleRepository courseModuleRepository;

    // Проверка наличия теста для модуля (для ModulePage.jsx)
    @GetMapping
    public ResponseEntity<?> getTestByModuleId(@RequestParam Long moduleId) {
        try {
            Optional<Test> test = testRepository.findAll().stream()
                    .filter(t -> t.getModule().getId().equals(moduleId))
                    .findFirst();
            if (test.isPresent()) {
                return ResponseEntity.ok(test.get());
            }
            return ResponseEntity.ok(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    // TEACHER: Создать тест
    @PostMapping
    public ResponseEntity<?> createTest(
            @RequestHeader("User-Id") Long userId,
            @Valid @RequestBody TestCreationDTO testDTO) {
        try {
            // Проверка роли пользователя
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!"TEACHER".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("message", "Access denied: Only teachers can create tests"));
            }

            // Проверка, что модуль существует и принадлежит курсу преподавателя
            CourseModule module = courseModuleRepository.findById(testDTO.getCourseModuleId())
                    .orElseThrow(() -> new RuntimeException("Module not found"));
            Course course = module.getCourse();
            if (course.getTeacher() == null || !course.getTeacher().getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("message", "Access denied: You do not own this course"));
            }

            // Проверка, нет ли уже теста для этого модуля
            if (testRepository.existsByModuleId(testDTO.getCourseModuleId())) {
                return ResponseEntity.badRequest().body(Map.of("message", "A test already exists for this module"));
            }

            // Создание теста
            Test test = new Test();
            test.setTitle(testDTO.getTitle());
            test.setModule(module);

            // Создание вопросов с валидацией
            List<Question> questions = testDTO.getQuestions().stream().map(qDTO -> {
                Question q = new Question();
                q.setText(qDTO.getText());
                q.setOption1(qDTO.getOption1());
                q.setOption2(qDTO.getOption2());
                q.setOption3(qDTO.getOption3());
                q.setOption4(qDTO.getOption4());
                q.setCorrectOption(qDTO.getCorrectOption());
                return q;
            }).collect(Collectors.toList());

            test = testService.createTest(test, questions);
            return ResponseEntity.ok(test);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error creating test: " + e.getMessage()));
        }
    }

    // STUDENT: Получить тест
    @GetMapping("/{testId}")
    public ResponseEntity<?> getTest(
            @RequestHeader("User-Id") Long userId,
            @PathVariable Long testId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
            }

            // Fetch the test
            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new RuntimeException("Test not found"));

            // Fetch the course for the test
            Course course = test.getModule().getCourse();

            // Check if the student has completed all modules in the course
            boolean isCourseCompleted = testService.isCourseFullyCompleted(userId, course.getId());

            // If the course is not completed, deny access to the test
            if (!isCourseCompleted) {
                return ResponseEntity.status(403).body(Map.of("message", "You must complete all modules before taking the test."));
            }

            // If the course is completed, fetch the test questions
            List<Question> questions = questionRepository.findByTestId(testId);
            List<QuestionDTO> questionDTOs = questions.stream().map(q -> {
                QuestionDTO dto = new QuestionDTO();
                dto.setId(q.getId());
                dto.setText(q.getText());
                dto.setOptions(Arrays.asList(q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4()));
                return dto;
            }).collect(Collectors.toList());

            TestDTO testDTO = new TestDTO();
            testDTO.setId(test.getId());
            testDTO.setTitle(test.getTitle());
            testDTO.setModule(test.getModule());
            testDTO.setQuestions(questionDTOs);

            return ResponseEntity.ok(testDTO);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    // STUDENT: Отправить ответы
    @PostMapping("/{testId}/submit")
    public ResponseEntity<?> submitTest(
            @RequestHeader("User-Id") Long userId,
            @PathVariable Long testId,
            @RequestBody TestSubmissionDTO submission) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!"STUDENT".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
            }

            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new RuntimeException("Test not found"));

            if (!testService.isEnrolled(userId, testId)) {
                return ResponseEntity.status(403).body(Map.of("message", "Access denied: Not enrolled"));
            }

            if (!testService.canTakeTest(userId, testId)) {
                return ResponseEntity.status(403).body(Map.of("message", "You must complete all modules before taking the test."));
            }

            TestResult result = testService.submitTest(userId, testId, submission.getAnswers());

            TestResultDTO resultDTO = new TestResultDTO();
            resultDTO.setScore(result.getScore());
            resultDTO.setTotalQuestions(result.getTotalQuestions());
            resultDTO.setStudentAnswers(new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(result.getAnswers(), Map.class));

            List<Question> questions = questionRepository.findByTestId(testId);
            Map<Long, Integer> correctAnswers = questions.stream()
                    .collect(Collectors.toMap(Question::getId, Question::getCorrectOption));
            Map<Long, String> options = questions.stream()
                    .collect(Collectors.toMap(
                            q -> q.getId(),
                            q -> List.of(q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4())
                                    .get(q.getCorrectOption())
                    ));
            resultDTO.setCorrectAnswers(correctAnswers);
            resultDTO.setOptions(options);

            return ResponseEntity.ok(resultDTO);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    // TEACHER: Просмотреть результаты
    @GetMapping("/{testId}/results")
    public ResponseEntity<?> getTestResults(
            @RequestHeader("User-Id") Long userId,
            @PathVariable Long testId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            if (!"TEACHER".equalsIgnoreCase(user.getRole())) {
                return ResponseEntity.status(403).body(Map.of("message", "Access denied"));
            }

            Test test = testRepository.findById(testId)
                    .orElseThrow(() -> new RuntimeException("Test not found"));
            CourseModule courseMoodule = test.getModule();
            Course course = courseMoodule.getCourse();
            if (course.getTeacher() == null || !course.getTeacher().getUser().getId().equals(userId)) {
                return ResponseEntity.status(403).body(Map.of("message", "Access denied: You do not own this course"));
            }

            List<TestResult> results = testService.getTestResults(testId);
            List<TestResultSummaryDTO> summaries = results.stream().map(r -> {
                TestResultSummaryDTO dto = new TestResultSummaryDTO();
                dto.setStudentName(r.getUser().getName());
                dto.setScore(r.getScore());
                dto.setTotalQuestions(r.getTotalQuestions());
                dto.setCompletedAt(r.getCompletedAt());
                return dto;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(summaries);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error: " + e.getMessage()));
        }
    }

    // STUDENT: Получить свой результат
    @GetMapping("/{testId}/result")
    public ResponseEntity<?> getStudentResult(
            @RequestHeader("User-Id") Long userId,
            @PathVariable Long testId) {
        try {
            TestResult result = testService.getStudentResult(userId, testId);

            List<Question> questions = questionRepository.findByTestId(testId);
            Map<Long, Integer> correctAnswers = questions.stream()
                    .collect(Collectors.toMap(Question::getId, Question::getCorrectOption));
            Map<Long, String> options = questions.stream()
                    .collect(Collectors.toMap(
                            q -> q.getId(),
                            q -> List.of(q.getOption1(), q.getOption2(), q.getOption3(), q.getOption4())
                                    .get(q.getCorrectOption())
                    ));
            Map<Long, Integer> studentAnswers = new com.fasterxml.jackson.databind.ObjectMapper()
                    .readValue(result.getAnswers(), Map.class);

            TestResultDTO dto = new TestResultDTO();
            dto.setScore(result.getScore());
            dto.setTotalQuestions(result.getTotalQuestions());
            dto.setCorrectAnswers(correctAnswers);
            dto.setStudentAnswers(studentAnswers);
            dto.setOptions(options);

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Error: " + e.getMessage()));
        }
    }
}