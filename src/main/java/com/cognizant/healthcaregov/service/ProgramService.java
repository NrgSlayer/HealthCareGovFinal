package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.AppointmentRepository;
import com.cognizant.healthcaregov.dao.ComplianceRecordRepository;
import com.cognizant.healthcaregov.dao.HospitalRepository;
import com.cognizant.healthcaregov.dao.ReportRepository;
import com.cognizant.healthcaregov.dao.TreatmentRepository;
import com.cognizant.healthcaregov.dto.DashboardResponse;
import com.cognizant.healthcaregov.dto.ReportRequest;
import com.cognizant.healthcaregov.dto.ReportResponse;
import com.cognizant.healthcaregov.entity.Hospital;
import com.cognizant.healthcaregov.entity.Report;
import com.cognizant.healthcaregov.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgramService {

    private static final Set<String> VALID_SCOPES =
            Set.of("Appointment", "Treatment", "Hospital", "Compliance");

    private final AppointmentRepository appointmentRepository;
    private final TreatmentRepository treatmentRepository;
    private final HospitalRepository hospitalRepository;
    private final ComplianceRecordRepository complianceRepository;
    private final ReportRepository reportRepository;
    private final HospitalService hospitalService;
    private final ResourceService resourceService;


    @Transactional(readOnly = true)
    public DashboardResponse getDashboard(LocalDate startDate, LocalDate endDate) {
        log.info("Building program dashboard startDate={} endDate={}", startDate, endDate);

        long totalAppts = appointmentRepository.count();
        long confirmed  = appointmentRepository.findAll().stream()
                .filter(a -> "Confirmed".equalsIgnoreCase(a.getStatus()))
                .filter(a -> inRange(a.getDate(), startDate, endDate)).count();
        long cancelled  = appointmentRepository.findAll().stream()
                .filter(a -> "Cancelled".equalsIgnoreCase(a.getStatus()))
                .filter(a -> inRange(a.getDate(), startDate, endDate)).count();

        long totalTreatments     = treatmentRepository.count();
        long activeTreatments    = treatmentRepository.findAll().stream()
                .filter(t -> "Active".equalsIgnoreCase(t.getStatus())).count();
        long completedTreatments = treatmentRepository.findAll().stream()
                .filter(t -> "Completed".equalsIgnoreCase(t.getStatus())).count();

        long totalHospitals = hospitalRepository.count();
        long totalCapacity  = hospitalRepository.findAll().stream()
                .mapToLong(h -> h.getCapacity() != null ? h.getCapacity() : 0L).sum();

        long totalCompliance  = complianceRepository.count();
        long passedCompliance = complianceRepository.findAll().stream()
                .filter(c -> "Pass".equalsIgnoreCase(c.getResult())).count();
        long failedCompliance = complianceRepository.findAll().stream()
                .filter(c -> "Fail".equalsIgnoreCase(c.getResult())).count();

        return new DashboardResponse(totalAppts, confirmed, cancelled,
                totalTreatments, activeTreatments, completedTreatments,
                totalHospitals, totalCapacity,
                totalCompliance, passedCompliance, failedCompliance);
    }


    @Transactional
    public ReportResponse generateReport(ReportRequest req) {
        log.info("Generating report scope={} hospitalId={}", req.getScope(), req.getHospitalId());
        if (!VALID_SCOPES.contains(req.getScope())) {
            throw new BadRequestException(
                    "Invalid scope. Allowed: Appointment, Treatment, Hospital, Compliance");
        }
        Hospital hospital = hospitalService.findById(req.getHospitalId());
        String metrics = buildMetrics(req.getScope(), req.getHospitalId(), hospital);

        Report report = new Report();
        report.setHospital(hospital);
        report.setScope(req.getScope());
        report.setMetrics(metrics);
        Report saved = reportRepository.save(report);
        log.info("Report generated id={}", saved.getReportID());

        return new ReportResponse(saved.getReportID(), hospital.getHospitalID(),
                hospital.getName(), saved.getScope(), saved.getMetrics(), saved.getGeneratedDate());
    }



    private boolean inRange(LocalDate date, LocalDate start, LocalDate end) {
        if (date == null) return true;
        if (start != null && date.isBefore(start)) return false;
        if (end   != null && date.isAfter(end))   return false;
        return true;
    }

    private String buildMetrics(String scope, Integer hospitalId, Hospital hospital) {
        return switch (scope) {
            case "Appointment" -> {
                long confirmed = appointmentRepository.findAll().stream()
                        .filter(a -> "Confirmed".equalsIgnoreCase(a.getStatus())
                                && a.getHospital().getHospitalID().equals(hospitalId)).count();
                long cancelled = appointmentRepository.findAll().stream()
                        .filter(a -> "Cancelled".equalsIgnoreCase(a.getStatus())
                                && a.getHospital().getHospitalID().equals(hospitalId)).count();
                yield String.format(
                        "{\"scope\":\"Appointment\",\"hospitalId\":%d,\"confirmed\":%d,\"cancelled\":%d}",
                        hospitalId, confirmed, cancelled);
            }
            case "Treatment" -> {
                long active    = treatmentRepository.findAll().stream()
                        .filter(t -> "Active".equalsIgnoreCase(t.getStatus())).count();
                long completed = treatmentRepository.findAll().stream()
                        .filter(t -> "Completed".equalsIgnoreCase(t.getStatus())).count();
                yield String.format(
                        "{\"scope\":\"Treatment\",\"hospitalId\":%d,\"active\":%d,\"completed\":%d}",
                        hospitalId, active, completed);
            }
            case "Hospital" -> String.format(
                    "{\"scope\":\"Hospital\",\"hospitalId\":%d,\"capacity\":%d,"
                            + "\"beds\":%d,\"equipment\":%d,\"staff\":%d}",
                    hospitalId, hospital.getCapacity(),
                    resourceService.sumByType("Beds"),
                    resourceService.sumByType("Equipment"),
                    resourceService.sumByType("Staff"));
            case "Compliance" -> {
                long passed = complianceRepository.findAll().stream()
                        .filter(c -> "Pass".equalsIgnoreCase(c.getResult())).count();
                long failed = complianceRepository.findAll().stream()
                        .filter(c -> "Fail".equalsIgnoreCase(c.getResult())).count();
                yield String.format(
                        "{\"scope\":\"Compliance\",\"hospitalId\":%d,\"passed\":%d,\"failed\":%d}",
                        hospitalId, passed, failed);
            }
            default -> "{}";
        };
    }
}
