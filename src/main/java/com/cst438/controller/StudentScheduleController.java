package com.cst438.controller;

import com.cst438.domain.*;
import com.cst438.dto.EnrollmentDTO;
import com.cst438.dto.SectionDTO;
import com.cst438.service.GradebookServiceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
public class StudentScheduleController {

    private final EnrollmentRepository enrollmentRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final GradebookServiceProxy gradebook;

    public StudentScheduleController(
            EnrollmentRepository enrollmentRepository,
            SectionRepository sectionRepository,
            UserRepository userRepository,
            GradebookServiceProxy gradebook
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.gradebook = gradebook;
    }


    @PostMapping("/enrollments/sections/{sectionNo}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public EnrollmentDTO addCourse(
            @PathVariable int sectionNo,
            Principal principal ) throws Exception  {

        String email = principal.getName();
        User student = userRepository.findByEmail(email);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }

        // Find the section by sectionNo
        Section section = sectionRepository.findById(sectionNo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found"));

        //Check that the student is not already enrolled in the section
        Enrollment existingEnrollment = enrollmentRepository.findEnrollmentBySectionNoAndStudentId(sectionNo, student.getId());
        if (existingEnrollment != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Already enrolled in this section");
        }

        // Check that teh current date is not before addDate, not after addDeadline
        // of the section's term
        Date currentDate = new Date();
        if (currentDate.before(section.getTerm().getAddDate()) || currentDate.after(section.getTerm().getAddDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enrollment period is not valid");
        }

        // Create and save the enrollment
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setSection(section);
        enrollmentRepository.save(enrollment);

        // Return the EnrollmentDTO with the id of the Enrollment and other fields
        EnrollmentDTO result =  new EnrollmentDTO(
                enrollment.getEnrollmentId(), // enrollmentId
                null,               // grade (initially null)
                student.getId(),    // studentId
                student.getName(),  // name
                student.getEmail(), // email
                section.getCourse().getCourseId(), // courseId
                section.getCourse().getTitle(), // title
                section.getSectionId(), // sectionId
                section.getSectionNo(), // sectionNo
                section.getBuilding(), // building
                section.getRoom(),     // room
                section.getTimes(),    // times
                section.getCourse().getCredits(), // credits
                section.getTerm().getYear(), // year
                section.getTerm().getSemester() // semester
        );

        // create and save an EnrollmentEntity
        gradebook.sendMessage("addEnrollment", result);
        return result;
    }

    // student drops a course
    @DeleteMapping("/enrollments/{enrollmentId}")
    @PreAuthorize("hasAuthority('SCOPE_ROLE_STUDENT')")
    public void dropCourse(@PathVariable("enrollmentId") int enrollmentId, Principal principal) throws Exception {

        // check that enrollment belongs to the logged in student
        // and that today is not after the dropDeadLine for the term.

        // Get the current student
        String email = principal.getName();
        User student = userRepository.findByEmail(email);
        if (student == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found");
        }
        // Find the enrollment
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Enrollment not found"));
        // Check that the enrollment belongs to the logged-in student
        if (enrollment.getStudent().getId() != student.getId()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You do not have permission to drop this course");
        }

        Date currentDate = new Date();
        if (currentDate.after(enrollment.getSection().getTerm().getDropDeadline())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Drop deadline has passed");
        }
        // Delete the enrollment
        gradebook.sendMessage("deleteEnrollment", enrollment.getEnrollmentId());
        enrollmentRepository.delete(enrollment);
    }

}
