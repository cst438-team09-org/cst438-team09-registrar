package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentControllerUnitTest {

    @Autowired
    private WebTestClient client;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    public void StudentScheduleWithEnrollmentTest() throws Exception {
        // 1. Login as student
        String jwt = client.get().uri("/login")
                .headers(headers -> headers.setBasicAuth("sam@csumb.edu", "sam2025"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginDTO.class)
                .returnResult()
                .getResponseBody()
                .jwt();

        assertNotNull(jwt);

        // 2. Enroll student in a class
        Section existingSection = sectionRepository.findById(1).orElseThrow();

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(userRepository.findByEmail("sam@csumb.edu"));
        enrollment.setSection(existingSection);
        enrollment.setGrade("A"); // Assign grade
        enrollmentRepository.save(enrollment);

        // 3. Retrieve schedule and verify enrollment
        EntityExchangeResult<List<EnrollmentDTO>> result = client.get()
                .uri("/enrollments?year=2025&semester=Fall")
                .headers(h -> h.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnrollmentDTO.class)
                .returnResult();

        List<EnrollmentDTO> enrollments = result.getResponseBody();
        assertNotNull(enrollments);
        assertFalse(enrollments.isEmpty());
        assertEquals("A", enrollments.get(0).grade());
        assertEquals("cst489", enrollments.get(0).courseId()); // From data.sql
    }

    @Test
    public void StudentTranscriptWithGradeTest() throws Exception {
        // 1. Login
        String jwt = client.get().uri("/login")
                .headers(headers -> headers.setBasicAuth("sam@csumb.edu", "sam2025"))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginDTO.class)
                .returnResult()
                .getResponseBody()
                .jwt();

        // 2. Enroll student in the existing section and assign a grade
        Section section = sectionRepository.findById(1).orElseThrow(() ->
                new RuntimeException("Section not found"));
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(userRepository.findByEmail("sam@csumb.edu"));
        enrollment.setSection(section);
        enrollment.setGrade("A"); // Assign a grade
        enrollmentRepository.save(enrollment);

        // 2. Verify transcript shows graded course
        EntityExchangeResult<List<EnrollmentDTO>> result = client.get()
                .uri("/transcripts")
                .headers(h -> h.setBearerAuth(jwt))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(EnrollmentDTO.class)
                .returnResult();

        List<EnrollmentDTO> transcript = result.getResponseBody();
        assertNotNull(transcript);

        // Check if the enrollment with the expected course ID and grade exists
        boolean hasGradedCourse = transcript.stream().anyMatch(e ->
                "cst489".equals(e.courseId()) && e.grade() != null);

        assertTrue(hasGradedCourse, "Transcript should contain the graded course with ID 'cst489'");
    }
}
