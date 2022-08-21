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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import info.codywilliams.qsg.models.player.*;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

@JsonPropertyOrder({"name", "home"})
public class Team implements Comparable<Team> {
    final static public int TOTAL_BEATERS = 4;
    final static public int TOTAL_CHASERS = 6;
    final static public int TOTAL_KEEPERS = 2;
    final static public int TOTAL_SEEKERS = 2;
    final static public int STARTING_BEATERS = 2;
    final static public int STARTING_CHASERS = 3;
    final static public int STARTING_KEEPERS = 1;
    final static public int STARTING_SEEKERS = 1;

    final private StringProperty name;
    final private StringProperty home;
    final private ListProperty<Beater> beaters;
    final private ListProperty<Chaser> chasers;
    final private ListProperty<Keeper> keepers;
    final private ListProperty<Seeker> seekers;

    public Team() {
        name = new SimpleStringProperty(this, "name", "");
        home = new SimpleStringProperty(this, "home", "");
        beaters = new SimpleListProperty<>(FXCollections.observableArrayList());
        chasers = new SimpleListProperty<>(FXCollections.observableList(new LinkedList<>()));
        keepers = new SimpleListProperty<>(FXCollections.observableArrayList());
        seekers = new SimpleListProperty<>(FXCollections.observableArrayList());
    }

    @JsonIgnore
    public List<Beater> getStartingBeaters(LocalDate date) {
        List<Beater> beaters = new ArrayList<>();
        Beater beater;
        Iterator<Beater> iter = this.beaters.iterator();
        while (iter.hasNext() && beaters.size() <= STARTING_BEATERS) {
            beater = iter.next();
            if (beater.isInjured(date)) {
                beaters.add(beater);
            }
        }

        return beaters;
    }

    @JsonIgnore
    public List<Chaser> getStartingChasers(LocalDate date) {
        List<Chaser> chasers = new ArrayList<>();
        Chaser chaser;
        Iterator<Chaser> iter = this.chasers.iterator();
        while (iter.hasNext() && chasers.size() <= STARTING_CHASERS) {
            chaser = iter.next();
            if (chaser.isInjured(date)) {
                chasers.add(chaser);
            }
        }

        return chasers;
    }

    @JsonIgnore
    public List<Keeper> getStartingKeepers(LocalDate date) {
        List<Keeper> keepers = new ArrayList<>();
        Keeper keeper;
        Iterator<Keeper> iter = this.keepers.iterator();
        while (iter.hasNext() && keepers.size() <= STARTING_KEEPERS) {
            keeper = iter.next();
            if (keeper.isInjured(date)) {
                keepers.add(keeper);
            }
        }

        return keepers;
    }

    @JsonIgnore
    public List<Seeker> getStartingSeeker(LocalDate date) {
        List<Seeker> seekers = new ArrayList<>();
        Seeker seeker;
        Iterator<Seeker> iter = this.seekers.iterator();
        while (iter.hasNext() && seekers.size() <= STARTING_SEEKERS) {
            seeker = iter.next();
            if (seeker.isInjured(date)) {
                seekers.add(seeker);
            }
        }

        return seekers;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getHome() {
        return home.get();
    }

    public StringProperty homeProperty() {
        return home;
    }

    public void setHome(String home) {
        this.home.set(home);
    }

    public ObservableList<Beater> getBeaters() {
        return beaters.get();
    }

    public ListProperty<Beater> beatersProperty() {
        return beaters;
    }

    public void setBeaters(ObservableList<Beater> beaters) {
        this.beaters.set(beaters);
    }

    public ObservableList<Chaser> getChasers() {
        return chasers.get();
    }

    public ListProperty<Chaser> chasersProperty() {
        return chasers;
    }

    public void setChasers(ObservableList<Chaser> chasers) {
        this.chasers.set(chasers);
    }

    public ObservableList<Keeper> getKeepers() {
        return keepers.get();
    }

    public ListProperty<Keeper> keepersProperty() {
        return keepers;
    }

    public void setKeepers(ObservableList<Keeper> keepers) {
        this.keepers.set(keepers);
    }

    public ObservableList<Seeker> getSeekers() {
        return seekers.get();
    }

    public ListProperty<Seeker> seekersProperty() {
        return seekers;
    }

    public void setSeekers(ObservableList<Seeker> seekers) {
        this.seekers.set(seekers);
    }

    @Override
    public String toString() {

        return "Team {\n\tName: " + getName() + "\n\tHome: " + getHome() +
                "\n\tBeaters: [" +
                playerListToString(getBeaters()) +
                "\n\t]" +
                "\n\tChasers: [" +
                playerListToString(getChasers()) +
                "\n\t]" +
                "\n\tKeepers: [" +
                playerListToString(getKeepers()) +
                "\n\t]" +
                "\n\tSeekers: [" +
                playerListToString(getSeekers()) +
                "\n\t]" +
                "\n}";
    }

    private String playerListToString(List<? extends Player> list) {
        StringBuilder sb = new StringBuilder();
        for (Player player : list) {
            sb.append("\n\t\t");
            sb.append(player.toString());
        }
        return sb.toString();
    }

    @Override
    public int compareTo(Team o) {
        return getName().compareTo(o.getName());
    }

}
