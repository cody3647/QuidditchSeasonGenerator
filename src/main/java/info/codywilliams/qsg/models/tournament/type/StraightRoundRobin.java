/*
 * Quidditch Season Generator
 * Copyright (C) 2022.  Cody Williams
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package info.codywilliams.qsg.models.tournament.type;

import info.codywilliams.qsg.models.tournament.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class StraightRoundRobin extends Tournament {
    int numMatchesPerRound;
    int numMatchesPerWeek;
    int numMatchesPerDay;

    public StraightRoundRobin(TournamentOptions tournamentOptions) {
        super(tournamentOptions, TournamentType.STRAIGHT_ROUND_ROBIN);
    }

    public StraightRoundRobin(TournamentOptions tournamentOptions, TournamentType type) {
        super(tournamentOptions, type);

    }

    @Override
    protected void calculateNums(Integer numTeams) {
        int totalMatches = (int) ((numTeams / 2.0) * (numTeams - 1));
        int totalRounds = numTeams % 2 == 0 ? numTeams - 1 : numTeams;

        if (zeroCheckNums(totalMatches, totalRounds)) {
            numMatchesPerRound = 0;
            numMatchesPerWeek = 0;
            numMatchesPerDay = 0;
            return;
        }

        setNumMatches(totalMatches);
        setNumRounds(totalRounds);

        calculateRoundRobinNumbers(numTeams);

        System.out.printf("Num Teams: %d\nTotal Matches: %d\nTotal Rounds: %d\nMatches Per Round: %d\nMatches Per Week: %d\nMatches Per Day: %d\n\n",
                numTeams, totalMatches, totalRounds, numMatchesPerRound, numMatchesPerWeek, numMatchesPerDay);

    }

    protected void calculateRoundRobinNumbers(int numTeams) {
        numMatchesPerRound = (numTeams % 2 == 0) ? (numTeams / 2) : ((numTeams - 1) / 2);
        numMatchesPerWeek = (int) Math.ceil(tournamentOptions.getRoundsPerWeek() * numMatchesPerRound);
        numMatchesPerDay = (int) Math.ceil(numMatchesPerWeek / (double) numMatchesPerRound);
    }

    @Override
    protected LocalDateTime calculateMatchDates() {

        template = makeMatchTemplate();

        if (template == null)
            return null;

        // Move template to first acceptable day of the week selected in get start date
        LocalDate date = tournamentOptions.getStartDate();
        DayOfWeek dayMatchesStart = date.getDayOfWeek();

        // Setup iterators
        List<BlackoutDates> blackoutDatesList = tournamentOptions.getBlackoutDates();
        blackoutDatesList.sort(null);
        Iterator<BlackoutDates> blackoutDatesIterator = blackoutDatesList.listIterator();
        Iterator<TimeEntry> timeEntryIterator = template.iterator();

        // Setup All Round Set<Dates>
        matchDates.clear();
        for (int i = 1; i <= numRounds.get(); i++)
            matchDates.put(i, new TreeSet<>());

        BlackoutDates blackoutDates = null;
        if (blackoutDatesIterator.hasNext())
            blackoutDates = blackoutDatesIterator.next();

        int totalMatchCount = 0;
        int roundMatchCount = 0;
        int round = 1;
        int compare;
        LocalDateTime localDateTime = null;
        Set<LocalDateTime> dateTimeSet = matchDates.get(round);
        while (timeEntryIterator.hasNext() && totalMatchCount < getNumMatches()) {
            // Have we finished a round?
            if (roundMatchCount == numMatchesPerRound) {
                // Reset round counter, start the week template over again
                roundMatchCount = 0;
                timeEntryIterator = template.iterator();
                // Adjust date to the next week
                date = date.with(TemporalAdjusters.next(dayMatchesStart));
                // Get the set for the round
                dateTimeSet = matchDates.get(++round);
            }
            // Have we split a round across months?
            else if (roundMatchCount % numMatchesPerWeek == 0) {
                timeEntryIterator = template.iterator();
                // Adjust date to the next week
                date = date.with(TemporalAdjusters.next(dayMatchesStart));
            }


            TimeEntry timeEntry = timeEntryIterator.next();

            while ((compare = isDateInBlackout(date, blackoutDates)) > 0) {
                date = date.plusDays(1);

                if (compare == 2) {
                    if (blackoutDatesIterator.hasNext()) {
                        blackoutDates = blackoutDatesIterator.next();
                    } else
                        blackoutDates = null;
                }

            }

            while (!date.getDayOfWeek().equals(timeEntry.getDayOfWeek())) {
                date = date.plusDays(1);
            }
            localDateTime = timeEntry.getLocalTime().atDate(date);

            dateTimeSet.add(localDateTime);

            roundMatchCount += timeEntry.getCount();
            totalMatchCount += timeEntry.getCount();
            if (!timeEntryIterator.hasNext() && totalMatchCount < getNumMatches()) {
                timeEntryIterator = template.iterator();
            }
        }

        return localDateTime;
    }


    TreeSet<TimeEntry> makeMatchTemplate() {
        TreeMap<String, TimeEntry> template = new TreeMap<>();

        Iterator<ValidStartTime> iterator = tournamentOptions.getValidStartTimes().iterator();
        int hoursBetweenMatches = tournamentOptions.getHoursBetweenMatches();

        int matches = 0;
        LocalTime midnight = LocalTime.MAX;

        while (iterator.hasNext()) {
            // Add the matches to try to schedule for the day to the number left over from previous day
            matches += numMatchesPerDay;

            ValidStartTime validStartTime = iterator.next();
            if (!validStartTime.getEnableDay())
                continue;

            LocalTime time;
            // When hours between matches is 0, matches will be equally divided between valid days starting at the latest time available
            if (hoursBetweenMatches == 0)
                time = validStartTime.getLatest();
            else
                time = validStartTime.getEarliest();
            // Add a minute to the end so that adding hours will not land on latest time and be ineligible
            LocalTime end = validStartTime.getLatest().plusMinutes(1);

            // Loop through the acceptable time adding a match every hoursBetweenMatches
            while (matches > 0 && time.isBefore(end)) {
                String key = Integer.toString(validStartTime.getDayOfWeek().getValue()) + time;
                if (template.containsKey(key))
                    template.get(key).incrementCount();
                else
                    template.put(key, new TimeEntry(validStartTime.getDayOfWeek(), time));
                matches--;
                // If we are going to wrap around midnight, don't.
                if (midnight.minusHours(hoursBetweenMatches).isBefore(time))
                    break;
                time = time.plusHours(hoursBetweenMatches);
            }

            // If all matches aren't at the same time, move the last match for each day to the last valid start time
            if (hoursBetweenMatches != 0) {
                Map.Entry<String, TimeEntry> last = template.lastEntry();
                TimeEntry newEntry = new TimeEntry(last.getValue().getDayOfWeek(), end.minusMinutes(1));
                newEntry.setCount(last.getValue().getCount());
                template.put(last.getKey(), newEntry);
            }
        }

        if (template.isEmpty())
            return null;

        // Fill in the remaining matches
        if (matches > 0) {
            // Order by fewest matches on the day, then by Fri-Sun-Mon-Thu then by descending time
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
        }
        return new TreeSet<>(template.values());
    }


}
