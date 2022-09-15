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

package info.codywilliams.qsg.models.tournament;

import info.codywilliams.qsg.models.tournament.type.TournamentType;
import info.codywilliams.qsg.util.Formatters;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.TreeMap;
import java.util.TreeSet;

public abstract class Tournament {
    protected ObjectProperty<TournamentType> type;
    protected IntegerProperty numWeeks;
    protected IntegerProperty numMatches;
    protected IntegerProperty numRounds;
    protected MapProperty<Integer, TreeSet<LocalDateTime>> matchDates;
    protected TreeSet<TimeEntry> template;
    protected ObjectProperty<LocalDate> endDate;
    protected StringBinding endDateStringBinding;
    protected TournamentOptions tournamentOptions;


    public Tournament(TournamentOptions tournamentOptions, TournamentType type) {

        this.type = new SimpleObjectProperty<>(this, "type", type);
        numWeeks = new SimpleIntegerProperty(this, "numWeeks", 0);
        numMatches = new SimpleIntegerProperty(this, "numMathces", 0);
        numRounds = new SimpleIntegerProperty(this, "numRounds", 0);

        matchDates = new SimpleMapProperty<>(this, "matchDates", FXCollections.observableMap(new TreeMap<>()));
        endDate = new SimpleObjectProperty<>(this, "endDate");
        this.tournamentOptions = tournamentOptions;


        endDateStringBinding = Bindings.createStringBinding(() -> {
                    if(endDateProperty().getValue() == null)
                        return "";
                    return endDateProperty().getValue().format(Formatters.dateFormatter);
        }, endDateProperty());
    }



    public void recalculateTournament(int numTeams) {
        if(numTeams <2)
            return;

        calculateNums(numTeams);
        LocalDateTime lastMatchDate = calculateMatchDates();
        setEndDate(lastMatchDate.toLocalDate());
    }

    protected abstract void calculateNums(Integer numTeams);

    protected boolean zeroCheckNums(int totalMatches, int totalRounds){
        if(totalMatches <= 0 && totalRounds <= 0){
            setNumMatches(0);
            setNumMatches(0);

            return true;
        }
        return false;
    }

    protected abstract LocalDateTime calculateMatchDates();

    protected void checkBlackoutDates(){
        for(BlackoutDates blackoutDates: tournamentOptions.getBlackoutDates()){
            int diff = blackoutDates.getStart().getDayOfWeek().getValue() - DayOfWeek.MONDAY.getValue();
            if(diff != 0){
                blackoutDates.setStart(blackoutDates.getStart().minusDays(diff));
            }
            diff = DayOfWeek.SUNDAY.getValue() - blackoutDates.getStart().getDayOfWeek().getValue();
            if(diff != 0){
                blackoutDates.setEnd(blackoutDates.getEnd().plusDays(diff));
            }
        }

    }

    protected void checkStartDay() {
        LocalDate startDate = tournamentOptions.getStartDate();
        DayOfWeek day = tournamentOptions.getValidStartDay();

        int i = 0;
        for(; i < day.ordinal(); i++){
            ValidStartTime validStartTime = tournamentOptions.getValidStartTimes().get(i);
            if(validStartTime != null && validStartTime.getEnableDay()){
                day = validStartTime.getDayOfWeek();
                break;
            }
        }


        int diff = startDate.getDayOfWeek().getValue() - day.getValue();
        if(diff > 0){
            day = startDate.getDayOfWeek();
            tournamentOptions.setValidStartDay(day);
        } else if (diff < 0) {
            startDate = startDate.plusDays(diff * -1);
            tournamentOptions.setStartDate(startDate);
        }
    }

    protected int isDateInBlackout(LocalDate date, BlackoutDates blackoutDates){
        if(blackoutDates == null)
            return 0;

        if(date.isAfter(blackoutDates.getStart()) && date.isBefore(blackoutDates.getEnd()) || date.isEqual(blackoutDates.getStart()))
            return 1;
        if(date.isEqual(blackoutDates.getEnd()))
            return 2;

        return 0;
    }

    public TournamentType getType() {
        return type.get();
    }

    public ObjectProperty<TournamentType> typeProperty() {
        return type;
    }

    public void setType(TournamentType type) {
        this.type.set(type);
    }

    public int getNumWeeks() {
        return numWeeks.get();
    }

    public IntegerProperty numWeeksProperty() {
        return numWeeks;
    }

    public void setNumWeeks(int numWeeks) {
        this.numWeeks.set(numWeeks);
    }

    public int getNumMatches() {
        return numMatches.get();
    }

    public IntegerProperty numMatchesProperty() {
        return numMatches;
    }

    public void setNumMatches(int numMatches) {
        this.numMatches.set(numMatches);
    }

    public int getNumRounds() {
        return numRounds.get();
    }

    public IntegerProperty numRoundsProperty() {
        return numRounds;
    }

    public void setNumRounds(int numRounds) {
        this.numRounds.set(numRounds);
    }

    public ObservableMap<Integer, TreeSet<LocalDateTime>> getMatchDates() {
        return matchDates.get();
    }

    public MapProperty<Integer, TreeSet<LocalDateTime>> matchDatesProperty() {
        return matchDates;
    }

    public LocalDate getEndDate() {
        return endDate.get();
    }

    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate.set(endDate);
    }

    public StringBinding endDateStringBinding(){
        return endDateStringBinding;

    }
}
