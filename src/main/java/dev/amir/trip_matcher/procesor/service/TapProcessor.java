package dev.amir.trip_matcher.procesor.service;

import dev.amir.trip_matcher.datastore.TripFareManager;
import dev.amir.trip_matcher.reader.model.TapModel;
import dev.amir.trip_matcher.ruleengine.service.BackToBackTapRuleEngine;
import dev.amir.trip_matcher.writer.model.TripModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TapProcessor {

    public static final String UNKNOWN = "UNKNOWN";
    private final BackToBackTapRuleEngine ruleEngine;

    public TapProcessor(TripFareManager tripFareManager) {
        this.ruleEngine = new BackToBackTapRuleEngine(tripFareManager);
    }

    public List<TripModel> tripMaker(List<TapModel> taps) {

        return taps.parallelStream()
                // Group data based on companyId, busId, and pan
                .collect(Collectors.groupingBy(
                        tap -> new GroupKey(tap.getCompanyId(), tap.getBusId(), tap.getPan()),
                        Collectors.toList()))
                .values().stream()

                // Sort data based on dateTimeUTC
                .flatMap(group -> group.parallelStream().sorted(Comparator.comparing(TapModel::getDateTimeUTC)))

                // Process each group and create TripModels
                .collect(Collectors.groupingBy(tap -> new GroupKey(tap.getCompanyId(), tap.getBusId(), tap.getPan())))
                .values().stream()
                .flatMap(group -> {
                    List<TripModel> trips = new ArrayList<>();
                    TapModel previousTap = addHeadAndTail(group);

                    for (TapModel currentTap : group) {
                        ruleEngine.processBackToBackTaps(previousTap, currentTap, trips);
                        previousTap = currentTap;
                    }
                    return trips.stream();
                }).toList();
    }


    private static TapModel addHeadAndTail(List<TapModel> group) {
        // Add a dummy tail element to the group to handle the last iteration
        group.add(new TapModel(0, null, "GROUP_TAIL", UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN));

        // Add a dummy head element to the group to handle the first iteration
        TapModel previousTap = new TapModel(0, null, "GROUP_HEAD", UNKNOWN, UNKNOWN, UNKNOWN, UNKNOWN);
        group.add(0, previousTap);

        return previousTap;
    }


    private static class GroupKey {
        private final String companyId;
        private final String busId;
        private final String pan;

        public GroupKey(String companyId, String busId, String pan) {
            this.companyId = companyId;
            this.busId = busId;
            this.pan = pan;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            GroupKey groupKey = (GroupKey) o;
            return Objects.equals(companyId, groupKey.companyId) &&
                   Objects.equals(busId, groupKey.busId) &&
                   Objects.equals(pan, groupKey.pan);
        }


        @Override
        public int hashCode() {
            return Objects.hash(companyId, busId, pan);
        }
    }
}



