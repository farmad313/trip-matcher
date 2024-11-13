package dev.amir.trip_matcher.writer.service;

import com.opencsv.CSVWriter;
import dev.amir.trip_matcher.writer.model.TripModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

@Service
public class CsvWritingService {
    @Value("${csv.outputFile.path}")
    private final String filePath;

    public CsvWritingService(String filePath) {
        this.filePath = filePath;
    }


    public void writeTripsToCsv(List<TripModel> trips) throws IOException {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            // Write header
            writer.writeNext(new String[]{"Started", "Finished", "DurationSecs", "FromStopId", "ToStopId", "ChargeAmount", "CompanyId", "BusID", "PAN", "Status"});

            // Write data
            for (TripModel trip : trips) {
                writer.writeNext(new String[]{
                        trip.getStarted().toString(),
                        trip.getFinished().toString(),
                        String.valueOf(trip.getDurationSecs()),
                        trip.getFromStopId(),
                        trip.getToStopId(),
                        trip.getChargeAmount(),
                        trip.getCompanyId(),
                        trip.getBusId(),
                        trip.getPan(),
                        trip.getStatus()
                });
            }
        }
    }
}


