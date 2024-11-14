package dev.amir.trip_matcher.writer.service;

import com.opencsv.CSVWriter;
import dev.amir.trip_matcher.writer.model.TripModel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
@NoArgsConstructor
@Slf4j
public class CsvWritingService {

    @Value("${csv.output-file.path}")
    private String filePath ;

    public CsvWritingService(String filePath) {
        this.filePath = filePath;
    }


    public void writeTripsToCsv(List<TripModel> trips) throws IOException {
        log.info("Writing trips to CSV file: {}", filePath);

        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            writer.writeNext(new String[]{"Started", "Finished", "DurationSecs", "FromStopId", "ToStopId", "ChargeAmount", "CompanyId", "BusID", "PAN", "Status"});

            // Write data
            for (TripModel trip : trips) {
                writer.writeNext(new String[]{
                        trip.getStarted() != null ? trip.getStarted().toString() : "UNKNOWN",
                        trip.getFinished() != null ? trip.getFinished().toString() : "UNKNOWN",
                        String.valueOf(trip.getDurationSecs()),
                        trip.getFromStopId(),
                        trip.getToStopId(),
                        (trip.getChargeAmount() != null) ? "$"+trip.getChargeAmount() : "NOT_FOUND",
                        trip.getCompanyId(),
                        trip.getBusId(),
                        trip.getPan(),
                        String.valueOf(trip.getStatus())
                });
            }
        }
    }


}


