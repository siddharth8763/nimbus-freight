package com.nimbusfreight.config;

import com.nimbusfreight.domain.CargoType;
import com.nimbusfreight.entity.Truck;
import com.nimbusfreight.entity.TruckAlertStateEntity;
import com.nimbusfreight.repo.TruckAlertStateRepository;
import com.nimbusfreight.repo.TruckRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class TruckSeeder implements ApplicationRunner {

    private final TruckRepository trucks;
    private final TruckAlertStateRepository states;

    public TruckSeeder(TruckRepository trucks, TruckAlertStateRepository states) {
        this.trucks = trucks;
        this.states = states;
    }

    @Override
    public void run(ApplicationArguments args) {
        seed("NF-01", "Aurora", CargoType.PHARMA);
        seed("NF-02", "Borealis", CargoType.PHARMA);
        seed("NF-03", "Cirrus", CargoType.GROCERY);
        seed("NF-04", "Drift", CargoType.PHARMA);
        seed("NF-05", "Ember", CargoType.GROCERY);
    }

    private void seed(String id, String name, CargoType cargoType) {
        if (!trucks.existsById(id)) {
            trucks.save(new Truck(id, name, cargoType));
        }
        if (!states.existsById(id)) {
            states.save(new TruckAlertStateEntity(id));
        }
    }
}
