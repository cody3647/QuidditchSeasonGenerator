/*
 * Copyright (c) 2022. Cody Williams
 *
 * Settings.java is part of Quidditch Season Generator.
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

package info.codywilliams.settings;

import info.codywilliams.models.Team;

import java.util.List;

public class Settings {
    int year;
    int seed;
    List<Team> teams;
    public Settings (){

    }


    public Settings(List<Team> teams, int year, int seed) {
        this.teams = teams;
        this.year = year;
        this.seed = seed;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }
}
