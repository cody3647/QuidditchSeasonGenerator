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

package info.codywilliams.qsg.models.tournament;

import info.codywilliams.qsg.models.SaveSettings;
import javafx.beans.Observable;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

public class TournamentOptions {
    private final DoubleProperty roundsPerWeek;
    private final IntegerProperty hoursBetweenMatches;
    private final ListProperty<ValidStartTime> validStartTimes;
    private final ListProperty<BlackoutDates> blackoutDates;
    private final ObjectProperty<LocalDate> startDate;
    private final ObjectProperty<DayOfWeek> validStartDay;

    public TournamentOptions() {
        roundsPerWeek = new SimpleDoubleProperty(this, "roundsPerWeek", 1);
        hoursBetweenMatches = new SimpleIntegerProperty(this, "hoursBetweenMatches", 0);
        validStartTimes = new SimpleListProperty<>(this, "acceptableTimes",
                FXCollections.observableArrayList(validStartTime -> new Observable[]{validStartTime.earliestProperty(), validStartTime.latestProperty(), validStartTime.enableDayProperty()}));
        for (DayOfWeek day: DayOfWeek.values()) {
            ValidStartTime validStartTime = new ValidStartTime();
            validStartTime.setDayOfWeek(day);
            validStartTime.setEnableDay(day.getValue() >= 5);
            validStartTimes.add(validStartTime);
        }
        startDate = new SimpleObjectProperty<>(this, "startDate", firstFridayOctober);
        blackoutDates = new SimpleListProperty<>(this, "blackoutDates", FXCollections.observableArrayList(blackoutDate -> new Observable[]{blackoutDate.startProperty(), blackoutDate.endProperty()}));
        validStartDay = new SimpleObjectProperty<>();
        validStartDay.setValue(getStartDate().getDayOfWeek());

        // Add listener to valid start times to change valid start day whenever there is a change to the list
        validStartTimes.addListener((ListChangeListener<ValidStartTime>) change -> {
            change.next();
            for (ValidStartTime validStartTime : change.getList()) {
                if (validStartTime.getEnableDay()) {
                    validStartDayProperty().setValue(validStartTime.getDayOfWeek());
                    break;
                }
            }

            if (getStartDate().getDayOfWeek() != validStartDayProperty().getValue()) {
                int daysDiff = validStartDayProperty().getValue().getValue() - getStartDate().getDayOfWeek().getValue();
                startDateProperty().setValue(getStartDate().plusDays(daysDiff));
            }
        });
    }

    public void clear(){
        roundsPerWeek.set(1);
        hoursBetweenMatches.set(0);
        for(ValidStartTime startTime : validStartTimes.get()){
            startTime.setEnableDay(startTime.getDayOfWeek().getValue() >= 5);
            startTime.setEarliest(ValidStartTime.defaultEarliest);
            startTime.setLatest(ValidStartTime.defaultLatest);
        }
        startDate.setValue(firstFridayOctober);
        blackoutDates.clear();
        validStartDay.setValue(getStartDate().getDayOfWeek());

    }

    public void loadSettings(SaveSettings settings){
        roundsPerWeek.setValue(settings.getRoundsPerWeek());
        hoursBetweenMatches.setValue(settings.getHoursBetweenMatches());
        for(int i = 0; i < validStartTimes.size(); i++){
            validStartTimes.get(i).copyValues(settings.getValidStartTimes().get(i));
        }
        startDate.setValue(settings.getStartDate());
        blackoutDates.addAll(settings.getBlackoutDates());
        validStartDay.setValue(getStartDate().getDayOfWeek());

    }

    public double getRoundsPerWeek() {
        return roundsPerWeek.get();
    }

    public DoubleProperty roundsPerWeekProperty() {
        return roundsPerWeek;
    }

    public void setRoundsPerWeek(double roundsPerWeek) {
        this.roundsPerWeek.set(roundsPerWeek);
    }

    public int getHoursBetweenMatches() {
        return hoursBetweenMatches.get();
    }

    public IntegerProperty hoursBetweenMatchesProperty() {
        return hoursBetweenMatches;
    }

    public void setHoursBetweenMatches(int hoursBetweenMatches) {
        this.hoursBetweenMatches.set(hoursBetweenMatches);
    }

    public ObservableList<ValidStartTime> getValidStartTimes() {
        return validStartTimes.get();
    }

    public ListProperty<ValidStartTime> validStartTimesProperty() {
        return validStartTimes;
    }

    public void setValidStartTimes(ObservableList<ValidStartTime> validStartTimes) {
        this.validStartTimes.set(validStartTimes);
    }

    public LocalDate getStartDate() {
        return startDate.get();
    }

    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate.set(startDate);
    }

    public ObservableList<BlackoutDates> getBlackoutDates() {
        return blackoutDates.get();
    }

    public ListProperty<BlackoutDates> blackoutDatesProperty() {
        return blackoutDates;
    }

    public void setBlackoutDates(ObservableList<BlackoutDates> blackoutDates) {
        this.blackoutDates.set(blackoutDates);
    }

    public DayOfWeek getValidStartDay() {
        return validStartDay.get();
    }

    public ObjectProperty<DayOfWeek> validStartDayProperty() {
        return validStartDay;
    }

    public void setValidStartDay(DayOfWeek validStartDay) {
        this.validStartDay.set(validStartDay);
    }

    static final private LocalDate firstFridayOctober = LocalDate.now().withMonth(Month.OCTOBER.getValue()).with(TemporalAdjusters.firstInMonth(DayOfWeek.FRIDAY));
}
