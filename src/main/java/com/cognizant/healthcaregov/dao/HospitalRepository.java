package com.cognizant.healthcaregov.dao;

import com.cognizant.healthcaregov.entity.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Integer> {

    @Query("SELECT h FROM Hospital h WHERE " +
            "LOWER(h.name) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
            "LOWER(h.location) LIKE LOWER(CONCAT('%',:q,'%'))")
    List<Hospital> search(@Param("q") String query);
}
