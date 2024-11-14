package dev.amir.trip_matcher.ruleengine.service;

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
    public static final String TAP_TYPE_TRANSITION_LOG = "Tap Type Transition: {} -> {}; Trip Type= {} ({}) \n";
    public static final String INCOMPLETE = "INCOMPLETE";
    public static final String COMPLETED = "COMPLETED";
    public static final String CANCELLED = "CANCELLED";
    public static final String GROUP_TAIL = "GROUP_TAIL";
    public static final String GROUP_HEAD = "GROUP_HEAD";
    public static final String ON = "ON";
    public static final String OFF = "OFF";
    public static final String LOGGED_AND_IGNORED = "Just Logged and Ignored";
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
                Back-to-back Taps Process Is Being Executed For The Following Taps:
                currentTap: {} 
                previousTap: {}""", currentTap, previousTap);
    }


    private void initializeRuleHandlers() {
        handlers.put((previous, current) -> GROUP_HEAD.equals(previous.getTapType()), this::handleGroupHead);
        handlers.put((previous, current) -> ON.equals(previous.getTapType()) && GROUP_TAIL.equals(current.getTapType()), this::handleOnToGroupTail);

        handlers.put((previous, current) -> ON.equals(previous.getTapType()) && OFF.equals(current.getTapType()) && current.getStopId().equals(previous.getStopId()), this::handleOnToOffSameStop);
        handlers.put((previous, current) -> ON.equals(previous.getTapType()) && OFF.equals(current.getTapType()) && !current.getStopId().equals(previous.getStopId()), this::handleOnToOffDifferentStop);

        handlers.put((previous, current) -> ON.equals(current.getTapType()) && ON.equals(previous.getTapType()), this::handleOnToOn);
        handlers.put((previous, current) -> ON.equals(current.getTapType()) && ON.equals(previous.getTapType()) &&
                                            (current.getStopId().equals(previous.getStopId())) &&
                                            (current.getDateTimeUTC().compareTo(previous.getDateTimeUTC()) < IMMEDIATE_TAP_ON_DURATION_IN_SEC), this::handleImmediateTaps);

        handlers.put((previous, current) -> OFF.equals(previous.getTapType()) && ON.equals(current.getTapType()), this::handleOffToOn);
        handlers.put((previous, current) -> OFF.equals(current.getTapType()) && OFF.equals(previous.getTapType()), this::handleOffToOff);

        // More complex transit companies rules can be added here ...
    }


    private void handleGroupHead(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info(TAP_TYPE_TRANSITION_LOG, GROUP_HEAD, currentTap.getTapType(), GROUP_HEAD,"First Tap Ignored");
    }


    private void handleOnToOffSameStop(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info(TAP_TYPE_TRANSITION_LOG, previousTap.getTapType(), currentTap.getTapType(), TripStatus.valueOf(CANCELLED),"Trip added");

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
                .status(TripStatus.valueOf(CANCELLED))
                .build());
    }


    private void handleOnToOffDifferentStop(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info(TAP_TYPE_TRANSITION_LOG, previousTap.getTapType(), currentTap.getTapType(), TripStatus.valueOf(COMPLETED), "Trip added");

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
                .status(TripStatus.valueOf(COMPLETED))
                .build());

    }


    private void handleOnToGroupTail(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info(TAP_TYPE_TRANSITION_LOG, previousTap.getTapType(), currentTap.getTapType(), TripStatus.valueOf(INCOMPLETE),"Process Period Closed, Trip added" );

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
                .status(TripStatus.valueOf(INCOMPLETE))
                .build());
    }


    private void handleOnToOn(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info(TAP_TYPE_TRANSITION_LOG, previousTap.getTapType(), currentTap.getTapType(), TripStatus.valueOf(INCOMPLETE), "New Trip Started, Trip added");

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
                .status(TripStatus.valueOf(INCOMPLETE))
                .build());
    }

    private void handleImmediateTaps(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info(TAP_TYPE_TRANSITION_LOG, previousTap.getTapType(), currentTap.getTapType(),"IMMEDIATE_DOUBLE_TAPS", LOGGED_AND_IGNORED);
    }


    private void handleOffToOn(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info(TAP_TYPE_TRANSITION_LOG, previousTap.getTapType(), currentTap.getTapType(), "UNDEFINED", LOGGED_AND_IGNORED);
    }


    private void handleOffToOff(TapModel previousTap, TapModel currentTap, List<TripModel> trips) {
        log.info(TAP_TYPE_TRANSITION_LOG, previousTap.getTapType(), currentTap.getTapType(), "UNDEFINED", LOGGED_AND_IGNORED);
    }


    @FunctionalInterface
    private interface Handler {
        void handle(TapModel previousTap, TapModel currentTap, List<TripModel> trips);
    }
}

