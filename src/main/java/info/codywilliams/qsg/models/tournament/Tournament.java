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

import com.fasterxml.jackson.annotation.JsonIgnore;
import info.codywilliams.qsg.models.Team;
import info.codywilliams.qsg.models.match.Match;
import info.codywilliams.qsg.models.tournament.type.TournamentType;
import info.codywilliams.qsg.util.Formatters;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.collections.ObservableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Tournament {
    protected ObjectProperty<TournamentType> type;
    protected IntegerProperty numWeeks;
    protected IntegerProperty numMatches;
    protected IntegerProperty numRounds;
    protected IntegerProperty numMatchesPerRound;
    protected SetProperty<Match> matches;
    protected ObjectProperty<LocalDate> endDate;
    protected StringBinding endDateStringBinding;
    protected TournamentOptions tournamentOptions;
    protected SimpleMapProperty<String, Integer> tournamentPoints;
    protected SimpleBooleanProperty teamsAssigned;
    @JsonIgnore
    protected ArrayList<Team> teamList;

    protected Logger logger = LoggerFactory.getLogger(Tournament.class);

    public Tournament(TournamentOptions tournamentOptions, TournamentType type) {

        this.type = new SimpleObjectProperty<>(this, "type", type);
        numWeeks = new SimpleIntegerProperty(this, "numWeeks", 0);
        numMatches = new SimpleIntegerProperty(this, "numMatches", 0);
        numRounds = new SimpleIntegerProperty(this, "numRounds", 0);

        numMatchesPerRound = new SimpleIntegerProperty(this, "numMatchesPerRound", 0);

        matches = new SimpleSetProperty<>(this, "matches", FXCollections.observableSet(new TreeSet<>()));
        endDate = new SimpleObjectProperty<>(this, "endDate");
        this.tournamentOptions = tournamentOptions;
        tournamentPoints = new SimpleMapProperty<>(this, "tournamentPoints", FXCollections.observableMap(new ConcurrentHashMap<>()));


        endDateStringBinding = Bindings.createStringBinding(() -> {
            if (endDateProperty().getValue() == null)
                return "";
            return endDateProperty().getValue().format(Formatters.dateFormatter);
        }, endDateProperty());

        teamsAssigned = new SimpleBooleanProperty(this, "teamsAssigned", false);
    }


    public void recalculateTournament(int numTeams) {
        if (numTeams < 2)
            return;

        calculateNums(numTeams);
        LocalDate lastMatchDate = calculateMatchDates();
        setEndDate(lastMatchDate);
        if (teamList != null)
            teamList.clear();
        teamsAssigned.set(false);
    }

    protected void calculateNums(int numTeams) {
        int totalMatches = calculateTotalMatches(numTeams);
        int totalRounds = calculateTotalRounds(numTeams);

        if (zeroCheckNums(totalMatches, totalRounds)) return;

        setNumMatches(totalMatches);
        setNumRounds(totalRounds);

        int matchesPerRound = calculateMatchesPerRound(numTeams);
        setNumMatchesPerRound(matchesPerRound);
    }

    protected abstract int calculateTotalMatches(int numTeams);

    protected abstract int calculateTotalRounds(int numTeams);

    protected abstract int calculateMatchesPerRound(int numTeams);

    protected boolean zeroCheckNums(int totalMatches, int totalRounds) {
        if (totalMatches <= 0 && totalRounds <= 0) {
            setNumMatches(0);
            setNumRounds(0);
            setNumWeeks(0);
            setNumMatchesPerRound(0);
            return true;
        }
        return false;
    }

    protected abstract LocalDate calculateMatchDates();

    @JsonIgnore
    protected Set<LocalDate> getBlackoutDateSet() {
        Set<LocalDate> dates = new HashSet<>();

        for (BlackoutDates blackoutDates : tournamentOptions.getBlackoutDates()) {
            LocalDate date = blackoutDates.getStart();
            while (date.isBefore(blackoutDates.getEnd()) || date.isEqual(blackoutDates.getEnd())) {
                dates.add(date);
                date = date.plusDays(1);
            }
        }

        return dates;
    }

    public abstract TreeSet<Match> assignTeamsToMatches(List<Team> teams, long seed);


    public abstract void assignPoints();

    public abstract String getPoints(Match match);

    public TournamentType getType() {
        return type.get();
    }

    public void setType(TournamentType type) {
        this.type.set(type);
    }

    public ObjectProperty<TournamentType> typeProperty() {
        return type;
    }

    public int getNumWeeks() {
        return numWeeks.get();
    }

    public void setNumWeeks(int numWeeks) {
        this.numWeeks.set(numWeeks);
    }

    public IntegerProperty numWeeksProperty() {
        return numWeeks;
    }

    public int getNumMatches() {
        return numMatches.get();
    }

    public void setNumMatches(int numMatches) {
        this.numMatches.set(numMatches);
    }

    public IntegerProperty numMatchesProperty() {
        return numMatches;
    }

    public int getNumRounds() {
        return numRounds.get();
    }

    public void setNumRounds(int numRounds) {
        this.numRounds.set(numRounds);
    }

    public IntegerProperty numRoundsProperty() {
        return numRounds;
    }

    public int getNumMatchesPerRound() {
        return numMatchesPerRound.get();
    }

    public void setNumMatchesPerRound(int numMatchesPerRound) {
        this.numMatchesPerRound.set(numMatchesPerRound);
    }

    public IntegerProperty numMatchesPerRoundProperty() {
        return numMatchesPerRound;
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

    public void setEndDate(LocalDate endDate) {
        this.endDate.set(endDate);
    }

    public ObjectProperty<LocalDate> endDateProperty() {
        return endDate;
    }

    public StringBinding endDateStringBinding() {
        return endDateStringBinding;
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

    public ObservableMap<String, Integer> getTournamentPoints() {
        return tournamentPoints.get();
    }

    public SimpleMapProperty<String, Integer> tournamentPointsProperty() {
        return tournamentPoints;
    }

    public ArrayList<Team> getTeamList() {
        return teamList;
    }

    public void setTeamList(ArrayList<Team> teamList) {
        this.teamList = teamList;
    }
}
