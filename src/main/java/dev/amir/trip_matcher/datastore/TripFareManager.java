package dev.amir.trip_matcher.datastore;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class TripFareManager {
    private Map<Route, BigDecimal> priceRules = new HashMap<>();

    public void addPriceRule(String sourceStop, String destinationStop, BigDecimal price) {
        Route route = new Route(sourceStop, destinationStop);
        priceRules.put(route, price);
    }

    public BigDecimal getPrice(String sourceStop, String destinationStop) {
        Route route = new Route(sourceStop, destinationStop);
        return priceRules.get(route);
    }
}

