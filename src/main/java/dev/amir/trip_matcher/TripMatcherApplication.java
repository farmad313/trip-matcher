package dev.amir.trip_matcher;

import dev.amir.trip_matcher.datastore.DefaultTripFareLoader;
import dev.amir.trip_matcher.datastore.TripFareManager;
import dev.amir.trip_matcher.procesor.service.TapProcessor;
import dev.amir.trip_matcher.reader.model.TapModel;
import dev.amir.trip_matcher.reader.service.CsvReadingService;
import dev.amir.trip_matcher.writer.model.TripModel;
import dev.amir.trip_matcher.writer.service.CsvWritingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
public class TripMatcherApplication implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(TripMatcherApplication.class);

    @Autowired
    private CsvReadingService csvReadingService;

    @Autowired
    private CsvWritingService csvWritingService;

    public static void main(String[] args) {
        SpringApplication.run(TripMatcherApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("""
                                  
                ===================================================================
                Application started ...
                + Set up your input taps.csv path and trips.csv in application.yml.
                ===================================================================

                """);


        List<TapModel> taps = null;
        try {
            taps = csvReadingService.readTapsCsv();
            taps.forEach(e -> log.info(e.toString()));
        } catch (IOException e) {
            log.error("Error reading taps from CSV file", e);
        }

        if (taps != null) {
            TripFareManager tripFareManager = new TripFareManager();
            DefaultTripFareLoader.loadDatastore(tripFareManager);

            TapProcessor tapProcessor = new TapProcessor(tripFareManager);
            List<TripModel> trips = tapProcessor.tripMaker(taps);
            trips.forEach(e -> log.info(e.toString()));

            try {
                csvWritingService.writeTripsToCsv(trips);
            } catch (IOException e) {
                log.error("Error writing trips to CSV file", e);
            }
        }
    }
}