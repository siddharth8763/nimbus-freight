package com.nimbusfreight.repo;

import com.nimbusfreight.entity.TruckAlertStateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TruckAlertStateRepository extends JpaRepository<TruckAlertStateEntity, String> {
}
