package dev.amir.trip_matcher.reader.service;

import com.opencsv.bean.CsvToBeanBuilder;
import dev.amir.trip_matcher.reader.model.TapModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;

@Service
public class CsvReadingService {

    @Value("${csv.inputFile.path}")
    private final String filePath;

    public CsvReadingService(String filePath) {
        this.filePath = filePath;
    }

    public List<TapModel> readTapsCsv() throws IOException {
        try (var reader = new FileReader(filePath)) {
            return new CsvToBeanBuilder<TapModel>(reader)
                    .withType(TapModel.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .withSkipLines(1)
                    .build()
                    .parse();
        }
    }
}

