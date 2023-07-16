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

import info.codywilliams.qsg.models.player.*;
import info.codywilliams.qsg.util.ResourceBundleReplacer;

import java.time.Duration;
import java.time.LocalDate;

public abstract class Play {
    TeamType attackingTeamType;
    public enum BludgerOutcome {NONE, BLOCKED, HIT, MISSED}
    public enum InjuryType {NONE, BLUDGER_BLOCKED, BLUDGER_HIT, CHASER, KEEPER}
    Player injuredPlayer = null;
    InjuryType injuryType = InjuryType.NONE;
    TeamType injuredPlayerTeam;
    LocalDate injuryEndDate;
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

    public Player getInjuredPlayer() {
        return injuredPlayer;
    }

    public InjuryType getInjuryType() {
        return injuryType;
    }

    public TeamType getInjuredPlayerTeam() {
        return injuredPlayerTeam;
    }

    public LocalDate getInjuryEndDate() {
        return injuryEndDate;
    }

    protected abstract String getOutcomeString();

    public abstract String outputWithDetails(ResourceBundleReplacer resources, String homeTeamName, String awayTeamName);
    public abstract String outputWithoutDetails(ResourceBundleReplacer resources, String homeTeamName, String awayTeamName);

    public String outputInjuryWithDetails(ResourceBundleReplacer resources) {
        if (injuryType == InjuryType.NONE)
            return "";

        resources.addToken("injuredPlayer", injuredPlayer.getShortName());
        resources.addTeamToken("injuredPlayerTeam", injuredPlayerTeam.name().toLowerCase() + "Team");
        String resourceKey = "injury." + getInjuryType().name().toLowerCase();

        if(getInjuryType() == InjuryType.KEEPER && this instanceof PlayChaser playChaser) {
            if (playChaser.getQuaffleOutcome() == PlayChaser.QuaffleOutcome.MISSED || playChaser.getQuaffleOutcome() == PlayChaser.QuaffleOutcome.SCORED)
                resourceKey += ".missed";
            if (playChaser.getQuaffleOutcome() == PlayChaser.QuaffleOutcome.BLOCKED)
                resourceKey += ".blocked";
        }

        return resources.getString(resourceKey + ".player");
    }

    void addCommonTokens(ResourceBundleReplacer resources) {
        if(beaterHitter != null)
            resources.addToken("beaterHitter", beaterHitter.getShortName());
        if(beaterBlocker != null)
            resources.addToken("beaterBlocker", beaterBlocker.getShortName());
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

    public void setInjury(InjuryType type, Player player, TeamType playerTeam, LocalDate endDate) {
        switch(type) {
            case NONE -> {
            }
            case BLUDGER_BLOCKED -> {
                injuryType = type;
                injuredPlayer = player;
                injuredPlayerTeam = playerTeam;
                injuryEndDate = endDate;
            }
            case BLUDGER_HIT -> {
                if (player instanceof Chaser || player instanceof Seeker) {
                    injuryType = type;
                    injuredPlayer = player;
                    injuredPlayerTeam = playerTeam;
                    injuryEndDate = endDate;
                }
            }
            case CHASER -> {
                if (player instanceof Chaser) {
                    injuryType = type;
                    injuredPlayer = player;
                    injuredPlayerTeam = playerTeam;
                    injuryEndDate = endDate;
                }
            }
            case KEEPER -> {
                if (player instanceof Keeper) {
                    injuryType = type;
                    injuredPlayer = player;
                    injuredPlayerTeam = playerTeam;
                    injuryEndDate = endDate;
                }
            }
        }
    }
}
