/*
 * Quidditch Season NameGenerator
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

package info.codywilliams.qsg.models;

import info.codywilliams.qsg.generators.NameGenerator;
import info.codywilliams.qsg.models.tournament.Tournament;
import info.codywilliams.qsg.models.tournament.TournamentOptions;
import info.codywilliams.qsg.models.tournament.type.TournamentType;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.io.File;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.*;

public class Context {
    volatile static private Context instance;
    private final ObjectProperty<Team> currentTeam;
    final private ListProperty<Team> teams;
    final private TournamentOptions tournamentOptions;
    final private ObjectProperty<Tournament> currentTournament;
    final private MapProperty<TournamentType, Tournament> tournaments;
    final private IntegerProperty numTeams;
    final private IntegerProperty numLocations;
    final private StringProperty leftStatus;
    final private StringProperty rightStatus;
    final private NameGenerator femaleNames;
    final private NameGenerator maleNames;
    final private NameGenerator nonBinaryNames;
    final private NameGenerator surnames;
    final private NameGenerator teamNames;
    private final Locale locale;
    private final ResourceBundle textBundle;
    private final DateTimeFormatter dateTimeFormatter;
    private final DateTimeFormatter dateFormatter;
    private final DateTimeFormatter timeFormatter;
    private File currentSaveFile;
    private boolean listChangeAndChangeFlag = false;


    private Context() {
        locale = Locale.getDefault();
        dateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM).withLocale(locale).withZone(ZoneId.systemDefault());
        dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale).withZone(ZoneId.systemDefault());
        timeFormatter = DateTimeFormatter.ofLocalizedTime(FormatStyle.MEDIUM).withLocale(locale).withZone(ZoneId.systemDefault());
        textBundle = ResourceBundle.getBundle("info.codywilliams.qsg.language.Text", locale);

        currentTeam = new SimpleObjectProperty<>(this, "currentTeam");
        teams = new SimpleListProperty<>(this, "teams", FXCollections.observableList(new ArrayList<>(),
                team -> new Observable[]{team.nameProperty(), team.homeProperty()}
        ));

        tournamentOptions = TournamentOptions.getInstance();
        currentTournament = new SimpleObjectProperty<>(this, "tournament");
        tournaments = new SimpleMapProperty<>(this, "tournaments", FXCollections.observableHashMap());

        numTeams = new SimpleIntegerProperty(this, "numTeams", 0);
        numLocations = new SimpleIntegerProperty(this, "numLocations", 0);

        numTeams.bind(Bindings.size(teams));

        getTeams().addListener((ListChangeListener<Team>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasReplaced() || change.wasUpdated() || change.wasRemoved())
                    calculateTeamLocations();
            }
        });

        leftStatus = new SimpleStringProperty(this, "leftStatus", textBundle.getString("app.newStatus"));
        rightStatus = new SimpleStringProperty(this, "rightStatus");

        femaleNames = new NameGenerator("femaleNames");
        maleNames = new NameGenerator("maleNames");
        nonBinaryNames = new NameGenerator("nonBinaryNames");
        surnames = new NameGenerator("surnames");
        teamNames = new NameGenerator("teamNames");

        tournamentOptionsListeners();
    }

    public ObservableList<Team> getTeams() {
        return teams.get();
    }

    public void calculateTeamLocations() {
        Set<String> locations = new HashSet<>();
        getTeams().forEach((team) -> locations.add(team.getHome()));
        setNumLocations(locations.size());
    }

    private void tournamentOptionsListeners() {
        ListChangeListener<Object> listChangeListener = change -> {
            if (currentTournament.getValue() != null) {
                boolean flag = true;
                while (change.next()) {
                    if (!change.wasPermutated() && flag) {
                        currentTournament.getValue().recalculateTournament();
                        flag = false;
                        listChangeAndChangeFlag = true;
                    }
                }
            }
        };

        ChangeListener<Object> changeListener = (observableValue, oldObject, newObject) -> {
            if (currentTournament.getValue() != null  && !listChangeAndChangeFlag) {
                currentTournament.getValue().recalculateTournament();
            }
            listChangeAndChangeFlag = false;
        };


        tournamentOptions.blackoutDatesProperty().addListener(listChangeListener);
        tournamentOptions.validStartTimesProperty().addListener(listChangeListener);
        tournamentOptions.roundsPerWeekProperty().addListener(changeListener);
        tournamentOptions.hoursBetweenMatchesProperty().addListener(changeListener);
        tournamentOptions.startDateProperty().addListener(changeListener);
    }

    public static Context getInstance() {
        if (instance == null) {
            synchronized (Context.class) {
                if (instance == null) instance = new Context();
            }
        }
        return instance;
    }

    public void clearContext() {
        currentTeam.set(null);
        teams.clear();
        tournamentOptions.clear();
        currentTournament.set(null);
    }

    public void loadContext(SaveSettings settings) {
        teams.addAll(settings.getTeams());
    }

    public Team getCurrentTeam() {
        return currentTeam.get();
    }

    public void setCurrentTeam(Team currentTeam) {
        this.currentTeam.set(currentTeam);
    }

    public ObjectProperty<Team> currentTeamProperty() {
        return currentTeam;
    }

    public ListProperty<Team> teamsProperty() {
        return teams;
    }

    public String getLeftStatus() {
        return leftStatus.get();
    }

    public void setLeftStatus(String leftStatus) {
        this.leftStatus.set(leftStatus);
    }

    public StringProperty leftStatusProperty() {
        return leftStatus;
    }

    public String getRightStatus() {
        return rightStatus.get();
    }

    public void setRightStatus(String rightStatus) {
        this.rightStatus.set(rightStatus);
    }

    public StringProperty rightStatusProperty() {
        return rightStatus;
    }

    public TournamentOptions getTournamentOptions() {
        return tournamentOptions;
    }

    public Tournament getCurrentTournament() {
        return currentTournament.get();
    }

    public void setCurrentTournament(Tournament currentTournament) {
        this.currentTournament.set(currentTournament);
    }

    public ObjectProperty<Tournament> currentTournamentProperty() {
        return currentTournament;
    }

    public ObservableMap<TournamentType, Tournament> getTournaments() {
        return tournaments.get();
    }

    public MapProperty<TournamentType, Tournament> tournamentsProperty() {
        return tournaments;
    }

    public int getNumTeams() {
        return numTeams.get();
    }

    public void setNumTeams(int numTeams) {
        this.numTeams.set(numTeams);
    }

    public IntegerProperty numTeamsProperty() {
        return numTeams;
    }

    public int getNumLocations() {
        return numLocations.get();
    }

    public void setNumLocations(int numLocations) {
        this.numLocations.set(numLocations);
    }

    public IntegerProperty numLocationsProperty() {
        return numLocations;
    }

    public NameGenerator getFemaleNames() {
        return femaleNames;
    }

    public NameGenerator getMaleNames() {
        return maleNames;
    }

    public NameGenerator getNonBinaryNames() {
        return nonBinaryNames;
    }

    public NameGenerator getSurnames() {
        return surnames;
    }

    public NameGenerator getTeamNames() {
        return teamNames;
    }

    public ResourceBundle getTextBundle() {
        return textBundle;
    }

    public Locale getLocale() {
        return locale;
    }

    public DateTimeFormatter getDateTimeFormatter() {
        return dateTimeFormatter;
    }

    public DateTimeFormatter getDateFormatter() {
        return dateFormatter;
    }

    public DateTimeFormatter getTimeFormatter() {
        return timeFormatter;
    }

    public File getCurrentSaveFile() {
        return currentSaveFile;
    }

    public void setCurrentSaveFile(File currentSaveFile) {
        this.currentSaveFile = currentSaveFile;
    }
}
