package dev.amir.trip_matcher.datastore;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TripFareManagerTest {

    private TripFareManager tripFareManager;
    private DefaultTripFareLoader defaultTripFareLoader;

    @BeforeEach
    public void setUp() {
        tripFareManager = new TripFareManager();
    }

    @Test
    void testGetPrice_CalculationForIncompleteTrips_AlwaysMaxFare() {
        //given
        defaultTripFareLoader.loadDatastore(tripFareManager);

        //when
        BigDecimal fare1 = tripFareManager.getPrice("Stop1", "Stop2");
        BigDecimal fare2 = tripFareManager.getPrice("Stop1", "Stop3");
        BigDecimal expectedMaxFare = (fare1.compareTo(fare2) > 0) ? fare1 : fare2;

        //then
        BigDecimal actualFare = tripFareManager.getPrice("Stop1", "UNKNOWN");

        assertEquals(new BigDecimal(String.valueOf(expectedMaxFare)), actualFare);
    }


    @Test
    void testGetPrice_CalculationForCancelledTrips_AlwaysZero() {
        //given
        defaultTripFareLoader.loadDatastore(tripFareManager);

        //when
        BigDecimal expectedMaxFare = BigDecimal.ZERO.setScale(1);

        //then
        BigDecimal actualFare = tripFareManager.getPrice("Stop1", "Stop1");

        assertEquals(expectedMaxFare, actualFare);
    }

    @Test
    void testGetPrice_CalculationForBackwardTrips_alwaysSymmetrical() {
        //given
        defaultTripFareLoader.loadDatastore(tripFareManager);

        //when
        BigDecimal expectedMaxFare = tripFareManager.getPrice("Stop2", "Stop1");

        //then
        BigDecimal actualFare = tripFareManager.getPrice("Stop1", "Stop2");

        assertEquals(expectedMaxFare, actualFare);
    }


    @Test
    void testGetPrice_ExistingRoute() {
        tripFareManager.addPriceRule("Stop1", "Stop2", new BigDecimal("2.50"));
        BigDecimal price = tripFareManager.getPrice("Stop1", "Stop2");
        assertEquals(new BigDecimal("2.50"), price);
    }

    @Test
    void testGetPrice_NonExistingRoute() {
        BigDecimal price = tripFareManager.getPrice("Stop1", "StopX");
        assertNull(price);
    }
}