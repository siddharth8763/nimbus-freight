package com.nimbusfreight.web;

public class TruckNotFoundException extends RuntimeException {

    public TruckNotFoundException(String truckId) {
        super("Unknown truck: " + truckId);
    }
}
