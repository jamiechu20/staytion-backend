package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.District;


public interface DistrictRepository extends JpaRepository<District, Long> {
    List<District> findByCity_Cname(String cname);	List<District> findByCityId(Long cityId);

    List<District> findByDnameContainingIgnoreCase(String dname);
    
    Optional<District> findByDname(String dname); 
}
