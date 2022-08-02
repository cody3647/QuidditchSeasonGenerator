/*
 * Copyright (c) 2022. Cody Williams
 *
 * TournamentOptions.java is part of Quidditch Season Generator.
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
import java.time.LocalTime;
import java.util.Map;

public class TournamentOptions {
    private final int weeksPerRound;
    private final int hoursBetweenMatches;
    private final Map<DayOfWeek, LocalTime[]> acceptableTimes;
    private final LocalDate startDate;
    private final LocalDate blackoutStart;
    private final LocalDate blackoutEnd;


    public TournamentOptions(int weeksPerRound, int hoursBetweenMatches, Map<DayOfWeek, LocalTime[]> acceptableTimes, LocalDate startDate, LocalDate blackoutStart, LocalDate blackoutEnd) {
        this.weeksPerRound = weeksPerRound;
        this.hoursBetweenMatches = hoursBetweenMatches;
        this.acceptableTimes = acceptableTimes;
        this.startDate = startDate;
        this.blackoutStart = blackoutStart;
        this.blackoutEnd = blackoutEnd;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getBlackoutStart() {
        return blackoutStart;
    }

    public LocalDate getBlackoutEnd() {
        return blackoutEnd;
    }

    public int getWeeksPerRound() {
        return weeksPerRound;
    }

    public int getHoursBetweenMatches() {
        return hoursBetweenMatches;
    }

    public Map<DayOfWeek, LocalTime[]> getAcceptableTimes() {
        return acceptableTimes;
    }
}
