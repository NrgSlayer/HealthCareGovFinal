package com.cognizant.healthcaregov.dao;

import com.cognizant.healthcaregov.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Integer> {

    Optional<Schedule> findByDoctorUserIDAndAvailableDateAndTimeSlot(
            Integer doctorId, LocalDate date, String timeSlot);

    List<Schedule> findByDoctorUserID(Integer doctorId);
}
