package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.ScheduleRepository;
import com.cognizant.healthcaregov.dao.UserRepository;
import com.cognizant.healthcaregov.dto.ScheduleRequest;
import com.cognizant.healthcaregov.dto.ScheduleResponse;
import com.cognizant.healthcaregov.entity.Hospital;
import com.cognizant.healthcaregov.entity.Schedule;
import com.cognizant.healthcaregov.entity.User;
import com.cognizant.healthcaregov.exception.BadRequestException;
import com.cognizant.healthcaregov.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final HospitalService hospitalService;


    @Transactional
    public ScheduleResponse create(ScheduleRequest req) {
        log.info("Creating schedule slot for doctorId={} date={} slot={}",
                req.getDoctorId(), req.getAvailableDate(), req.getTimeSlot());

        LocalDate date = LocalDate.parse(req.getAvailableDate());


        if (scheduleRepository.findByDoctorUserIDAndAvailableDateAndTimeSlot(
                req.getDoctorId(), date, req.getTimeSlot()).isPresent()) {
            throw new BadRequestException(
                    "A slot already exists for this doctor on " + req.getAvailableDate()
                            + " at " + req.getTimeSlot() + ".");
        }

        User doctor = findDoctor(req.getDoctorId());
        Hospital hospital = hospitalService.findById(req.getHospitalId());

        Schedule s = new Schedule();
        s.setDoctor(doctor);
        s.setHospital(hospital);
        s.setAvailableDate(date);
        s.setTimeSlot(req.getTimeSlot());
        s.setStatus("Available");

        Schedule saved = scheduleRepository.save(s);
        log.info("Schedule slot created id={}", saved.getScheduleID());
        return toResponse(saved);
    }


    @Transactional
    public ScheduleResponse update(Integer scheduleId, ScheduleRequest req) {
        log.info("Updating scheduleId={}", scheduleId);
        Schedule s = findById(scheduleId);

        if ("Booked".equalsIgnoreCase(s.getStatus())) {
            throw new BadRequestException("Cannot edit a slot that is already Booked.");
        }

        LocalDate newDate = LocalDate.parse(req.getAvailableDate());
        String newSlot    = req.getTimeSlot();


        boolean dateChanged = !newDate.equals(s.getAvailableDate());
        boolean slotChanged = !newSlot.equals(s.getTimeSlot());
        if (dateChanged || slotChanged) {
            Optional<Schedule> conflict = scheduleRepository
                    .findByDoctorUserIDAndAvailableDateAndTimeSlot(
                            s.getDoctor().getUserID(), newDate, newSlot);
            if (conflict.isPresent() && !conflict.get().getScheduleID().equals(scheduleId)) {
                throw new BadRequestException(
                        "A slot already exists for this doctor on " + req.getAvailableDate()
                                + " at " + newSlot + ".");
            }
        }

        s.setAvailableDate(newDate);
        s.setTimeSlot(newSlot);
        Schedule saved = scheduleRepository.save(s);
        log.info("Schedule slot updated id={}", saved.getScheduleID());
        return toResponse(saved);
    }


    @Transactional(readOnly = true)
    public List<ScheduleResponse> getByDoctor(Integer doctorId) {
        log.info("Fetching slots for doctorId={}", doctorId);
        return scheduleRepository.findByDoctorUserID(doctorId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }


    public Schedule findSlot(Integer doctorId, LocalDate date, String timeSlot) {
        return scheduleRepository
                .findByDoctorUserIDAndAvailableDateAndTimeSlot(doctorId, date, timeSlot)
                .orElse(null);
    }

    public void saveSlot(Schedule slot) {
        scheduleRepository.save(slot);
    }


    private Schedule findById(Integer id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Schedule not found with id: " + id));
    }

    private User findDoctor(Integer doctorId) {
        return userRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor not found with id: " + doctorId));
    }

    private ScheduleResponse toResponse(Schedule s) {
        return new ScheduleResponse(
                s.getScheduleID(),
                s.getDoctor().getUserID(),  s.getDoctor().getName(),
                s.getHospital().getHospitalID(), s.getHospital().getName(),
                s.getAvailableDate(), s.getTimeSlot(), s.getStatus());
    }
}
