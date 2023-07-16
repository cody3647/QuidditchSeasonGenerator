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

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class Context {
    private final ObjectProperty<Team> currentTeam;
    final private ListProperty<Team> teams;
    final private TournamentOptions tournamentOptions;
    final private ObjectProperty<Tournament> currentTournament;
    final private MapProperty<TournamentType, Tournament> tournaments;
    final private LongProperty seed;
    final private IntegerProperty numTeams;
    final private IntegerProperty numLocations;
    final private StringProperty leftStatus;
    final private StringProperty rightStatus;
    private final Mediawiki mediawiki;
    private File currentSaveFile;
    private boolean listChangeAndChangeFlag = false;


    public Context() {
        currentTeam = new SimpleObjectProperty<>(this, "currentTeam");
        teams = new SimpleListProperty<>(this, "teams", FXCollections.observableList(new ArrayList<>(),
                team -> new Observable[]{team.nameProperty(), team.homeProperty()}
        ));

        tournamentOptions = new TournamentOptions();
        currentTournament = new SimpleObjectProperty<>(this, "tournament");
        tournaments = new SimpleMapProperty<>(this, "tournaments", FXCollections.observableHashMap());
        seed = new SimpleLongProperty(this, "seed", new Random().nextLong());

        numTeams = new SimpleIntegerProperty(this, "numTeams", 0);
        numLocations = new SimpleIntegerProperty(this, "numLocations", 0);

        leftStatus = new SimpleStringProperty(this, "leftStatus");
        rightStatus = new SimpleStringProperty(this, "rightStatus");

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

    public File getCurrentSaveFile() {
        return currentSaveFile;
    }

    public void setCurrentSaveFile(File currentSaveFile) {
        this.currentSaveFile = currentSaveFile;
    }

    public void changeCurrentTournament(TournamentType type) {
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
}
