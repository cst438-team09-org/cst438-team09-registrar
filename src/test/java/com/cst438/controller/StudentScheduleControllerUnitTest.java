package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.*;
import com.cst438.service.GradebookServiceProxy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StudentScheduleControllerUnitTest {

    @Autowired
    private WebTestClient client ;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SectionRepository sectionRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate;

    @Test
    public void addCourse() throws Exception {

        // login as student and get the security token
        String adminEmail = "sam@csumb.edu";
        String password = "sam2025";

        EntityExchangeResult<LoginDTO> login_dto =  client.get().uri("/login")
                .headers(headers -> headers.setBasicAuth(adminEmail, password))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginDTO.class).returnResult();

        String jwt = login_dto.getResponseBody().jwt();
        assertNotNull(jwt);

        EntityExchangeResult<EnrollmentDTO> enrollmentResponse = client.post()
                .uri("/enrollments/sections/" + 1)
                .headers(headers -> headers.setBearerAuth(jwt))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(EnrollmentDTO.class).returnResult();

        EnrollmentDTO actualEnrollment = enrollmentResponse.getResponseBody();
        assertNotNull(actualEnrollment);
        assertEquals(1, actualEnrollment.sectionNo());
        assertEquals(2, actualEnrollment.studentId());
        // Verify that the enrollment was saved in the database
        Enrollment enrollment = enrollmentRepository.findById(actualEnrollment.enrollmentId()).orElse(null);
        assertNotNull(enrollment, "Enrollment was not created in the database");

    }
    @Test
    public void dropCourse() throws Exception {
        // Setup initial enrollment data
        User student = userRepository.findByEmail("sam@csumb.edu");
        Section section = sectionRepository.findById(1).orElse(null);
        // Create an enrollment for the test
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollmentRepository.save(enrollment);
        // Login as student and get the security token
        String adminEmail = "sam@csumb.edu";
        String password = "sam2025"; // Use the correct password

        EntityExchangeResult<LoginDTO> login_dto = client.get().uri("/login")
                .headers(headers -> headers.setBasicAuth(adminEmail, password))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(LoginDTO.class).returnResult();
        String jwt = login_dto.getResponseBody().jwt();
        assertNotNull(jwt);

        // Call endpoint to drop course
        client.delete()
                .uri("/enrollments/" + enrollment.getEnrollmentId())
                .headers(headers -> headers.setBearerAuth(jwt))
                .exchange()
                .expectStatus().isOk();
        // Verify that the enrollment was deleted from the database
        Enrollment deletedEnrollment = enrollmentRepository.findById(enrollment.getEnrollmentId()).orElse(null);
        assertNull(deletedEnrollment, "Enrollment was not deleted from the database");

    }

}
