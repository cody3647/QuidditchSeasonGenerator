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

package info.codywilliams.qsg.models.player;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.binding.NumberBinding;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.time.LocalDate;
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
    private double defenseModifier;
    @JsonIgnore
    private double offenceModifier;
    @JsonIgnore
    private double teamworkModifier;
    @JsonIgnore
    private double foulModifier;

    @JsonIgnore
    final private NumberBinding skillLevel;

    @JsonIgnore
    public boolean isCurrentlyInjured = false;
    @JsonIgnore
    private double injuryDivisor = 2;

    public Player() {
        name = new SimpleStringProperty(this, "name", "");
        injuryHistory = new SimpleSetProperty<>(this, "injuryHistory", FXCollections.observableSet(new TreeSet<>()));
        skillDefense = new SimpleIntegerProperty(this, "skillDefense", 1);
        skillOffense = new SimpleIntegerProperty(this, "skillOffense", 1);
        skillTeamwork = new SimpleIntegerProperty(this, "skillTeamwork", 1);
        foulLikelihood = new SimpleIntegerProperty(this, "foulLikelihood", 1);
        skillLevel = skillDefense.add(skillOffense).add(skillTeamwork).subtract(foulLikelihood);
    }

    public String getName() {
        return name.get();
    }

    @JsonIgnore
    public String getShortName() {
        String[] nameParts = getName().split(" ");
        if(nameParts.length == 1)
            return getName();

        return nameParts[0].charAt(0) + ". " + nameParts[nameParts.length - 1];
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

    public boolean isCurrentlyInjured() {
        return isCurrentlyInjured;
    }

    public void setCurrentlyInjured(boolean currentlyInjured) {
        isCurrentlyInjured = currentlyInjured;
    }

    public double getInjuryDivisor() {
        return injuryDivisor;
    }

    public void setInjuryDivisor(double injuryDivisor) {
        this.injuryDivisor = injuryDivisor;
    }

    public void addInjuryDate(LocalDate startDate, LocalDate endDate){
        LocalDate date = startDate;
        while(date.isBefore(endDate) || date.isEqual(endDate)) {
            injuryHistory.add(date);
            date = date.plusDays(1);
        }
    }

    public @Nullable LocalDate findInjuryEndDate(LocalDate startDate) {
        if (!isInjured(startDate))
            return null;
        LocalDate date = startDate;
        while(isInjured(date)) {
            date = date.plusDays(1);
        }

        return date.minusDays(1);
    }

    public static int validateSkill(int skillNumber){
        if(skillNumber < MIN)
            skillNumber = MIN;
        else if (skillNumber > MAX) {
            skillNumber = MAX;
        }

        return skillNumber;
    }

    public static int validateSkill(Integer skillNumber){
        if(skillNumber == null)
            return 1;

        return validateSkill(skillNumber.intValue());

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
        defenseModifier = (double) skillDefense / MAX;
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
        offenceModifier = (double) skillOffense / MAX;
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
        teamworkModifier = (double) skillTeamwork / MAX;
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
        foulModifier = (double) foulLikelihood / MAX;
    }

    public IntegerProperty foulLikelihoodProperty() {
        return foulLikelihood;
    }

    public int getSkillLevel(){
        return skillLevel.intValue();
    }

    public double getDefenseModifier() {
        return isCurrentlyInjured ? defenseModifier / injuryDivisor : defenseModifier;
    }

    public double getOffenceModifier() {
        return isCurrentlyInjured ? offenceModifier / injuryDivisor : offenceModifier;
    }

    public double getTeamworkModifier() {
        return isCurrentlyInjured ? teamworkModifier / injuryDivisor : teamworkModifier;
    }

    public double getFoulModifier() {
        return foulModifier;
    }

    public double getModifiers() {
        return getOffenceModifier() + getDefenseModifier() + getTeamworkModifier() - getFoulModifier();
    }

    public String playerSkillsOutput() {
        return "Offense: " + getSkillOffense() + ", Defense: " + getSkillDefense() + ", Teamwork: " + getSkillTeamwork() +
                ", Foul Likelihood: " + getFoulLikelihood();
    }

    @Override
    public String toString() {
         return String.format("{Name: %-25s SkillLevel: %2d, SkillOffense: %2d, Skill Defense: %2d, SkillTeamwork: %2d, FoulLikelihood: %2d, InjuryHistory: %s}",
                 getName(), getSkillLevel(), getSkillOffense(), getSkillDefense(), getSkillTeamwork(), getFoulLikelihood(), getInjuryHistory());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Player player)) return false;

        return getName().equals(player.getName());
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public int compareTo(@NotNull Player o) {
        if(Math.abs(getModifiers() - o.getModifiers()) <= 0.0001)
            return 0;
        else if (getModifiers() > o.getModifiers())
            return 1;
        else
            return -1;
    }
}
