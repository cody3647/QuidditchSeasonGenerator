/*
 * Copyright (c) 2022. Cody Williams
 *
 * Player.java is part of Quidditch Season Generator.
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

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

abstract public class Player {

    String name;
    String[] pronouns;
    @JsonIgnore
    final Map<LocalDate, Boolean> injuryHistory;

    int skillDefense;
    int skillOffense;
    int skillTeamwork;
    int foulLikelihood;

    public Player(String name, String[] pronouns, int skillDefense, int skillOffense, int skillTeamwork, int foulLikelihood){
        this.name = name;
        this.pronouns = pronouns;
        this.skillDefense = skillDefense;
        this.skillOffense = skillOffense;
        this.skillTeamwork = skillTeamwork;
        this.foulLikelihood = foulLikelihood;
        this.injuryHistory = new ConcurrentSkipListMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getPronouns() {
        return pronouns;
    }

    public void setPronouns(String[] pronouns) {
        this.pronouns = pronouns;
    }

    public boolean isInjured(LocalDate date) {
        if (injuryHistory.containsKey(date))
            return injuryHistory.get(date);
        return false;
    }

    public void setInjured(LocalDate date){
        injuryHistory.put(date, true);
    }

    public Map<LocalDate, Boolean> getInjuryHistory() {
        return injuryHistory;
    }

    public int getSkillDefense() {
        return skillDefense;
    }

    public void setSkillDefense(int skillDefense) {
        this.skillDefense = skillDefense;
    }

    public int getSkillOffense() {
        return skillOffense;
    }

    public void setSkillOffense(int skillOffense) {
        this.skillOffense = skillOffense;
    }

    public int getSkillTeamwork() {
        return skillTeamwork;
    }

    public void setSkillTeamwork(int skillTeamwork) {
        this.skillTeamwork = skillTeamwork;
    }

    public int getFoulLikelihood() {
        return foulLikelihood;
    }

    public void setFoulLikelihood(int foulLikelihood) {
        this.foulLikelihood = foulLikelihood;
    }

    @Override
    public String toString() {
        return "{" +
                "name='" + name + '\'' +
                ", pronouns=" + Arrays.toString(pronouns) +
                ", injuryHistory=" + injuryHistory +
                ", skillDefense=" + skillDefense +
                ", skillOffense=" + skillOffense +
                ", foulLikelihood=" + foulLikelihood +
                '}';
    }
}
