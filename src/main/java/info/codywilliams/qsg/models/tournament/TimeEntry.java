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

package info.codywilliams.qsg.models.tournament;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class TimeEntry implements Comparable<TimeEntry> {
    private final DayOfWeek dayOfWeek;
    private final LocalTime localTime;
    private int count;

    public TimeEntry(DayOfWeek dayOfWeek, LocalTime localTime) {
        count = 1;
        this.dayOfWeek = dayOfWeek;
        this.localTime = localTime;
    }

    public void incrementCount() {
        count++;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getLocalTime() {
        return localTime;
    }

    public  int fridayZero() {
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
