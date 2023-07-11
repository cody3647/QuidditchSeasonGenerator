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
import info.codywilliams.qsg.util.ResourceBundleReplacer;

import java.time.Duration;

public abstract class Play {
    TeamType attackingTeamType;
    public enum BludgerOutcome {NONE, BLOCKED, HIT, MISSED}
    BludgerOutcome bludgerOutcome = BludgerOutcome.NONE;
    Beater beaterHitter;
    Beater beaterBlocker;
    int scoreHome;
    int scoreAway;
    Duration matchLength;
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

    public int getScoreHome() {
        return scoreHome;
    }

    public int getScoreAway() {
        return scoreAway;
    }

    public void setScores(int scoreHome, int scoreAway) {
        this.scoreHome = scoreHome;
        this.scoreAway = scoreAway;
    }

    public Duration getMatchLength() {
        return matchLength;
    }

    public void setMatchLength(Duration matchLength) {
        this.matchLength = matchLength;
    }

    protected abstract String getOutcomeString();

    public abstract String outputWithDetails(ResourceBundleReplacer resources, String homeTeamName, String awayTeamName);
    public abstract String outputWithoutDetails(ResourceBundleReplacer resources, String homeTeamName, String awayTeamName);

    void addCommonTokens(ResourceBundleReplacer resources) {
        if(beaterHitter != null)
            resources.addToken("beaterHitter", beaterHitter.getName());
        if(beaterBlocker != null)
            resources.addToken("beaterBlocker", beaterBlocker.getName());
        switch(attackingTeamType) {
            case HOME -> {
                resources.addTeamToken("attackingTeam", "homeTeam");
                resources.addTeamToken("defendingTeam", "awayTeam");
            }
            case AWAY -> {
                resources.addTeamToken("attackingTeam", "awayTeam");
                resources.addTeamToken("defendingTeam", "homeTeam");
            }
        }
    }
}
