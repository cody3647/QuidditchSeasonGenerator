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
import javafx.collections.transformation.SortedList;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;
import java.util.Comparator;

public class TournamentOptions {
    static final private LocalDate firstMondayOctober = LocalDate.now().withMonth(Month.OCTOBER.getValue()).with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));
    private final StringProperty leagueName;
    private final ListProperty<MatchDayTime> matchDayTimeList;
    private final SortedList<MatchDayTime> sortedMatchDayTimeList;
    private final ListProperty<BlackoutDates> blackoutDates;
    private final ObjectProperty<LocalDate> startDate;
    private final IntegerProperty matchesPerWeek;

    public TournamentOptions() {
        leagueName = new SimpleStringProperty(this, "leagueName", "");
        matchDayTimeList = new SimpleListProperty<>(this, "matchDayTimes",
                FXCollections.observableArrayList(matchDayTime -> new Observable[]{matchDayTime.dayOfWeekProperty(), matchDayTime.localTimeProperty(), matchDayTime.priorityProperty(), matchDayTime.countProperty()}));
        matchDayTimeList.add(defaultMatchDayTime());
        sortedMatchDayTimeList = new SortedList<>(matchDayTimeList, Comparator.naturalOrder());
        startDate = new SimpleObjectProperty<>(this, "startDate", firstMondayOctober);
        blackoutDates = new SimpleListProperty<>(this, "blackoutDates", FXCollections.observableArrayList(blackoutDate -> new Observable[]{blackoutDate.startProperty(), blackoutDate.endProperty()}));
        matchesPerWeek = new SimpleIntegerProperty(this, "matchesPerWeek", 1);

        // Add listener to matchDayTimeList to change valid start day whenever there is a change to the list
        matchDayTimeList.addListener((ListChangeListener<MatchDayTime>) change ->
                setMatchesPerWeek(getMatchDayTimeList().stream().mapToInt(MatchDayTime::getCount).sum())
        );
    }

    public void clear() {
        leagueName.setValue("");
        matchDayTimeList.clear();
        matchDayTimeList.add(defaultMatchDayTime());
        startDate.setValue(firstMondayOctober);
        blackoutDates.clear();
        matchesPerWeek.set(1);
    }

    public void loadSettings(SaveSettings settings) {
        leagueName.setValue(settings.getLeagueName());
        matchDayTimeList.addAll(settings.getMatchDayTimeList());
        startDate.setValue(settings.getStartDate());
        blackoutDates.addAll(settings.getBlackoutDates());
    }

    private MatchDayTime defaultMatchDayTime() {
        return new MatchDayTime(DayOfWeek.FRIDAY, LocalTime.of(20, 0, 0), 1);
    }

    public String getLeagueName() {
        return leagueName.get();
    }

    public void setLeagueName(String leagueName) {
        this.leagueName.set(leagueName);
    }

    public StringProperty leagueNameProperty() {
        return leagueName;
    }

    public ObservableList<MatchDayTime> getMatchDayTimeList() {
        return matchDayTimeList.get();
    }

    public void setMatchDayTimeList(ObservableList<MatchDayTime> matchDayTimeList) {
        this.matchDayTimeList.set(matchDayTimeList);
    }

    public ListProperty<MatchDayTime> matchDayTimeListProperty() {
        return matchDayTimeList;
    }

    public SortedList<MatchDayTime> getSortedMatchDayTimeList() {
        return sortedMatchDayTimeList;
    }

    public LocalDate getStartDate() {
        return startDate.get();
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate.set(startDate);
    }

    public ObjectProperty<LocalDate> startDateProperty() {
        return startDate;
    }

    public ObservableList<BlackoutDates> getBlackoutDates() {
        return blackoutDates.get();
    }

    public void setBlackoutDates(ObservableList<BlackoutDates> blackoutDates) {
        this.blackoutDates.set(blackoutDates);
    }

    public ListProperty<BlackoutDates> blackoutDatesProperty() {
        return blackoutDates;
    }

    public int getMatchesPerWeek() {
        return matchesPerWeek.get();
    }

    public void setMatchesPerWeek(int matchesPerWeek) {
        this.matchesPerWeek.set(matchesPerWeek);
    }

    public IntegerProperty matchesPerWeekProperty() {
        return matchesPerWeek;
    }
}
