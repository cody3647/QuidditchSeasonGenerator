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

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.LocalDate;

public class BlackoutDates implements Comparable<BlackoutDates> {
    private final ObjectProperty<LocalDate> start;
    private final ObjectProperty<LocalDate> end;

    public BlackoutDates() {
        start = new SimpleObjectProperty<>(this, "start");
        end = new SimpleObjectProperty<>(this, "end");
    }

    public LocalDate getStart() {
        return start.get();
    }

    public void setStart(LocalDate start) {
        this.start.set(start);
    }

    public ObjectProperty<LocalDate> startProperty() {
        return start;
    }

    public LocalDate getEnd() {
        return end.get();
    }

    public void setEnd(LocalDate end) {
        this.end.set(end);
    }

    public ObjectProperty<LocalDate> endProperty() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlackoutDates that = (BlackoutDates) o;

        if (getStart() != null ? !getStart().equals(that.getStart()) : that.getStart() != null) return false;
        return getEnd() != null ? getEnd().equals(that.getEnd()) : that.getEnd() == null;
    }

    @Override
    public int hashCode() {
        int result = getStart() != null ? getStart().hashCode() : 0;
        result = 31 * result + (getEnd() != null ? getEnd().hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(BlackoutDates that) {
        return getStart().compareTo(that.getStart());
    }

    @Override
    public String toString() {
        return String.format("%s — %s", getStart(), getEnd());
    }
}
