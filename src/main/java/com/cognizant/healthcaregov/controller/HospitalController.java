package com.cognizant.healthcaregov.controller;




import com.cognizant.healthcaregov.dto.HospitalRequest;

import com.cognizant.healthcaregov.dto.HospitalResponse;
import com.cognizant.healthcaregov.service.HospitalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @PostMapping
    public ResponseEntity<HospitalResponse> create(@Valid @RequestBody HospitalRequest req) {
        log.info("POST /api/hospitals");
        return new ResponseEntity<>(hospitalService.create(req), HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<HospitalResponse>> getAll() {
        return ResponseEntity.ok(hospitalService.getAll());
    }


    @GetMapping("/{id}")
    public ResponseEntity<HospitalResponse> getById(@PathVariable Integer id) {
        return ResponseEntity.ok(hospitalService.getById(id));
    }


    @GetMapping("/search")
    public ResponseEntity<List<HospitalResponse>> search(@RequestParam String query) {
        return ResponseEntity.ok(hospitalService.search(query));
    }


    @PutMapping("/{id}")
    public ResponseEntity<HospitalResponse> update(
            @PathVariable Integer id, @Valid @RequestBody HospitalRequest req) {
        return ResponseEntity.ok(hospitalService.update(id, req));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        hospitalService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
