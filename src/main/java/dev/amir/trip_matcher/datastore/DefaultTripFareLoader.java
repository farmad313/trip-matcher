package dev.amir.trip_matcher.datastore;

import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@Slf4j
public class DefaultTripFareLoader {
    // TODO: make the constructor private

    public static void loadDatastore(TripFareManager tripFareManager) {


        // Forward trips
        tripFareManager.addPriceRule("Stop1", "Stop2", new BigDecimal("3.25"));
        tripFareManager.addPriceRule("Stop2", "Stop3", new BigDecimal("5.50"));
        tripFareManager.addPriceRule("Stop1", "Stop3", new BigDecimal("7.30"));
        // Backward trips (symmetrical or undirected)
        tripFareManager.addPriceRule("Stop2", "Stop1", new BigDecimal("3.25"));
        tripFareManager.addPriceRule("Stop3", "Stop2", new BigDecimal("5.50"));
        tripFareManager.addPriceRule("Stop3", "Stop1", new BigDecimal("7.30"));
        // Cancelled trips (self-loop)
        tripFareManager.addPriceRule("Stop1", "Stop1", new BigDecimal("0.0"));
        tripFareManager.addPriceRule("Stop2", "Stop2", new BigDecimal("0.0"));
        tripFareManager.addPriceRule("Stop3", "Stop3", new BigDecimal("0.0"));
        // Incomplete trips (non-terminal or infinite)
        tripFareManager.addPriceRule("Stop1", "UNKNOWN", new BigDecimal("7.30"));
        tripFareManager.addPriceRule("Stop2", "UNKNOWN", new BigDecimal("5.50"));
        tripFareManager.addPriceRule("Stop3", "UNKNOWN", new BigDecimal("7.30"));


        log.info("Datastore loaded successfully");
    }
}
