package com.nimbusfreight.repo;

import com.nimbusfreight.entity.Truck;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TruckRepository extends JpaRepository<Truck, String> {
}
