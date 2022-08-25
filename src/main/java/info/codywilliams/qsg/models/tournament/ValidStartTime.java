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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.time.DayOfWeek;
import java.time.LocalTime;

public class ValidStartTime{
    final private ObjectProperty<DayOfWeek> dayOfWeek;
    final private BooleanProperty enableDay;
    final private ObjectProperty<LocalTime> earliest;
    final private ObjectProperty<LocalTime> latest;

    public ValidStartTime(){
        dayOfWeek = new SimpleObjectProperty<>(this, "dayOfWeek");
        enableDay = new SimpleBooleanProperty(this, "enableDay");
        earliest = new SimpleObjectProperty<>(this, "earliest", LocalTime.of(10,0));
        latest = new SimpleObjectProperty<>(this, "latest", LocalTime.of(20,0));
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek.get();
    }

    public ObjectProperty<DayOfWeek> dayOfWeekProperty() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek.set(dayOfWeek);
    }

    public boolean getEnableDay() {
        return enableDay.get();
    }

    public BooleanProperty enableDayProperty() {
        return enableDay;
    }

    public void setEnableDay(boolean enableDay) {
        this.enableDay.set(enableDay);
    }

    public LocalTime getEarliest() {
        return earliest.get();
    }

    public ObjectProperty<LocalTime> earliestProperty() {
        return earliest;
    }

    public void setEarliest(LocalTime earliest) {
        this.earliest.set(earliest);
    }

    public LocalTime getLatest() {
        return latest.get();
    }

    public ObjectProperty<LocalTime> latestProperty() {
        return latest;
    }

    public void setLatest(LocalTime latest) {
        this.latest.set(latest);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ValidStartTime that = (ValidStartTime) o;

        return getDayOfWeek() != null ? getDayOfWeek().equals(that.getDayOfWeek()) : that.getDayOfWeek() == null;
    }

    @Override
    public int hashCode() {
        return getDayOfWeek() != null ? getDayOfWeek().hashCode() : 0;
    }

    @Override
    public String toString() {
        return String.format("%s: %s \u2014 %s", getDayOfWeek(), getEarliest(), getLatest());
    }
}
