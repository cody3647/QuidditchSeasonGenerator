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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import info.codywilliams.qsg.models.player.*;
import info.codywilliams.qsg.util.TeamDeserializer;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDate;
import java.util.*;

@JsonDeserialize(using = TeamDeserializer.class)
@JsonPropertyOrder({"name", "shortName", "home"})
public class Team implements Comparable<Team> {
    final static public int TOTAL_BEATERS = 4;
    final static public int TOTAL_CHASERS = 6;
    final static public int TOTAL_KEEPERS = 2;
    final static public int TOTAL_SEEKERS = 2;

    final private StringProperty name;
    final private StringProperty shortName;
    final private StringProperty home;
    final private ListProperty<Beater> beaters;
    final private ListProperty<Chaser> chasers;
    final private ListProperty<Keeper> keepers;
    final private ListProperty<Seeker> seekers;

    public Team() {
        name = new SimpleStringProperty(this, "name", "");
        shortName = new SimpleStringProperty(this, "shortName", "");
        home = new SimpleStringProperty(this, "home", "");
        beaters = new SimpleListProperty<>(this, "beaters", FXCollections.observableArrayList());
        chasers = new SimpleListProperty<>(this, "chasers", FXCollections.observableArrayList());
        keepers = new SimpleListProperty<>(this, "keepers", FXCollections.observableArrayList());
        seekers = new SimpleListProperty<>(this, "seekers", FXCollections.observableArrayList());
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

    public String getShortName() {
        if(shortName.get().isEmpty())
            return getName();

        return shortName.get();
    }

    public StringProperty shortNameProperty() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName.set(shortName);
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

        return "Team {\n\tName: " + getName() +
                "\n\tShort Name: " + getShortName() +
                "\n\tHome: " + getHome() +
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Team team = (Team) o;

        if (!Objects.equals(name, team.name)) return false;
        if (!Objects.equals(home, team.home)) return false;
        if (!Objects.equals(beaters, team.beaters)) return false;
        if (!Objects.equals(chasers, team.chasers)) return false;
        if (!Objects.equals(keepers, team.keepers)) return false;
        return Objects.equals(seekers, team.seekers);
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (home != null ? home.hashCode() : 0);
        result = 31 * result + (beaters != null ? beaters.hashCode() : 0);
        result = 31 * result + (chasers != null ? chasers.hashCode() : 0);
        result = 31 * result + (keepers != null ? keepers.hashCode() : 0);
        result = 31 * result + (seekers != null ? seekers.hashCode() : 0);
        return result;
    }
}
