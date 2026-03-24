package com.cognizant.healthcaregov.service;

import com.cognizant.healthcaregov.dao.ResourceRepository;

import com.cognizant.healthcaregov.dto.ResourceRequest;

import com.cognizant.healthcaregov.dto.ResourceResponse;
import com.cognizant.healthcaregov.entity.Hospital;
import com.cognizant.healthcaregov.entity.Resource;
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
public class ResourceService {

    private final ResourceRepository resourceRepository;
    private final HospitalService hospitalService;


    @Transactional
    public ResourceResponse add(ResourceRequest req) {
        log.info("Adding resource type={} to hospitalId={}", req.getType(), req.getHospitalID());
        Hospital hospital = hospitalService.findById(req.getHospitalID());
        Resource r = new Resource();
        r.setHospital(hospital);
        r.setType(req.getType());
        r.setQuantity(req.getQuantity());
        r.setStatus(req.getStatus());
        return toResponse(resourceRepository.save(r));
    }


    @Transactional(readOnly = true)
    public List<ResourceResponse> getAll(Integer hospitalId) {
        List<Resource> list = hospitalId != null
                ? resourceRepository.findByHospitalHospitalID(hospitalId)
                : resourceRepository.findAll();
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResourceResponse getById(Integer id) {
        return toResponse(findById(id));
    }


    @Transactional
    public ResourceResponse update(Integer id, ResourceRequest req) {
        log.info("Updating resourceId={}", id);
        Resource r = findById(id);
        if (req.getHospitalID() != null) {
            r.setHospital(hospitalService.findById(req.getHospitalID()));
        }
        r.setType(req.getType());
        r.setQuantity(req.getQuantity());
        r.setStatus(req.getStatus());
        return toResponse(resourceRepository.save(r));
    }

    @Transactional
    public void delete(Integer id) {
        if (!resourceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Resource not found with id: " + id);
        }
        resourceRepository.deleteById(id);
        log.info("Resource deleted id={}", id);
    }

    public long sumByType(String type) {
        Long val = resourceRepository.sumQuantityByType(type);
        return val != null ? val : 0L;
    }

    private Resource findById(Integer id) {
        return resourceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found with id: " + id));
    }

    private ResourceResponse toResponse(Resource r) {
        return new ResourceResponse(r.getResourceID(),
                r.getHospital().getHospitalID(), r.getHospital().getName(),
                r.getType(), r.getQuantity(), r.getStatus());
    }
}
