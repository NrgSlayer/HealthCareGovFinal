package com.cognizant.healthcaregov.controller;

import com.cognizant.healthcaregov.dto.ResourceRequest;
import com.cognizant.healthcaregov.dto.ResourceResponse;
import com.cognizant.healthcaregov.service.ResourceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;


    @PostMapping
    public ResponseEntity<ResourceResponse> add(@Valid @RequestBody ResourceRequest req) {
        log.info("POST /api/resources");
        return new ResponseEntity<>(resourceService.add(req), HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<ResourceResponse>> getAll(
            @RequestParam(required = false) Integer hospitalId) {
        return ResponseEntity.ok(resourceService.getAll(hospitalId));
    }


    @GetMapping("/{id}")
    public ResponseEntity<ResourceResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(resourceService.getById(id));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ResourceResponse> update(
            @PathVariable Integer id, @Valid @RequestBody ResourceRequest req) {
        return ResponseEntity.ok(resourceService.update(id, req));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        resourceService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
