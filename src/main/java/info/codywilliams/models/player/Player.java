/*
 * Copyright (c) 2022. Cody Williams
 *
 * Player.java is part of Quidditch Season NameGenerator.
 *
 * Quidditch Season NameGenerator is free software: you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Quidditch Season NameGenerator is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar.
 * If not, see <https://www.gnu.org/licenses/>.
 */

package info.codywilliams.models.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;
import java.util.TreeSet;

abstract public class Player implements Serializable, Comparable<Player> {
    final static int MAX = 10;
    final static int MIN = 1;
    final private StringProperty name;
    @JsonIgnore
    final private SetProperty<LocalDate> injuryHistory;

    final private IntegerProperty skillDefense;
    final private IntegerProperty skillOffense;
    final private IntegerProperty skillTeamwork;
    final private IntegerProperty foulLikelihood;

    @JsonIgnore
    final private NumberBinding skillLevel;

    public Player() {
        name = new SimpleStringProperty(this, "name", "");
        injuryHistory = new SimpleSetProperty<>(this, "injuryHistory", FXCollections.observableSet(new TreeSet<>()));
        skillDefense = new SimpleIntegerProperty(this, "skillDefense");
        skillOffense = new SimpleIntegerProperty(this, "skillOffense");
        skillTeamwork = new SimpleIntegerProperty(this, "skillTeamwork");
        foulLikelihood = new SimpleIntegerProperty(this, "foulLikelihood");
        skillLevel = skillDefense.add(skillOffense).add(skillTeamwork).subtract(foulLikelihood);
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public ObservableSet<LocalDate> getInjuryHistory() {
        return injuryHistory.get();
    }

    public void setInjuryHistory(ObservableSet<LocalDate> injuryHistory) {
        this.injuryHistory.set(injuryHistory);
    }

    public SetProperty<LocalDate> injuryHistoryProperty() {
        return injuryHistory;
    }

    public boolean isInjured(LocalDate date) {
        return injuryHistory.contains(date);
    }

    public boolean addInjuryDate(LocalDate date){
        return injuryHistory.add(date);
    }

    public static boolean validateSkill(int skillNumber){
        return skillNumber >= MIN && skillNumber <= MAX;
    }

    public int getSkillDefense() {
        return skillDefense.get();
    }

    public void setSkillDefense(int skillDefense) {
        if(skillDefense > MAX)
            skillDefense = MAX;
        else if(skillDefense < MIN)
            skillDefense = MIN;

        this.skillDefense.set(skillDefense);
    }

    public IntegerProperty skillDefenseProperty() {
        return skillDefense;
    }

    public int getSkillOffense() {
        return skillOffense.get();
    }

    public void setSkillOffense(int skillOffense) {
        if(skillOffense > MAX)
        skillOffense = MAX;
        else if(skillOffense < MIN)
            skillOffense = MIN;

        this.skillOffense.set(skillOffense);
    }

    public IntegerProperty skillOffenseProperty() {
        return skillOffense;
    }

    public int getSkillTeamwork() {
        return skillTeamwork.get();
    }

    public void setSkillTeamwork(int skillTeamwork) {
        if(skillTeamwork > MAX)
            skillTeamwork = MAX;
        else if(skillTeamwork < MIN)
            skillTeamwork = MIN;
        this.skillTeamwork.set(skillTeamwork);
    }

    public IntegerProperty skillTeamworkProperty() {
        return skillTeamwork;
    }

    public int getFoulLikelihood() {
        return foulLikelihood.get();
    }

    public void setFoulLikelihood(int foulLikelihood) {
        if(foulLikelihood > MAX)
            foulLikelihood = MAX;
        else if(foulLikelihood < MIN)
            foulLikelihood = MIN;
        this.foulLikelihood.set(foulLikelihood);
    }

    public IntegerProperty foulLikelihoodProperty() {
        return foulLikelihood;
    }

    public int getSkillLevel(){
        return skillLevel.intValue();
    }

    @Override
    public String toString() {
         return String.format("{Name: %-25s SkillLevel: %2d, SkillOffense: %2d, Skill Defense: %2d, SkillTeamwork: %2d, FoulLikelihood: %2d, InjuryHistory: %s}",
                 getName(), getSkillLevel(), getSkillOffense(), getSkillDefense(), getSkillTeamwork(), getFoulLikelihood(), getInjuryHistory());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player)) return false;

        Player player = (Player) o;



        if (!Objects.equals(name, player.name)) return false;
        if (!Objects.equals(injuryHistory, player.injuryHistory))
            return false;
        if (!Objects.equals(skillDefense, player.skillDefense))
            return false;
        if (!Objects.equals(skillOffense, player.skillOffense))
            return false;
        if (!Objects.equals(skillTeamwork, player.skillTeamwork))
            return false;
        return Objects.equals(foulLikelihood, player.foulLikelihood);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (injuryHistory.hashCode());
        result = 31 * result + (skillDefense.hashCode());
        result = 31 * result + (skillOffense.hashCode());
        result = 31 * result + (skillTeamwork.hashCode());
        result = 31 * result + (foulLikelihood.hashCode());
        return result;
    }

    @Override
    public int compareTo(Player o) {
        int diff = (getSkillDefense() + getSkillOffense() + getSkillTeamwork() - getFoulLikelihood()) -
                (o.getSkillDefense() + o.getSkillOffense() + o.getSkillTeamwork() - o.getFoulLikelihood());
        if(diff == 0)
            return getName().compareTo(o.getName());
        return diff;
    }
}
