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

import info.codywilliams.qsg.models.tournament.Tournament;
import info.codywilliams.qsg.models.tournament.TournamentOptions;
import info.codywilliams.qsg.models.tournament.type.TournamentType;
import info.codywilliams.qsg.service.Mediawiki;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Context {
    private final ObjectProperty<Team> currentTeam;
    final private ListProperty<Team> teams;
    final private TournamentOptions tournamentOptions;
    final private ObjectProperty<TournamentType> currentType;
    final private ObjectProperty<Tournament> currentTournament;
    final private MapProperty<TournamentType, Tournament> tournaments;
    final private LongProperty seed;
    final private IntegerProperty numTeams;
    final private IntegerProperty numLocations;
    final private StringProperty settingsStatus;
    final private StringProperty outputStatus;
    final private BooleanProperty loggedInToMediawiki;
    private final BooleanProperty matchesReady;
    private final Mediawiki mediawiki;
    private boolean listChangeAndChangeFlag = false;


    public Context() {
        currentTeam = new SimpleObjectProperty<>(this, "currentTeam");
        teams = new SimpleListProperty<>(this, "teams", FXCollections.observableList(new ArrayList<>(),
                team -> new Observable[]{team.nameProperty(), team.homeProperty()}
        ));

        tournamentOptions = new TournamentOptions();
        currentTournament = new SimpleObjectProperty<>(this, "currentTournament");
        currentType = new SimpleObjectProperty<>(this, "currentTournamentType");
        tournaments = new SimpleMapProperty<>(this, "tournaments", FXCollections.observableHashMap());
        seed = new SimpleLongProperty(this, "seed", new Random().nextLong());

        numTeams = new SimpleIntegerProperty(this, "numTeams", 0);
        numLocations = new SimpleIntegerProperty(this, "numLocations", 0);

        settingsStatus = new SimpleStringProperty(this, "settingsStatus");
        outputStatus = new SimpleStringProperty(this, "outputStatus");
        loggedInToMediawiki = new SimpleBooleanProperty(this, "loggedInToMediawiki", false);
        matchesReady = new SimpleBooleanProperty(this, "matchesReady", false);

        mediawiki = new Mediawiki();

        teamListenersAndBindings();
        tournamentListeners();
    }

    public ObservableList<Team> getTeams() {
        return teams.get();
    }

    public void calculateTeamLocations() {
        Set<String> locations = new HashSet<>();
        getTeams().forEach((team) -> locations.add(team.getHome()));
        setNumLocations(locations.size());
    }

    private void teamListenersAndBindings() {
        numTeams.bind(Bindings.size(teams));
        numTeams.addListener((observableValue, oldNumTeams, newNumTeams) -> {
            if (currentTournament.get() != null)
                currentTournament.get().recalculateTournament(numTeams.get());
        });

        getTeams().addListener((ListChangeListener<Team>) change -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasReplaced() || change.wasUpdated() || change.wasRemoved())
                    calculateTeamLocations();
            }
        });

        matchesReady.bind(Bindings.and(teamsProperty().emptyProperty().not(), currentTournamentProperty().isNotNull()).and(tournamentOptions.leagueNameProperty().isEmpty().not()));
    }

    private void tournamentListeners() {
        ListChangeListener<Object> listChangeListener = change -> {
            if (currentTournament.getValue() != null) {
                boolean flag = true;
                while (change.next()) {
                    if (!change.wasPermutated() && flag) {
                        currentTournament.getValue().recalculateTournament(numTeams.get());
                        flag = false;
                        listChangeAndChangeFlag = true;
                    }
                }
            }
        };

        ChangeListener<Object> changeListener = (observableValue, oldObject, newObject) -> {
            if (currentTournament.getValue() != null && !listChangeAndChangeFlag) {
                currentTournament.getValue().recalculateTournament(numTeams.get());
            }
            listChangeAndChangeFlag = false;
        };


        tournamentOptions.blackoutDatesProperty().addListener(listChangeListener);
        tournamentOptions.matchDayTimeListProperty().addListener(listChangeListener);
        tournamentOptions.startDateProperty().addListener(changeListener);
    }

    public void clearContext() {
        currentTeam.set(null);
        teams.clear();
        currentTournament.set(null);
        currentType.set(null);
        tournaments.clear();
        tournamentOptions.clear();
        seed.set(new Random().nextLong());
    }

    public void loadContext(SaveSettings settings) {
        teams.addAll(settings.getTeams());
        tournamentOptions.loadSettings(settings);
        changeCurrentTournament(settings.getTournamentType());
        seed.set(settings.getSeed());

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

    public String getSettingsStatus() {
        return settingsStatus.get();
    }

    public void setSettingsStatus(String settingsStatus) {
        this.settingsStatus.set(settingsStatus);
    }

    public StringProperty settingsStatusProperty() {
        return settingsStatus;
    }

    public String getOutputStatus() {
        return outputStatus.get();
    }

    public void setOutputStatus(String outputStatus) {
        this.outputStatus.set(outputStatus);
    }

    public StringProperty outputStatusProperty() {
        return outputStatus;
    }

    public TournamentOptions getTournamentOptions() {
        return tournamentOptions;
    }

    public TournamentType getCurrentType() {
        return currentType.get();
    }

    public ObjectProperty<TournamentType> currentTypeProperty() {
        return currentType;
    }

    public void setCurrentType(TournamentType currentType) {
        this.currentType.set(currentType);
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

    public long getSeed() {
        return seed.get();
    }

    public void setSeed(long seed) {
        this.seed.set(seed);
    }

    public LongProperty seedProperty() {
        return seed;
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

    public void changeCurrentTournament(TournamentType type) {
        if (type == null)
            return;

        if (!getTournaments().containsKey(type)) {
            try {
                getTournaments().put(type, type.getConstructor().newInstance(getTournamentOptions()));
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
        }
        setCurrentTournament(getTournaments().get(type));
        getCurrentTournament().recalculateTournament(getNumTeams());
    }

    public Mediawiki getMediawiki() {
        return mediawiki;
    }

    public boolean isLoggedInToMediawiki() {
        return loggedInToMediawiki.get();
    }

    public BooleanProperty loggedInToMediawikiProperty() {
        return loggedInToMediawiki;
    }

    public void setLoggedInToMediawiki(boolean loggedInToMediawiki) {
        this.loggedInToMediawiki.set(loggedInToMediawiki);
    }

    public boolean isMatchesReady() {
        return matchesReady.get();
    }

    public BooleanProperty matchesReadyProperty() {
        return matchesReady;
    }

    public void setMatchesReady(boolean matchesReady) {
        this.matchesReady.set(matchesReady);
    }
}
