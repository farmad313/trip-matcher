package dev.amir.trip_matcher.reader.service;


import dev.amir.trip_matcher.reader.model.TapModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvReadingServiceTest {

    private static final String TAPS_CSV_FILE_PATH = "src/test/resources/csvs/taps-readerTest.csv";

    private CsvReadingService csvReadingService;

    @BeforeEach
    void setUp() {
        csvReadingService = new CsvReadingService(TAPS_CSV_FILE_PATH);
    }

    @Test
    void readTapsCsv_ShouldReturnListOfTapModels() throws IOException {
        // given
        List<TapModel> expectedTapModels = List.of(
                new TapModel(1, LocalDateTime.of(2024, 1, 1, 1, 1, 1), "ON", "Stop1", "Company1", "Bus1", "PAN1"),
                new TapModel(2, LocalDateTime.of(2024, 1, 1, 1, 1, 1), "OFF", "Stop2", "Company2", "Bus2", "PAN2")
        );
        // when
        List<TapModel> actualTapModels = csvReadingService.readTapsCsv();

        // then
        assertEquals(expectedTapModels, actualTapModels);
    }
}
