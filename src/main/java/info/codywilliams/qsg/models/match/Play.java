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

import java.time.Duration;
import java.time.LocalDate;

public abstract class Play {
    TeamType attackingTeamType;
    Player injuredPlayer = null;
    Injury injury = Injury.NONE;
    TeamType injuredPlayerTeam;
    LocalDate injuryEndDate;
    Bludger bludger = Bludger.NONE;
    Beater beaterHitter;
    Beater beaterBlocker;
    int scoreHome;
    int scoreAway;
    Duration matchLength;
    int playDurationSeconds;

    public TeamType getAttackingTeamType() {
        return attackingTeamType;
    }

    public Bludger getBludgerOutcome() {
        return bludger;
    }

    public void setBludgerOutcome(Bludger bludger) {
        this.bludger = bludger;
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

    public Injury getInjuryType() {
        return injury;
    }

    public TeamType getInjuredPlayerTeam() {
        return injuredPlayerTeam;
    }

    public LocalDate getInjuryEndDate() {
        return injuryEndDate;
    }

    public void setInjury(Injury type, Player player, TeamType playerTeam, LocalDate endDate) {
        switch (type) {
            case NONE -> {
            }
            case BLUDGER_BLOCKED -> {
                injury = type;
                injuredPlayer = player;
                injuredPlayerTeam = playerTeam;
                injuryEndDate = endDate;
            }
            case BLUDGER_HIT -> {
                if (player instanceof Chaser || player instanceof Seeker) {
                    injury = type;
                    injuredPlayer = player;
                    injuredPlayerTeam = playerTeam;
                    injuryEndDate = endDate;
                }
            }
            case CHASER -> {
                if (player instanceof Chaser) {
                    injury = type;
                    injuredPlayer = player;
                    injuredPlayerTeam = playerTeam;
                    injuryEndDate = endDate;
                }
            }
            case KEEPER -> {
                if (player instanceof Keeper) {
                    injury = type;
                    injuredPlayer = player;
                    injuredPlayerTeam = playerTeam;
                    injuryEndDate = endDate;
                }
            }
        }
    }

}
