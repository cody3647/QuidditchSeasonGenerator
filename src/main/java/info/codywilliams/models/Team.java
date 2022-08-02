/*
 * Copyright (c) 2022. Cody Williams
 *
 * Team.java is part of Quidditch Season Generator.
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

package info.codywilliams.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import info.codywilliams.models.player.Beater;
import info.codywilliams.models.player.Chaser;
import info.codywilliams.models.player.Keeper;
import info.codywilliams.models.player.Seeker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;


public class Team implements Comparable<Team> {

    private List<Chaser> chasers;
    private List<Keeper> keepers;
    private List<Beater> beaters;
    private List<Seeker> seekers;
    private String name;
    private String home;

    public Team() {
    }

    public Team(String name, String home) {
        this.name = name;
        this.home = home;
        chasers = new ArrayList<>();
        keepers = new ArrayList<>();
        beaters = new ArrayList<>();
        seekers = new ArrayList<>();
    }

    public Team(List<Chaser> chasers, List<Keeper> keepers, List<Beater> beaters, List<Seeker> seekers, String name, String home) {
        this.chasers = chasers;
        this.keepers = keepers;
        this.beaters = beaters;
        this.seekers = seekers;
        this.name = name;
        this.home = home;
    }

    public boolean addChaser(Chaser chaser) {
        if (chasers.size() > teamSize.CHASERS.getTotalNum()) {
            chasers.add(chaser);
            return true;
        } else
            return false;
    }

    public boolean addKeeper(Keeper keeper) {
        if (keepers.size() > teamSize.KEEPERS.getTotalNum()) {
            keepers.add(keeper);
            return true;
        } else
            return false;
    }

    public boolean addBeater(Beater beater) {
        if (beaters.size() > teamSize.BEATERS.getTotalNum()) {
            beaters.add(beater);
            return true;
        } else
            return false;
    }

    public boolean addSeeker(Seeker seeker) {
        if (seekers.size() > teamSize.CHASERS.getTotalNum()) {
            seekers.add(seeker);
            return true;
        } else
            return false;
    }

    public List<Chaser> getChasers(LocalDate date) {
        List<Chaser> chasers = new ArrayList<>();
        Chaser chaser;
        Iterator<Chaser> iter = this.chasers.iterator();
        while (iter.hasNext() && chasers.size() <= teamSize.CHASERS.getStartNum()) {
            chaser = iter.next();
            if (!chaser.isInjured(date)) {
                chasers.add(chaser);
            }
        }

        return chasers;
    }

    public List<Keeper> getKeepers(LocalDate date) {
        List<Keeper> keepers = new ArrayList<>();
        Keeper keeper;
        Iterator<Keeper> iter = this.keepers.iterator();
        while (iter.hasNext() && keepers.size() <= teamSize.KEEPERS.getStartNum()) {
            keeper = iter.next();
            if (!keeper.isInjured(date)) {
                keepers.add(keeper);
            }
        }

        return keepers;
    }

    public List<Beater> getBeaters(LocalDate date) {
        List<Beater> beaters = new ArrayList<>();
        Beater beater;
        Iterator<Beater> iter = this.beaters.iterator();
        while (iter.hasNext() && beaters.size() <= teamSize.BEATERS.getStartNum()) {
            beater = iter.next();
            if (!beater.isInjured(date)) {
                beaters.add(beater);
            }
        }

        return beaters;
    }

    public List<Seeker> getSeekers(LocalDate date) {
        List<Seeker> seekers = new ArrayList<>();
        Seeker seeker;
        Iterator<Seeker> iter = this.seekers.iterator();
        while (iter.hasNext() && seekers.size() <= teamSize.SEEKERS.getStartNum()) {
            seeker = iter.next();
            if (!seeker.isInjured(date)) {
                seekers.add(seeker);
            }
        }

        return seekers;
    }

    public String getHome() {
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }

    public void setChasers(List<Chaser> chasers) {
        this.chasers = chasers;
    }

    public void setKeepers(List<Keeper> keepers) {
        this.keepers = keepers;
    }

    public void setBeaters(List<Beater> beaters) {
        this.beaters = beaters;
    }

    public void setSeekers(List<Seeker> seekers) {
        this.seekers = seekers;
    }

    @Override
    public String toString() {
        return "Team{" +
                "\n\tname='" + name + '\'' +
                "\n\thome='" + home + '\'' +
                "\n\tchasers=" + playerListToString(chasers) +
                "\n\tkeepers=" + playerListToString(keepers) +
                "\n\tbeaters=" + playerListToString(beaters) +
                "\n\tseekers=" + playerListToString(seekers) +
                '}';
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
        return name.compareTo(o.getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj instanceof Team) {
            Team other = (Team) obj;
            return name.equals(other.name) && beaters.equals(other.getAllBeaters()) && chasers.equals(other.getAllChasers()) && keepers.equals(other.getAllKeepers()) && seekers.equals(getAllSeekers());
        }
        return false;
    }

    @JsonIgnore
    public List<Beater> getAllBeaters() {
        return beaters;
    }

    @JsonIgnore
    public List<Chaser> getAllChasers() {
        return chasers;
    }

    @JsonIgnore
    public List<Keeper> getAllKeepers() {
        return keepers;
    }

    @JsonIgnore
    public List<Seeker> getAllSeekers() {
        return seekers;
    }

    @Override
    public int hashCode() {
        return Objects.hash(chasers, keepers, beaters, seekers, name, home);
    }

    public enum teamSize {
        CHASERS(6),
        KEEPERS(2),
        BEATERS(4),
        SEEKERS(2);

        private final int num;

        teamSize(int num) {
            this.num = num;
        }

        public int getTotalNum() {
            return this.num;
        }

        public int getStartNum() {
            return this.num / 2;
        }
    }
}
