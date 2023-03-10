/*
 * Quidditch Season Generator
 * Copyright (C) 2023.  Cody Williams
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

package info.codywilliams.qsg.models.match;

import info.codywilliams.qsg.models.player.Beater;

import java.util.ResourceBundle;

public abstract class Play {
    TeamType attackingTeamType;
    public enum BludgerOutcome {NONE, BLOCKED, HIT, MISSED}
    BludgerOutcome bludgerOutcome = BludgerOutcome.NONE;
    Beater beaterHitter;
    Beater beaterBlocker;

    int playDurationSeconds;

    public TeamType getAttackingTeamType() {
        return attackingTeamType;
    }

    public BludgerOutcome getBludgerOutcome() {
        return bludgerOutcome;
    }

    public void setBludgerOutcome(BludgerOutcome bludgerOutcome) {
        this.bludgerOutcome = bludgerOutcome;
    }

    public Beater getBeaterHitter() {
        return beaterHitter;
    }

    public void setBeaterHitter(Beater beaterHitter) {
        this.beaterHitter = beaterHitter;
    }

    public Beater getBeaterBlocker() {
        return beaterBlocker;
    }

    public void setBeaterBlocker(Beater beaterBlocker) {
        this.beaterBlocker = beaterBlocker;
    }

    public int getPlayDurationSeconds() {
        return playDurationSeconds;
    }

    public void setPlayDurationSeconds(int playDurationSeconds) {
        this.playDurationSeconds = playDurationSeconds;
    }

    protected abstract String getOutcomeString();

    public abstract String outputWithDetails(ResourceBundle playProperties, String homeTeamName, String awayTeamName);
    public abstract String outputWithoutDetails(ResourceBundle playProperties, String homeTeamName, String awayTeamName);

    String outputTeams(String output, String homeTeamName, String awayTeamName) {
        return switch(attackingTeamType) {
            case HOME -> output.replace("${attackingTeam}", homeTeamName).replace("${defendingTeam}", awayTeamName);
            case AWAY -> output.replace("${attackingTeam}", awayTeamName).replace("${defendingTeam}", homeTeamName);
        };
    }

    String outputCommonNames(String output) {
        if(beaterHitter != null)
            output = output.replace("${beaterHitter}", beaterHitter.getName());
        if(beaterBlocker != null)
            output = output.replace("${beaterBlocker}", beaterBlocker.getName());
        return output;
    }
    public enum TeamType {HOME, AWAY}
}
