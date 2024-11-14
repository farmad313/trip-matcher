package dev.amir.trip_matcher.reader.model;


import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvCustomBindByPosition;
import dev.amir.trip_matcher.reader.converter.LeadingWhiteSpaceConvertor;
import dev.amir.trip_matcher.reader.converter.LocalDateTimeConverter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class TapModel {
    @CsvBindByPosition(position = 0)
    private int id;

    @CsvCustomBindByPosition(position = 1, converter = LocalDateTimeConverter.class)
    LocalDateTime dateTimeUTC;

    @CsvCustomBindByPosition(position = 2, converter = LeadingWhiteSpaceConvertor.class)
    private String tapType;

    @CsvCustomBindByPosition(position = 3, converter = LeadingWhiteSpaceConvertor.class)
    private String stopId;

    @CsvCustomBindByPosition(position = 4, converter = LeadingWhiteSpaceConvertor.class)
    private String companyId;

    @CsvCustomBindByPosition(position = 5, converter = LeadingWhiteSpaceConvertor.class)
    private String busId;

    @CsvCustomBindByPosition(position = 6, converter = LeadingWhiteSpaceConvertor.class)
    private String pan;
}

