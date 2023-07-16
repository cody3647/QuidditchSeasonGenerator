/*
 * Quidditch Season Generator
 * Copyright (C) 2023.  Cody Williams
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

package info.codywilliams.qsg.layout;

import info.codywilliams.qsg.models.Context;
import info.codywilliams.qsg.models.match.Match;
import info.codywilliams.qsg.models.tournament.Tournament;
import info.codywilliams.qsg.models.tournament.TournamentOptions;
import info.codywilliams.qsg.util.DependencyInjector;
import info.codywilliams.qsg.util.Formatters;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ObservableSet;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.ResourceBundle;

public class TournamentCalendar extends GridPane {
    private final ObservableSet<Match> matches;
    private final ObjectProperty<LocalDate> startDate;
    private final ObjectProperty<LocalDate> endDate;
    private final boolean teamsAssigned;
    private final ResourceBundle resourceBundle;

    public TournamentCalendar(Tournament tournament, TournamentOptions tournamentOptions, ResourceBundle resourceBundle) {
        super();
        matches = tournament.getMatches();
        startDate = tournamentOptions.startDateProperty();
        endDate = tournament.endDateProperty();
        teamsAssigned = tournament.isTeamsAssigned();
        this.resourceBundle = resourceBundle;

        if (endDate.get() == null)
            return;

        drawCalendar();
    }

    public static void displayTournamentCalendarWindow(Context context, ResourceBundle resourceBundle) {
        Stage calendarWindow = new Stage();
        calendarWindow.initModality(Modality.NONE);
        ScrollPane scrollPane = new ScrollPane();
        Scene calendarScene = new Scene(scrollPane, 1000, 1000);
        DependencyInjector.addStylesheet(calendarScene, "styles.css");

        DependencyInjector.setUpAndShowStage(calendarWindow, calendarScene, "tournament.calendar.window.title");

        if (context.getCurrentTournament() == null) {
            scrollPane.setContent(new Text("No matches to show yet, please configure the tournament"));
            return;
        }

        if (!context.getCurrentTournament().isTeamsAssigned())
            context.getCurrentTournament().assignTeamsToMatches(context.getTeams(), context.getSeed());

        TournamentCalendar tournamentCalendar = new TournamentCalendar(context.getCurrentTournament(), context.getTournamentOptions(), resourceBundle);
        scrollPane.setContent(tournamentCalendar);
    }

    private void drawCalendar() {
        setAlignment(Pos.CENTER);
        setMinHeight(300);
        ColumnConstraints columnConstraints = new ColumnConstraints();
        columnConstraints.setFillWidth(true);

        for (int i = 0; i < 7; i++) {
            getColumnConstraints().add(columnConstraints);
        }

        LocalDate firstMonday = startDate.get().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // Until and datesUtil are exclusive, so add 1 day to end date for calculations.
        LocalDate dayAfterLast = endDate.get().plusDays(1);
        int totalDates = (int) firstMonday.until(dayAfterLast, ChronoUnit.DAYS);
        TournamentCalendarDay[] calendarDays = new TournamentCalendarDay[totalDates];

        firstMonday.datesUntil(dayAfterLast).forEach(date -> {
            int index = (int) firstMonday.until(date, ChronoUnit.DAYS);
            calendarDays[index] = new TournamentCalendarDay(date);
        });

        for (Match match : matches) {
            int index = (int) firstMonday.until(match.getStartDateTime().toLocalDate(), ChronoUnit.DAYS);
            calendarDays[index].addMatch(match);
        }

        int row = 1;
        Month previousMonth = null;

        String yearRange = startDate.get().getYear() == endDate.get().getYear() ? String.valueOf(startDate.get().getYear()) :
                startDate.get().getYear() + " - " + endDate.get().getYear();

        Label calendarLabel = new Label(yearRange + " Tournament Calendar");
        calendarLabel.getStyleClass().add("calendar-title");
        add(calendarLabel, 0, 0, 7, 1);
        setHalignment(calendarLabel, HPos.CENTER);

        for (TournamentCalendarDay calendarDay : calendarDays) {
            LocalDate date = calendarDay.getDate();
            Month currentMonth = date.getMonth();

            // If the day of week is Monday, advance row by 1 if the month hasn't changed since the month changing will advance the row
            if (date.getDayOfWeek() == DayOfWeek.MONDAY && currentMonth == previousMonth) {
                row++;
            }
            // Check if it is a new month, add the title and advance the row to the return value
            if (currentMonth != previousMonth) {
                previousMonth = currentMonth;
                row = addMonthTitle(date, ++row);
            }

            Node entry = calendarDay.getNode();
            if (date.getDayOfMonth() % 2 == 0)
                entry.getStyleClass().add("calendar-entry-even");
            else
                entry.getStyleClass().add("calendar-entry-odd");
            add(entry, date.getDayOfWeek().getValue() - 1, row);

        }
    }

    private int addMonthTitle(LocalDate date, int row) {
        Label monthTitle = new Label(date.getMonth().getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()) + " " + date.getYear());
        monthTitle.getStyleClass().add("calendar-month-title");
        add(monthTitle, 0, row++, 7, 1);
        setHalignment(monthTitle, HPos.CENTER);

        for (DayOfWeek dayOfWeek : DayOfWeek.values()) {
            HBox dayBox = new HBox();
            dayBox.getStyleClass().add("calendar-header");

            Label dayTitle = new Label(dayOfWeek.getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()));
            dayTitle.getStyleClass().add("calendar-header-title");

            dayBox.getChildren().add(dayTitle);

            add(dayBox, dayOfWeek.getValue() - 1, row);
            setHalignment(dayBox, HPos.CENTER);
        }
        return row + 1;
    }


    private class TournamentCalendarDay {
        final LocalDate date;
        final ArrayList<Match> matches;

        TournamentCalendarDay(LocalDate date) {
            this.date = date;
            matches = new ArrayList<>();
        }

        void addMatch(Match match) {
            matches.add(match);
        }

        public LocalDate getDate() {
            return date;
        }

        public ArrayList<Match> getMatches() {
            return matches;
        }

        public Node getNode() {

            VBox calendarEntry = new VBox();
            calendarEntry.getStyleClass().add("calendar-entry");

            Label dayNumber = new Label(String.valueOf(date.getDayOfMonth()));
            dayNumber.getStyleClass().add("calendar-entry-title");
            HBox hBox = new HBox(dayNumber);
            hBox.setAlignment(Pos.TOP_RIGHT);

            calendarEntry.getChildren().add(hBox);

            Collections.sort(matches);
            for (Match match : matches) {
                Label matchTime = new Label(match.getStartDateTime().format(Formatters.timeFormatter));
                matchTime.getStyleClass().add("calendar-entry-time");
                calendarEntry.getChildren().add(matchTime);

                if (teamsAssigned) {
                    Label teams = new Label(match.getHomeTeam().getName() + " " + resourceBundle.getString("match.versus.abbr") + " " + match.getAwayTeam().getName());
                    teams.getStyleClass().add("calendar-entry-name");
                    calendarEntry.getChildren().add(teams);
                }
            }

            return calendarEntry;
        }

    }
}
