package dev.amir.trip_matcher.writer.service;

import dev.amir.trip_matcher.writer.model.TripModel;
import dev.amir.trip_matcher.writer.model.TripStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class CsvWritingServiceTest {

    private static final String TAPS_CSV_FILE_PATH = "src/test/resources/data/trips-writerTest.csv";

    private CsvWritingService csvWritingService;

    @BeforeEach
    void setUp() {
        csvWritingService = new CsvWritingService(TAPS_CSV_FILE_PATH);
    }

    @Test
    void writeTapsCsv_ShouldWriteOnFile() {
        // given
        List<TripModel> trips = List.of(
                new TripModel(LocalDateTime.of(2024, 1, 1, 1, 1, 1), LocalDateTime.of(2024, 1, 1, 1, 1, 1), 0, "Stop1", "Stop2", "3.25", "Company1", "Bus1", "PAN1", TripStatus.valueOf("COMPLETED")),
                new TripModel(LocalDateTime.of(2024, 1, 1, 1, 1, 1), LocalDateTime.of(2024, 1, 1, 1, 1, 1), 0, "Stop1", "Stop2", "3.25", "Company1", "Bus1", "PAN1", TripStatus.valueOf("COMPLETED"))
        );

        // when & then
        assertDoesNotThrow(() -> csvWritingService.writeTripsToCsv(trips));
    }
}
