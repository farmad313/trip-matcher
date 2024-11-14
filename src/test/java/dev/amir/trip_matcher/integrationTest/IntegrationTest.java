package dev.amir.trip_matcher.integrationTest;

import dev.amir.trip_matcher.datastore.TripFareManager;
import dev.amir.trip_matcher.procesor.service.TapProcessor;
import dev.amir.trip_matcher.reader.model.TapModel;
import dev.amir.trip_matcher.reader.service.CsvReadingService;
import dev.amir.trip_matcher.writer.model.TripModel;
import dev.amir.trip_matcher.writer.service.CsvWritingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static dev.amir.trip_matcher.datastore.DefaultTripFareLoader.loadDatastore;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;


/**
 * This test case is an integration test that tests the interaction between the CsvReadingService
 * and TapProcessor classes. The test case reads a CSV file containing tap data, processes the data
 * using the TapProcessor class, and asserts the output.
 */
class IntegrationTest {

    private static final String INPUT_TAPS_CSV_FILE_PATH = "src/test/resources/data/taps-integrationTest.csv";
    private static final String OUTOUT_TRIPS_CSV_FILE_PATH = "src/test/resources/data/trips-integrationTest.csv";

    private CsvReadingService csvReadingService;
    private TripFareManager tripFareManager;
    private CsvWritingService csvWritingService;

    @BeforeEach
    void setUp() {
        csvReadingService = new CsvReadingService(INPUT_TAPS_CSV_FILE_PATH);

        csvWritingService = new CsvWritingService(OUTOUT_TRIPS_CSV_FILE_PATH);

        tripFareManager = new TripFareManager();
        loadDatastore(tripFareManager);
    }


    @Test
    void tripMaker_ShouldSaveListOfTrips() throws IOException {
        TapProcessor tapProcessor = new TapProcessor(tripFareManager);
        List<TapModel> taps = csvReadingService.readTapsCsv();
        List<TripModel> trips = tapProcessor.tripMaker(taps);

        assertDoesNotThrow(() -> csvWritingService.writeTripsToCsv(trips));
    }
}