package com.cognizant.healthcaregov.dao;

import com.cognizant.healthcaregov.entity.ComplianceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ComplianceRecordRepository extends JpaRepository<ComplianceRecord, Integer> {

    @Query("SELECT c FROM ComplianceRecord c WHERE " +
            "(:type IS NULL OR c.type = :type) AND " +
            "(:result IS NULL OR c.result = :result) AND " +
            "(:entityId IS NULL OR c.entityId = :entityId) AND " +
            "(:startDate IS NULL OR c.date >= :startDate) AND " +
            "(:endDate IS NULL OR c.date <= :endDate)")
    List<ComplianceRecord> search(
            @Param("type") String type,
            @Param("result") String result,
            @Param("entityId") Integer entityId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
