package com.cognizant.healthcaregov.dao;

import com.cognizant.healthcaregov.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository extends JpaRepository<Resource, Integer> {

    List<Resource> findByHospitalHospitalID(Integer hospitalId);

    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM Resource r WHERE r.type = :type")
    Long sumQuantityByType(@Param("type") String type);
}
