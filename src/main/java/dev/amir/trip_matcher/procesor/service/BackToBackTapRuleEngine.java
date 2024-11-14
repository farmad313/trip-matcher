package dev.amir.trip_matcher.procesor.service;

import dev.amir.trip_matcher.datastore.TripFareManager;
import dev.amir.trip_matcher.reader.model.TapModel;
import dev.amir.trip_matcher.writer.model.TripModel;
import dev.amir.trip_matcher.writer.model.TripStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;

@Service
@Slf4j
public class BackToBackTapRuleEngine {

    public static final int IMMEDIATE_TAP_ON_DURATION_IN_SEC = 10;
    public static final String UNKNOWN = "UNKNOWN";
    private final TripFareManager tripFareManager;
    private final Map<BiPredicate<TapModel, TapModel>, Handler> handlers = new HashMap<>();


    public BackToBackTapRuleEngine(TripFareManager tripFareManager) {
        this.tripFareManager = tripFareManager;
        initializeRuleHandlers();
    }


    public void processBackToBackTaps(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log(previousTap, currentTap);

        handlers.entrySet().stream()
                .filter(entry -> entry.getKey().test(previousTap, currentTap))
                .forEach(entry -> entry.getValue().handle(previousTap, currentTap, trips));
    }

    private static void log(TapModel previousTap, TapModel currentTap) {
        log.info("""
                Process back to back taps for each group of passengers:
                currentTap: {} 
                previousTap: {}
                """, currentTap, previousTap);
    }


    private void initializeRuleHandlers() {
        handlers.put((previous, current) -> "GROUP_HEAD".equals(previous.getTapType()), this::handleGroupHead);
        handlers.put((previous, current) -> "ON".equals(previous.getTapType()) && "GROUP_TAIL".equals(current.getTapType()), this::handleOnToGroupTail);

        handlers.put((previous, current) -> "ON".equals(previous.getTapType()) && "OFF".equals(current.getTapType()) && current.getStopId().equals(previous.getStopId()), this::handleOnToOffSameStop);
        handlers.put((previous, current) -> "ON".equals(previous.getTapType()) && "OFF".equals(current.getTapType()) && !current.getStopId().equals(previous.getStopId()), this::handleOnToOffDifferentStop);

        handlers.put((previous, current) -> "ON".equals(current.getTapType()) && "ON".equals(previous.getTapType()), this::handleOnToOn);
        handlers.put((previous, current) -> "ON".equals(current.getTapType()) && "ON".equals(previous.getTapType()) &&
                                            (!current.getStopId().equals(previous.getStopId())) &&
                                            (current.getDateTimeUTC().compareTo(previous.getDateTimeUTC()) < IMMEDIATE_TAP_ON_DURATION_IN_SEC), this::handleImmediateTaps);

        handlers.put((previous, current) -> "OFF".equals(previous.getTapType()) && "ON".equals(current.getTapType()), this::handleOffToOn);
        handlers.put((previous, current) -> "OFF".equals(current.getTapType()) && "OFF".equals(previous.getTapType()), this::handleOffToOff);

        // More complex transit companies rules can be added here ...
    }


    private void handleGroupHead(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info("Tap type transition: {} -> {}; GROUP_HEAD (First Tap)", "NULL", currentTap.getTapType());
    }


    private void handleOnToOffSameStop(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info("Tap type transition: {} -> {}; {}", previousTap.getTapType(), currentTap.getTapType(), TripStatus.valueOf("CANCELLED"));

        trips.add(TripModel.builder()
                .started(previousTap.getDateTimeUTC())
                .finished(currentTap.getDateTimeUTC())
                .durationSecs(Duration.between(previousTap.getDateTimeUTC(), currentTap.getDateTimeUTC()).getSeconds())
                .fromStopId(previousTap.getStopId())
                .toStopId(currentTap.getStopId())
                .chargeAmount(String.valueOf(tripFareManager.getPrice(previousTap.getStopId(), currentTap.getStopId())))
                .companyId(previousTap.getCompanyId())
                .busId(previousTap.getBusId())
                .pan(previousTap.getPan())
                .status(TripStatus.valueOf("CANCELLED"))
                .build());
    }


    private void handleOnToOffDifferentStop(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info("Tap type transition: {} -> {}; {}", previousTap.getTapType(), currentTap.getTapType(), TripStatus.valueOf("COMPLETED"));

        trips.add(TripModel.builder()
                .started(previousTap.getDateTimeUTC())
                .finished(currentTap.getDateTimeUTC())
                .durationSecs(Duration.between(previousTap.getDateTimeUTC(), currentTap.getDateTimeUTC()).getSeconds())
                .fromStopId(previousTap.getStopId())
                .toStopId(currentTap.getStopId())
                .chargeAmount(String.valueOf(tripFareManager.getPrice(previousTap.getStopId(), currentTap.getStopId())))
                .companyId(previousTap.getCompanyId())
                .busId(previousTap.getBusId())
                .pan(previousTap.getPan())
                .status(TripStatus.valueOf("COMPLETED"))
                .build());

    }


    private void handleOnToGroupTail(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info("Tap type transition: {} -> {}; {} (Process Period Closed)", previousTap.getTapType(), currentTap.getTapType(), TripStatus.valueOf("INCOMPLETE"));

        trips.add(TripModel.builder()
                .started(previousTap.getDateTimeUTC())
                .finished(currentTap.getDateTimeUTC())
                .durationSecs(0)
                .fromStopId(previousTap.getStopId())
                .toStopId(UNKNOWN)
                .chargeAmount(String.valueOf(tripFareManager.getPrice(previousTap.getStopId(), UNKNOWN)))
                .companyId(previousTap.getCompanyId())
                .busId(previousTap.getBusId())
                .pan(previousTap.getPan())
                .status(TripStatus.valueOf("INCOMPLETE"))
                .build());
    }


    private void handleOnToOn(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info("Tap type transition: {} -> {}; {} (New Trip Started)", previousTap.getTapType(), currentTap.getTapType(), TripStatus.valueOf("INCOMPLETE"));

        trips.add(TripModel.builder()
                .started(previousTap.getDateTimeUTC())
                .finished(null)
                .durationSecs(0)
                .fromStopId(previousTap.getStopId())
                .toStopId(UNKNOWN)
                .chargeAmount(String.valueOf(tripFareManager.getPrice(previousTap.getStopId(), UNKNOWN)))
                .companyId(previousTap.getCompanyId())
                .busId(previousTap.getBusId())
                .pan(previousTap.getPan())
                .status(TripStatus.valueOf("INCOMPLETE"))
                .build());
    }

    private void handleImmediateTaps(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info("Tap type transition: {} -> {}; IMMEDIATE_DOUBLE_TAPS", previousTap.getTapType(), currentTap.getTapType());
    }


    private void handleOffToOn(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info("Tap type transition: {} -> {}; UNDEFINED", previousTap.getTapType(), currentTap.getTapType());
    }


    private void handleOffToOff(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info("Tap type transition: {} -> {}; UNDEFINED", previousTap.getTapType(), currentTap.getTapType());
    }


    @FunctionalInterface
    private interface Handler {
        void handle(TapModel previousTap, TapModel currentTap, List<TripModel> trips);
    }
}

