package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
public class StudentController {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    public StudentController(
            EnrollmentRepository enrollmentRepository,
            UserRepository userRepository
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
    }

    // retrieve schedule for student for a term
    @GetMapping("/enrollments")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getSchedule(
            @RequestParam("year") int year,
            @RequestParam("semester") String semester,
            Principal principal) {

        var user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User  not found");
        }

        // use the EnrollmentController findByYearAndSemesterOrderByCourseId
        // method to retrieve the enrollments given the year, semester and id
        // of the logged in student.
        List<Enrollment> enrollments = enrollmentRepository.findByYearAndSemesterOrderByCourseId(year, semester, user.getId());

        // Convert the list of Enrollment to EnrollmentDTO
        List<EnrollmentDTO> enrollmentDTOs = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            Section section = enrollment.getSection();
            Course course = section.getCourse();
            Term term = section.getTerm();

            enrollmentDTOs.add(new EnrollmentDTO(
                    enrollment.getEnrollmentId(),
                    enrollment.getGrade(),
                    enrollment.getStudent().getId(),
                    enrollment.getStudent().getName(),
                    enrollment.getStudent().getEmail(),
                    course.getCourseId(),
                    course.getTitle(),
                    section.getSectionId(),
                    section.getSectionNo(),
                    section.getBuilding(),
                    section.getRoom(),
                    section.getTimes(),
                    course.getCredits(),
                    term.getYear(),
                    term.getSemester()
            ));
        }
        // Return a list of EnrollmentDTO
        return enrollmentDTOs;
    }

    // return transcript for student
    @GetMapping("/transcripts")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public List<EnrollmentDTO> getTranscript(Principal principal) {

        var user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User  not found");
        }

        // use the EnrollmentController findEnrollmentsByStudentIdOrderByTermId
        // method to retrieve the enrollments given the id
        // of the logged in student.
        List<Enrollment> enrollments = enrollmentRepository.findEnrollmentsByStudentIdOrderByTermId(user.getId());

        // Return a list of EnrollmentDTO.
        List<EnrollmentDTO> enrollmentDTOs = new ArrayList<>();
        for (Enrollment enrollment : enrollments) {
            Section section = enrollment.getSection();
            Course course = section.getCourse();
            Term term = section.getTerm();

            enrollmentDTOs.add(new EnrollmentDTO(
                    enrollment.getEnrollmentId(),
                    enrollment.getGrade(),
                    enrollment.getStudent().getId(),
                    enrollment.getStudent().getName(),
                    enrollment.getStudent().getEmail(),
                    course.getCourseId(),
                    course.getTitle(),
                    section.getSectionId(),
                    section.getSectionNo(),
                    section.getBuilding(),
                    section.getRoom(),
                    section.getTimes(),
                    course.getCredits(),
                    term.getYear(),
                    term.getSemester()
            ));
        }
        // Return a list of EnrollmentDTO
        return enrollmentDTOs;
    }
}