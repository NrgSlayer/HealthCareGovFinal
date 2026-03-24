package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.AppointmentRepository;
import com.cognizant.healthcaregov.dao.PatientRepository;
import com.cognizant.healthcaregov.dao.UserRepository;
import com.cognizant.healthcaregov.dto.AppointmentBookRequest;
import com.cognizant.healthcaregov.dto.AppointmentCancelRequest;
import com.cognizant.healthcaregov.dto.AppointmentResponse;
import com.cognizant.healthcaregov.entity.Appointment;
import com.cognizant.healthcaregov.entity.Hospital;
import com.cognizant.healthcaregov.entity.Patient;
import com.cognizant.healthcaregov.entity.Schedule;
import com.cognizant.healthcaregov.entity.User;
import com.cognizant.healthcaregov.exception.BadRequestException;
import com.cognizant.healthcaregov.exception.ResourceNotFoundException;
import com.cognizant.healthcaregov.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository     patientRepository;
    private final UserRepository        userRepository;
    private final HospitalService       hospitalService;
    private final ScheduleService       scheduleService;
    private final NotificationService   notificationService;
    private final AuditLogService       auditLogService;
    private final SecurityUtils         securityUtils;


    @Transactional
    public AppointmentResponse book(AppointmentBookRequest req) {
        log.info("Booking: patientId={} doctorId={} date={} time={}",
                req.getPatientID(), req.getDoctorID(), req.getDate(), req.getTime());

        String formattedTime = req.getTime().format(TIME_FMT);
        Schedule slot = scheduleService.findSlot(req.getDoctorID(), req.getDate(), formattedTime);

        if (slot == null || !"Available".equalsIgnoreCase(slot.getStatus())) {
            throw new BadRequestException(
                    "Selected time slot is not available for this doctor.");
        }

        Patient patient = patientRepository.findById(req.getPatientID())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient not found with id: " + req.getPatientID()));
        User doctor = userRepository.findById(req.getDoctorID())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + req.getDoctorID()));
        Hospital hospital = hospitalService.findById(req.getHospitalID());

        slot.setStatus("Booked");
        scheduleService.saveSlot(slot);

        Appointment appt = new Appointment();
        appt.setPatient(patient);
        appt.setDoctor(doctor);
        appt.setHospital(hospital);
        appt.setDate(req.getDate());
        appt.setTime(req.getTime());
        appt.setStatus("Confirmed");

        Appointment saved = appointmentRepository.save(appt);
        log.info("Appointment confirmed appointmentId={}", saved.getAppointmentID());


        if (patient.getUser() != null) {
            notificationService.send(
                    patient.getUser().getUserID(),
                    saved.getAppointmentID(),
                    "Your appointment with Dr. " + doctor.getName()
                            + " on " + req.getDate() + " at " + req.getTime() + " is confirmed.",
                    "Appointment");
        }
        return toResponse(saved);
    }


    @Transactional
    public AppointmentResponse cancel(AppointmentCancelRequest req, UserDetails userDetails) {
        log.info("Cancelling appointmentId={}", req.getAppointmentID());
        Appointment appt = findById(req.getAppointmentID());

        if ("Cancelled".equalsIgnoreCase(appt.getStatus())) {
            throw new BadRequestException("Appointment is already cancelled.");
        }



        String callerEmail = userDetails.getUsername();
        User apptPatientUser = appt.getPatient().getUser();
        if (apptPatientUser == null
                || !apptPatientUser.getEmail().equalsIgnoreCase(callerEmail)) {
            log.warn("Cancellation denied: {} tried to cancel appointmentId={}",
                    callerEmail, req.getAppointmentID());
            throw new BadRequestException(
                    "Access denied: you may only cancel your own appointments.");
        }

        String formattedTime = appt.getTime().format(TIME_FMT);
        Schedule slot = scheduleService.findSlot(
                appt.getDoctor().getUserID(), appt.getDate(), formattedTime);
        if (slot != null) {
            slot.setStatus("Available");
            scheduleService.saveSlot(slot);
        }

        appt.setStatus("Cancelled");
        Appointment saved = appointmentRepository.save(appt);
        log.info("Appointment cancelled appointmentId={}", saved.getAppointmentID());


        if (appt.getPatient().getUser() != null) {
            notificationService.send(
                    appt.getPatient().getUser().getUserID(),
                    saved.getAppointmentID(),
                    "Your appointment on " + appt.getDate() + " has been cancelled.",
                    "Appointment");
        }
        return toResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<AppointmentResponse> getByDoctor(Integer doctorId) {
        log.info("Fetching appointments for doctorId={}", doctorId);
        return appointmentRepository.findByDoctorUserID(doctorId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }


    @Transactional(readOnly = true)
    public List<AppointmentResponse> getAll() {
        return appointmentRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }


    @Transactional
    public AppointmentResponse reassign(Integer appointmentId, Integer newDoctorId,
                                        Integer adminId) {
        log.info("Admin {} reassigning appointmentId={} to doctorId={}",
                adminId, appointmentId, newDoctorId);
        Appointment appt = findById(appointmentId);
        if ("Cancelled".equalsIgnoreCase(appt.getStatus())) {
            throw new BadRequestException("Cannot reassign a cancelled appointment.");
        }
        User newDoctor = userRepository.findById(newDoctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + newDoctorId));
        appt.setDoctor(newDoctor);
        Appointment saved = appointmentRepository.save(appt);

        return toResponse(saved);
    }


    @Transactional
    public AppointmentResponse checkIn(Integer appointmentId, Integer adminId) {
        log.info("Admin {} checking in appointmentId={}", adminId, appointmentId);
        Appointment appt = findById(appointmentId);
        if ("Cancelled".equalsIgnoreCase(appt.getStatus())) {
            throw new BadRequestException("Cannot check in a cancelled appointment.");
        }
        if ("Arrived".equalsIgnoreCase(appt.getStatus())) {
            throw new BadRequestException("Patient is already checked in.");
        }
        appt.setStatus("Arrived");
        Appointment saved = appointmentRepository.save(appt);

        log.info("Patient checked in appointmentId={}", appointmentId);
        return toResponse(saved);
    }


    private Appointment findById(Integer id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Appointment not found with id: " + id));
    }

    private AppointmentResponse toResponse(Appointment a) {
        return new AppointmentResponse(
                a.getAppointmentID(),
                a.getPatient().getPatientID(), a.getPatient().getName(),
                a.getDoctor().getUserID(), a.getDoctor().getName(),
                a.getHospital().getHospitalID(), a.getHospital().getName(),
                a.getDate(), a.getTime(), a.getStatus());
    }
}
