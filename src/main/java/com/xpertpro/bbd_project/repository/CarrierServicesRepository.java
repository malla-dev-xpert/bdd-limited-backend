package com.xpertpro.bbd_project.repository;

import com.xpertpro.bbd_project.entity.CarrierServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarrierServicesRepository extends JpaRepository<CarrierServiceEntity, Long> {
}
