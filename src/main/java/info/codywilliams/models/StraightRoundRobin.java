/*
 * Copyright (c) 2022. Cody Williams
 *
 * StraightRoundRobin.java is part of Quidditch Season Generator.
 *
 * Quidditch Season Generator is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quidditch Season Generator is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package info.codywilliams.models;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class StraightRoundRobin extends Tournament {
    boolean homeAndAway;
    int matchesPerRound;
    int matchesPerWeek;
    int matchesPerDay;

    public StraightRoundRobin(TournamentOptions tournamentOptions, int numTeams, int numLocations, boolean homeAndAway) {
        super(tournamentOptions, numTeams, numLocations);
        this.homeAndAway = homeAndAway;
        calculateTotalMatches();
        calculateMatchesPer();
        calculateMatchDates();
    }

    @Override
    int calculateTotalMatches() {
        for (int i = 0; i < numTeams; i++)
            totalMatches += i;
        if (homeAndAway)
            totalMatches *= 2;
        return totalMatches;
    }

    @Override
    void calculateMatchesPer() {
        matchesPerRound = numTeams / 2;
        matchesPerWeek = matchesPerRound / weeksPerRound;
        matchesPerDay = matchesPerWeek / acceptableTimes.size();
    }

    @Override
    TreeSet<LocalDateTime> calculateMatchDates() {
        matchDates = new TreeSet<>();

        TreeSet<TimeEntry> template = makeMatchTemplate();

        LocalDate date = startDate;
        DayOfWeek templateFirstDay = template.first().getDayOfWeek();

        while (!date.getDayOfWeek().equals(templateFirstDay)) {
            date = date.plusDays(1);
        }

        Iterator<TimeEntry> iterator = template.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            TimeEntry timeEntry = iterator.next();
            while (!date.getDayOfWeek().equals(timeEntry.getDayOfWeek()) || (date.isAfter(blackoutStart) && date.isBefore(blackoutEnd))) {
                date = date.plusDays(1);
            }
            matchDates.add(timeEntry.getLocalTime().atDate(date));

            i++;
            if (!iterator.hasNext() && i < totalMatches) {
                iterator = template.iterator();
            }
        }

        return null;
    }


    TreeSet<TimeEntry> makeMatchTemplate() {
        TreeMap<String, TimeEntry> template = new TreeMap<>();

        Iterator<Map.Entry<DayOfWeek, LocalTime[]>> iterator = acceptableTimes.entrySet().iterator();

        int matches = 0;
        LocalTime midnight = LocalTime.MAX;

        while (iterator.hasNext()) {
            // Add the matches to try to schedule for the day to the number left over from previous day
            matches += matchesPerDay;

            Map.Entry<DayOfWeek, LocalTime[]> entry = iterator.next();

            LocalTime time;
            if (hoursBetweenMatches == 0)
                time = entry.getValue()[1];
            else
                time = entry.getValue()[0];
            // Add a minute to the end so that if spacing
            LocalTime end = entry.getValue()[1].plusMinutes(1);

            // Loop through the acceptable time adding a match every 4 hours
            while (matches > 0 && time.isBefore(end)) {
                String key = Integer.toString(entry.getKey().getValue()) + time;
                if (template.containsKey(key))
                    template.get(key).incrementCount();
                else
                    template.put(key, new TimeEntry(entry.getKey(), time));
                matches--;
                // If we are going to wrap around midnight, don't.
                if (midnight.minusHours(hoursBetweenMatches).isBefore(time))
                    break;
                time = time.plusHours(hoursBetweenMatches);
            }

            if (hoursBetweenMatches != 0) {
                Map.Entry<String, TimeEntry> last = template.lastEntry();
                TimeEntry newEntry = new TimeEntry(last.getValue().getDayOfWeek(), end.minusMinutes(1));
                newEntry.setCount(last.getValue().getCount());
                template.put(last.getKey(), newEntry);
            }
        }


        Comparator<TimeEntry> comparator = Comparator.comparingInt(TimeEntry::getCount).thenComparingInt(TimeEntry::fridayZero).thenComparing(TimeEntry::getLocalTime, Comparator.reverseOrder());
        TreeSet<TimeEntry> templateByCount = new TreeSet<>(comparator);
        templateByCount.addAll(template.values());

        while (matches > 0) {
            TimeEntry timeEntry = templateByCount.pollFirst();
            timeEntry.incrementCount();
            templateByCount.add(timeEntry);
            matches--;
        }
        System.out.println(matches);

        return new TreeSet<>(template.values());
    }


}
