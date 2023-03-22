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

import info.codywilliams.qsg.models.match.Match;
import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.models.tournament.type.TournamentType;
import info.codywilliams.qsg.util.Formatters;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Tournament {
    protected ObjectProperty<TournamentType> type;
    protected IntegerProperty numWeeks;
    protected IntegerProperty numMatches;
    protected IntegerProperty numRounds;
    protected SetProperty<Match> matches;
    protected TreeSet<TimeEntry> template;
    protected ObjectProperty<LocalDate> endDate;
    protected StringBinding endDateStringBinding;
    protected TournamentOptions tournamentOptions;
    protected SimpleMapProperty<String, Integer> tournamentPoints;
    protected SimpleBooleanProperty teamsAssigned;


    public Tournament(TournamentOptions tournamentOptions, TournamentType type) {

        this.type = new SimpleObjectProperty<>(this, "type", type);
        numWeeks = new SimpleIntegerProperty(this, "numWeeks", 0);
        numMatches = new SimpleIntegerProperty(this, "numMatches", 0);
        numRounds = new SimpleIntegerProperty(this, "numRounds", 0);

        matches = new SimpleSetProperty<>(this, "matches", FXCollections.observableSet(new TreeSet<>()));
        endDate = new SimpleObjectProperty<>(this, "endDate");
        this.tournamentOptions = tournamentOptions;
        tournamentPoints = new SimpleMapProperty<>(this, "tournamentPoints", FXCollections.observableMap(new ConcurrentHashMap<>()));


        endDateStringBinding = Bindings.createStringBinding(() -> {
                    if(endDateProperty().getValue() == null)
                        return "";
                    return endDateProperty().getValue().format(Formatters.dateFormatter);
        }, endDateProperty());

        teamsAssigned = new SimpleBooleanProperty(this, "teamsAssigned", false);
    }



    public void recalculateTournament(int numTeams) {
        if(numTeams <2)
            return;

        calculateNums(numTeams);
        LocalDateTime lastMatchDate = calculateMatchDates();
        setEndDate(lastMatchDate.toLocalDate());
        teamsAssigned.set(false);
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
    protected int isDateInBlackout(LocalDate date, BlackoutDates blackoutDates){
        if(blackoutDates == null)
            return 0;

        if(date.isAfter(blackoutDates.getStart()) && date.isBefore(blackoutDates.getEnd()) || date.isEqual(blackoutDates.getStart()))
            return 1;
        if(date.isEqual(blackoutDates.getEnd()))
            return 2;

        return 0;
    }

    public abstract TreeSet<Match> assignTeamsToMatches(List<Team> teams, long seed);

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

    public ObservableSet<Match> getMatches() {
        return matches.get();
    }

    public SetProperty<Match> matchesProperty() {
        return matches;
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

    public TreeSet<TimeEntry> getTemplate() {
        return template;
    }

    public boolean isTeamsAssigned() {
        return teamsAssigned.get();
    }

    public SimpleBooleanProperty teamsAssignedProperty() {
        return teamsAssigned;
    }

    public TournamentOptions getTournamentOptions() {
        return tournamentOptions;
    }
}
