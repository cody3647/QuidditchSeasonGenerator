/*
 * Copyright (c) 2022. Cody Williams
 *
 * Tournament.java is part of Quidditch Season Generator.
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
import java.util.Map;
import java.util.TreeSet;

public abstract class Tournament {

    int numTeams;
    int numLocations;
    int numWeeks;
    int totalMatches;
    int weeksPerRound;
    int hoursBetweenMatches;
    Map<DayOfWeek, LocalTime[]> acceptableTimes;
    LocalDate startDate;
    LocalDate endDate;
    LocalDate blackoutStart;
    LocalDate blackoutEnd;
    TreeSet<LocalDateTime> matchDates;

    public Tournament(TournamentOptions tournamentOptions, int numTeams, int numLocations) {
        this.numTeams = numTeams;
        this.numLocations = numLocations;
        this.weeksPerRound = tournamentOptions.getWeeksPerRound();
        this.hoursBetweenMatches = tournamentOptions.getHoursBetweenMatches();
        this.acceptableTimes = tournamentOptions.getAcceptableTimes();
        this.startDate = tournamentOptions.getStartDate();
        this.blackoutStart = tournamentOptions.getBlackoutStart();
        this.blackoutEnd = tournamentOptions.getBlackoutEnd();
        totalMatches = 0;
    }

    public void changeStartDate(LocalDate date) {
        this.startDate = date;
    }

    public void changeEndDate(LocalDate date) {
        this.endDate = date;
    }

    public void changeBlackoutStart(LocalDate date) {
        this.blackoutStart = date;
    }

    public void changeBlackoutEnd(LocalDate date) {
        this.blackoutEnd = date;
    }

    public void recalculateMatches() {
        calculateMatchesPer();
        calculateMatchDates();
    }

    abstract void calculateMatchesPer();

    abstract TreeSet<LocalDateTime> calculateMatchDates();

    abstract int calculateTotalMatches();

    class TimeEntry implements Comparable<TimeEntry> {
        private final DayOfWeek dayOfWeek;
        private final LocalTime localTime;
        private int count;

        TimeEntry(DayOfWeek dayOfWeek, LocalTime localTime) {
            count = 1;
            this.dayOfWeek = dayOfWeek;
            this.localTime = localTime;
        }

        void incrementCount() {
            count++;
        }

        public int getCount() {
            return count;
        }

        void setCount(int count) {
            this.count = count;
        }

        public DayOfWeek getDayOfWeek() {
            return dayOfWeek;
        }

        public LocalTime getLocalTime() {
            return localTime;
        }

        int fridayZero() {
            return (dayOfWeek.getValue() - 5) % 7;
        }

        @Override
        public int compareTo(TimeEntry other) {
            int compareDay = dayOfWeek.compareTo(other.dayOfWeek);
            if (compareDay != 0) {
                return compareDay;
            }
            return localTime.compareTo(other.localTime);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            TimeEntry timeEntry = (TimeEntry) o;

            if (dayOfWeek != timeEntry.dayOfWeek) return false;
            return localTime.equals(timeEntry.localTime);
        }

        @Override
        public int hashCode() {
            int result = dayOfWeek.hashCode();
            result = 31 * result + localTime.hashCode();
            return result;
        }
    }

}
