package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.HospitalRepository;

import com.cognizant.healthcaregov.dto.HospitalRequest;

import com.cognizant.healthcaregov.dto.HospitalResponse;
import com.cognizant.healthcaregov.entity.Hospital;
import com.cognizant.healthcaregov.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HospitalService {

    private final HospitalRepository hospitalRepository;


    @Transactional
    public HospitalResponse create(HospitalRequest req) {
        log.info("Creating hospital: {}", req.getName());
        Hospital h = new Hospital();
        h.setName(req.getName());
        h.setLocation(req.getLocation());
        h.setCapacity(req.getCapacity());
        h.setStatus(req.getStatus());
        return toResponse(hospitalRepository.save(h));
    }


    @Transactional(readOnly = true)
    public List<HospitalResponse> getAll() {
        return hospitalRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public HospitalResponse getById(Integer id) {

        return toResponse(findById(id));
    }

    @Transactional(readOnly = true)
    public List<HospitalResponse> search(String query) {
        return hospitalRepository.search(query).stream().map(this::toResponse).collect(Collectors.toList());
    }

    // HADM-003
    @Transactional
    public HospitalResponse update(Integer id, HospitalRequest req) {
        log.info("Updating hospitalId={}", id);
        Hospital h = findById(id);
        h.setName(req.getName());
        h.setLocation(req.getLocation());
        h.setCapacity(req.getCapacity());
        h.setStatus(req.getStatus());
        return toResponse(hospitalRepository.save(h));
    }

    @Transactional
    public void delete(Integer id) {
        if (!hospitalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Hospital not found with id: " + id);
        }
        hospitalRepository.deleteById(id);
        log.info("Hospital deleted id={}", id);
    }

    public Hospital findById(Integer id) {
        return hospitalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hospital not found with id: " + id));
    }

    private HospitalResponse toResponse(Hospital h) {
        return new HospitalResponse(h.getHospitalID(), h.getName(), h.getLocation(),
                h.getCapacity(), h.getStatus());
    }
}
