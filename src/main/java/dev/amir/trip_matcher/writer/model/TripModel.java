package dev.amir.trip_matcher.writer.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TripModel {
    private LocalDateTime started;
    private LocalDateTime finished;
    private long durationSecs;
    private String fromStopId;
    private String toStopId;
    private String chargeAmount;
    private String companyId;
    private String busId;
    private String pan;
    private String status;
}
