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

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Map;

public class MatchDayTime implements Comparable<MatchDayTime> {
    private final ObjectProperty<DayOfWeek> dayOfWeek;
    private final ObjectProperty<LocalTime> localTime;
    private final IntegerProperty priority;
    private final IntegerProperty count;
    private int dayOfWeekPriority;

    public MatchDayTime(DayOfWeek dayOfWeek, LocalTime localTime, int priority) {
        count = new SimpleIntegerProperty(this, "count", 1);
        this.dayOfWeek = new SimpleObjectProperty<>(this, "dayOfWeek", dayOfWeek);
        this.localTime = new SimpleObjectProperty<>(this, "localTime", localTime);
        this.priority = new SimpleIntegerProperty(this, "priority", priority);
        dayOfWeekPriority = dayOfWeekPriorityMap.get(dayOfWeek);
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek.get();
    }

    public ObjectProperty<DayOfWeek> dayOfWeekProperty() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek.set(dayOfWeek);
        this.dayOfWeekPriority = dayOfWeekPriorityMap.get(dayOfWeek);
    }

    public LocalTime getLocalTime() {
        return localTime.get();
    }

    public ObjectProperty<LocalTime> localTimeProperty() {
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime.set(localTime);
    }

    public int getCount() {
        return count.get();
    }

    public IntegerProperty countProperty() {
        return count;
    }

    public void setCount(int count) {
        this.count.set(count);
    }

    public void incrementCount() {
        this.count.set(getCount() + 1);
    }

    public int getPriority() {
        return priority.get();
    }

    public IntegerProperty priorityProperty() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority.set(priority);
    }

    private int getDayOfWeekPriority() {
        return dayOfWeekPriority;
    }

    @Override
    public String toString() {
        return "MatchDayTime{" +
                "dayOfWeek=" + getDayOfWeek() +
                ", localTime=" + getLocalTime() +
                ", priority=" + getPriority() +
                ", count=" + getCount() +
                '}';
    }

    @Override
    public int compareTo(MatchDayTime other) {
        int comparePriority = getPriority() - other.getPriority();
        if(comparePriority != 0)
            return comparePriority;

        comparePriority = getDayOfWeekPriority() - other.getDayOfWeekPriority();
        if(comparePriority != 0)
            return comparePriority;

        return getLocalTime().compareTo(other.getLocalTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MatchDayTime that = (MatchDayTime) o;

        if (!getDayOfWeek().equals(that.getDayOfWeek())) return false;
        return getLocalTime().equals(that.getLocalTime());
    }

    @Override
    public int hashCode() {
        int result = getDayOfWeek().hashCode();
        result = 31 * result + getLocalTime().hashCode();
        return result;
    }

    static private final Map<DayOfWeek, Integer> dayOfWeekPriorityMap = Map.of(
            DayOfWeek.MONDAY, 7,
            DayOfWeek.TUESDAY, 6,
            DayOfWeek.WEDNESDAY, 5,
            DayOfWeek.THURSDAY, 4,
            DayOfWeek.FRIDAY, 1,
            DayOfWeek.SATURDAY, 2,
            DayOfWeek.SUNDAY, 3
    );
}
